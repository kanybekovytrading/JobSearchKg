package job.search.kg.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.request.payment.CreateWithdrawalRequest;
import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.GetServicesResponse;
import job.search.kg.dto.response.payment.WithdrawalInfo;
import job.search.kg.dto.response.payment.WithdrawalResponse;
import job.search.kg.entity.Withdrawal;
import job.search.kg.payment.WithdrawalService;
import job.search.kg.service.user.BotPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/bot/withdrawals")
@Tag(name = "Withdrawals", description = "Эндпоинты для вывода средств")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final BotPointsService pointsService;

    @Operation(
            summary = "Информация о доступном выводе",
            description = "Возвращает баланс баллов пользователя и доступную сумму для вывода"
    )
    @GetMapping("/info/{telegramId}")
    public ResponseEntity<WithdrawalInfo> getWithdrawalInfo(
            @PathVariable Long telegramId
    ) {
        WithdrawalInfo info = pointsService.getWithdrawalInfo(telegramId);
        return ResponseEntity.ok(info);
    }

    @Operation(
            summary = "Создать запрос на вывод баллов",
            description = "Конвертирует баллы в сомы и создает заявку на вывод"
    )
    @PostMapping("/withdraw/{telegramId}")
    public ResponseEntity<String> withdrawPoints(
            @PathVariable Long telegramId,
            @RequestParam Integer points,
            @RequestBody CreateWithdrawalRequest request
    ) throws Exception {

        pointsService.withdrawPointsToMoney(
                telegramId,
                points,
                request.getServiceId(),
                request.getServiceName(),
                request.getRecipientPhone()
        );

        return ResponseEntity.ok("Withdrawal request created successfully");
    }
    /**
     * Проверка получателя перед выводом
     * GET /api/bot/withdrawals/check-recipient?serviceId=averspay&phone=+996XXXXXXXXX&amount=100
     */
    @GetMapping("/check-recipient")
    @Operation(summary = "Проверить получателя",
            description = "Проверяет существование получателя в указанной системе перед выводом")
    public ResponseEntity<CheckRecipientResponse> checkRecipient(
            @RequestParam String serviceId,
            @RequestParam String phone,
            @RequestParam Integer amount
    ) {
        try {
            log.info("Checking recipient: serviceId={}, phone={}, amount={}", serviceId, phone, amount);

            CheckRecipientResponse response = withdrawalService.checkRecipient(serviceId, phone, amount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking recipient", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Создание запроса на вывод средств
     * POST /api/bot/withdrawals/{telegramId}
     */
    @PostMapping("/{telegramId}")
    @Operation(summary = "Создать вывод средств",
            description = "Создает запрос на вывод средств на указанную услугу")
    public ResponseEntity<WithdrawalResponse> createWithdrawal(
            @PathVariable Long telegramId,
            @RequestBody CreateWithdrawalRequest request
    ) {
        try {
            log.info("Creating withdrawal: telegramId={}, serviceId={}, phone={}, amount={}",
                    telegramId, request.getServiceId(), request.getRecipientPhone(), request.getAmount());

            Withdrawal withdrawal = withdrawalService.createWithdrawal(
                    telegramId,
                    request.getServiceId(),
                    request.getServiceName(),
                    request.getRecipientPhone(),
                    request.getAmount(), 1L
            );

            WithdrawalResponse response = mapToResponse(withdrawal);

            log.info("Withdrawal created: id={}, status={}",
                    withdrawal.getId(), withdrawal.getStatus());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid withdrawal request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating withdrawal", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение информации о выводе
     * GET /api/bot/withdrawals/{withdrawalId}
     */
    @GetMapping("/detail/{withdrawalId}")
    @Operation(summary = "Получить информацию о выводе")
    public ResponseEntity<WithdrawalResponse> getWithdrawal(
            @PathVariable Long withdrawalId
    ) {
        try {
            Withdrawal withdrawal = withdrawalService.getWithdrawal(withdrawalId);
            WithdrawalResponse response = mapToResponse(withdrawal);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получение списка доступных услуг для вывода
     * GET /api/bot/withdrawals/services?locale=RU
     */
    @GetMapping("/services")
    @Operation(summary = "Получить список доступных услуг",
            description = "Возвращает список всех доступных услуг для вывода средств")
    public ResponseEntity<GetServicesResponse> getAvailableServices(
            @RequestParam(required = false, defaultValue = "RU") String locale
    ) {
        try {
            log.info("Getting available services, locale={}", locale);

            GetServicesResponse response = withdrawalService.getAvailableServices(locale);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching services", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * История выводов пользователя
     * GET /api/bot/withdrawals/history/{telegramId}
     */
    @GetMapping("/history/{telegramId}")
    @Operation(summary = "История выводов пользователя")
    public ResponseEntity<List<WithdrawalResponse>> getUserWithdrawals(
            @PathVariable Long telegramId
    ) {
        try {
            List<Withdrawal> withdrawals = withdrawalService.getUserWithdrawals(telegramId);

            List<WithdrawalResponse> responses = withdrawals.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error fetching withdrawal history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Маппинг Withdrawal -> WithdrawalResponse
     */
    private WithdrawalResponse mapToResponse(Withdrawal withdrawal) {
        return WithdrawalResponse.builder()
                .id(withdrawal.getId())
                .transactionId(withdrawal.getTransactionId())
                .finikTransactionId(withdrawal.getFinikTransactionId())
                .serviceId(withdrawal.getServiceId())
                .serviceName(withdrawal.getServiceName())
                .recipientPhone(withdrawal.getRecipientPhone())
                .recipientName(withdrawal.getRecipientName())
                .amount(withdrawal.getAmount())
                .status(withdrawal.getStatus())
                .createdAt(withdrawal.getCreatedAt())
                .completedAt(withdrawal.getCompletedAt())
                .errorMessage(withdrawal.getErrorMessage())
                .build();
    }
}
