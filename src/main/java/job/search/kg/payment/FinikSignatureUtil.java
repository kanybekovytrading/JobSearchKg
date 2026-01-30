package job.search.kg.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinikSignatureUtil {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public String generateSignature(
            String httpMethod,
            String path,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object body,
            String privateKeyPath
    ) throws Exception {

        String canonicalString = createCanonicalString(
                httpMethod, path, headers, queryParams, body
        );

        log.info("=== CANONICAL STRING DEBUG ===");
        log.info("HTTP Method: {}", httpMethod);
        log.info("Path: {}", path);
        log.info("Headers: {}", headers);
        log.info("Query Params: {}", queryParams);
        log.info("Body: {}", body);
        log.info("--- Canonical String ---");
        log.info("{}", canonicalString);
        log.info("--- Canonical String (with visible newlines) ---");
        log.info("{}", canonicalString.replace("\n", "\\n\n"));
        log.info("Canonical String Length: {}", canonicalString.length());
        log.info("===============================");

        return signWithPrivateKey(canonicalString, privateKeyPath);
    }

    private String createCanonicalString(
            String httpMethod,
            String path,
            Map<String, String> headers,
            Map<String, String> queryParams,
            Object body
    ) throws Exception {

        StringBuilder canonical = new StringBuilder();

        // 1. HTTP Method (lowercase)
        canonical.append(httpMethod.toLowerCase()).append("\n");
        log.debug("Step 1 - HTTP Method: {}", httpMethod.toLowerCase());

        // 2. Path
        canonical.append(path).append("\n");
        log.debug("Step 2 - Path: {}", path);

        // 3. Headers (Host + x-api-*)
        String headersString = buildCanonicalHeaders(headers);
        canonical.append(headersString).append("\n");
        log.debug("Step 3 - Headers: {}", headersString);

        // 4. Query params (если есть)
        if (queryParams != null && !queryParams.isEmpty()) {
            String queryString = buildCanonicalQueryString(queryParams);
            canonical.append(queryString).append("\n");
            log.debug("Step 4 - Query String: {}", queryString);
        } else {
            log.debug("Step 4 - No query params");
        }

        // 5. Body (JSON)
        if (body != null) {
            String jsonBody = objectMapper.writeValueAsString(body);
            canonical.append(jsonBody);
            log.debug("Step 5 - Body: {}", jsonBody);
        } else {
            log.debug("Step 5 - No body");
        }

        return canonical.toString();
    }

    private String buildCanonicalHeaders(Map<String, String> headers) {
        List<String> parts = new ArrayList<>();

        // Добавляем Host
        if (headers.containsKey("Host")) {
            parts.add("host:" + headers.get("Host"));
        }

        // Добавляем все x-api-* headers (отсортированные)
        headers.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().startsWith("x-api-"))
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(e -> parts.add(e.getKey().toLowerCase() + ":" + e.getValue()));

        return String.join("&", parts);
    }

    private String buildCanonicalQueryString(Map<String, String> queryParams) {
        return queryParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String signWithPrivateKey(String data, String privateKeyPath) throws Exception {
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String privateKeyPEM;

        // Сначала пробуем взять из environment variable
        String privateKeyFromEnv = System.getenv("FINIK_PRIVATE_KEY_CONTENT");

        if (privateKeyFromEnv != null && !privateKeyFromEnv.isEmpty()) {
            privateKeyPEM = privateKeyFromEnv;
        } else {
            // Читаем из файла
            Resource resource = resourceLoader.getResource(path);
            privateKeyPEM = new String(resource.getInputStream().readAllBytes());
        }

        // Определяем формат ключа
        boolean isPKCS1 = privateKeyPEM.contains("BEGIN RSA PRIVATE KEY");

        // Очищаем ключ
        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "")
                .trim();

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        if (isPKCS1) {
            // Конвертируем PKCS#1 в PKCS#8 на лету
            return convertPKCS1ToPKCS8(encoded, keyFactory);
        } else {
            // Уже PKCS#8
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        }
    }

    private PrivateKey convertPKCS1ToPKCS8(byte[] pkcs1Bytes, KeyFactory keyFactory) throws Exception {
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;

        byte[] pkcs8Header = new byte[] {
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff),
                0x2, 0x1, 0x0,
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0,
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff)
        };

        byte[] pkcs8bytes = new byte[pkcs8Header.length + pkcs1Length];
        System.arraycopy(pkcs8Header, 0, pkcs8bytes, 0, pkcs8Header.length);
        System.arraycopy(pkcs1Bytes, 0, pkcs8bytes, pkcs8Header.length, pkcs1Length);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8bytes);
        return keyFactory.generatePrivate(keySpec);
    }
}