package job.search.kg.dto.response.user;

import lombok.Data;

@Data
public class VacancyStatsResponse {
    private Long totalCount;
    private Long activeCount;
    private Long inactiveCount;
}