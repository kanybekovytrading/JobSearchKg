package job.search.kg.dto.response.user;


import lombok.Data;

@Data
public class AccessCheckResponse {
    private Boolean hasActiveSubscription;
    private Boolean canSearchJobs;
    private Boolean canSearchEmployees;
    private final Integer pointForSearchAccess = 1500;
}
