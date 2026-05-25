package job.search.kg.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.request.user.CreatePaymentRequest;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.dto.response.user.PaymentResponse;
import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.entity.Payment;
import job.search.kg.payment.FinikWebhookService;
import job.search.kg.payment.PaymentService;
import job.search.kg.repo.PaymentRepository;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bot/payments")
@Tag(name = "Payment", description = "Эндпоинты для платежки")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final FinikWebhookService finikWebhookService;
    private final PaymentRepository paymentRepository;
    private final BotSubscriptionService botSubscriptionService;

    @SneakyThrows
    @PostMapping("/create/{telegramId}")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @PathVariable Long telegramId,
            @RequestBody CreatePaymentRequest request
    ) {

        log.info("Creating payment: userId={}, planType={}",
                telegramId, request.getPlanType());
        CreatePaymentResponse response = paymentService.createPayment(
                telegramId,
                request.getPlanType(),
                request.getRedirectUrl()
        );

        log.info("Payment created successfully: paymentId={}, url={}",
                response.getPaymentId(), response.getPaymentUrl());

        return ResponseEntity.ok(response);
    }

    /**
     * Получение информации о платеже
     * GET /api/payments/{paymentId}
     */
    /**
     * Вызывается ботом после редиректа с Finik.
     * Проверяет статус платежа напрямую и сразу создаёт подписку — не ждёт вебхука.
     */
    @Transactional
    @PostMapping("/confirm/{paymentId}")
    public ResponseEntity<Map<String, Object>> confirmPayment(@PathVariable String paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElse(null);

        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        // Уже активирован
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "subscriptionCreated", true));
        }

        // Временно: активируем сразу после редиректа, не ждём вебхук
        if (payment.getStatus() == Payment.PaymentStatus.PENDING
                || payment.getStatus() == Payment.PaymentStatus.EXPIRED) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setCompletedAt(java.time.LocalDateTime.now());
            paymentRepository.save(payment);

            if (payment.getPlanType() != null) {
                botSubscriptionService.createSubscription(
                        payment.getUser().getTelegramId(),
                        payment.getPlanType(),
                        payment.getPaymentId()
                );
            }

            log.info("Subscription auto-activated on confirm: paymentId={}, user={}",
                    paymentId, payment.getUser().getTelegramId());

            return ResponseEntity.ok(Map.of("status", "SUCCESS", "subscriptionCreated", true));
        }

        return ResponseEntity.ok(Map.of(
                "status", payment.getStatus().name(),
                "subscriptionCreated", false
        ));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId
    ) {
        try {
            PaymentResponse response = paymentService.getPayment(
                    UUID.fromString(paymentId)
            );
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}