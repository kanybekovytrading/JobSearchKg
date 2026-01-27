package job.search.kg.dto.response;

import job.search.kg.entity.Subcategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubcategoryResponse {
    private Integer id;
    private String nameRu;
    private String nameKy;
    private String nameEn;
    private Boolean isActive;

    public SubcategoryResponse(Subcategory subcategory) {
        this.id = subcategory.getId();
        this.nameRu = subcategory.getNameRu();
        this.nameKy = subcategory.getNameKy();
        this.nameEn = subcategory.getNameEn();
        this.isActive = subcategory.getIsActive();
    }
}
