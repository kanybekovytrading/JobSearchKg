package job.search.kg.telegram.notification;

import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.VacancyRepository;
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
public class ResumeNotificationService {

    private final VacancyRepository vacancyRepository;
    private final TelegramService telegramService;
    private final BotSubscriptionService botSubscriptionService;

    /**
     * Отправить уведомления работодателям о новом резюме.
     * Уведомляем тех, у кого есть активная вакансия в той же подкатегории и городе.
     */
    @Async
    @Transactional(readOnly = true)
    public void notifyEmployersAboutNewResume(Resume resume) {
        try {
            List<Vacancy> matchingVacancies = vacancyRepository
                    .findBySubcategoryIdAndCityIdAndIsActiveTrue(
                            resume.getSubcategory().getId(),
                            resume.getCity().getId()
                    );

            Set<Long> notifiedUserIds = new HashSet<>();

            for (Vacancy vacancy : matchingVacancies) {
                User employer = vacancy.getUser();

                // Пропускаем автора резюме
                if (employer.getTelegramId().equals(resume.getUser().getTelegramId())) {
                    continue;
                }

                // Не отправляем дубликаты
                if (notifiedUserIds.contains(employer.getTelegramId())) {
                    continue;
                }

                boolean hasSubscription = botSubscriptionService.hasActiveSubscription(employer.getTelegramId());

                String message = buildNotificationMessage(resume, employer.getLanguage(), hasSubscription);

                telegramService.sendMessage(employer.getTelegramId(), message);
                notifiedUserIds.add(employer.getTelegramId());

                log.info("Notification sent to employer {} about new resume {}",
                        employer.getTelegramId(), resume.getId());
            }

            log.info("Notified {} employers about new resume in {} - {}",
                    notifiedUserIds.size(),
                    resume.getCity().getNameRu(),
                    resume.getSubcategory().getNameRu());

        } catch (Exception e) {
            log.error("Error notifying employers about new resume {}: {}",
                    resume.getId(), e.getMessage(), e);
        }
    }

    private String buildNotificationMessage(Resume resume, User.Language language, boolean hasSubscription) {
        String subcategoryName = getSubcategoryName(resume, language);
        String cityName = getCityName(resume, language);
        String name = resume.getUser().getFirstName() + " " + resume.getUser().getLastName();
        String phone = resume.getPhone();

        return switch (language) {
            case KY -> buildKyrgyzMessage(subcategoryName, cityName, name, phone, hasSubscription);
            case EN -> buildEnglishMessage(subcategoryName, cityName, name, phone, hasSubscription);
            default -> buildRussianMessage(subcategoryName, cityName, name, phone, hasSubscription);
        };
    }

    private String buildRussianMessage(String subcategory, String city, String name,
                                       String phone, boolean hasSubscription) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>Новое резюме!</b>\n\n");
        msg.append("👤 ").append(name).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (hasSubscription && phone != null && !phone.isEmpty()) {
            msg.append("📞 ").append(phone).append("\n");
            msg.append("\nВ вашем городе появился новый соискатель по категории <b>")
                    .append(subcategory).append("</b>!\n\n");
            msg.append("⚡️ Свяжитесь с ним прямо сейчас!");
        } else {
            msg.append("📞 <i>Чтобы увидеть контакт — приобретите подписку быстрее!</i>\n");
            msg.append("\nВ вашем городе появился новый соискатель по категории <b>")
                    .append(subcategory).append("</b>!\n\n");
            msg.append("⚡️ Скорее откройте наше приложение и приобретите подписку!");
        }

        return msg.toString();
    }

    private String buildKyrgyzMessage(String subcategory, String city, String name,
                                      String phone, boolean hasSubscription) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>Жаңы резюме!</b>\n\n");
        msg.append("👤 ").append(name).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (hasSubscription && phone != null && !phone.isEmpty()) {
            msg.append("📞 ").append(phone).append("\n");
            msg.append("\n<b>").append(subcategory)
                    .append("</b> категориясы боюнча сиздин шаарыңызда жаңы резюме кошулду!\n\n");
            msg.append("⚡️ Азыр эле байланышыңыз!");
        } else {
            msg.append("📞 <i>Байланышты көрүү үчүн — жазылууну тезирээк сатып алыңыз!</i>\n");
            msg.append("\n<b>").append(subcategory)
                    .append("</b> категориясы боюнча сиздин шаарыңызда жаңы резюме кошулду!\n\n");
            msg.append("⚡️ Тездик менен биздин тиркемени ачып, жазылуу алыңыз!");
        }

        return msg.toString();
    }

    private String buildEnglishMessage(String subcategory, String city, String name,
                                       String phone, boolean hasSubscription) {
        StringBuilder msg = new StringBuilder();
        msg.append("🔔 <b>New Resume!</b>\n\n");
        msg.append("👤 ").append(name).append("\n");
        msg.append("📂 ").append(subcategory).append("\n");
        msg.append("🏙 ").append(city).append("\n");

        if (hasSubscription && phone != null && !phone.isEmpty()) {
            msg.append("📞 ").append(phone).append("\n");
            msg.append("\nA new applicant in <b>").append(subcategory)
                    .append("</b> category has appeared in your city!\n\n");
            msg.append("⚡️ Reach out right now!");
        } else {
            msg.append("📞 <i>To see the contact — get a subscription faster!</i>\n");
            msg.append("\nA new applicant in <b>").append(subcategory)
                    .append("</b> category has appeared in your city!\n\n");
            msg.append("⚡️ Open our app now and get a subscription!");
        }

        return msg.toString();
    }

    private String getSubcategoryName(Resume resume, User.Language language) {
        return switch (language) {
            case KY -> resume.getSubcategory().getNameKy() != null ?
                    resume.getSubcategory().getNameKy() : resume.getSubcategory().getNameRu();
            case EN -> resume.getSubcategory().getNameEn() != null ?
                    resume.getSubcategory().getNameEn() : resume.getSubcategory().getNameRu();
            default -> resume.getSubcategory().getNameRu();
        };
    }

    private String getCityName(Resume resume, User.Language language) {
        return switch (language) {
            case EN -> resume.getCity().getNameEn() != null ?
                    resume.getCity().getNameEn() : resume.getCity().getNameRu();
            default -> resume.getCity().getNameRu();
        };
    }
}