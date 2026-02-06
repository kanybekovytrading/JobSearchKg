package job.search.kg.service.user;

import job.search.kg.entity.ResumeBoost;
import job.search.kg.entity.User;
import job.search.kg.entity.VacancyBoost;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoostNotificationService {

    private final TelegramService telegramService;

    /**
     * Уведомление об истечении буста вакансии
     */
    public void notifyVacancyBoostExpiring(VacancyBoost boost) {
        try {
            User user = boost.getUser();
            String vacancyTitle = boost.getVacancy().getTitle();
            String expiresAt = formatDateTime(boost.getExpiresAt());
            String timeLeft = formatTimeLeft(boost.getExpiresAt());

            String message = getBoostExpiringMessage(
                    user.getLanguage(),
                    "вакансия",
                    vacancyTitle,
                    expiresAt,
                    timeLeft
            );

            telegramService.sendMessage(user.getTelegramId(), message);

            log.info("Sent boost expiring notification: userId={}, vacancyId={}",
                    user.getTelegramId(), boost.getVacancy().getId());
        } catch (Exception e) {
            log.error("Failed to send boost expiring notification for vacancy boost: {}",
                    boost.getId(), e);
        }
    }

    /**
     * Уведомление об истечении буста резюме
     */
    public void notifyResumeBoostExpiring(ResumeBoost boost) {
        try {
            User user = boost.getUser();
            String resumeName = boost.getResume().getName();
            String expiresAt = formatDateTime(boost.getExpiresAt());
            String timeLeft = formatTimeLeft(boost.getExpiresAt());

            String message = getBoostExpiringMessage(
                    user.getLanguage(),
                    "резюме",
                    resumeName,
                    expiresAt,
                    timeLeft
            );

            telegramService.sendMessage(user.getTelegramId(), message);

            log.info("Sent boost expiring notification: userId={}, resumeId={}",
                    user.getTelegramId(), boost.getResume().getId());
        } catch (Exception e) {
            log.error("Failed to send boost expiring notification for resume boost: {}",
                    boost.getId(), e);
        }
    }

    /**
     * Уведомление о том, что буст истек
     */
    public void notifyVacancyBoostExpired(VacancyBoost boost) {
        try {
            User user = boost.getUser();
            String vacancyTitle = boost.getVacancy().getTitle();

            String message = getBoostExpiredMessage(
                    user.getLanguage(),
                    "вакансия",
                    vacancyTitle
            );

            telegramService.sendMessage(user.getTelegramId(), message);

            log.info("Sent boost expired notification: userId={}, vacancyId={}",
                    user.getTelegramId(), boost.getVacancy().getId());
        } catch (Exception e) {
            log.error("Failed to send boost expired notification for vacancy boost: {}",
                    boost.getId(), e);
        }
    }

    /**
     * Уведомление о том, что буст резюме истек
     */
    public void notifyResumeBoostExpired(ResumeBoost boost) {
        try {
            User user = boost.getUser();
            String resumeName = boost.getResume().getName();

            String message = getBoostExpiredMessage(
                    user.getLanguage(),
                    "резюме",
                    resumeName
            );

            telegramService.sendMessage(user.getTelegramId(), message);

            log.info("Sent boost expired notification: userId={}, resumeId={}",
                    user.getTelegramId(), boost.getResume().getId());
        } catch (Exception e) {
            log.error("Failed to send boost expired notification for resume boost: {}",
                    boost.getId(), e);
        }
    }

    private String getBoostExpiringMessage(
            User.Language language,
            String type,
            String title,
            String expiresAt,
            String timeLeft
    ) {
        return switch (language) {
            case RU -> String.format(
                    "⏰ Скоро истечет Поднятие Показа!\n\n" +
                            "Ваша %s \"%s\" перестанет показываться в топе через %s\n\n" +
                            "Истекает: %s\n\n" +
                            "💡 Вы можете продлить Поднятие Показа, чтобы оставаться в топе!",
                    type, title, timeLeft, expiresAt
            );
            case KY -> String.format(
                    "⏰ Топко чыгаруу бүтүп баратат!\n\n" +
                            "Сиздин %s \"%s\" %s ичинде топтон чыгат\n\n" +
                            "Бүтөт: %s\n\n" +
                            "💡 Топто калуу үчүн узартсаңыз болот!",
                    type, title, timeLeft, expiresAt
            );
            case EN -> String.format(
                    "⏰ Boost expiring soon!\n\n" +
                            "Your %s \"%s\" will stop showing at the top in %s\n\n" +
                            "Expires: %s\n\n" +
                            "💡 You can extend Boost to stay on top!",
                    type, title, timeLeft, expiresAt
            );
        };
    }

    private String getBoostExpiredMessage(
            User.Language language,
            String type,
            String title
    ) {
        return switch (language) {
            case RU -> String.format(
                    "❌ Поднятие Показа истек\n\n" +
                            "Ваша %s \"%s\" больше не показывается в топе.\n\n" +
                            "💡 Активируйте новое Поднятие Показа, чтобы снова подняться в топ результатов поиска!",
                    type, title
            );
            case KY -> String.format(
                    "❌ Топко чыгаруу бүттү\n\n" +
                            "Сиздин %s \"%s\" топто көрсөтүлбөйт.\n\n" +
                            "💡 Издөө жыйынтыктарында топко кайра көтөрүлүү үчүн жаңы Топко чыгарууну активдештириңиз!",
                    type, title
            );
            case EN -> String.format(
                    "❌ Boost expired\n\n" +
                            "Your %s \"%s\" is no longer showing at the top.\n\n" +
                            "💡 Activate new Boost to get back to the top of search results!",
                    type, title
            );
        };
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private String formatTimeLeft(LocalDateTime expiresAt) {
        Duration duration = Duration.between(LocalDateTime.now(), expiresAt);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return String.format("%d ч. %d мин.", hours, minutes);
        } else {
            return String.format("%d мин.", minutes);
        }
    }
}