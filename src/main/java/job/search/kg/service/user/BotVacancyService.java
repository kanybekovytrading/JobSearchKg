package job.search.kg.service.user;

import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotVacancyService {

    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    @Transactional(readOnly = true)
    public VacancyStatsResponse getUserVacancyStats(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);

        long totalCount = vacancies.size();
        long activeCount = vacancies.stream()
                .filter(Vacancy::getIsActive)
                .count();
        long inactiveCount = totalCount - activeCount;

        VacancyStatsResponse response = new VacancyStatsResponse();
        response.setTotalCount(totalCount);
        response.setActiveCount(activeCount);
        response.setInactiveCount(inactiveCount);

        return response;
    }

    @Transactional
    public Vacancy updateVacancyStatus(Long vacancyId, Long telegramId, Boolean isActive) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        vacancy.setIsActive(isActive);
        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public Vacancy createVacancy(Long telegramId, CreateVacancyRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));

        Vacancy vacancy = new Vacancy();
        vacancy.setUser(user);
        vacancy.setTitle(request.getTitle());
        vacancy.setDescription(request.getDescription());
        vacancy.setSalary(request.getSalary());
        vacancy.setCompanyName(request.getCompanyName());
        vacancy.setPhone(request.getPhone() != null ? request.getPhone() : user.getPhone());
        vacancy.setCity(city);
        vacancy.setCategory(category);
        vacancy.setSubcategory(subcategory);
        vacancy.setAddress(request.getAddress());
        vacancy.setPreferredGender(request.getPreferredGender());
        vacancy.setMinAge(request.getMinAge());
        vacancy.setMaxAge(request.getMaxAge());
        vacancy.setSchedule(request.getSchedule());
        vacancy.setExperienceInYear(request.getExperienceInYear());
        vacancy.setIsActive(true);

        return vacancyRepository.save(vacancy);
    }

    @Transactional(readOnly = true)
    public List<VacancyResponse> getUserVacancies(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return vacancyRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVacancy(Long vacancyId, Long telegramId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        vacancyRepository.delete(vacancy);
    }

    @Transactional
    public Vacancy updateVacancy(Long vacancyId, Long telegramId, CreateVacancyRequest request) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        if (!vacancy.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        // Обновляем город если изменился
        if (request.getCityId() != null && !vacancy.getCity().getId().equals(request.getCityId())) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found"));
            vacancy.setCity(city);
        }

        // Обновляем категорию если изменилась
        if (request.getCategoryId() != null && !vacancy.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            vacancy.setCategory(category);
        }

        // Обновляем подкатегорию если изменилась
        if (request.getSubcategoryId() != null && !vacancy.getSubcategory().getId().equals(request.getSubcategoryId())) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));
            vacancy.setSubcategory(subcategory);
        }

        // Обновляем остальные поля
        if (request.getTitle() != null) {
            vacancy.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            vacancy.setDescription(request.getDescription());
        }
        if (request.getSalary() != null) {
            vacancy.setSalary(request.getSalary());
        }
        if (request.getCompanyName() != null) {
            vacancy.setCompanyName(request.getCompanyName());
        }
        if (request.getPhone() != null) {
            vacancy.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            vacancy.setAddress(request.getAddress());
        }
        if (request.getPreferredGender() != null) {
            vacancy.setPreferredGender(request.getPreferredGender());
        }
        if (request.getMinAge() != null) {
            vacancy.setMinAge(request.getMinAge());
        }
        if (request.getMaxAge() != null) {
            vacancy.setMaxAge(request.getMaxAge());
        }
        if (request.getSchedule() != null) {
            vacancy.setSchedule(request.getSchedule());
        }
        if (request.getExperienceInYear() != null) {
            vacancy.setExperienceInYear(request.getExperienceInYear());
        }

        return vacancyRepository.save(vacancy);
    }

    private VacancyResponse mapToResponse(Vacancy vacancy) {
        VacancyResponse response = new VacancyResponse();
        response.setId(vacancy.getId());
        response.setTitle(vacancy.getTitle());
        response.setDescription(vacancy.getDescription());
        response.setSalary(vacancy.getSalary());
        response.setCompanyName(vacancy.getCompanyName());
        response.setPhone(vacancy.getPhone());
        response.setCityName(vacancy.getCity().getNameRu());
        response.setCategoryName(vacancy.getCategory().getNameRu());
        response.setSubcategoryName(vacancy.getSubcategory().getNameRu());
        response.setIsActive(vacancy.getIsActive());
        response.setCreatedAt(vacancy.getCreatedAt());

        return response;
    }
}