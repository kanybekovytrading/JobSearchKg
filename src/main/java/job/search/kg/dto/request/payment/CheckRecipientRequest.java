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
public class CheckRecipientRequest {

    /**
     * Объект с обязательными реквизитами получателя
     * Пример: { "amount": 100, "phone": "+996502502502" }
     */
    private Map<String, Object> fields;

    /**
     * ID услуги в домене Finik (например: "averspay")
     */
    private String service;
}
