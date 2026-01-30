package job.search.kg.dto.response.user;

import job.search.kg.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private UUID paymentId;
    private String paymentUrl;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}