package job.search.kg.dto.response.user;

import job.search.kg.dto.response.MediaResponse;
import job.search.kg.entity.Resume;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private String phone;
    private List<MediaResponse> media;
    private boolean isFree;
    private boolean isBoosted;
    private Integer cityId;
    private Integer sphereId;
    private Integer categoryId;
    private Integer subcategoryId;
    private String profilePhoto;
}