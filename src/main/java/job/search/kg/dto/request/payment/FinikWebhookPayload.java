package job.search.kg.dto.request.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class FinikWebhookPayload {

    private String id;

    @JsonProperty("transactionId")
    private String transactionId;

    private String status; // SUCCEEDED, FAILED, PROCESSING

    private Integer amount;

    private Integer net;

    @JsonProperty("accountId")
    private String accountId;

    private Map<String, Object> fields;

    @JsonProperty("requestDate")
    private Long requestDate;

    @JsonProperty("transactionDate")
    private Long transactionDate;

    @JsonProperty("transactionType")
    private String transactionType;

    @JsonProperty("receiptNumber")
    private String receiptNumber;

    // Опциональные поля
    private Map<String, Object> data; // только для WEB

    private ItemInfo item;

    private ServiceInfo service;

    @Data
    public static class ItemInfo {
        private String id;
    }

    @Data
    public static class ServiceInfo {
        private String id;
    }
}
