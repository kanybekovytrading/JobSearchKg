package job.search.kg.service;

import job.search.kg.dto.response.CustomResponse;
import job.search.kg.entity.Category;
import job.search.kg.entity.Sphere;
import job.search.kg.entity.User;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.CategoryRepository;
import job.search.kg.repo.SphereRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SphereRepository sphereRepository;

    @Transactional(readOnly = true)
    public List<CustomResponse> getAllSpheres(Long telegramId) {
        User.Language language;
        if(telegramId != null) {
            // Получаем язык пользователя
            User user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

           language = user.getLanguage();

        } else {
            language = User.Language.RU;
        }
        // Получаем все активные сферы
        List<Sphere> spheres = sphereRepository.findByIsActive(true);

        // Преобразуем в CustomResponse с учетом языка
        return spheres.stream()
                .map(sphere -> CustomResponse.builder()
                        .id(sphere.getId())
                        .name(getSNameByLanguage(sphere, language))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomResponse> getAllActiveCategories(Long telegramId, Integer sphereId) {
        User user;
        if(telegramId != null) {
           user = userRepository.findByTelegramId(telegramId).orElseThrow(
                    () -> new ResourceNotFoundException("Пользователь не найден")
            );
        } else {
            user = null;
        }
        List<Category> categories = categoryRepository.findBySphereIdAndIsActive(sphereId, true);

        return categories.stream().map(category -> {
            return CustomResponse.builder()
                    .id(category.getId())
                    .name(getNameByLanguage(category, user != null ? user.getLanguage() : User.Language.RU))
                    .build();
        }).toList();

    }

    @Transactional(readOnly = true)
    public CustomResponse getCategoryById(Integer id, Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId).orElseThrow(
                () -> new ResourceNotFoundException("Пользователь не найден")
        );
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

        return CustomResponse.builder()
                .id(category.getId())
                .name(getNameByLanguage(category, user.getLanguage()))
                .build();
    }

    private String getNameByLanguage(Category category, User.Language language) {
        return switch (language) {
            case RU -> category.getNameRu();
            case KY -> category.getNameKy();
            case EN -> category.getNameEn();
        };
    }

    private String getSNameByLanguage(Sphere sphere, User.Language language) {
        return switch (language) {
            case EN -> sphere.getNameEn();
            case KY -> sphere.getNameKy();
            default -> sphere.getNameRu();
        };
    }
}
