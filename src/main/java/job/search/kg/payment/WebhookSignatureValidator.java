package job.search.kg.payment;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.Signature;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookSignatureValidator {

    private final FinikWConfig finikConfig;
    private final ObjectMapper objectMapper;

    /**
     * Валидация подписи webhook от Finik
     */
    public boolean validateSignature(
            String method,
            String path,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object body,
            String receivedSignature
    ) {
        try {
            // 1. Строим canonical string
            String canonicalString = buildCanonicalString(
                    method,
                    path,
                    headers,
                    queryParams,
                    body
            );

            log.debug("Canonical string for validation:\n{}", canonicalString);

            // 2. Загружаем публичный ключ Finik
            PublicKey publicKey = loadFinikPublicKey();

            // 3. Проверяем подпись
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(canonicalString.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(receivedSignature);
            boolean isValid = signature.verify(signatureBytes);

            if (!isValid) {
                log.error("Webhook signature validation failed!");
                log.error("Received signature: {}", receivedSignature);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error validating webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Построение canonical string (такой же как при создании платежа)
     */
    private String buildCanonicalString(
            String method,
            String path,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object body
    ) throws Exception {
        StringBuilder canonical = new StringBuilder();

        // 1. HTTP метод (lowercase)
        canonical.append(method.toLowerCase()).append("\n");

        // 2. URI path
        canonical.append(path).append("\n");

        // 3. Headers (Host + x-api-*)
        canonical.append(buildCanonicalHeaders(headers)).append("\n");

        // 4. Query parameters (если есть)
        if (queryParams != null && !queryParams.isEmpty()) {
            canonical.append(buildCanonicalQuery(queryParams)).append("\n");
        }

        // 5. Body (JSON с отсортированными ключами)
        if (body != null) {
            String jsonBody = objectMapper.writeValueAsString(body);
            canonical.append(jsonBody);
        }

        return canonical.toString();
    }

    /**
     * Построение canonical headers
     */
    private String buildCanonicalHeaders(Map<String, String> headers) {
        Map<String, String> filteredHeaders = new TreeMap<>();

        // Host header
        if (headers.containsKey("host") || headers.containsKey("Host")) {
            String host = headers.getOrDefault("host", headers.get("Host"));
            filteredHeaders.put("host", host);
        }

        // x-api-* headers
        headers.forEach((key, value) -> {
            String lowerKey = key.toLowerCase();
            if (lowerKey.startsWith("x-api-")) {
                filteredHeaders.put(lowerKey, value);
            }
        });

        // Формируем строку
        return filteredHeaders.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    /**
     * Построение canonical query
     */
    private String buildCanonicalQuery(Map<String, String> queryParams) {
        return queryParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * URL encoding
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Загрузка публичного ключа Finik
     */
    private PublicKey loadFinikPublicKey() throws Exception {
        String publicKeyPath = finikConfig.getPublicKeyPath();
        String publicKeyContent = new String(Files.readAllBytes(Paths.get(publicKeyPath)));

        // Убираем header/footer
        publicKeyContent = publicKeyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

        java.security.spec.X509EncodedKeySpec spec =
                new java.security.spec.X509EncodedKeySpec(keyBytes);
        java.security.KeyFactory keyFactory =
                java.security.KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }
}