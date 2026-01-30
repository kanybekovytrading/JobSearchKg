package job.search.kg.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;          // из Resume или Vacancy
    private String category;      // из Resume или Vacancy
    private String subcategory;   // из Resume или Vacancy
    private String role;
    private LocalDateTime registrationDate;
}
