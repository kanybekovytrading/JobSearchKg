package job.search.kg.dto.response.user;


import lombok.Data;

@Data
public class AccessCheckResponse {
    private final Integer pointForSearchAccess = 1500;
    private Boolean hasActiveSubscription;
    private Boolean canSearchJobs;
    private Boolean canSearchEmployees;
}
