package job.search.kg.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class PageRequestDTO {
    private int page = 0;
    private int size = 10;
    private List<String> sort = new ArrayList<>();
}
