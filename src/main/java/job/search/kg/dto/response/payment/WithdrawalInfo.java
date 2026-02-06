package job.search.kg.dto.response.payment;

@lombok.Data
@lombok.Builder
public class WithdrawalInfo {
    private Integer currentPoints;
    private Integer availableSoms;
    private Integer minWithdrawalSoms;
    private Integer maxWithdrawalSoms;
    private Boolean canWithdraw;
    private Integer pointsPerSom;
}