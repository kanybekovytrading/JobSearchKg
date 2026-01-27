package job.search.kg.scheduler;

import job.search.kg.service.admin.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    // Каждый день в 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        refreshTokenService.deleteExpiredTokens();
        log.info("Expired refresh tokens cleanup completed");
    }
}
