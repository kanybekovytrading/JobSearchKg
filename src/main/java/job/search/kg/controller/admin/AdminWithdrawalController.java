package job.search.kg.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.response.admin.WithdrawalAnalyticsResponse;
import job.search.kg.dto.response.admin.WithdrawalListItemResponse;
import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.GetServicesResponse;
import job.search.kg.entity.Withdrawal;
import job.search.kg.payment.BankConfig;
import job.search.kg.payment.WithdrawalService;
import job.search.kg.service.user.BotPointsService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/withdrawals")
@Tag(name = "Admin Withdrawals Controller", description = "Эндпоинты для вывода средств")
@RequiredArgsConstructor
public class AdminWithdrawalController {

    private final WithdrawalService withdrawalService;
    private final BotPointsService pointsService;

    @GetMapping("/analytics")
    @Operation(summary = "Аналитика выплат")
    public ResponseEntity<WithdrawalAnalyticsResponse> getAnalytics() {
        log.info("Get withdrawals analytics request");

        WithdrawalAnalyticsResponse analytics = withdrawalService.getWithdrawalAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/list")
    @Operation(summary = "Список всех выплат с пагинацией")
    public ResponseEntity<Page<WithdrawalListItemResponse>> getWithdrawalsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("Get withdrawals list: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<WithdrawalListItemResponse> withdrawals = withdrawalService.getWithdrawalsList(pageable);
        return ResponseEntity.ok(withdrawals);
    }

    @GetMapping("/list/status/{status}")
    @Operation(summary = "Список выплат по статусу")
    public ResponseEntity<Page<WithdrawalListItemResponse>> getWithdrawalsByStatus(
            @PathVariable Withdrawal.WithdrawalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        log.info("Get withdrawals by status: status={}, page={}, size={}",
                status, page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<WithdrawalListItemResponse> withdrawals =
                withdrawalService.getWithdrawalsByStatus(status, pageable);
        return ResponseEntity.ok(withdrawals);
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
     * ✅ ПОЛУЧИТЬ СПИСОК ДОСТУПНЫХ БАНКОВ
     */
    @Operation(
            summary = "Получить список банков",
            description = "Возвращает список всех доступных банков для вывода средств с их лимитами"
    )
    @GetMapping("/banks")
    public ResponseEntity<List<BankInfo>> getAvailableBanks() {
        List<BankInfo> banks = Arrays.stream(BankConfig.values())
                .map(bank -> new BankInfo(
                        bank.getServiceId(),
                        bank.getName(),
                        bank.getMinAmount(),
                        bank.getMaxAmount()
                ))
                .toList();

        return ResponseEntity.ok(banks);
    }

    /**
     * ✅ ПРОВЕРКА ПОЛУЧАТЕЛЯ
     */
    @Operation(
            summary = "Проверить получателя",
            description = "Проверяет, что получатель существует в выбранном банке"
    )
    @ApiResponse(responseCode = "200", description = "Получатель найден")
    @PostMapping("/check-recipient")
    public ResponseEntity<CheckRecipientResponse> checkRecipient(
            @RequestParam String serviceId,

            @RequestParam String phone,

            @RequestParam Integer amount
    ) throws Exception {

        CheckRecipientResponse response = withdrawalService.checkRecipient(serviceId, phone, amount);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ СОЗДАНИЕ ВЫВОДА
     */
    @Operation(
            summary = "Создать вывод в банк",
            description = "Создает вывод средств в выбранный банк по номеру телефона. " +
                    "Используйте GET /api/withdrawal/banks для получения списка доступных банков."
    )
    @ApiResponse(responseCode = "200", description = "Вывод успешно создан")
    @PostMapping("/create/{telegramId}")
    public ResponseEntity<WithdrawalResponse> createWithdrawal(
            @PathVariable Long telegramId,
            @RequestBody CreateWithdrawalRequest request
    ) throws Exception {

        log.info("Creating withdrawal: telegramId={}, bank={}, phone={}, amount={}",
                telegramId, request.getServiceId(),
                request.getRecipientPhone(), request.getAmount());

        Withdrawal withdrawal = withdrawalService.createWithdrawal(
                telegramId,
                request.getServiceId(),  // ID банка
                request.getRecipientPhone(),
                request.getAmount(),
                request.getComment(),
                1L
        );

        return ResponseEntity.ok(mapToResponse(withdrawal));
    }

    /**
     * История выводов
     */
    @Operation(summary = "История выводов пользователя")
    @GetMapping("/history")
    public ResponseEntity<List<WithdrawalResponse>> getHistory(
            @RequestParam Long telegramId
    ) {
        List<Withdrawal> withdrawals = withdrawalService.getUserWithdrawals(telegramId);
        List<WithdrawalResponse> response = withdrawals.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Детали вывода
     */
    @Operation(summary = "Детали вывода")
    @GetMapping("/{withdrawalId}")
    public ResponseEntity<WithdrawalResponse> getWithdrawal(
            @PathVariable Long withdrawalId
    ) {
        Withdrawal withdrawal = withdrawalService.getWithdrawal(withdrawalId);
        return ResponseEntity.ok(mapToResponse(withdrawal));
    }

    // DTO классы

    /**
     * Маппинг Withdrawal -> WithdrawalResponse
     */
    private WithdrawalResponse mapToResponse(Withdrawal withdrawal) {
        return WithdrawalResponse.builder()
                .id(withdrawal.getId())
                .transactionId(withdrawal.getTransactionId())
                .recipientPhone(withdrawal.getRecipientPhone())
                .recipientName(withdrawal.getRecipientName())
                .amount(withdrawal.getAmount())
                .status(String.valueOf(withdrawal.getStatus()))
                .createdAt(withdrawal.getCreatedAt())
                .completedAt(withdrawal.getCompletedAt())
                .errorMessage(withdrawal.getErrorMessage())
                .build();
    }

    @Data
    public static class BankInfo {
        private String serviceId;
        private String name;
        private int minAmount;
        private int maxAmount;

        public BankInfo(String serviceId, String name, int minAmount, int maxAmount) {
            this.serviceId = serviceId;
            this.name = name;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    @Data
    public static class CreateWithdrawalRequest {
        private String serviceId;          // ✅ ID банка (обязательно!)
        private String recipientPhone;     // Номер телефона
        private BigDecimal amount;         // Сумма
        private String comment;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WithdrawalResponse {
        private Long id;
        private String transactionId;
        private String bankName;
        private String recipientPhone;
        private String recipientName;
        private BigDecimal amount;
        private String status;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime completedAt;
        private String errorMessage;
    }
}
