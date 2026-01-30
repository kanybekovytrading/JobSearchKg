package job.search.kg.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.request.user.CreatePaymentRequest;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.dto.response.user.PaymentResponse;
import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.payment.FinikWebhookService;
import job.search.kg.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final FinikWebhookService webhookService;


    @PostMapping("/create/{telegramId}")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @PathVariable Long telegramId,
            @RequestBody CreatePaymentRequest request
    ) {
        try {
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

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (RuntimeException e) {
            log.error("Business error: {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().build();

        } catch (Exception e) {
            log.error("Error creating payment", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение информации о платеже
     * GET /api/payments/{paymentId}
     */
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

    @PostMapping("/finik")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody WebhookData webhook,
            @RequestHeader Map<String, String> headers
    ) {
        try {
            log.info("Received webhook: transactionId={}, status={}",
                    webhook.getTransactionId(), webhook.getStatus());
            log.info("All webhook headers: {}", headers);

            // Обрабатываем webhook
            webhookService.processWebhook(webhook);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}