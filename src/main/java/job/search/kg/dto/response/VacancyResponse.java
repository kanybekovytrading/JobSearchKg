package job.search.kg.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
    private Boolean isActive;
    private LocalDateTime createdAt;
}
