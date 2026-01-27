package job.search.kg.dto.response.user;

import lombok.Data;

@Data
public class ReferralInfoResponse {
    private String referralCode;
    private String referralLink;
    private Integer referralsCount;
    private Integer rewardPerReferral;
}