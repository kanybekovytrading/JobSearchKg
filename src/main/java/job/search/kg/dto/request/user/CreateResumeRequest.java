package job.search.kg.dto.request.user;

import job.search.kg.entity.Resume;
import lombok.Data;

@Data
public class CreateResumeRequest {
    private String name;
    private Integer age;
    private Resume.Gender gender;
    private Integer cityId;
    private Integer categoryId;
    private Integer subcategoryId;
    private Integer experience;
    private String description;
    private Boolean isActive = true;
}
