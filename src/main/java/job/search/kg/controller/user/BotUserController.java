package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.request.user.UserRegistrationRequest;
import job.search.kg.dto.response.user.UserProfileResponse;
import job.search.kg.entity.User;
import job.search.kg.service.user.BotUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "API для управления пользователями бота")
@RestController
@RequestMapping("/api/bot/users")
@RequiredArgsConstructor
public class BotUserController {

    private final BotUserService botUserService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя в системе на основе данных из Telegram"
    )
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationRequest request) {
        User user = botUserService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(
            summary = "Получить пользователя по Telegram ID",
            description = "Возвращает информацию о пользователе по его Telegram ID"
    )
    @GetMapping("/{telegramId}")
    public ResponseEntity<User> getUserByTelegramId(@PathVariable Long telegramId) {
        User user = botUserService.getUserByTelegramId(telegramId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Получить профиль пользователя",
            description = "Возвращает детальную информацию профиля пользователя"
    )
    @GetMapping("/{telegramId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long telegramId) {
        UserProfileResponse profile = botUserService.getUserProfile(telegramId);
        return ResponseEntity.ok(profile);
    }

    @Operation(
            summary = "Обновить язык пользователя",
            description = "Изменяет предпочитаемый язык интерфейса для пользователя"
    )
    @PutMapping("/{telegramId}/language")
    public ResponseEntity<Void> updateLanguage(
            @PathVariable Long telegramId,
            @RequestParam String language) {
        botUserService.updateLanguage(telegramId, User.Language.valueOf(language.toUpperCase()));
        return ResponseEntity.ok().build();
    }
}
