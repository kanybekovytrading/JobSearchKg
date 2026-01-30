package job.search.kg.dto.request.user;

import job.search.kg.entity.Subscription;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Subscription.PlanType planType;
    private String redirectUrl;
}