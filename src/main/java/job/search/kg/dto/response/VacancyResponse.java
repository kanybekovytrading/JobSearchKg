package job.search.kg.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Boolean isActive;
    private String telegramUsername;
    private LocalDateTime createdAt;
}
