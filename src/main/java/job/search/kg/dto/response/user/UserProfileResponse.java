package job.search.kg.dto.response.user;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private Long telegramId;
    private String username;
    private String firstName;
    private String phone;
    private Integer balance;
    private String referralCode;
}
