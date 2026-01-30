package job.search.kg.payment;


import com.fasterxml.jackson.databind.ObjectMapper;
import job.search.kg.dto.response.user.CreatePaymentResponse;
import job.search.kg.entity.Payment;
import job.search.kg.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinikPaymentService {

    private final FinikConfig config;
    private final FinikSignatureUtil signatureService;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public String createPayment(
            UUID paymentId,
            BigDecimal amount,
            String description,
            String redirectUrl
    ) throws Exception {

        // Подготавливаем тело запроса
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Amount", amount.intValue());
        requestBody.put("CardType", "FINIK_QR");
        requestBody.put("PaymentId", paymentId.toString());
        requestBody.put("RedirectUrl", redirectUrl);

        Map<String, Object> data = new HashMap<>();
        data.put("accountId", config.getAccountId());
        data.put("merchantCategoryCode", "0742");
        data.put("name_en", truncate(description, 50));
        data.put("description", description);
        data.put("webhookUrl", getWebhookUrl());
        requestBody.put("Data", data);

        // Генерируем timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Подготавливаем заголовки
        URI uri = URI.create(config.getBaseUrl() + "/v1/payment");
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", uri.getHost());
        headers.put("x-api-key", config.getApiKey());
        headers.put("x-api-timestamp", timestamp);

        // Генерируем подпись
        String signature = signatureService.generateSignature(
                "POST",
                "/v1/payment",
                headers,
                null,
                requestBody,
                config.getPrivateKeyPath()
        );

        // ⚠️ ВАЖНО: Создаем jsonBody и url ПЕРЕД логированием
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        String url = config.getBaseUrl() + "/v1/payment";

        // ✅ Теперь логируем:
        log.info("=== FINIK REQUEST DEBUG ===");
        log.info("API Key: {}", config.getApiKey());
        log.info("Account ID: {}", config.getAccountId());
        log.info("Timestamp: {}", timestamp);
        log.info("Signature: {}", signature);
        log.info("Request URL: {}", url);
        log.info("Request Body: {}", jsonBody);
        log.info("Host header: {}", uri.getHost());
        log.info("===========================");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("x-api-key", config.getApiKey());
        httpHeaders.set("x-api-timestamp", timestamp);
        httpHeaders.set("signature", signature);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);

        log.info("Sending payment request to Finik: paymentId={}, amount={}",
                paymentId, amount);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Finik response: status={}", response.getStatusCode());

            // Обрабатываем редирект 302
            if (response.getStatusCode() == HttpStatus.FOUND) {
                String location = response.getHeaders().getLocation().toString();
                log.info("Payment URL received: {}", location);
                return location;
            }

            // Если 201 Created
            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.warn("Received 201 instead of 302, body: {}", response.getBody());
            }

            throw new RuntimeException("Unexpected response from Finik: " +
                    response.getStatusCode() + ", body: " + response.getBody());

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Finik error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Finik payment creation failed: " +
                    e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error calling Finik API", e);
            throw new RuntimeException("Failed to create payment in Finik", e);
        }
    }

    /**
     * Получение платежа по ID
     */
    public CreatePaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId.toString())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return new CreatePaymentResponse(
                payment.getPaymentId(),
                payment.getPaymentUrl(),
                payment.getStatus().name()
        );
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    private String getWebhookUrl() {
        // TODO: Замените на ваш реальный публичный URL
        return "https://jobsearchkg-production.up.railway.app/api/payments/finik";
    }
}
