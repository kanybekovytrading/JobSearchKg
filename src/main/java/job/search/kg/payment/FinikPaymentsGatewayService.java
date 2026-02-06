package job.search.kg.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import job.search.kg.dto.request.payment.CheckRecipientRequest;
import job.search.kg.dto.request.payment.GetServicesRequest;
import job.search.kg.dto.request.payment.MakePaymentRequest;
import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.GetServicesResponse;
import job.search.kg.dto.response.payment.MakePaymentResponse;
import job.search.kg.dto.response.payment.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с Finik Payments Gateway API
 * Для автоматизированного вывода средств на банковские приложения
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinikPaymentsGatewayService {

    private final FinikWConfig config;
    private final FinikSignatureUtil signatureUtil;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * Проверка получателя перед отправкой платежа
     * POST /v2/recipient
     */
    public CheckRecipientResponse checkRecipient(
            String serviceId,
            String phone,
            Integer amount
    ) throws Exception {

        log.info("Checking recipient: service={}, phone={}, amount={}",
                serviceId, phone, amount);

        // Подготовка запроса
        CheckRecipientRequest request = new CheckRecipientRequest();
        request.setService(serviceId);

        Map<String, Object> fields = new HashMap<>();
        fields.put("phone", phone);
        fields.put("amount", amount);
        request.setFields(fields);

        // Генерация timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // URL для Payments Gateway API
        String baseUrl = config.getBaseUrl();
        URI uri = URI.create(baseUrl + "/v2/recipient");

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

        // Отправка запроса
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("x-api-key", config.getApiKey());
        httpHeaders.set("x-api-timestamp", timestamp);
        httpHeaders.set("signature", signature);

        String jsonBody = objectMapper.writeValueAsString(request);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);

        try {
            ResponseEntity<CheckRecipientResponse> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.POST,
                    entity,
                    CheckRecipientResponse.class
            );

            CheckRecipientResponse result = response.getBody();
            log.info("Recipient check result: statusCode={}, name={}",
                    result.getStatusCode(), result.getName());

            return result;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Recipient check error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            // Парсим ошибку
            return objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    CheckRecipientResponse.class
            );
        }
    }

    /**
     * Создание платежа (вывод средств)
     * POST /v2/payment
     */
    public MakePaymentResponse makePayment(
            String transactionId,
            String accountId,
            String userId,
            String serviceId,
            String phone,
            Integer amount
    ) throws Exception {

        log.info("Making payment: transactionId={}, service={}, phone={}, amount={}",
                transactionId, serviceId, phone, amount);

        // Подготовка запроса
        MakePaymentRequest request = new MakePaymentRequest();
        request.setTransactionId(transactionId);
        request.setAccountId(accountId);
        request.setUserId(userId);

        MakePaymentRequest.ServiceInfo serviceInfo = new MakePaymentRequest.ServiceInfo();
        serviceInfo.setId(serviceId);
        request.setService(serviceInfo);

        Map<String, Object> fields = new HashMap<>();
        fields.put("phone", phone);
        fields.put("amount", amount);
        request.setFields(fields);

        // Генерация timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // URL для Payments Gateway API
        String baseUrl = config.getBaseUrl();
        URI uri = URI.create(baseUrl + "/v2/payment");

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

        log.info("Sending payment request: url={}", uri);

        try {
            ResponseEntity<MakePaymentResponse> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.POST,
                    entity,
                    MakePaymentResponse.class
            );

            MakePaymentResponse result = response.getBody();
            log.info("Payment result: statusCode={}, status={}, id={}",
                    result.getStatusCode(), result.getStatus(), result.getId());

            return result;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Payment error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            // Парсим ошибку
            return objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    MakePaymentResponse.class
            );
        }
    }

    /**
     * Проверка статуса платежа
     * GET /v2/payments/{paymentId}
     */
    public PaymentStatusResponse checkPaymentStatus(String paymentId) throws Exception {

        log.info("Checking payment status: paymentId={}", paymentId);

        // Генерация timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // URL для Payments Gateway API
        String baseUrl = config.getBaseUrl();
        URI uri = URI.create(baseUrl + "/v2/payments/" + paymentId);

        // Подготовка заголовков
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", uri.getHost());
        headers.put("x-api-key", config.getApiKey());
        headers.put("x-api-timestamp", timestamp);

        // Генерация подписи (без body для GET)
        String signature = signatureUtil.generateSignature(
                "GET",
                "/v2/payments/" + paymentId,
                headers,
                null,
                null,
                config.getPrivateKeyPath()
        );

        // Отправка запроса
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("x-api-key", config.getApiKey());
        httpHeaders.set("x-api-timestamp", timestamp);
        httpHeaders.set("signature", signature);

        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<PaymentStatusResponse> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.GET,
                    entity,
                    PaymentStatusResponse.class
            );

            PaymentStatusResponse result = response.getBody();
            log.info("Payment status: status={}, transactionId={}",
                    result.getStatus(), result.getTransactionId());

            return result;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Payment status check error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            return objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    PaymentStatusResponse.class
            );
        }
    }

    /**
     * Получение списка доступных услуг
     * POST /v2/services
     */
    public GetServicesResponse getAvailableServices(
            Integer from,
            Integer size,
            String locale,
            String parentId
    ) throws Exception {

        log.info("Getting available services: from={}, size={}, locale={}, parentId={}",
                from, size, locale, parentId);

        // Подготовка запроса
        GetServicesRequest request = GetServicesRequest.builder()
                .from(from != null ? from : 0)
                .size(size != null ? size : 50)
                .locale(locale != null ? locale : "RU")
                .query("")
                .build();

        // Добавляем фильтр если указан parentId
        if (parentId != null) {
            GetServicesRequest.FilterInfo filter = GetServicesRequest.FilterInfo.builder()
                    .parentId(parentId)
                    .build();
            request.setFilter(filter);
        }
        // Генерация timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // URL для Payments Gateway API
        String baseUrl = config.getBaseUrl();
        URI uri = URI.create(baseUrl + "/v2/services");

        // Подготовка заголовков
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", uri.getHost());
        headers.put("x-api-key", config.getApiKey());
        headers.put("x-api-timestamp", timestamp);

        // Генерация подписи
        String signature = signatureUtil.generateSignature(
                "POST",
                "/v2/services",
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
        log.info("URL: {}", uri.toString());
        log.info("Timestamp: {}", timestamp);
        log.info("API Key: {}", config.getApiKey());
        log.info("Signature: {}", signature);
        log.info("Payload: {}", entity);
        log.info("========================");
        try {
            ResponseEntity<GetServicesResponse> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.POST,
                    entity,
                    GetServicesResponse.class
            );

            GetServicesResponse result = response.getBody();
            log.info("Services loaded: total={}, returned={}",
                    result.getTotal(), result.getServices().size());

            return result;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Services loading error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            return objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    GetServicesResponse.class
            );
        }
    }
}