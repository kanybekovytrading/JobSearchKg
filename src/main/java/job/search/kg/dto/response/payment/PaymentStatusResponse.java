package job.search.kg.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {

    /**
     * Уникальный ID транзакции в домене Finik
     */
    private String id;

    /**
     * ID счета
     */
    private String accountId;

    /**
     * Сумма платежа
     */
    private Integer amount;

    /**
     * ID клиента
     */
    private String clientId;

    /**
     * Реквизиты получателя
     */
    private Map<String, Object> fields;

    /**
     * Информация о получателе
     */
    private RecipientInfo recipient;

    /**
     * Дата запроса в формате UNIX timestamp
     */
    private Long requestDate;

    /**
     * Информация об услуге
     */
    private ServiceInfo service;

    /**
     * Статус платежа: SUCCEEDED, FAILED, PROCESSING
     */
    private String status;

    /**
     * Статус код ответа
     */
    private Integer statusCode;

    /**
     * Дата транзакции в формате UNIX timestamp
     */
    private Long transactionDate;

    /**
     * ID транзакции
     */
    private String transactionId;

    /**
     * Тип транзакции
     */
    private String transactionType;

    /**
     * ID пользователя
     */
    private String userId;

    /**
     * Сообщение об ошибке (если есть)
     */
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        private String id;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private String id;
    }
}
