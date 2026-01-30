package job.search.kg.controller.user;

import job.search.kg.dto.request.user.SubscriptionPurchaseRequest;
import job.search.kg.dto.response.user.SubscriptionPurchaseResponse;
import job.search.kg.dto.response.user.SubscriptionStatusResponse;
import job.search.kg.service.user.BotPointsService;
import job.search.kg.service.user.BotSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/subscriptions")
@RequiredArgsConstructor
public class BotSubscriptionController {

    private final BotSubscriptionService botSubscriptionService;
    private final BotPointsService pointsService;

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

    @PostMapping("/purchase-with-points")
    public ResponseEntity<SubscriptionPurchaseResponse> purchaseWithPoints(
            @RequestBody SubscriptionPurchaseRequest request) {

        pointsService.purchaseSubscriptionWithPoints(
                request.getTelegramId(),
                request.getPlanType()
        );

        return ResponseEntity.ok(new SubscriptionPurchaseResponse(
                true,
                "Подписка успешно активирована",
                request.getPlanType()
        ));
    }
}

