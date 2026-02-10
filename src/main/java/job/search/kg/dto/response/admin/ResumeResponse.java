package job.search.kg.dto.response.admin;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeResponse {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private SimpleResponse city;
    private SimpleResponse category;
    private SimpleResponse subcategory;
    private Integer experience;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
