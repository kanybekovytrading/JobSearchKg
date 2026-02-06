package job.search.kg.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetServicesResponse {

    /**
     * Список услуг
     */
    private List<ServiceDTO> services;

    /**
     * Статус код
     */
    private Integer statusCode;

    /**
     * Общее количество услуг
     */
    private Integer total;

    /**
     * Сообщение об ошибке (если есть)
     */
    private String errorMessage;

    @JsonProperty("message")
    public void setMessage(String message) {
        this.errorMessage = message;
    }
}
