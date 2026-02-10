package job.search.kg.scheduler;

import job.search.kg.entity.ResumeBoost;
import job.search.kg.entity.VacancyBoost;
import job.search.kg.repo.ResumeBoostRepository;
import job.search.kg.repo.VacancyBoostRepository;
import job.search.kg.service.user.BoostNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoostExpirationScheduler {

    private final VacancyBoostRepository vacancyBoostRepository;
    private final ResumeBoostRepository resumeBoostRepository;
    private final BoostNotificationService notificationService;

    /**
     * Деактивация истекших бустов вакансий
     * Запускается каждый час
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deactivateExpiredVacancyBoosts() {
        LocalDateTime now = LocalDateTime.now();

        // Находим все активные бусты с истекшим сроком
        List<VacancyBoost> expiredBoosts = vacancyBoostRepository
                .findByIsActiveTrueAndExpiresAtBefore(now);

        if (!expiredBoosts.isEmpty()) {
            log.info("Found {} expired vacancy boosts to deactivate", expiredBoosts.size());

            // Деактивируем
            expiredBoosts.forEach(boost -> {
                notificationService.notifyVacancyBoostExpired(boost);
                boost.setIsActive(false);
                log.debug("Deactivating vacancy boost: vacancyId={}, boostId={}, expiresAt={}",
                        boost.getVacancy().getId(),
                        boost.getId(),
                        boost.getExpiresAt());
            });

            vacancyBoostRepository.saveAll(expiredBoosts);


            log.info("Successfully deactivated {} vacancy boosts", expiredBoosts.size());
        }
    }

    /**
     * Деактивация истекших бустов резюме
     * Запускается каждый час
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deactivateExpiredResumeBoosts() {
        LocalDateTime now = LocalDateTime.now();

        // Находим все активные бусты с истекшим сроком
        List<ResumeBoost> expiredBoosts = resumeBoostRepository
                .findByIsActiveTrueAndExpiresAtBefore(now);

        if (!expiredBoosts.isEmpty()) {
            log.info("Found {} expired resume boosts to deactivate", expiredBoosts.size());

            // Деактивируем
            expiredBoosts.forEach(boost -> {
                boost.setIsActive(false);
                notificationService.notifyResumeBoostExpired(boost);
                log.debug("Deactivating resume boost: resumeId={}, boostId={}, expiresAt={}",
                        boost.getResume().getId(),
                        boost.getId(),
                        boost.getExpiresAt());
            });

            resumeBoostRepository.saveAll(expiredBoosts);

            log.info("Successfully deactivated {} resume boosts", expiredBoosts.size());
        }
    }

    /**
     * Очистка старых деактивированных бустов (опционально)
     * Запускается каждый день в 3:00 ночи
     * Удаляет бусты старше 30 дней
     */
    @Scheduled(cron = "0 0 3 * * *") // Каждый день в 3:00
    @Transactional
    public void cleanupOldBoosts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(5);

        // Удаляем старые неактивные бусты вакансий
        List<VacancyBoost> oldVacancyBoosts = vacancyBoostRepository
                .findByIsActiveFalseAndCreatedAtBefore(thirtyDaysAgo);

        if (!oldVacancyBoosts.isEmpty()) {
            vacancyBoostRepository.deleteAll(oldVacancyBoosts);
            log.info("Cleaned up {} old vacancy boosts", oldVacancyBoosts.size());
        }

        // Удаляем старые неактивные бусты резюме
        List<ResumeBoost> oldResumeBoosts = resumeBoostRepository
                .findByIsActiveFalseAndCreatedAtBefore(thirtyDaysAgo);

        if (!oldResumeBoosts.isEmpty()) {
            resumeBoostRepository.deleteAll(oldResumeBoosts);
            log.info("Cleaned up {} old resume boosts", oldResumeBoosts.size());
        }
    }

    /**
     * Уведомление пользователей об истечении буста (за 1 час)
     * Запускается каждый час
     */
    @Scheduled(cron = "0 0 * * * *") // Каждый час
    @Transactional(readOnly = true)
    public void notifyExpiringBoosts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // Находим бусты, которые истекут в течение часа
        List<VacancyBoost> expiringVacancyBoosts = vacancyBoostRepository
                .findByIsActiveTrueAndExpiresAtBetween(now, oneHourLater);
        List<ResumeBoost> expiringResumeBoosts = resumeBoostRepository
                .findByIsActiveTrueAndExpiresAtBetween(now, oneHourLater);

        // TODO: Отправить уведомления в Telegram
        if (!expiringVacancyBoosts.isEmpty()) {
            expiringVacancyBoosts.forEach(notificationService::notifyVacancyBoostExpiring);
            log.info("Found {} vacancy boosts expiring within 1 hour", expiringVacancyBoosts.size());
        }

        if (!expiringResumeBoosts.isEmpty()) {
            log.info("Found {} resume boosts expiring within 1 hour", expiringResumeBoosts.size());
            expiringResumeBoosts.forEach(notificationService::notifyResumeBoostExpiring);
        }
    }
}
