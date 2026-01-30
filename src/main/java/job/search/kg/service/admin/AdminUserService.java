package job.search.kg.service.admin;

import job.search.kg.dto.response.admin.SubscriptionDTO;
import job.search.kg.dto.response.admin.UserProfileDTO;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ResumeRepository repository;
    private final VacancyRepository vacancyRepository;

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ищем первое резюме
        Resume firstResume = repository.findFirstByUserIdOrderByCreatedAtAsc(userId)
                .orElse(null);

        // Если резюме нет, ищем первую вакансию
        Vacancy firstVacancy = null;
        if (firstResume == null) {
            firstVacancy = vacancyRepository.findFirstByUserIdOrderByCreatedAtAsc(userId)
                    .orElse(null);
        }

        // Извлекаем данные из резюме или вакансии
        String city = null;
        String category = null;
        String subcategory = null;

        if (firstResume != null) {
            city = firstResume.getCity() != null ? firstResume.getCity().getNameRu() : null;
            category = firstResume.getCategory() != null ? firstResume.getCategory().getNameRu() : null;
            subcategory = firstResume.getSubcategory() != null ? firstResume.getSubcategory().getNameRu() : null;
        } else if (firstVacancy != null) {
            city = firstVacancy.getCity() != null ? firstVacancy.getCity().getNameRu() : null;
            category = firstVacancy.getCategory() != null ? firstVacancy.getCategory().getNameRu() : null;
            subcategory = firstVacancy.getSubcategory() != null ? firstVacancy.getSubcategory().getNameRu() : null;
        }

        return new UserProfileDTO(
                user.getId(),
                user.getTelegramId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                city,
                category,
                subcategory,
                "user",
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public SubscriptionDTO getUserActivity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription activeSubscription = subscriptionRepository
                .findFirstByUserIdAndIsActiveTrueOrderByEndDateDesc(userId)
                .orElse(null);

        // Подсчет сообщений
        Long messageCount = chatMessageRepository.countByUserId(userId);

        String status = activeSubscription != null &&
                activeSubscription.getEndDate().isAfter(LocalDateTime.now())
                ? "Активна" : "Неактивна";

        String history = activeSubscription != null
                ? calculateSubscriptionDuration(activeSubscription)
                : "Нет подписки";

        LocalDateTime subDate = activeSubscription != null
                ? activeSubscription.getStartDate()
                : null;

        return new SubscriptionDTO(
                status,
                history,
                subDate,
                messageCount.intValue()
        );
    }

    private String calculateSubscriptionDuration(Subscription subscription) {
        long weeks = ChronoUnit.WEEKS.between(subscription.getStartDate(),
                subscription.getEndDate());
        if (weeks == 1) return "1 неделя";
        if (weeks == 4) return "1 месяц";
        if (weeks >= 12) return "3 месяца";
        return weeks + " недель";
    }

    @Transactional
    public void toggleBanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsBanned(!user.getIsBanned());
        userRepository.save(user);
    }
}
