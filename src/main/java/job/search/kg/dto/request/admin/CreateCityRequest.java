package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class CreateCityRequest {
    private String nameRu;
    private String nameEn;
    private Boolean isActive;
}