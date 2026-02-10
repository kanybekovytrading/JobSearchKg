package job.search.kg.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import job.search.kg.dto.request.payment.CheckRecipientRequest;
import job.search.kg.dto.request.payment.MakePaymentRequest;
import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.MakePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ ПРАВИЛЬНЫЙ сервис для вывода денег в банки
 * Использует специфичные поля для каждого банка
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankWithdrawalService {

    private final FinikWConfig config;
    private final FinikSignatureUtil signatureUtil;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public CheckRecipientResponse checkRecipientForBank(
            String serviceId,
            String serviceCode,
            String phone,
            Integer amount,
            boolean isMBank
    ) throws Exception {

        log.info("Checking bank recipient: serviceId={}, phone={}, amount={}, isMBank={}",
                serviceId, phone, amount, isMBank);

        CheckRecipientRequest request = new CheckRecipientRequest();
        request.setService(serviceId);

        Map<String, Object> fields = new HashMap<>();

        if (isMBank) {
            // ✅ MBank - используем поля из requiredFields
            log.info("Using MBank format");
            fields.put("account", phone);              // fieldId: "account"
            fields.put("amount", amount);              // fieldId: "amount"
            fields.put("transactionType", "10");       // value: "10" (фиксированное)
            fields.put("provider", "c2c.mbank.kg");    // value: "c2c.mbank.kg" (фиксированное)
            fields.put("merchantCode", "9999");        // value: "9999" (фиксированное)
            // qrComment - опционально, не добавляем при проверке
        } else {
            // ✅ Обычные банки
            log.info("Using standard bank format");
            fields.put("account.value.persacc", phone);
            fields.put("service", serviceCode);
            fields.put("total", amount);
        }

        request.setFields(fields);

        // Генерация timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        URI uri = URI.create(config.getBaseUrl() + "/v2/recipient");

        // Подготовка заголовков
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", uri.getHost());
        headers.put("x-api-key", config.getApiKey());
        headers.put("x-api-timestamp", timestamp);

        // Генерация подписи
        String signature = signatureUtil.generateSignature(
                "POST",
                "/v2/recipient",
                headers,
                null,
                request,
                config.getPrivateKeyPath()
        );

        log.info("=== HTTP REQUEST DETAILS ===");
        log.info("URL: {} {}", HttpMethod.POST, uri);
        log.info("Headers:");
        // Отправка запроса
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("x-api-key", config.getApiKey());
        httpHeaders.set("x-api-timestamp", timestamp);
        httpHeaders.set("signature", signature);

        String jsonBody = objectMapper.writeValueAsString(request);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);

        log.info("Request body: {}", jsonBody);

        try {
            ResponseEntity<CheckRecipientResponse> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.POST,
                    entity,
                    CheckRecipientResponse.class
            );

            CheckRecipientResponse result = response.getBody();
            log.info("Recipient check success: statusCode={}, name={}",
                    result.getStatusCode(), result.getName());

            log.info("=== HTTP RESPONSE DETAILS ===");
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Headers: {}", response.getHeaders());
            log.info("Response Body: {}", objectMapper.writeValueAsString(result));
            log.info("===========================");

            log.info("Recipient check success: statusCode={}, name={}",
                    result.getStatusCode(), result.getName());

            return result;

        } catch (HttpStatusCodeException e) {
            log.error("Recipient check error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            CheckRecipientResponse errorResponse = new CheckRecipientResponse();
            errorResponse.setStatusCode(e.getStatusCode().value());
            errorResponse.setErrorMessage(e.getResponseBodyAsString());

            // Пытаемся распарсить JSON ошибку
            try {
                CheckRecipientResponse parsedError = objectMapper.readValue(
                        e.getResponseBodyAsString(),
                        CheckRecipientResponse.class
                );
                if (parsedError.getErrorMessage() != null) {
                    errorResponse.setErrorMessage(parsedError.getErrorMessage());
                }
            } catch (Exception parseEx) {
                log.warn("Could not parse error response: {}", parseEx.getMessage());
            }

            return errorResponse;

        } catch (Exception e) {
            // ✅ Другие ошибки (сеть, таймаут и т.д.)
            log.error("Unexpected error during recipient check: {}", e.getMessage(), e);

            CheckRecipientResponse errorResponse = new CheckRecipientResponse();
            errorResponse.setStatusCode(500);
            errorResponse.setErrorMessage("Unexpected error: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * ✅ СОЗДАНИЕ ПЛАТЕЖА для конкретного банка
     */
    public MakePaymentResponse makePaymentToBank(
            String transactionId,
            String serviceId,
            String serviceCode,
            String phone,
            Integer amount,
            String comment,
            boolean isMBank
    ) throws Exception {

        log.info("Making bank payment: transactionId={}, serviceId={}, phone={}, amount={}, isMBank={}",
                transactionId, serviceId, phone, amount, isMBank);

        MakePaymentRequest request = new MakePaymentRequest();
        request.setTransactionId(transactionId);
        request.setAccountId(config.getAccountId());
        request.setUserId(config.getUserId());

        MakePaymentRequest.ServiceInfo serviceInfo = new MakePaymentRequest.ServiceInfo();
        serviceInfo.setId(serviceId);
        request.setService(serviceInfo);

        Map<String, Object> fields = new HashMap<>();

        if (isMBank) {
            // ✅ MBank - используем те же поля + комментарий
            log.info("Using MBank format for payment");
            fields.put("account", phone);              // fieldId: "account"
            fields.put("amount", amount);              // fieldId: "amount"
            fields.put("transactionType", "10");       // value: "10" (фиксированное)
            fields.put("provider", "c2c.mbank.kg");    // value: "c2c.mbank.kg" (фиксированное)
            fields.put("merchantCode", "9999");        // value: "9999" (фиксированное)

            // Добавляем комментарий если есть
            if (comment != null && !comment.isEmpty()) {
                fields.put("qrComment", comment);      // fieldId: "qrComment"
            }
        } else {
            // ✅ Обычные банки
            log.info("Using standard bank format for payment");
            fields.put("account.value.persacc", phone);
            fields.put("service", serviceCode);
            fields.put("total", amount);

            if (comment != null && !comment.isEmpty()) {
                fields.put("qp_comment", comment);
            } else {
                fields.put("qp_comment", " ");
            }
        }

        request.setFields(fields);

        // Генерация timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        URI uri = URI.create(config.getBaseUrl() + "/v2/payment");

        // Подготовка заголовков
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", uri.getHost());
        headers.put("x-api-key", config.getApiKey());
        headers.put("x-api-timestamp", timestamp);

        // Генерация подписи
        String signature = signatureUtil.generateSignature(
                "POST",
                "/v2/payment",
                headers,
                null,
                request,
                config.getPrivateKeyPath()
        );

        // Отправка запроса
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("x-api-key", config.getApiKey());
        httpHeaders.set("x-api-timestamp", timestamp);
        httpHeaders.set("signature", signature);

        String jsonBody = objectMapper.writeValueAsString(request);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);

        log.info("Payment request body: {}", jsonBody);

        try {
            ResponseEntity<MakePaymentResponse> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.POST,
                    entity,
                    MakePaymentResponse.class
            );

            MakePaymentResponse result = response.getBody();
            log.info("Payment success: statusCode={}, status={}, id={}",
                    result.getStatusCode(), result.getStatus(), result.getId());

            return result;

        } catch (HttpStatusCodeException e) {
            // ✅ Ловит ВСЕ HTTP ошибки: 4xx и 5xx
            log.error("Payment error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            MakePaymentResponse errorResponse = new MakePaymentResponse();
            errorResponse.setStatusCode(e.getStatusCode().value());
            errorResponse.setErrorMessage(e.getResponseBodyAsString());

            // Пытаемся распарсить JSON ошибку
            try {
                MakePaymentResponse parsedError = objectMapper.readValue(
                        e.getResponseBodyAsString(),
                        MakePaymentResponse.class
                );
                if (parsedError.getErrorMessage() != null) {
                    errorResponse.setErrorMessage(parsedError.getErrorMessage());
                }
            } catch (Exception parseEx) {
                log.warn("Could not parse error response: {}", parseEx.getMessage());
            }

            return errorResponse;

        } catch (Exception e) {
            // ✅ Другие ошибки
            log.error("Unexpected error during payment: {}", e.getMessage(), e);

            MakePaymentResponse errorResponse = new MakePaymentResponse();
            errorResponse.setStatusCode(500);
            errorResponse.setErrorMessage("Unexpected error: " + e.getMessage());
            return errorResponse;
        }
    }
}
