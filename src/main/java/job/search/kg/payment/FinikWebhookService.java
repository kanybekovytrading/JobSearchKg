package job.search.kg.payment;

import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.entity.Payment;
import job.search.kg.entity.ResumeBoost;
import job.search.kg.entity.VacancyBoost;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.repo.ResumeBoostRepository;
import job.search.kg.repo.VacancyBoostRepository;
import job.search.kg.service.user.BoostService;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinikWebhookService {

    private final PaymentRepository paymentRepository;
    private final BotSubscriptionService botSubscriptionService;
    private final BoostService boostService;
    private final VacancyBoostRepository vacancyBoostRepository;
    private final ResumeBoostRepository resumeBoostRepository;

    @Transactional
    public void processWebhook(WebhookData webhook) {
        log.info("webhook {}", webhook.toString());
        // Ищем платеж по transactionId
        Optional<Payment> existingPayment = paymentRepository
                .findByTransactionId(webhook.getTransactionId());

        if (existingPayment.isPresent()) {
            log.warn("Webhook already processed: transactionId={}", webhook.getTransactionId());
            return; // Идемпотентность
        }

        // Извлекаем PaymentId: пробуем fields["PaymentId"], fields["paymentId"], затем id
        String paymentIdStr = extractPaymentId(webhook);
        if (paymentIdStr == null) {
            log.error("Cannot extract paymentId from webhook: id={}, transactionId={}, fields={}",
                    webhook.getId(), webhook.getTransactionId(), webhook.getFields());
            return;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByPaymentId(paymentIdStr);
        if (paymentOpt.isEmpty()) {
            log.error("Payment not found for paymentId={} (webhook id={}, transactionId={})",
                    paymentIdStr, webhook.getId(), webhook.getTransactionId());
            return;
        }
        Payment payment = paymentOpt.get();

        // Обновляем статус
        payment.setTransactionId(webhook.getTransactionId());
        payment.setReceiptNumber(webhook.getReceiptNumber());

        if ("SUCCEEDED".equalsIgnoreCase(webhook.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(webhook.getTransactionDate()),
                    ZoneId.systemDefault()
            ));

            log.info("Payment succeeded: paymentId={}", payment.getPaymentId());

            if (payment.getPlanType() != null) {
                botSubscriptionService.createSubscription(payment.getUser().getTelegramId(), payment.getPlanType(), paymentIdStr);
            } else {
                VacancyBoost vacancyBoost = vacancyBoostRepository.findByPaymentId(payment.getPaymentId()).orElse(null);
                if (vacancyBoost != null) {
                    boostService.deactivateOldVacancyBoosts(vacancyBoost.getId());
                    vacancyBoost.setIsActive(true);
                    vacancyBoostRepository.save(vacancyBoost);
                } else {
                    ResumeBoost resumeBoost = resumeBoostRepository.findByPaymentId(payment.getPaymentId()).orElse(null);
                    if (resumeBoost != null) {
                        boostService.deactivateOldResumeBoosts(resumeBoost.getId());
                        resumeBoost.setIsActive(true);
                        resumeBoostRepository.save(resumeBoost);
                    }
                }
            }
        } else {
            payment.setStatus(Payment.PaymentStatus.PENDING);
            log.warn("Payment failed: paymentId={}", payment.getPaymentId());
        }

        paymentRepository.save(payment);
    }

    private String extractPaymentId(WebhookData webhook) {
        if (webhook.getFields() != null) {
            Object val = webhook.getFields().get("PaymentId");
            if (val instanceof String s && !s.isBlank()) return s;
            val = webhook.getFields().get("paymentId");
            if (val instanceof String s && !s.isBlank()) return s;
        }
        if (webhook.getId() != null && !webhook.getId().isBlank()) {
            return webhook.getId();
        }
        return null;
    }
}
