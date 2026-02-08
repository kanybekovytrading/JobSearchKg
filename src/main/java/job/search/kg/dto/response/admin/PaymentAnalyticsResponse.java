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
public class PaymentAnalyticsResponse {

    // Подписки
    private Long activeSubscriptionsCount;      // Кол-во активных подписок
    private Long totalSubscriptionsPurchased;   // Всего куплено подписок

    // Финансы
    private BigDecimal totalRevenue;            // Общий доход (сом)
    private BigDecimal paymentGatewayFee;       // Комиссия платежной системы 2% (сом)
    private BigDecimal afterGatewayFee;         // После комиссии платежки (сом)
    private BigDecimal partnerShare;            // Доля партнера 25% (от суммы после платежки)
    private BigDecimal netProfit;               // Чистая прибыль (остаток после всех вычетов)

    // Детализация
    private BigDecimal totalPaid;               // Всего оплачено успешно
    private BigDecimal totalPending;            // Ожидает оплаты
    private BigDecimal totalFailed;             // Неудачные платежи
}
