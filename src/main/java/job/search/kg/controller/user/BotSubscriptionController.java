package job.search.kg.controller.user;

import job.search.kg.dto.response.user.SubscriptionStatusResponse;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/subscriptions")
@RequiredArgsConstructor
public class BotSubscriptionController {

    private final BotSubscriptionService botSubscriptionService;

    @GetMapping("/{telegramId}/status")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(@PathVariable Long telegramId) {
        SubscriptionStatusResponse status = botSubscriptionService.getSubscriptionStatus(telegramId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{telegramId}/check")
    public ResponseEntity<Boolean> hasActiveSubscription(@PathVariable Long telegramId) {
        boolean hasActive = botSubscriptionService.hasActiveSubscription(telegramId);
        return ResponseEntity.ok(hasActive);
    }
}

