package job.search.kg.dto.response.user;

import job.search.kg.entity.Subscription;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubscriptionStatusResponse {
    private Boolean hasActiveSubscription;
    private Subscription.PlanType planType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long daysLeft;
}
