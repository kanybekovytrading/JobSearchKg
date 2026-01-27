package job.search.kg.dto.request.user;

import lombok.Data;

@Data
public class SearchRequest {
    private Integer cityId;
    private Integer categoryId;
    private Integer subcategoryId;
}
