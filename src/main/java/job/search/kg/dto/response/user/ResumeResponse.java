package job.search.kg.dto.response.user;

import job.search.kg.entity.Resume;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeResponse {
    private Long id;
    private String name;
    private Integer age;
    private Resume.Gender gender;
    private String cityName;
    private String categoryName;
    private String subcategoryName;
    private Integer experience;
    private String description;
    private String telegramUsername;
    private Boolean isActive;
    private LocalDateTime createdAt;
}