package job.search.kg.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import job.search.kg.entity.Vacancy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VacancyResponse {
    private Long id;
    private String title;
    private String description;
    private String salary;
    private String companyName;
    private String phone;
    private String cityName;
    private String categoryName;
    private String subcategoryName;
    private Integer cityId;
    private Integer sphereId;
    private Integer categoryId;
    private Integer subcategoryId;
    private Boolean isActive;
    private String telegramUsername;
    private LocalDateTime createdAt;
    private String address;
    private String schedule;
    private Integer experienceInYear;
    private Integer minAge;
    private Integer maxAge;
    private Vacancy.GenderPreference preferredGender;
    private List<MediaResponse> media;
    private Double distanceKm;    // 7.5
    private Double latitude;
    private Double longitude;
    private boolean isFree;
    private boolean isBoosted;
}
