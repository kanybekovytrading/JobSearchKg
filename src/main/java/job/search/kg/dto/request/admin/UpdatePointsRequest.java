package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class UpdatePointsRequest {
    private Integer newBalance;
    private String reason; // Опционально
}
