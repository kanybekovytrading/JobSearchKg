package job.search.kg.payment;

import job.search.kg.dto.response.payment.CheckRecipientResponse;
import job.search.kg.dto.response.payment.GetServicesResponse;
import job.search.kg.dto.response.payment.MakePaymentResponse;
import job.search.kg.entity.User;
import job.search.kg.entity.Withdrawal;
import job.search.kg.repo.UserRepository;
import job.search.kg.repo.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;
    private final BankWithdrawalService bankWithdrawalService;
    private final FinikWConfig finikConfig;
    private final FinikPaymentsGatewayService paymentsGatewayService;

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
    @Transactional
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
            withdrawalRepository.save(withdrawal);

            log.error("Failed to process withdrawal: transactionId={}", transactionId, e);
            throw e;
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

        return  cleaned;
    }
}