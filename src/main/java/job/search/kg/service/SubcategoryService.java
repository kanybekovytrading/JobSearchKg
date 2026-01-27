package job.search.kg.service;

import job.search.kg.dto.response.CustomResponse;
import job.search.kg.entity.Subcategory;
import job.search.kg.entity.User;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.SubcategoryRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static job.search.kg.entity.User.Language.EN;

@Service
@RequiredArgsConstructor
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public List<CustomResponse> getSubcategoriesByCategory(Integer categoryId, Long telegramId) {
        User user =  userRepository.findByTelegramId(telegramId).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        List<Subcategory> responses = subcategoryRepository.findByCategoryIdAndIsActive(categoryId, true);
        return responses.stream()
                .map(subcategory -> CustomResponse.builder()
                        .id(subcategory.getId())
                        .name(getNameByLanguage(subcategory, user.getLanguage()))
                        .build())
                .toList();

    }

    @Transactional(readOnly = true)
    public CustomResponse getSubcategoryById(Integer id, Long telegramId) {
        User user =  userRepository.findByTelegramId(telegramId).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));

        return CustomResponse.builder()
                .id(subcategory.getId())
                .name(user.getLanguage().equals(EN) ? subcategory.getNameEn(): subcategory.getNameRu())
                .build();
    }

    private String getNameByLanguage(Subcategory subcategory, User.Language language) {
        return switch (language) {
            case RU -> subcategory.getNameRu();
            case KY -> subcategory.getNameKy();
            case EN -> subcategory.getNameEn();
        };
    }
}
