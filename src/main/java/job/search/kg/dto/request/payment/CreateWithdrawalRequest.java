package job.search.kg.dto.request.payment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWithdrawalRequest {

    /**
     * Номер телефона получателя (Finik)
     * Формат: +996XXXXXXXXX или 0XXXXXXXXX
     */
    private String recipientPhone;
    private String serviceId;
    private String serviceName;

    /**
     * Сумма вывода (в сомах)
     * Минимум: 1, Максимум: 10000
     */
    private BigDecimal amount;
}