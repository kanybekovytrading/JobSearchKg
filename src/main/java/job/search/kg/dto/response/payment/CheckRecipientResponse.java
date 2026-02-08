package job.search.kg.dto.response.payment;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckRecipientResponse {

    private Integer statusCode;

    /**
     * ФИО получателя (если доступно)
     * Для Finik - инициалы, например "А. У."
     */
    private String name;

    /**
     * Телефон получателя (для услуги Finik)
     */
    private String phone;

    /**
     * Сообщение об ошибке (если есть)
     */
    private String errorMessage;

    @JsonProperty("transactionType")
    private String transactionType;  // MBank возвращает это поле

    // Дополнительные поля которые могут прийти
    @JsonProperty("fields")
    private Object fields;
}