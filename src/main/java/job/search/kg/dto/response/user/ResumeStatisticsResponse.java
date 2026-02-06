package job.search.kg.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeStatisticsResponse {
    private Long resumeId;
    private String resumeName;
    private Long viewsCount;
    private Long contactClicksCount;
    private Long invitationCount;
}