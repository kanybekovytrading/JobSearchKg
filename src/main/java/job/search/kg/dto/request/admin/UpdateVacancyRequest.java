package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class UpdateVacancyRequest {
    private String title;
    private String description;
    private String salary;
    private String companyName;
    private String phone;
    private Boolean isActive;
}

