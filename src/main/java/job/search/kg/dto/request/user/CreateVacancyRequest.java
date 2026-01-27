package job.search.kg.dto.request.user;

import lombok.Data;

@Data
public class CreateVacancyRequest {
    private String title;
    private String description;
    private String salary;
    private String companyName;
    private String phone; // Опционально, если не указан - берётся из User
    private Integer cityId;
    private Integer categoryId;
    private Integer subcategoryId;
}
