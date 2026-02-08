package job.search.kg.dto.response.admin;

import job.search.kg.entity.Withdrawal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalListItemResponse {

    private Long id;
    private Long telegramId;
    private String recipientPhone;
    private String recipientName;
    private BigDecimal amount;              // Сумма (сом)
    private Integer points;                 // Баллы
    private String paymentMethod;           // Метод оплаты (M-Bank, Megacom и т.д.)
    private Withdrawal.WithdrawalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}
