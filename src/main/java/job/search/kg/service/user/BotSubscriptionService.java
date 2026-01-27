package job.search.kg.service.user;


import job.search.kg.dto.response.user.SubscriptionStatusResponse;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.SubscriptionRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> subscription = subscriptionRepository
                .findActiveSubscription(user, LocalDateTime.now());

        return subscription.isPresent();
    }

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Subscription> activeSubscription = subscriptionRepository
                .findActiveSubscription(user, LocalDateTime.now());

        SubscriptionStatusResponse response = new SubscriptionStatusResponse();

        if (activeSubscription.isPresent()) {
            Subscription sub = activeSubscription.get();
            response.setHasActiveSubscription(true);
            response.setPlanType(sub.getPlanType());
            response.setStartDate(sub.getStartDate());
            response.setEndDate(sub.getEndDate());
            response.setDaysLeft(calculateDaysLeft(sub.getEndDate()));
        } else {
            response.setHasActiveSubscription(false);
        }

        return response;
    }

    @Transactional
    public Subscription createSubscription(Long telegramId, Subscription.PlanType planType, String paymentId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, planType);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanType(planType);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setIsActive(true);
        subscription.setPaymentId(paymentId);

        return subscriptionRepository.save(subscription);
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, Subscription.PlanType planType) {
        return switch (planType) {
            case ONE_WEEK -> startDate.plusDays(7);
            case ONE_MONTH -> startDate.plusDays(30);
            case THREE_MONTHS -> startDate.plusDays(90);
        };
    }

    private long calculateDaysLeft(LocalDateTime endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }
}
