package job.search.kg.telegram.notification;

import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.ResumeRepository;
import job.search.kg.service.user.BotSubscriptionService;
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
    private final BotSubscriptionService botSubscriptionService;

    /**
     * Отправить уведомления соискателям о новой вакансии.
     * Уведомляем тех, у кого есть активное резюме в той же подкатегории и городе.
     */
    @Async
    @Transactional(readOnly = true)
    public void notifyUsersAboutNewVacancy(Vacancy vacancy) {
        try {
            List<Resume> matchingResumes = resumeRepository
                    .findBySubcategoryIdAndCityIdAndIsActiveTrue(
                            vacancy.getSubcategory().getId(),
                            vacancy.getCity().getId()
                    );

            Set<Long> notifiedUserIds = new HashSet<>();

            for (Resume resume : matchingResumes) {
                User user = resume.getUser();

                // Пропускаем автора вакансии
                if (user.getTelegramId().equals(vacancy.getUser().getTelegramId())) {
                    continue;
                }

                if (notifiedUserIds.contains(user.getTelegramId())) {
                    continue;
                }

                boolean hasSubscription = botSubscriptionService.hasActiveSubscription(user.getTelegramId());

                String message = buildNotificationMessage(vacancy, user.getLanguage(), hasSubscription);

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

    private String buildNotificationMessage(Vacancy vacancy, User.Language language, boolean hasSubscription) {
        String subcategoryName = getSubcategoryName(vacancy, language);
        String cityName = getCityName(vacancy, language);
        String title = vacancy.getTitle();
        String salary = vacancy.getSalary() != null ? vacancy.getSalary() : "";
        String phone = vacancy.getPhone();

        return switch (language) {
            case KY -> buildKyrgyzMessage(subcategoryName, cityName, title, salary, phone, hasSubscription);
            case EN -> buildEnglishMessage(subcategoryName, cityName, title, salary, phone, hasSubscription);
            default -> buildRussianMessage(subcategoryName, cityName, title, salary, phone, hasSubscription);
        };
    }

    private String buildRussianMessage(String subcategory, String city, String title,
                                       String salary, String phone, boolean hasSubscription) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>Новая вакансия!</b>\n\n");
        msg.append("📋 ").append(title).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (!salary.isEmpty()) {
            msg.append("💰 ").append(salary).append("\n");
        }

        if (hasSubscription && phone != null && !phone.isEmpty()) {
            msg.append("📞 ").append(phone).append("\n");
            msg.append("\nДобавлена новая вакансия по категории <b>").append(subcategory)
                    .append("</b> в вашем городе!\n\n");
            msg.append("⚡️ Свяжитесь с работодателем прямо сейчас!");
        } else {
            msg.append("📞 <i>Чтобы увидеть контакт — приобретите подписку быстрее!</i>\n");
            msg.append("\nДобавлена новая вакансия по категории <b>").append(subcategory)
                    .append("</b> в вашем городе!\n\n");
            msg.append("⚡️ Скорее откройте наше приложение и приобретите подписку!");
        }

        return msg.toString();
    }

    private String buildKyrgyzMessage(String subcategory, String city, String title,
                                      String salary, String phone, boolean hasSubscription) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>Жаңы вакансия!</b>\n\n");
        msg.append("📋 ").append(title).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (!salary.isEmpty()) {
            msg.append("💰 ").append(salary).append("\n");
        }

        if (hasSubscription && phone != null && !phone.isEmpty()) {
            msg.append("📞 ").append(phone).append("\n");
            msg.append("\n<b>").append(subcategory)
                    .append("</b> категориясы боюнча сиздин шаарыңызда жаңы вакансия кошулду!\n\n");
            msg.append("⚡️ Азыр эле иш берүүчү менен байланышыңыз!");
        } else {
            msg.append("📞 <i>Байланышты көрүү үчүн — жазылууну тезирээк сатып алыңыз!</i>\n");
            msg.append("\n<b>").append(subcategory)
                    .append("</b> категориясы боюнча сиздин шаарыңызда жаңы вакансия кошулду!\n\n");
            msg.append("⚡️ Тездик менен биздин тиркемени ачып, жазылуу алыңыз!");
        }

        return msg.toString();
    }

    private String buildEnglishMessage(String subcategory, String city, String title,
                                       String salary, String phone, boolean hasSubscription) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>New Vacancy!</b>\n\n");
        msg.append("📋 ").append(title).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (!salary.isEmpty()) {
            msg.append("💰 ").append(salary).append("\n");
        }

        if (hasSubscription && phone != null && !phone.isEmpty()) {
            msg.append("📞 ").append(phone).append("\n");
            msg.append("\nA new vacancy in <b>").append(subcategory)
                    .append("</b> category has been added in your city!\n\n");
            msg.append("⚡️ Contact the employer right now!");
        } else {
            msg.append("📞 <i>To see the contact — get a subscription faster!</i>\n");
            msg.append("\nA new vacancy in <b>").append(subcategory)
                    .append("</b> category has been added in your city!\n\n");
            msg.append("⚡️ Open our app now and get a subscription!");
        }

        return msg.toString();
    }
    private String getSubcategoryName(Vacancy vacancy, User.Language language) {
        return switch (language) {
            case KY -> vacancy.getSubcategory().getNameKy() != null ?
                    vacancy.getSubcategory().getNameKy() : vacancy.getSubcategory().getNameRu();
            case EN -> vacancy.getSubcategory().getNameEn() != null ?
                    vacancy.getSubcategory().getNameEn() : vacancy.getSubcategory().getNameRu();
            default -> vacancy.getSubcategory().getNameRu();
        };
    }

    private String getCityName(Vacancy vacancy, User.Language language) {
        return switch (language) {
            case EN -> vacancy.getCity().getNameEn() != null ?
                    vacancy.getCity().getNameEn() : vacancy.getCity().getNameRu();
            default -> vacancy.getCity().getNameRu();
        };
    }
}