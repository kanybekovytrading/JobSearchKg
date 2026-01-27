package job.search.kg.controller.user;

import job.search.kg.dto.response.user.ReferralInfoResponse;
import job.search.kg.service.user.BotReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/referrals")
@RequiredArgsConstructor
public class BotReferralController {

    private final BotReferralService botReferralService;

    @GetMapping("/{telegramId}/info")
    public ResponseEntity<ReferralInfoResponse> getReferralInfo(@PathVariable Long telegramId) {
        ReferralInfoResponse info = botReferralService.getReferralInfo(telegramId);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processReferral(
            @RequestParam Long referrerId,
            @RequestParam Long newUserId) {
        botReferralService.processReferral(referrerId, newUserId);
        return ResponseEntity.ok().build();
    }
}
