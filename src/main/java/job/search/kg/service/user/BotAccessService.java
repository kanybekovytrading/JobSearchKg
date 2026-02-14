package job.search.kg.service.user;

import job.search.kg.dto.response.user.AccessCheckResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotAccessService {

    private final BotSubscriptionService subscriptionService;

    public boolean canSearchJobs(Long telegramId) {
        return subscriptionService.hasActiveSubscription(telegramId);
    }

    public boolean canSearchEmployees(Long telegramId) {
        return subscriptionService.hasActiveSubscription(telegramId);
    }

    @Transactional(readOnly = true)
    public AccessCheckResponse checkAccess(Long telegramId) {
        boolean hasSubscription = subscriptionService.hasActiveSubscription(telegramId);
        AccessCheckResponse response = new AccessCheckResponse();
        response.setHasActiveSubscription(hasSubscription);
        response.setCanSearchJobs(hasSubscription);
        response.setCanSearchEmployees(hasSubscription);
        return response;
    }
}