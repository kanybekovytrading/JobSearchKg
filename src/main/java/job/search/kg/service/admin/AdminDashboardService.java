package job.search.kg.service.admin;

import job.search.kg.dto.response.admin.DashboardResponse;
import job.search.kg.entity.Feedback;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final VacancyRepository vacancyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedbackRepository feedbackRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        DashboardResponse response = new DashboardResponse();

        // Пользователи
        long totalUsers = userRepository.count();
        long newUsersToday = countNewUsersToday();
        response.setTotalUsers(totalUsers);
        response.setNewUsersToday(newUsersToday);

        // Анкеты
        long totalResumes = resumeRepository.count();
        long activeResumes = resumeRepository.countByIsActive(true);
        response.setTotalResumes(totalResumes);
        response.setActiveResumes(activeResumes);

        // Вакансии
        long totalVacancies = vacancyRepository.count();
        long activeVacancies = vacancyRepository.countByIsActive(true);
        response.setTotalVacancies(totalVacancies);
        response.setActiveVacancies(activeVacancies);

        // Подписки
        long activeSubscriptions = subscriptionRepository.countByIsActive(true);
        response.setActiveSubscriptions(activeSubscriptions);

        // Баллы
        Integer totalPointsInSystem = userRepository.findAll().stream()
                .mapToInt(User::getBalance)
                .sum();
        response.setTotalPointsInSystem(totalPointsInSystem);

        // Обратная связь
        long pendingFeedback = feedbackRepository.countByStatus(Feedback.FeedbackStatus.PENDING);
        response.setPendingFeedback(pendingFeedback);

        // Последние вакансии
        List<Vacancy> recentVacancies = vacancyRepository.findAll(
                PageRequest.of(0, 5)
        ).getContent();
        response.setRecentVacancies(recentVacancies);

        return response;
    }

    private long countNewUsersToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(startOfDay))
                .count();
    }
}
