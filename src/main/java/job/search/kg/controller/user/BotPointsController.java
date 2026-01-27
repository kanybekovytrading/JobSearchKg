package job.search.kg.controller.user;
import job.search.kg.dto.response.user.BalanceResponse;
import job.search.kg.service.user.BotPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/points")
@RequiredArgsConstructor
public class BotPointsController {

    private final BotPointsService botPointsService;

    @GetMapping("/{telegramId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long telegramId) {
        BalanceResponse balance = botPointsService.getBalance(telegramId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{telegramId}/check")
    public ResponseEntity<Boolean> hasEnoughPoints(
            @PathVariable Long telegramId,
            @RequestParam Integer amount) {
        boolean hasEnough = botPointsService.hasEnoughPoints(telegramId, amount);
        return ResponseEntity.ok(hasEnough);
    }
}
