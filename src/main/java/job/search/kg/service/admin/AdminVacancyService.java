package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.CreateVacancyAdminRequest;
import job.search.kg.dto.request.admin.UpdateVacancyRequest;
import job.search.kg.entity.*;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminVacancyService {
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    @Transactional(readOnly = true)
    public Page<Vacancy> getAllVacancies(Pageable pageable) {
        return vacancyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Vacancy getVacancyById(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));
    }

    @Transactional
    public Vacancy createVacancy(CreateVacancyAdminRequest request, Long adminUserId) {
        // Админ создаёт вакансию от имени системного пользователя или конкретного
        User user = userRepository.findById(adminUserId)
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
        vacancy.setPhone(request.getPhone());
        vacancy.setCity(city);
        vacancy.setCategory(category);
        vacancy.setSubcategory(subcategory);
        vacancy.setIsActive(true);

        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public Vacancy updateVacancy(Long id, UpdateVacancyRequest request) {
        Vacancy vacancy = getVacancyById(id);

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
        if (request.getIsActive() != null) {
            vacancy.setIsActive(request.getIsActive());
        }

        return vacancyRepository.save(vacancy);
    }

    @Transactional
    public void deleteVacancy(Long id) {
        Vacancy vacancy = getVacancyById(id);
        vacancyRepository.delete(vacancy);
    }

    @Transactional(readOnly = true)
    public Long countActiveVacancies() {
        return vacancyRepository.countByIsActive(true);
    }
}