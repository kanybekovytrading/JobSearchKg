package job.search.kg.dto.request.user;

import job.search.kg.entity.User;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private User.Language language;
    private String referralCode; // Если регистрация по реферальной ссылке
}