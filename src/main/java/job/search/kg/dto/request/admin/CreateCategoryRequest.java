package job.search.kg.dto.request.admin;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String nameRu;
    private String nameKy;
    private String nameEn;
    private String icon; // emoji
    private Boolean isActive;
}
