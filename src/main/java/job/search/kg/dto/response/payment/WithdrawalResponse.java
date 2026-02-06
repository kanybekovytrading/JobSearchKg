package job.search.kg.dto.response.payment;


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
public class WithdrawalResponse {

    private Long id;

    private String transactionId;

    private String finikTransactionId;

    private String serviceId;

    private String serviceName;

    private String recipientPhone;

    private String recipientName;

    private BigDecimal amount;

    private Withdrawal.WithdrawalStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private String errorMessage;
}
