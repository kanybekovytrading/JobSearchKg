package job.search.kg.scheduler;

import job.search.kg.repo.FreeAccessTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class FreeAccessCleanupScheduler {

    private final FreeAccessTrackingRepository freeAccessTrackingRepository;

    /**
     * Очистка записей старше 7 дней
     * Запускается каждый день в 3:00 ночи
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldRecords() {
        LocalDate cutoffDate = LocalDate.now().minusDays(7);

        try {
            freeAccessTrackingRepository.deleteOldRecords(cutoffDate);
            log.info("Successfully cleaned up free access tracking records older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("Error during free access tracking cleanup", e);
        }
    }
}