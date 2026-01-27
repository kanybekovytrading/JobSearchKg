package job.search.kg.dto.response.admin;

import lombok.Data;

@Data
public class PointsStatsResponse {
    private Integer totalInSystem;
    private Integer totalEarned;
    private Integer totalSpent;
}
