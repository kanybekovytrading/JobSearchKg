package job.search.kg.scheduler;

import job.search.kg.repo.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void deactivateExpiredSubscriptions() {
        log.info("Starting scheduled task to deactivate expired subscriptions");

        LocalDateTime now = LocalDateTime.now();
        int deactivatedCount = subscriptionRepository.deactivateExpiredSubscriptions(now);

        log.info("Deactivated {} expired subscriptions", deactivatedCount);
    }
}
