package job.search.kg.dto.request.admin;
import job.search.kg.entity.Subscription;
import lombok.Data;

@Data
public class GrantSubscriptionRequest {
    private Long telegramId; // Либо telegramId
    private String username; // Либо username
    private Subscription.PlanType planType;
    private String reason; // Причина выдачи (опционально)
}