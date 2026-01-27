package job.search.kg.controller.user;

import job.search.kg.dto.response.user.AccessCheckResponse;
import job.search.kg.service.user.BotAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/access")
@RequiredArgsConstructor
public class BotAccessController {

    private final BotAccessService botAccessService;

    @GetMapping("/{telegramId}/check")
    public ResponseEntity<AccessCheckResponse> checkAccess(@PathVariable Long telegramId) {
        AccessCheckResponse access = botAccessService.checkAccess(telegramId);
        return ResponseEntity.ok(access);
    }
}
