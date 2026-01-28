package job.search.kg.dto.response.user;

import lombok.Data;

@Data
public class ResumeStatsResponse {
    private Long totalCount;
    private Long activeCount;
    private Long inactiveCount;
}