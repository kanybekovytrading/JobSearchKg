package job.search.kg.payment;

import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.entity.Payment;
import job.search.kg.repo.PaymentRepository;
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

    @Transactional
    public void processWebhook(WebhookData webhook) {
       log.info("webhook {}" , webhook.toString());
        // Ищем платеж по transactionId
        Optional<Payment> existingPayment = paymentRepository
                .findByTransactionId(webhook.getTransactionId());

        if (existingPayment.isPresent()) {
            log.warn("Webhook already processed: transactionId={}", webhook.getTransactionId());
            return; // Идемпотентность
        }

        // Извлекаем PaymentId из fields
        String paymentIdStr = (String) webhook.getFields().get("paymentId"); // adjust based on actual field

        Payment payment = paymentRepository
                .findByPaymentId(paymentIdStr)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

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

           botSubscriptionService.createSubscription(payment.getUser().getTelegramId(), payment.getPlanType(), paymentIdStr);

        } else {
            payment.setStatus(Payment.PaymentStatus.PENDING);
            log.warn("Payment failed: paymentId={}", payment.getPaymentId());
        }

        paymentRepository.save(payment);
    }
}
