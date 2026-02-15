package job.search.kg.service.user;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.ResumeRepository;
import job.search.kg.repo.UserRepository;
import job.search.kg.repo.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final VacancyRepository vacancyRepository;
    private final UserRepository telegramUserRepository;
    private final ResumeRepository resumeRepository;
    private final BotSearchService botSearchService;
    private final BotAccessService accessService;

    @Transactional(readOnly = true)
    public List<VacancyResponse> getRecommendedVacancies(Long telegramId, int limit) {
        User user = telegramUserRepository.findByTelegramId(telegramId)
                .orElse(null);

        // Проверяем подписку
        boolean hasSubscription = accessService.canSearchJobs(telegramId);

        if (user == null) {
            // Пользователь не найден - общие рекомендации
            List<Vacancy> vacancies = vacancyRepository.findRecommendedVacancies(null, null, limit);
            return vacancies.stream()
                    .limit(limit)
                    .map(vacancy -> hasSubscription
                            ? botSearchService.mapVacancyToResponse(vacancy)
                            : botSearchService.mapVacancyToResponseWithoutSubs(vacancy))
                    .collect(Collectors.toList());
        }

        // Ищем резюме пользователя
        List<Resume> userResumes = resumeRepository.findByUserAndIsActiveTrue(user);

        if (userResumes.isEmpty()) {
            // Нет резюме - общие рекомендации без города и подкатегорий
            List<Vacancy> vacancies = vacancyRepository.findRecommendedVacancies(null, null, limit);
            return vacancies.stream()
                    .limit(limit)
                    .map(vacancy -> hasSubscription
                            ? botSearchService.mapVacancyToResponse(vacancy)
                            : botSearchService.mapVacancyToResponseWithoutSubs(vacancy))
                    .collect(Collectors.toList());
        }

        // Берем город из первого резюме
        Long userCityId = userResumes.stream()
                .filter(resume -> resume.getCity() != null)
                .map(resume -> resume.getCity().getId().longValue())
                .findFirst()
                .orElse(null);

        // Извлекаем подкатегории из всех активных резюме
        List<Long> subcategoryIds = userResumes.stream()
                .filter(resume -> resume.getSubcategory() != null)
                .map(resume -> resume.getSubcategory().getId().longValue())
                .distinct()
                .collect(Collectors.toList());

        List<Vacancy> vacancies;
        if (subcategoryIds.isEmpty()) {
            // Есть резюме, но без подкатегорий - рекомендации только по городу
            vacancies = vacancyRepository.findRecommendedVacancies(userCityId, null, limit);
        } else {
            // Персонализированные рекомендации по городу и подкатегориям резюме
            vacancies = vacancyRepository.findRecommendedVacancies(userCityId, subcategoryIds, limit);
        }

        // Маппинг с учетом подписки
        return vacancies.stream()
                .limit(limit)
                .map(vacancy -> hasSubscription
                        ? botSearchService.mapVacancyToResponse(vacancy)
                        : botSearchService.mapVacancyToResponseWithoutSubs(vacancy))
                .collect(Collectors.toList());
    }
}
