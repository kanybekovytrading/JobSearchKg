package job.search.kg.service.user;

import job.search.kg.dto.request.user.SearchRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatisticsResponse;
import job.search.kg.dto.response.user.SearchResultResponse;
import job.search.kg.dto.response.user.VacancyStatisticsResponse;
import job.search.kg.entity.Resume;
import job.search.kg.entity.ResumeStatistics;
import job.search.kg.entity.Vacancy;
import job.search.kg.entity.VacancyStatistics;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotSearchService {

    private final ResumeRepository resumeRepository;
    private final VacancyRepository vacancyRepository;
    private final BotAccessService accessService;
    private final VacancyBoostRepository vacancyBoostRepository;
    private final ResumeBoostRepository resumeBoostRepository;
    private final VacancyStatisticsRepository vacancyStatisticsRepository;
    private final ResumeStatisticsRepository resumeStatisticsRepository;

    @Transactional(readOnly = true)
    public SearchResultResponse<ResumeResponse> searchResumes(Long telegramId, SearchRequest request) {

        Specification<Resume> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("isActive"), true));

            if (request.getCityId() != null) {
                predicates.add(cb.equal(root.get("city").get("id"), request.getCityId()));
            }

            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }

            if (request.getSubcategoryId() != null) {
                predicates.add(cb.equal(root.get("subcategory").get("id"), request.getSubcategoryId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Resume> resumes = resumeRepository.findAll(spec);

        // Сортировка: сначала с активным Boost, потом по дате
        resumes = sortResumesByBoost(resumes);

        List<ResumeResponse> responses;

        // Проверка доступа
        if (!accessService.canSearchEmployees(telegramId)) {
            responses = resumes.stream()
                    .map(this::mapResumeToResponseWithoutSubs)
                    .collect(Collectors.toList());
        } else {
            responses = resumes.stream()
                    .map(this::mapResumeToResponse)
                    .collect(Collectors.toList());
        }

        SearchResultResponse<ResumeResponse> result = new SearchResultResponse<>();
        result.setResults(responses);
        result.setTotal(responses.size());

        return result;
    }

    @Transactional(readOnly = true)
    public SearchResultResponse<VacancyResponse> searchVacancies(Long telegramId, SearchRequest request) {

        Specification<Vacancy> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("isActive"), true));

            if (request.getCityId() != null) {
                predicates.add(cb.equal(root.get("city").get("id"), request.getCityId()));
            }
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }

            if (request.getSubcategoryId() != null) {
                predicates.add(cb.equal(root.get("subcategory").get("id"), request.getSubcategoryId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Vacancy> vacancies = vacancyRepository.findAll(spec);

        // Сортировка: сначала с активным Boost, потом по дате
        vacancies = sortVacanciesByBoost(vacancies);

        List<VacancyResponse> responses;
        if (!accessService.canSearchJobs(telegramId)) {
            responses = vacancies.stream()
                    .map(this::mapVacancyToResponseWithoutSubs)
                    .collect(Collectors.toList());
        } else {
            responses = vacancies.stream()
                    .map(this::mapVacancyToResponse)
                    .collect(Collectors.toList());
        }

        SearchResultResponse<VacancyResponse> result = new SearchResultResponse<>();
        result.setResults(responses);
        result.setTotal(responses.size());

        return result;
    }

    /**
     * Просмотр вакансии (увеличивает счетчик)
     */
    @Transactional
    public void trackVacancyView(Long vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseGet(() -> {
                    VacancyStatistics newStats = VacancyStatistics.builder()
                            .vacancy(vacancy)
                            .viewsCount(0L)
                            .contactClicksCount(0L)
                            .responseCount(0L)
                            .build();
                    return vacancyStatisticsRepository.save(newStats);
                });

        stats.incrementViews();
        vacancyStatisticsRepository.save(stats);
    }

    /**
     * Клик по контактам вакансии
     */
    @Transactional
    public void trackVacancyContactClick(Long vacancyId) {
        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy statistics not found"));

        stats.incrementContactClicks();
        vacancyStatisticsRepository.save(stats);
    }

    /**
     * Просмотр резюме
     */
    @Transactional
    public void trackResumeView(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseGet(() -> {
                    ResumeStatistics newStats = ResumeStatistics.builder()
                            .resume(resume)
                            .viewsCount(0L)
                            .contactClicksCount(0L)
                            .invitationCount(0L)
                            .build();
                    return resumeStatisticsRepository.save(newStats);
                });

        stats.incrementViews();
        resumeStatisticsRepository.save(stats);
    }

    /**
     * Клик по контактам резюме
     */
    @Transactional
    public void trackResumeContactClick(Long resumeId) {
        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume statistics not found"));

        stats.incrementContactClicks();
        resumeStatisticsRepository.save(stats);
    }

    /**
     * Сортировка вакансий: Boost вверх, потом по дате
     */
    private List<Vacancy> sortVacanciesByBoost(List<Vacancy> vacancies) {
        LocalDateTime now = LocalDateTime.now();

        return vacancies.stream()
                .sorted((v1, v2) -> {
                    boolean v1HasBoost = vacancyBoostRepository
                            .existsByVacancyIdAndIsActiveTrueAndExpiresAtAfter(v1.getId(), now);
                    boolean v2HasBoost = vacancyBoostRepository
                            .existsByVacancyIdAndIsActiveTrueAndExpiresAtAfter(v2.getId(), now);

                    if (v1HasBoost && !v2HasBoost) return -1;
                    if (!v1HasBoost && v2HasBoost) return 1;

                    // Если оба с бустом или оба без, сортируем по дате
                    return v2.getCreatedAt().compareTo(v1.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    /**
     * Сортировка резюме: Boost вверх, потом по дате
     */
    private List<Resume> sortResumesByBoost(List<Resume> resumes) {
        LocalDateTime now = LocalDateTime.now();

        return resumes.stream()
                .sorted((r1, r2) -> {
                    boolean r1HasBoost = resumeBoostRepository
                            .existsByResumeIdAndIsActiveTrueAndExpiresAtAfter(r1.getId(), now);
                    boolean r2HasBoost = resumeBoostRepository
                            .existsByResumeIdAndIsActiveTrueAndExpiresAtAfter(r2.getId(), now);

                    if (r1HasBoost && !r2HasBoost) return -1;
                    if (!r1HasBoost && r2HasBoost) return 1;

                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    private ResumeResponse mapResumeToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setAge(resume.getAge());
        response.setGender(resume.getGender());
        response.setCityName(resume.getCity().getNameRu());
        response.setCategoryName(resume.getCategory().getNameRu());
        response.setSubcategoryName(resume.getSubcategory().getNameRu());
        response.setExperience(resume.getExperience());
        response.setDescription(resume.getDescription());
        response.setTelegramUsername(resume.getUser().getUsername());
        response.setPhone(resume.getUser().getPhone());

        return response;
    }

    private ResumeResponse mapResumeToResponseWithoutSubs(Resume resume) {

        ResumeResponse response = new ResumeResponse();

        if (resume.getUser().getPhone() != null && !resume.getUser().getPhone().isEmpty()) {
            String phone = resume.getUser().getPhone();
            String maskedPhone = phone.substring(0, 6) + " *** ***";
            response.setPhone(maskedPhone);
        }

        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setAge(resume.getAge());
        response.setGender(resume.getGender());
        response.setCityName(resume.getCity().getNameRu());
        response.setCategoryName(resume.getCategory().getNameRu());
        response.setSubcategoryName(resume.getSubcategory().getNameRu());
        response.setExperience(resume.getExperience());
        response.setDescription(resume.getDescription());


        return response;
    }

    private VacancyResponse mapVacancyToResponse(Vacancy vacancy) {
        VacancyResponse response = new VacancyResponse();
        response.setPhone(vacancy.getPhone());
        return getVacancyResponse(vacancy, response);
    }

    private VacancyResponse mapVacancyToResponseWithoutSubs(Vacancy vacancy) {
        VacancyResponse response = new VacancyResponse();

        if (vacancy.getUser().getPhone() != null && !vacancy.getUser().getPhone().isEmpty()) {
            String phone = vacancy.getPhone();
            String maskedPhone = phone.substring(0, 6) + " *** ***";
            response.setPhone(maskedPhone);
        }
        return getVacancyResponse(vacancy, response);
    }

    @NonNull
    private VacancyResponse getVacancyResponse(Vacancy vacancy, VacancyResponse response) {
        response.setId(vacancy.getId());
        response.setTitle(vacancy.getTitle());
        response.setDescription(vacancy.getDescription());
        response.setSalary(vacancy.getSalary());
        response.setCompanyName(vacancy.getCompanyName());
        response.setCityName(vacancy.getCity().getNameRu());
        response.setCategoryName(vacancy.getCategory().getNameRu());
        response.setSubcategoryName(vacancy.getSubcategory().getNameRu());
        response.setCreatedAt(vacancy.getCreatedAt());
        response.setTelegramUsername(vacancy.getUser().getUsername());
        response.setExperienceInYear(vacancy.getExperienceInYear());
        response.setAddress(vacancy.getAddress());
        response.setMaxAge(vacancy.getMaxAge());
        response.setMinAge(vacancy.getMinAge());
        response.setPreferredGender(vacancy.getPreferredGender());
        response.setSchedule(vacancy.getSchedule());

        return response;
    }

    /**
     * Получить статистику вакансии
     */
    @Transactional(readOnly = true)
    public VacancyStatisticsResponse getVacancyStatistics(Long vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        VacancyStatistics stats = vacancyStatisticsRepository
                .findByVacancyId(vacancyId)
                .orElseGet(() -> VacancyStatistics.builder()
                        .vacancy(vacancy)
                        .viewsCount(0L)
                        .contactClicksCount(0L)
                        .responseCount(0L)
                        .build());

        return VacancyStatisticsResponse.builder()
                .vacancyId(vacancy.getId())
                .vacancyTitle(vacancy.getTitle())
                .viewsCount(stats.getViewsCount())
                .contactClicksCount(stats.getContactClicksCount())
                .responseCount(stats.getResponseCount())
                .build();
    }

    /**
     * Получить статистику резюме
     */
    @Transactional(readOnly = true)
    public ResumeStatisticsResponse getResumeStatistics(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        ResumeStatistics stats = resumeStatisticsRepository
                .findByResumeId(resumeId)
                .orElseGet(() -> ResumeStatistics.builder()
                        .resume(resume)
                        .viewsCount(0L)
                        .contactClicksCount(0L)
                        .invitationCount(0L)
                        .build());

        return ResumeStatisticsResponse.builder()
                .resumeId(resume.getId())
                .resumeName(resume.getName())
                .viewsCount(stats.getViewsCount())
                .contactClicksCount(stats.getContactClicksCount())
                .invitationCount(stats.getInvitationCount())
                .build();
    }
}
