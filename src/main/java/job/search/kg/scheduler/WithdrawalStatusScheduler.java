package job.search.kg.scheduler;

import job.search.kg.dto.response.payment.PaymentStatusResponse;
import job.search.kg.entity.Withdrawal;
import job.search.kg.payment.FinikPaymentsGatewayService;
import job.search.kg.repo.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Планировщик для проверки статуса выводов в статусе PROCESSING
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalStatusScheduler {

    private final WithdrawalRepository withdrawalRepository;
    private final FinikPaymentsGatewayService paymentsGatewayService;

    /**
     * Каждые 30 секунд проверяем статус выводов в PROCESSING
     * По документации, 99% платежей завершаются в течение 30 секунд
     */
    @Scheduled(fixedRate = 30000) // 30 секунд
    public void checkPendingWithdrawals() {

        List<Withdrawal> processingWithdrawals = withdrawalRepository
                .findByStatus(Withdrawal.WithdrawalStatus.PROCESSING);

        if (processingWithdrawals.isEmpty()) {
            return;
        }

        log.info("Checking status of {} processing withdrawals", processingWithdrawals.size());

        for (Withdrawal withdrawal : processingWithdrawals) {
            try {
                // Проверяем статус только если есть Finik transaction ID
                if (withdrawal.getFinikTransactionId() == null) {
                    log.warn("Withdrawal {} has no Finik transaction ID, skipping",
                            withdrawal.getId());
                    continue;
                }

                PaymentStatusResponse statusResponse = paymentsGatewayService
                        .checkPaymentStatus(withdrawal.getFinikTransactionId());

                // Обновляем статус
                if (statusResponse.getStatusCode() == 200) {
                    String status = statusResponse.getStatus();

                    if ("SUCCEEDED".equals(status)) {
                        withdrawal.setStatus(Withdrawal.WithdrawalStatus.SUCCEEDED);
                        if (statusResponse.getTransactionDate() != null) {
                            withdrawal.setCompletedAt(LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(statusResponse.getTransactionDate()),
                                    ZoneId.systemDefault()
                            ));
                        }
                        log.info("Withdrawal {} succeeded", withdrawal.getId());

                    } else if ("FAILED".equals(status)) {
                        withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
                        log.warn("Withdrawal {} failed", withdrawal.getId());

                    } else if ("CANCELED".equals(status)) {
                        withdrawal.setStatus(Withdrawal.WithdrawalStatus.CANCELED);
                        log.warn("Withdrawal {} canceled", withdrawal.getId());

                    } else {
                        // Все еще PROCESSING
                        log.debug("Withdrawal {} still processing", withdrawal.getId());
                    }

                    withdrawalRepository.save(withdrawal);
                }

            } catch (Exception e) {
                log.error("Error checking withdrawal status: id={}",
                        withdrawal.getId(), e);
            }
        }
    }

    /**
     * Каждый час проверяем старые PROCESSING выводы
     * Если вывод в PROCESSING более 1 часа, помечаем как FAILED
     */
    @Scheduled(fixedRate = 3600000) // 1 час
    public void markOldWithdrawalsAsFailed() {

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        List<Withdrawal> processingWithdrawals = withdrawalRepository
                .findByStatus(Withdrawal.WithdrawalStatus.PROCESSING);

        int marked = 0;
        for (Withdrawal withdrawal : processingWithdrawals) {
            if (withdrawal.getCreatedAt().isBefore(oneHourAgo)) {
                withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
                withdrawal.setErrorMessage("Transaction timeout - exceeded 1 hour");
                withdrawalRepository.save(withdrawal);
                marked++;

                log.warn("Marked old withdrawal as failed: id={}, createdAt={}",
                        withdrawal.getId(), withdrawal.getCreatedAt());
            }
        }

        if (marked > 0) {
            log.info("Marked {} old withdrawals as failed", marked);
        }
    }
}
