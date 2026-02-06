package job.search.kg.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyStatisticsResponse {
    private Long vacancyId;
    private String vacancyTitle;
    private Long viewsCount;
    private Long contactClicksCount;
    private Long responseCount;
}