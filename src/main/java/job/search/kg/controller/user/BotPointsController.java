package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import job.search.kg.dto.request.payment.PurchaseSubscriptionRequest;
import job.search.kg.dto.request.payment.WithdrawPointsRequest;
import job.search.kg.dto.response.payment.WithdrawalInfo;
import job.search.kg.dto.response.user.BalanceResponse;
import job.search.kg.service.user.BotPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/points")
@RequiredArgsConstructor
@Slf4j
public class BotPointsController {

    private final BotPointsService pointsService;

    @GetMapping("/{telegramId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long telegramId) {
        BalanceResponse balance = pointsService.getBalance(telegramId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{telegramId}/check")
    public ResponseEntity<Boolean> hasEnoughPoints(
            @PathVariable Long telegramId,
            @RequestParam Integer amount) {
        boolean hasEnough = pointsService.hasEnoughPoints(telegramId, amount);
        return ResponseEntity.ok(hasEnough);
    }

    @PostMapping("/subscription/purchase")
    @Operation(summary = "Покупка подписки за баллы")
    public ResponseEntity<String> purchaseSubscriptionWithPoints(
            @RequestHeader("X-Telegram-Id") Long telegramId,
            @Valid @RequestBody PurchaseSubscriptionRequest request
    ) {
        log.info("Purchase subscription request: telegramId={}, planType={}",
                telegramId, request.getPlanType());

        try {
            pointsService.purchaseSubscriptionWithPoints(
                    telegramId,
                    request.getPlanType()
            );

            return ResponseEntity.ok("Подписка успешно активирована");

        } catch (IllegalStateException e) {
            log.warn("User already has active subscription: telegramId={}", telegramId);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("У вас уже есть активная подписка");

        } catch (Exception e) {
            log.error("Error purchasing subscription: telegramId={}", telegramId, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Вывод баллов в деньги")
    public ResponseEntity<String> withdrawPointsToMoney(
            @RequestHeader("X-Telegram-Id") Long telegramId,
            @Valid @RequestBody WithdrawPointsRequest request
    ) {
        log.info("Withdraw points request: telegramId={}, points={}, service={}",
                telegramId, request.getPointsAmount(), request.getServiceId());

        try {
            pointsService.withdrawPointsToMoney(
                    telegramId,
                    request.getPointsAmount(),
                    request.getServiceId(),
                    request.getRecipientPhone()
            );

            return ResponseEntity.ok(
                    "Запрос на вывод создан. Средства поступят в течение нескольких минут."
            );

        } catch (IllegalArgumentException e) {
            log.warn("Invalid withdrawal amount: telegramId={}, error={}",
                    telegramId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (Exception e) {
            log.error("Error withdrawing points: telegramId={}", telegramId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при создании запроса на вывод: " + e.getMessage());
        }
    }

    @GetMapping("/withdrawal/info")
    @Operation(summary = "Информация о доступном выводе баллов")
    public ResponseEntity<WithdrawalInfo> getWithdrawalInfo(
            @RequestHeader("X-Telegram-Id") Long telegramId
    ) {
        log.info("Get withdrawal info request: telegramId={}", telegramId);

        try {
            WithdrawalInfo info = pointsService.getWithdrawalInfo(telegramId);
            return ResponseEntity.ok(info);

        } catch (Exception e) {
            log.error("Error getting withdrawal info: telegramId={}", telegramId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
