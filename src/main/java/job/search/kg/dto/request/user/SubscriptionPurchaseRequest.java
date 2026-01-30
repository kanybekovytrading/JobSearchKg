package job.search.kg.dto.request.user;

import jakarta.validation.constraints.NotNull;
import job.search.kg.entity.Subscription;
import lombok.Data;

@Data
public class SubscriptionPurchaseRequest {

    @NotNull(message = "Telegram ID обязателен")
    private Long telegramId;

    @NotNull(message = "Тип подписки обязателен")
    private Subscription.PlanType planType;
}