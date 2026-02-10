package job.search.kg.scheduler;

import job.search.kg.entity.Withdrawal;
import job.search.kg.repo.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Планировщик для проверки статуса выводов в статусе PROCESSING
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawalStatusScheduler1 {

    private final WithdrawalRepository withdrawalRepository;

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
                log.warn("Marked old withdrawal as failed: id={}", withdrawal.getId());
            }
        }

        if (marked > 0) {
            log.info("Marked {} old withdrawals as failed", marked);
        }
    }
}