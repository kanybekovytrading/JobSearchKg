package job.search.kg.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakePaymentResponse {

    /**
     * ID счета, откуда списывается платеж
     */
    private String accountId;

    /**
     * Реквизиты получателя
     */
    private Map<String, Object> fields;

    /**
     * Уникальный ID транзакции в домене Finik
     */
    private String id;

    /**
     * Дата поступившего запроса в формате UNIX timestamp
     */
    private Long requestDate;

    /**
     * Информация об услуге
     */
    private ServiceInfo service;

    /**
     * Статус платежа: CANCELED, FAILED, PENDING, PROCESSING, SUCCEEDED
     */
    private String status;

    /**
     * Статус ответа: 200 (синхронные услуги) или 201 (асинхронные)
     */
    private Integer statusCode;

    /**
     * Дата проведения транзакции в формате UNIX timestamp
     * Наличие этого поля указывает на конечное состояние платежа
     */
    private Long transactionDate;

    /**
     * ID транзакции клиента
     */
    private String transactionId;

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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceInfo {
        private String id;
    }
}
