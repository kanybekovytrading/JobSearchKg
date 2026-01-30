package job.search.kg.dto.response.user;

import job.search.kg.entity.Subscription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPurchaseResponse {

    private boolean success;
    private String message;
    private Subscription.PlanType planType;
}