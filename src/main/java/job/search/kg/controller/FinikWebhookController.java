package job.search.kg.controller;

import job.search.kg.dto.request.payment.FinikWebhookPayload;
import job.search.kg.dto.response.user.WebhookData;
import job.search.kg.payment.FinikWebhookService;
import job.search.kg.payment.WebhookSignatureValidator;
import job.search.kg.payment.WithdrawalWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/api/webhooks/finik")
@RequiredArgsConstructor
@Slf4j
public class FinikWebhookController {

    private static final long TIMESTAMP_SKEW_MS = 5 * 60 * 1000; // 5 минут
    private final WebhookSignatureValidator signatureValidator;
    private final WithdrawalWebhookService withdrawalWebhookService;
    private final FinikWebhookService webhookService;

    @PostMapping("/payment-status")
    public ResponseEntity<Map<String, String>> handlePaymentStatus(
            @RequestBody FinikWebhookPayload payload,
            HttpServletRequest request
    ) {
        try {
            log.info("Received Finik webhook: transactionId={}, status={}",
                    payload.getTransactionId(), payload.getStatus());

            // 1. Извлекаем заголовки
            Map<String, String> headers = extractHeaders(request);
            String signature = request.getHeader("signature");

            if (signature == null || signature.isEmpty()) {
                log.error("Missing signature header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing signature"));
            }

            // 3. Валидируем подпись
            boolean isValid = signatureValidator.validateSignature(
                    "post",
                    "/api/webhooks/finik/payment-status",
                    headers,
                    null,
                    payload,
                    signature
            );

            if (!isValid) {
                log.error("Invalid webhook signature for transaction: {}",
                        payload.getTransactionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Invalid signature"));
            }

            // 4. Обрабатываем webhook
            withdrawalWebhookService.processPaymentStatusUpdate(payload);

            // 5. Быстро отвечаем 200 OK
            return ResponseEntity.ok(Map.of("status", "accepted"));

        } catch (Exception e) {
            log.error("Error processing Finik webhook", e);
            // Возвращаем 200 чтобы Finik не повторял запрос
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/payment")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody WebhookData webhook,
            @RequestHeader Map<String, String> headers
    ) {
        try {

            // Обрабатываем webhook
            webhookService.processWebhook(webhook);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Извлечение всех заголовков
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName.toLowerCase(), request.getHeader(headerName));
        }

        return headers;
    }
}
