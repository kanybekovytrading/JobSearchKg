package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
