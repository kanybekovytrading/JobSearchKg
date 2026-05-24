package job.search.kg.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.request.user.CreatePaymentRequest;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.dto.response.user.PaymentResponse;
import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.payment.FinikWebhookService;
import job.search.kg.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    private final FinikWebhookService finikWebhookService;

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