package job.search.kg.dto.request.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakePaymentRequest {

    /**
     * ID счета, откуда списывается платеж
     */
    private String accountId;

    /**
     * Объект с обязательными реквизитами получателя
     * Пример: { "amount": 100, "phone": "+996502502502" }
     */
    private Map<String, Object> fields;

    /**
     * Объект с ID услуги
     * Пример: { "id": "averspay" }
     */
    private ServiceInfo service;

    /**
     * Уникальный ID транзакции для предотвращения дубликатов
     */
    private String transactionId;

    /**
     * ID пользователя, от имени которого делается запрос
     */
    private String userId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private String id;
    }
}
