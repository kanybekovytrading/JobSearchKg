package job.search.kg.dto.response.admin;

 import job.search.kg.entity.Admin;
 import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long adminId;
    private String email;
    private String name;
    private Admin.AdminRole role;
}