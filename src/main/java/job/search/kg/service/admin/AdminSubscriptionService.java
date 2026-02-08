package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.GrantSubscriptionRequest;
import job.search.kg.entity.Admin;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public Page<Subscription> getAllSubscriptions(Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }

    @Transactional
    public Subscription grantSubscription(GrantSubscriptionRequest request, String adminEmail) {
        User user;

        if (request.getTelegramId() != null) {
            user = userRepository.findByTelegramId(request.getTelegramId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } else if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } else {
            throw new IllegalArgumentException("Either telegramId or username must be provided");
        }

        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, request.getPlanType());

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanType(request.getPlanType());
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setIsActive(true);
        subscription.setGrantedByAdmin(admin);

        subscriptionRepository.save(subscription);

        // Уведомление пользователю
        telegramService.sendMessage(
                user.getTelegramId(),
                String.format(
                        "🎁 Вам выдана подписка бесплатно!\n\n" +
                                "Тариф: %s\n" +
                                "Действует до: %s\n\n" +
                                "%s",
                        getPlanName(request.getPlanType()),
                        endDate.toLocalDate(),
                        request.getReason() != null ? "Причина: " + request.getReason() : ""
                )
        );

        return subscription;
    }

    @Transactional
    public void deactivateSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public Long countActiveSubscriptions() {
        return subscriptionRepository.countByIsActive(true);
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, Subscription.PlanType planType) {
        return switch (planType) {
            case THREE_DAYS -> startDate.plusDays(3);
            case ONE_WEEK -> startDate.plusDays(7);
            case ONE_MONTH -> startDate.plusDays(30);
            case THREE_MONTHS -> startDate.plusDays(90);
        };
    }

    private String getPlanName(Subscription.PlanType planType) {
        return switch (planType) {
            case THREE_DAYS -> "3 дня";
            case ONE_WEEK -> "1 неделя";
            case ONE_MONTH -> "1 месяц";
            case THREE_MONTHS -> "3 месяца";
        };
    }
}