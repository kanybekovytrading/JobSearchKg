package job.search.kg.service.user;

import job.search.kg.dto.request.user.SearchRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.SearchResultResponse;
import job.search.kg.entity.Resume;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotSearchService {

    private final ResumeRepository resumeRepository;
    private final VacancyRepository vacancyRepository;
    private final BotAccessService accessService;

    @Transactional(readOnly = true)
    public SearchResultResponse<ResumeResponse> searchResumes(Long telegramId, SearchRequest request) {
        // Проверка доступа
        if (!accessService.canSearchEmployees(telegramId)) {
            throw new AccessDeniedException("Access denied. Subscription or points required.");
        }

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

        List<ResumeResponse> responses = resumes.stream()
                .map(this::mapResumeToResponse)
                .collect(Collectors.toList());

        SearchResultResponse<ResumeResponse> result = new SearchResultResponse<>();
        result.setResults(responses);
        result.setTotal(responses.size());

        return result;
    }

    @Transactional(readOnly = true)
    public SearchResultResponse<VacancyResponse> searchVacancies(Long telegramId, SearchRequest request) {
        // Проверка доступа
        if (!accessService.canSearchJobs(telegramId)) {
            throw new AccessDeniedException("Access denied. Subscription or points required.");
        }

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

        List<VacancyResponse> responses = vacancies.stream()
                .map(this::mapVacancyToResponse)
                .collect(Collectors.toList());

        SearchResultResponse<VacancyResponse> result = new SearchResultResponse<>();
        result.setResults(responses);
        result.setTotal(responses.size());

        return result;
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
        // Номер телефона НЕ возвращаем - требуется отдельный запрос с оплатой

        return response;
    }

    private VacancyResponse mapVacancyToResponse(Vacancy vacancy) {
        VacancyResponse response = new VacancyResponse();
        response.setId(vacancy.getId());
        response.setTitle(vacancy.getTitle());
        response.setDescription(vacancy.getDescription());
        response.setSalary(vacancy.getSalary());
        response.setCompanyName(vacancy.getCompanyName());
        response.setPhone(vacancy.getPhone()); // Номер телефона для вакансий бесплатный
        response.setCityName(vacancy.getCity().getNameRu());
        response.setCategoryName(vacancy.getCategory().getNameRu());
        response.setSubcategoryName(vacancy.getSubcategory().getNameRu());
        response.setCreatedAt(vacancy.getCreatedAt());

        return response;
    }
}
