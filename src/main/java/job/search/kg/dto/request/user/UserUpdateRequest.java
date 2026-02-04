package job.search.kg.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление данных пользователя")
public class UserUpdateRequest {

    @Schema(description = "Имя пользователя", example = "Иван")
    @Size(min = 1, max = 100, message = "Имя должно быть от 1 до 100 символов")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    private String lastName;

    @Schema(description = "Username в Telegram", example = "ivan_ivanov")
    @Size(max = 100, message = "Username не должен превышать 100 символов")
    private String username;

    @Schema(description = "Номер телефона", example = "+996555123456")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Неверный формат номера телефона")
    private String phone;

    @Schema(description = "Предпочитаемый язык", example = "RU")
    private String language;
}
