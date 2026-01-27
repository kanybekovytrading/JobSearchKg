package job.search.kg.service.user;

import job.search.kg.dto.request.user.CreateResumeRequest;
import job.search.kg.dto.response.user.ResumeResponse;
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
