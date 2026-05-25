package job.search.kg.payment;

import job.search.kg.entity.Payment;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationScheduler {

    private final PaymentRepository paymentRepository;
    private final BotSubscriptionService botSubscriptionService;

    // ВРЕМЕННО: активируем подписки для PENDING платежей без вебхука
    // Убрать когда вебхуки будут стабильно работать
    @Transactional
    @Scheduled(fixedRate = 600000) // каждые 10 минут
    public void autoActivatePendingSubscriptions() {
        LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);
        LocalDateTime twentyNineMinutesAgo = LocalDateTime.now().minusMinutes(29);

        List<Payment> pending = paymentRepository.findByStatusAndCreatedAtBetween(
                Payment.PaymentStatus.PENDING,
                twentyNineMinutesAgo,
                threeMinutesAgo
        );

        for (Payment payment : pending) {
            if (payment.getPlanType() == null) continue;

            try {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setCompletedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                botSubscriptionService.createSubscription(
                        payment.getUser().getTelegramId(),
                        payment.getPlanType(),
                        payment.getPaymentId()
                );

                log.info("Auto-activated subscription: paymentId={}, user={}, plan={}",
                        payment.getPaymentId(), payment.getUser().getTelegramId(), payment.getPlanType());
            } catch (Exception e) {
                log.error("Failed to auto-activate subscription for paymentId={}: {}",
                        payment.getPaymentId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 300000) // 5 минут
    public void expireOldPayments() {
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
                    payment.getPaymentId(), payment.getCreatedAt());
        }

        if (!expiredPayments.isEmpty()) {
            log.info("Expired {} old payments", expiredPayments.size());
        }
    }
}