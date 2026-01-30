package job.search.kg.dto.response;

import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponseDto {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String city;
    private String category;
    private String subcategory;
    private Integer experience;
    private String description;
    private String phone;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    public static ResumeResponseDto fromEntity(Resume resume, User.Language language) {
        ResumeResponseDto dto = new ResumeResponseDto();
        dto.setId(resume.getId());
        dto.setName(resume.getName());
        dto.setAge(resume.getAge());
        dto.setGender(getGenderText(resume.getGender(), language));
        dto.setCity(getCityName(resume, language));
        dto.setCategory(getCategoryName(resume, language));
        dto.setSubcategory(getSubcategoryName(resume, language));
        dto.setExperience(resume.getExperience());
        dto.setDescription(resume.getDescription());
        dto.setPhone(resume.getUser() != null ? resume.getUser().getPhone() : null);
        dto.setIsActive(resume.getIsActive());
        dto.setCreatedAt(resume.getCreatedAt() != null ? resume.getCreatedAt().toString() : null);
        dto.setUpdatedAt(resume.getUpdatedAt() != null ? resume.getUpdatedAt().toString() : null);
        return dto;
    }

    private static String getGenderText(Resume.Gender gender, User.Language language) {
        if (gender == null) {
            return null;
        }

        return switch (language) {
            case EN -> gender == Resume.Gender.MALE ? "Male 👨" : "Female 👩";
            case KY -> gender == Resume.Gender.MALE ? "Эркек 👨" : "Аял 👩";
            case RU -> gender == Resume.Gender.MALE ? "Мужчина 👨" : "Женщина 👩";
            default -> gender == Resume.Gender.MALE ? "Мужчина 👨" : "Женщина 👩";
        };
    }

    private static String getCityName(Resume resume, User.Language language) {
        if (resume.getCity() == null) {
            return null;
        }

        String cityName = switch (language) {
            case EN -> resume.getCity().getNameEn();
            case RU -> resume.getCity().getNameRu();
            default -> resume.getCity().getNameRu();
        };

        // Добавляем флаг Кыргызстана
        return cityName + " 🇰🇬";
    }

    private static String getCategoryName(Resume resume, User.Language language) {
        if (resume.getCategory() == null) {
            return null;
        }

        return switch (language) {
            case EN -> resume.getCategory().getNameEn();
            case KY -> resume.getCategory().getNameKy();
            case RU -> resume.getCategory().getNameRu();
            default -> resume.getCategory().getNameRu();
        };
    }

    private static String getSubcategoryName(Resume resume, User.Language language) {
        if (resume.getSubcategory() == null) {
            return null;
        }

        return switch (language) {
            case EN -> resume.getSubcategory().getNameEn();
            case KY -> resume.getSubcategory().getNameKy();
            case RU -> resume.getSubcategory().getNameRu();
            default -> resume.getSubcategory().getNameRu();
        };
    }
}
