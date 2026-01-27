package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class CreateVacancyAdminRequest {
    private String title;
    private String description;
    private String salary;
    private String companyName;
    private String phone;
    private Integer cityId;
    private Integer categoryId;
    private Integer subcategoryId;
}
