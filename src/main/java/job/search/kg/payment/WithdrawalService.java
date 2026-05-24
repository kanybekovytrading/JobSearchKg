package job.search.kg.payment;

import job.search.kg.dto.response.admin.WithdrawalAnalyticsResponse;
import job.search.kg.dto.response.admin.WithdrawalListItemResponse;
import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.GetServicesResponse;
import job.search.kg.dto.response.payment.MakePaymentResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import job.search.kg.entity.Withdrawal;
import job.search.kg.repo.PointsTransactionRepository;
import job.search.kg.repo.UserRepository;
import job.search.kg.repo.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalService {

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.01"); // 1%
    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;
    private final BankWithdrawalService bankWithdrawalService;
    private final FinikWConfig finikConfig;
    private final FinikPaymentsGatewayService paymentsGatewayService;
    private final PointsTransactionRepository pointsTransactionRepository;

    /**
     * ✅ АНАЛИТИКА ВЫПЛАТ
     */
    @Transactional(readOnly = true)
    public WithdrawalAnalyticsResponse getWithdrawalAnalytics() {

        // Все выплаты
        List<Withdrawal> allWithdrawals = withdrawalRepository.findAll();

        // Подсчет по статусам
        long total = allWithdrawals.size();
        long successful = allWithdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.SUCCEEDED)
                .count();
        long failed = allWithdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.FAILED)
                .count();
        long pending = allWithdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.PENDING)
                .count();
        long processing = allWithdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.PROCESSING)
                .count();

        // Сумма успешных выплат
        BigDecimal totalAmountPaid = allWithdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.SUCCEEDED)
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Комиссия платформы (1% от успешных выплат)
        BigDecimal platformFee = totalAmountPaid
                .multiply(PLATFORM_FEE_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);

        // Общая сумма с комиссией
        BigDecimal totalWithFee = totalAmountPaid.add(platformFee);

        // Подсчет баллов
        Long totalPointsWithdrawn = allWithdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.SUCCEEDED)
                .filter(w -> w.getPointsTransactionId() != null)
                .map(w -> {
                    PointsTransaction pt = pointsTransactionRepository
                            .findById(w.getPointsTransactionId())
                            .orElse(null);
                    return pt != null ? Math.abs(pt.getAmount()) : 0;
                })
                .mapToLong(Integer::longValue)
                .sum();

        return WithdrawalAnalyticsResponse.builder()
                .totalWithdrawals(total)
                .successfulWithdrawals(successful)
                .failedWithdrawals(failed)
                .pendingWithdrawals(pending)
                .processingWithdrawals(processing)
                .totalAmountPaid(totalAmountPaid)
                .platformFee(platformFee)
                .totalWithFee(totalWithFee)
                .totalPointsWithdrawn(totalPointsWithdrawn)
                .build();
    }

    /**
     * ✅ СПИСОК ВЫПЛАТ С ПАГИНАЦИЕЙ
     */
    @Transactional(readOnly = true)
    public Page<WithdrawalListItemResponse> getWithdrawalsList(Pageable pageable) {

        Page<Withdrawal> withdrawals = withdrawalRepository.findAll(pageable);

        return withdrawals.map(this::mapToListItem);
    }

    /**
     * ✅ СПИСОК ВЫПЛАТ ПО СТАТУСУ
     */
    @Transactional(readOnly = true)
    public Page<WithdrawalListItemResponse> getWithdrawalsByStatus(
            Withdrawal.WithdrawalStatus status,
            Pageable pageable
    ) {
        Page<Withdrawal> withdrawals = withdrawalRepository.findByStatus(status, pageable);

        return withdrawals.map(this::mapToListItem);
    }

    /**
     * Маппинг Withdrawal -> WithdrawalListItemResponse
     */
    private WithdrawalListItemResponse mapToListItem(Withdrawal withdrawal) {

        // Получаем количество баллов
        Integer points = null;
        if (withdrawal.getPointsTransactionId() != null) {
            PointsTransaction pt = pointsTransactionRepository
                    .findById(withdrawal.getPointsTransactionId())
                    .orElse(null);
            if (pt != null) {
                points = Math.abs(pt.getAmount());
            }
        }

        return WithdrawalListItemResponse.builder()
                .id(withdrawal.getId())
                .telegramId(withdrawal.getUser().getTelegramId())
                .recipientPhone(withdrawal.getRecipientPhone())
                .recipientName(withdrawal.getRecipientName())
                .amount(withdrawal.getAmount())
                .points(points)
                .paymentMethod(withdrawal.getServiceName())
                .status(withdrawal.getStatus())
                .createdAt(withdrawal.getCreatedAt())
                .completedAt(withdrawal.getCompletedAt())
                .errorMessage(withdrawal.getErrorMessage())
                .build();
    }

    /**
     * ✅ ПРОВЕРКА ПОЛУЧАТЕЛЯ для конкретного банка
     */
    public CheckRecipientResponse checkRecipient(
            String serviceId,
            String phone,
            Integer amount
    ) throws Exception {

        // Находим конфигурацию банка
        BankConfig bank = BankConfig.findByServiceId(serviceId);
        if (bank == null) {
            throw new IllegalArgumentException("Unknown bank service ID: " + serviceId);
        }

        // Валидация суммы
        if (!bank.isAmountValid(amount)) {
            throw new IllegalArgumentException(
                    String.format("Amount must be between %d and %d for %s",
                            bank.getMinAmount(), bank.getMaxAmount(), bank.getName())
            );
        }

        // Форматируем номер телефона
        String formattedPhone = formatPhoneNumber(phone);

        return bankWithdrawalService.checkRecipientForBank(
                bank.getServiceId(),
                bank.getServiceCode(),
                formattedPhone,
                amount,
                bank.isRequiresTransactionType()  // ✅ Передаем флаг для MBank
        );
    }

    /**
     * ✅ СОЗДАНИЕ ВЫВОДА для конкретного банка
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Withdrawal createWithdrawal(
            Long telegramId,
            String serviceId,
            String recipientPhone,
            BigDecimal amount,
            String comment,
            Long pointsTransactionId
    ) throws Exception {

        // 1. Находим конфигурацию банка
        BankConfig bank = BankConfig.findByServiceId(serviceId);
        if (bank == null) {
            throw new IllegalArgumentException("Unknown bank service ID: " + serviceId);
        }

        // 2. Валидация суммы
        if (!bank.isAmountValid(amount.intValue())) {
            throw new IllegalArgumentException(
                    String.format("Amount must be between %d and %d KGS for %s",
                            bank.getMinAmount(), bank.getMaxAmount(), bank.getName())
            );
        }

        // 2. Проверяем пользователя
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Форматируем номер телефона
        String formattedPhone = formatPhoneNumber(recipientPhone);

        // 5. Проверяем получателя
        CheckRecipientResponse recipientCheck = bankWithdrawalService.checkRecipientForBank(
                bank.getServiceId(),
                bank.getServiceCode(),
                formattedPhone,
                amount.intValue(),
                bank.isRequiresTransactionType()
        );

        if (recipientCheck.getStatusCode() != 200) {
            throw new RuntimeException("Recipient validation failed: " +
                    recipientCheck.getErrorMessage());
        }

        String transactionType = null;
        if (bank.isRequiresTransactionType()) {
            transactionType = recipientCheck.getTransactionType();
            log.info("MBank transactionType received: {}", transactionType);

            if (transactionType == null) {
                throw new RuntimeException(
                        "MBank requires transactionType but it was not returned from checkRecipient"
                );
            }
        }

        log.info("Recipient validated for {}: phone={}, name={}",
                bank.getName(), formattedPhone, recipientCheck.getName());


        // 6. Генерируем уникальный transactionId
        String transactionId = UUID.randomUUID().toString();

        // 7. Создаем запись в БД
        Withdrawal withdrawal = Withdrawal.builder()
                .user(user)
                .transactionId(transactionId)
                .serviceId(bank.getServiceId())
                .serviceName(bank.getName())
                .recipientPhone(formattedPhone)
                .recipientName(recipientCheck.getName())
                .amount(amount)
                .pointsTransactionId(pointsTransactionId)
                .accountId(finikConfig.getAccountId())
                .status(Withdrawal.WithdrawalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        withdrawal = withdrawalRepository.save(withdrawal);

        log.info("Withdrawal record created: id={}, transactionId={}, bank={}, amount={}",
                withdrawal.getId(), transactionId, bank.getName(), amount);

        try {
            // 8. Отправляем запрос на вывод
            MakePaymentResponse paymentResponse = bankWithdrawalService.makePaymentToBank(
                    transactionId,
                    bank.getServiceId(),
                    bank.getServiceCode(),
                    formattedPhone,
                    amount.intValue(),
                    comment,
                    bank.isRequiresTransactionType()
            );

            // 9. Обновляем статус
            if (paymentResponse.getStatusCode() == null ||
                    (paymentResponse.getStatusCode() != 200 && paymentResponse.getStatusCode() != 201)) {

                withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
                withdrawal.setErrorMessage(paymentResponse.getErrorMessage());
                withdrawalRepository.save(withdrawal);
                throw new RuntimeException("Payment failed: " + paymentResponse.getErrorMessage());
            }

            // Обновляем данные транзакции
            withdrawal.setFinikTransactionId(paymentResponse.getId());
            log.error("Payment response: {}", paymentResponse);

            // Определяем статус
            String status = paymentResponse.getStatus();
            if ("SUCCEEDED".equals(status)) {
                withdrawal.setStatus(Withdrawal.WithdrawalStatus.SUCCEEDED);
                if (paymentResponse.getTransactionDate() != null) {
                    withdrawal.setCompletedAt(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(paymentResponse.getTransactionDate()),
                            ZoneId.systemDefault()
                    ));
                }
            } else if ("FAILED".equals(status) || "CANCELED".equals(status)) {
                withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
            } else {
                withdrawal.setStatus(Withdrawal.WithdrawalStatus.PROCESSING);
            }

            withdrawal = withdrawalRepository.save(withdrawal);

            log.info("Withdrawal processed to {}: id={}, finikId={}, status={}",
                    bank.getName(), withdrawal.getId(), paymentResponse.getId(), status);

            return withdrawal;

        } catch (Exception e) {
            withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
            withdrawal.setErrorMessage(e.getMessage());
            withdrawal = withdrawalRepository.save(withdrawal);
            log.error("Failed to process withdrawal: transactionId={}", transactionId, e);
            return withdrawal;
        }
    }

    /**
     * Получение вывода по ID
     */
    public Withdrawal getWithdrawal(Long withdrawalId) {
        return withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
    }

    /**
     * История выводов пользователя
     */
    public List<Withdrawal> getUserWithdrawals(Long telegramId) {
        return withdrawalRepository.findByUserTelegramIdOrderByCreatedAtDesc(telegramId);
    }


    public GetServicesResponse getAvailableServices(String locale) throws Exception {
        // Получаем все активные услуги
        // Можно отфильтровать по категории (например, только мобильные операторы)
        return paymentsGatewayService.getAvailableServices(
                0,      // from
                50,     // size (максимум)
                locale, // язык (RU, EN, KY)
                "kyrgyzstan"    // parentId (null = все услуги)
        );
    }

    /**
     * Форматирование номера телефона
     */
    private String formatPhoneNumber(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("996")) {
            return cleaned;
        }

        if (cleaned.startsWith("0")) {
            return "996" + cleaned.substring(1);
        }

        if (cleaned.length() == 9) {
            return "996" + cleaned;
        }

        return cleaned;
    }
}