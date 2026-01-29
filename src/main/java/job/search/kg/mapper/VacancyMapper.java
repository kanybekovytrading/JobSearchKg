package job.search.kg.mapper;

import job.search.kg.dto.response.admin.VacancyResponse;
import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import org.springframework.stereotype.Component;

@Component
public class VacancyMapper {

    public VacancyResponse toResponse(Vacancy vacancy, User.Language language) {
        if (vacancy == null) {
            return null;
        }

        return VacancyResponse.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .description(vacancy.getDescription())
                .salary(vacancy.getSalary() != null ? Double.valueOf(vacancy.getSalary()) : null)
                .companyName(vacancy.getCompanyName())
                .phone(vacancy.getPhone())
                .isActive(vacancy.getIsActive())
                .createdAt(vacancy.getCreatedAt())
                .updatedAt(vacancy.getUpdatedAt())
                // User
                .userId(vacancy.getUser() != null ? vacancy.getUser().getId() : null)
                .userName(vacancy.getUser() != null ? vacancy.getUser().getUsername() : null)
                // City - multilingual
                .cityId(Long.valueOf(vacancy.getCity() != null ? vacancy.getCity().getId() : null))
                .cityName(getCityName(vacancy, language))
                // Category - multilingual
                .categoryId(Long.valueOf(vacancy.getCategory() != null ? vacancy.getCategory().getId() : null))
                .categoryName(getCategoryName(vacancy, language))
                // Subcategory - multilingual
                .subcategoryId(Long.valueOf(vacancy.getSubcategory() != null ? vacancy.getSubcategory().getId() : null))
                .subcategoryName(getSubcategoryName(vacancy, language))
                .build();
    }

    private String getCityName(Vacancy vacancy, User.Language language) {
        if (vacancy.getCity() == null) {
            return null;
        }

        return switch (language) {
            case EN -> vacancy.getCity().getNameEn();
            case RU -> vacancy.getCity().getNameRu();
            default -> vacancy.getCity().getNameRu(); // fallback to Russian
        };
    }

    private String getCategoryName(Vacancy vacancy, User.Language language) {
        if (vacancy.getCategory() == null) {
            return null;
        }

        return switch (language) {
            case EN -> vacancy.getCategory().getNameEn();
            case KY -> vacancy.getCategory().getNameKy();
            case RU -> vacancy.getCategory().getNameRu();
            default -> vacancy.getCategory().getNameRu(); // fallback to Russian
        };
    }

    private String getSubcategoryName(Vacancy vacancy, User.Language language) {
        if (vacancy.getSubcategory() == null) {
            return null;
        }

        return switch (language) {
            case EN -> vacancy.getSubcategory().getNameEn();
            case KY -> vacancy.getSubcategory().getNameKy();
            case RU -> vacancy.getSubcategory().getNameRu();
            default -> vacancy.getSubcategory().getNameRu(); // fallback to Russian
        };
    }
}