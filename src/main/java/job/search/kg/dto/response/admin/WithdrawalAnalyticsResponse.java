package job.search.kg.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalAnalyticsResponse {

    // Общая статистика
    private Long totalWithdrawals;           // Всего заявок
    private Long successfulWithdrawals;      // Успешных
    private Long failedWithdrawals;          // Неудачных
    private Long pendingWithdrawals;         // Ожидают
    private Long processingWithdrawals;      // В обработке

    // Финансы
    private BigDecimal totalAmountPaid;      // Всего выплачено (сом)
    private BigDecimal platformFee;          // Комиссия платформы 1% (сом)
    private BigDecimal totalWithFee;         // Общая сумма с комиссией (сом)

    // По баллам
    private Long totalPointsWithdrawn;       // Всего баллов выведено
}
