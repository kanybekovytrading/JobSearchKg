package job.search.kg.dto.response.admin;

import job.search.kg.entity.Vacancy;
import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {
    private Long totalUsers;
    private Long newUsersToday;
    private Long totalResumes;
    private Long activeResumes;
    private Long totalVacancies;
    private Long activeVacancies;
    private Long activeSubscriptions;
    private Integer totalPointsInSystem;
    private Long pendingFeedback;
    private Long pendingWithdrawals;
    private List<Vacancy> recentVacancies;
}
