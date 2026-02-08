package job.search.kg.dto.request.payment;

import job.search.kg.entity.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на покупку подписки за баллы")
public class PurchaseSubscriptionRequest {

    @NotNull(message = "Plan type is required")
    @Schema(description = "Тип подписки", example = "THREE_DAYS")
    private Subscription.PlanType planType;
}
