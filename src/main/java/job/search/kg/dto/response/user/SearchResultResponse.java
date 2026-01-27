package job.search.kg.dto.response.user;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultResponse<T> {
    private List<T> results;
    private Integer total;
}