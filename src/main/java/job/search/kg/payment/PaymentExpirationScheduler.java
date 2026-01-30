package job.search.kg.payment;

import job.search.kg.entity.Payment;
import job.search.kg.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationScheduler {

    private final PaymentRepository paymentRepository;

    /**
     * Каждые 5 минут проверяем старые PENDING платежи
     */
    @Scheduled(fixedRate = 300000) // 5 минут
    public void expireOldPayments() {

        // Платежи старше 30 минут считаем истекшими
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        List<Payment> expiredPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(
                        Payment.PaymentStatus.PENDING,
                        thirtyMinutesAgo
                );

        for (Payment payment : expiredPayments) {
            payment.setStatus(Payment.PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            log.info("Payment expired: paymentId={}, createdAt={}",
                    payment.getPaymentId(),
                    payment.getCreatedAt());
        }

        if (!expiredPayments.isEmpty()) {
            log.info("Expired {} old payments", expiredPayments.size());
        }
    }
}