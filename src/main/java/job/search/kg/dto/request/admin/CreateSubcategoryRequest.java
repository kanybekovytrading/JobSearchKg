package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class CreateSubcategoryRequest {
    private Integer categoryId;
    private String nameRu;
    private String nameKy;
    private String nameEn;
    private Boolean isActive;
}
