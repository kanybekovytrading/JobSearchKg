package job.search.kg.service.user;

import job.search.kg.dto.response.user.AccessCheckResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotAccessService {

    private final BotSubscriptionService subscriptionService;
    private final BotPointsService pointsService;
    private static final int POINTS_FOR_SEARCH_ACCESS = 1500;

    public boolean canSearchJobs(Long telegramId) {
        return subscriptionService.hasActiveSubscription(telegramId) ||
                pointsService.hasEnoughPoints(telegramId, POINTS_FOR_SEARCH_ACCESS);
    }

    public boolean canSearchEmployees(Long telegramId) {
        return subscriptionService.hasActiveSubscription(telegramId) ||
                pointsService.hasEnoughPoints(telegramId, POINTS_FOR_SEARCH_ACCESS);
    }

    @Transactional(readOnly = true)
    public AccessCheckResponse checkAccess(Long telegramId) {
        boolean hasSubscription = subscriptionService.hasActiveSubscription(telegramId);
        boolean hasPointsForSearch = pointsService.hasEnoughPoints(telegramId, POINTS_FOR_SEARCH_ACCESS);
        AccessCheckResponse response = new AccessCheckResponse();
        response.setHasActiveSubscription(hasSubscription);
        response.setCanSearchJobs(hasSubscription || hasPointsForSearch);
        response.setCanSearchEmployees(hasSubscription || hasPointsForSearch);
        return response;
    }
}