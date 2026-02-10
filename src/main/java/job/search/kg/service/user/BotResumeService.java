package job.search.kg.service.user;

import job.search.kg.dto.request.user.CreateResumeRequest;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatsResponse;
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
public class BotResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    @Transactional(readOnly = true)
    public ResumeStatsResponse getUserResumeStats(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Resume> resumes = resumeRepository.findByUser(user);

        long totalCount = resumes.size();
        long activeCount = resumes.stream()
                .filter(Resume::getIsActive)
                .count();
        long inactiveCount = totalCount - activeCount;

        ResumeStatsResponse response = new ResumeStatsResponse();
        response.setTotalCount(totalCount);
        response.setActiveCount(activeCount);
        response.setInactiveCount(inactiveCount);

        return response;
    }

    @Transactional
    public Resume createResume(Long telegramId, CreateResumeRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setName(request.getName());
        resume.setAge(request.getAge());
        resume.setGender(request.getGender());
        resume.setCity(city);
        resume.setCategory(category);
        resume.setSubcategory(subcategory);
        resume.setExperience(request.getExperience());
        resume.setDescription(request.getDescription());
        resume.setIsActive(request.getIsActive());

        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getUserResumes(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return resumeRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Resume updateResumeStatus(Long resumeId, Long telegramId, Boolean isActive) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // Проверка владельца
        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        resume.setIsActive(isActive);
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, Long telegramId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        resumeRepository.delete(resume);
    }


    @Transactional
    public Resume updateResume(Long resumeId, Long telegramId, CreateResumeRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getTelegramId().equals(telegramId)) {
            throw new AccessDeniedException("Access denied");
        }

        // Обновляем город если изменился
        if (request.getCityId() != null && !resume.getCity().getId().equals(request.getCityId())) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found"));
            resume.setCity(city);
        }

        // Обновляем категорию если изменилась
        if (request.getCategoryId() != null && !resume.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            resume.setCategory(category);
        }

        // Обновляем подкатегорию если изменилась
        if (request.getSubcategoryId() != null && !resume.getSubcategory().getId().equals(request.getSubcategoryId())) {
            Subcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));
            resume.setSubcategory(subcategory);
        }

        // Обновляем остальные поля
        if (request.getName() != null) {
            resume.setName(request.getName());
        }
        if (request.getAge() != null) {
            resume.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            resume.setGender(request.getGender());
        }
        if (request.getExperience() != null) {
            resume.setExperience(request.getExperience());
        }
        if (request.getDescription() != null) {
            resume.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            resume.setIsActive(request.getIsActive());
        }

        return resumeRepository.save(resume);
    }

    private ResumeResponse mapToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setName(resume.getName());
        response.setAge(resume.getAge());
        response.setGender(resume.getGender());
        response.setCityName(resume.getCity().getNameRu()); // TODO: учитывать язык
        response.setCategoryName(resume.getCategory().getNameRu());
        response.setSubcategoryName(resume.getSubcategory().getNameRu());
        response.setExperience(resume.getExperience());
        response.setDescription(resume.getDescription());
        response.setIsActive(resume.getIsActive());
        response.setCreatedAt(resume.getCreatedAt());

        return response;
    }
}
