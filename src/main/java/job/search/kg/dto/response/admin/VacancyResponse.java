package job.search.kg.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponse {
    private Long id;
    private String title;
    private String description;
    private Double salary;
    private String companyName;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private Long userId;
    private String userName;

    // City info
    private Long cityId;
    private String cityName;

    // Category info
    private Long categoryId;
    private String categoryName;

    // Subcategory info
    private Long subcategoryId;
    private String subcategoryName;
}