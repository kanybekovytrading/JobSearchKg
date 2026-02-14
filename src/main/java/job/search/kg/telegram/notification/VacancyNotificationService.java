package job.search.kg.telegram.notification;

import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.ResumeRepository;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyNotificationService {

    private final ResumeRepository resumeRepository;
    private final TelegramService telegramService;

    /**
     * Отправить уведомления пользователям о новой вакансии
     * Уведомляем тех, у кого есть активное резюме в той же подкатегории и городе
     */
    @Async
    @Transactional(readOnly = true)
    public void notifyUsersAboutNewVacancy(Vacancy vacancy) {
        try {
            // Находим всех пользователей с активными резюме в той же подкатегории и городе
            List<Resume> matchingResumes = resumeRepository
                    .findBySubcategoryIdAndCityIdAndIsActiveTrue(
                            vacancy.getSubcategory().getId(),
                            vacancy.getCity().getId()
                    );

            // Используем Set чтобы не отправлять дубликаты (если у пользователя несколько резюме)
            Set<Long> notifiedUserIds = new HashSet<>();

            for (Resume resume : matchingResumes) {
                User user = resume.getUser();

                // Пропускаем автора вакансии
                if (user.getTelegramId().equals(vacancy.getUser().getTelegramId())) {
                    continue;
                }

                // Пропускаем если уже отправили этому пользователю
                if (notifiedUserIds.contains(user.getTelegramId())) {
                    continue;
                }

                // Формируем сообщение на языке пользователя
                String message = buildNotificationMessage(
                        vacancy,
                        user.getLanguage()
                );

                // Отправляем уведомление
                telegramService.sendMessage(user.getTelegramId(), message);

                notifiedUserIds.add(user.getTelegramId());

                log.info("Notification sent to user {} about new vacancy {}",
                        user.getTelegramId(), vacancy.getId());
            }

            log.info("Notified {} users about new vacancy in {} - {}",
                    notifiedUserIds.size(),
                    vacancy.getCity().getNameRu(),
                    vacancy.getSubcategory().getNameRu());

        } catch (Exception e) {
            log.error("Error notifying users about new vacancy {}: {}",
                    vacancy.getId(), e.getMessage(), e);
        }
    }

    /**
     * Формирование текста уведомления на нужном языке
     */
    private String buildNotificationMessage(Vacancy vacancy, User.Language language) {
        String subcategoryName = getSubcategoryName(vacancy, language);
        String cityName = getCityName(vacancy, language);
        String title = vacancy.getTitle();
        String salary = vacancy.getSalary() != null ? vacancy.getSalary() : "";

        return switch (language) {
            case KY -> buildKyrgyzMessage(subcategoryName, cityName, title, salary);
            case EN -> buildEnglishMessage(subcategoryName, cityName, title, salary);
            default -> buildRussianMessage(subcategoryName, cityName, title, salary);
        };
    }

    private String buildRussianMessage(String subcategory, String city, String title, String salary) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>Новая вакансия!</b>\n\n");
        msg.append("📋 ").append(title).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (!salary.isEmpty()) {
            msg.append("💰 ").append(salary).append("\n");
        }

        msg.append("\n");
        msg.append("Добавлена новая вакансия по категории <b>").append(subcategory)
                .append("</b> в вашем городе!\n\n");
        msg.append("⚡️ Скорее откройте наше приложение и откликнитесь!");

        return msg.toString();
    }

    private String buildKyrgyzMessage(String subcategory, String city, String title, String salary) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>Жаңы вакансия!</b>\n\n");
        msg.append("📋 ").append(title).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (!salary.isEmpty()) {
            msg.append("💰 ").append(salary).append("\n");
        }

        msg.append("\n");
        msg.append("<b>").append(subcategory)
                .append("</b> категориясы боюнча сиздин шаарыңызда жаңы вакансия кошулду!\n\n");
        msg.append("⚡️ Тездик менен биздин тиркемени ачып, жооп бериңиз!");

        return msg.toString();
    }

    private String buildEnglishMessage(String subcategory, String city, String title, String salary) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>New Vacancy!</b>\n\n");
        msg.append("📋 ").append(title).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (!salary.isEmpty()) {
            msg.append("💰 ").append(salary).append("\n");
        }

        msg.append("\n");
        msg.append("A new vacancy in <b>").append(subcategory)
                .append("</b> category has been added in your city!\n\n");
        msg.append("⚡️ Open our app now and apply!");

        return msg.toString();
    }

    private String getSubcategoryName(Vacancy vacancy, User.Language language) {
        return switch (language) {
            case KY -> vacancy.getSubcategory().getNameKy() != null ?
                    vacancy.getSubcategory().getNameKy() :
                    vacancy.getSubcategory().getNameRu();
            case EN -> vacancy.getSubcategory().getNameEn() != null ?
                    vacancy.getSubcategory().getNameEn() :
                    vacancy.getSubcategory().getNameRu();
            default -> vacancy.getSubcategory().getNameRu();
        };
    }

    private String getCityName(Vacancy vacancy, User.Language language) {
        return switch (language) {
            case EN -> vacancy.getCity().getNameEn() != null ?
                    vacancy.getCity().getNameEn() :
                    vacancy.getCity().getNameRu();
            default -> vacancy.getCity().getNameRu();
        };
    }
}
