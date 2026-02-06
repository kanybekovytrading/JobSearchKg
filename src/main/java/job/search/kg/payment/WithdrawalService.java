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
    private final FinikPaymentsGatewayService paymentsGatewayService;
    private final FinikWConfig finikConfig;

    /**
     * Проверка получателя перед выводом
     */
    public CheckRecipientResponse checkRecipient(
            String serviceId,
            String phone,
            Integer amount
    ) throws Exception {

        // Форматируем номер телефона (если нужно)
        String formattedPhone = formatPhoneNumber(phone);

        return paymentsGatewayService.checkRecipient(
                serviceId,
                formattedPhone,
                amount
        );
    }

    /**
     * Создание запроса на вывод средств
     */
    @Transactional
    public Withdrawal createWithdrawal(
            Long telegramId,
            String serviceId,
            String serviceName,
            String recipientPhone,
            BigDecimal amount,
            Long pointsTransactionId
    ) throws Exception {

        // 1. Валидация
        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Minimum withdrawal amount is 1 KGS");
        }

        // Максимальная сумма вывода
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new IllegalArgumentException("Maximum withdrawal amount is 10000 KGS");
        }

        // 2. Проверяем пользователя
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Форматируем номер телефона
        String formattedPhone = formatPhoneNumber(recipientPhone);

        // 3. Проверяем получателя
        CheckRecipientResponse recipientCheck = paymentsGatewayService.checkRecipient(
                serviceId,
                formattedPhone,
                amount.intValue()
        );

        if (recipientCheck.getStatusCode() != 200) {
            throw new RuntimeException("Recipient validation failed: " +
                    recipientCheck.getErrorMessage());
        }

        // 4. Генерируем уникальный transactionId
        String transactionId = UUID.randomUUID().toString();

        // 5. Создаем запись в БД
        Withdrawal withdrawal = Withdrawal.builder()
                .user(user)
                .transactionId(transactionId)
                .serviceId(serviceId)
                .serviceName(serviceName)
                .recipientPhone(formattedPhone)
                .recipientName(recipientCheck.getName())
                .amount(amount)
                .pointsTransactionId(pointsTransactionId)
                .accountId(finikConfig.getAccountId())
                .status(Withdrawal.WithdrawalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        withdrawal = withdrawalRepository.save(withdrawal);

        log.info("Withdrawal record created: id={}, transactionId={}, service={}, amount={}, phone={}",
                withdrawal.getId(), transactionId, serviceId, amount, formattedPhone);

        try {
            // 6. Отправляем запрос на вывод
            MakePaymentResponse paymentResponse = paymentsGatewayService.makePayment(
                    transactionId,
                    finikConfig.getAccountId(),
                    finikConfig.getUserId(),  // userId
                    serviceId,
                    formattedPhone,
                    amount.intValue()
            );

            // 7. Обновляем статус
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
                // PENDING или PROCESSING
                withdrawal.setStatus(Withdrawal.WithdrawalStatus.PROCESSING);
            }

            withdrawal = withdrawalRepository.save(withdrawal);

            log.info("Withdrawal processed: id={}, finikId={}, status={}",
                    withdrawal.getId(), paymentResponse.getId(), status);

            return withdrawal;

        } catch (Exception e) {
            // При ошибке помечаем как FAILED
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

    /**
     * Получение списка доступных услуг для вывода
     */
    public GetServicesResponse getAvailableServices(String locale) throws Exception {
        // Получаем все активные услуги
        // Можно отфильтровать по категории (например, только мобильные операторы)
        GetServicesResponse allServices = paymentsGatewayService.getAvailableServices(
                0,      // from
                50,     // size (максимум)
                locale, // язык (RU, EN, KY)
                "kyrgyzstan"    // parentId (null = все услуги)
        );

//        List<ServiceDTO> filteredServices = allServices.getServices()
//                .stream()
//                .filter(service -> {
//                    String nameRu = service.getName_ru();
//                    if (nameRu == null) return false;
//
//                    // Фильтруем только банкинги и платежные системы
//                    return nameRu.toLowerCase().contains("o!") ||
//                            nameRu.toLowerCase().contains("megacom") ||
//                            nameRu.toLowerCase().contains("beeline") ||
//                            nameRu.toLowerCase().contains("банк") ||
//                            nameRu.toLowerCase().contains("элсом") ||
//                            nameRu.toLowerCase().contains("оптима") ||
//                            nameRu.toLowerCase().contains("bakai") ||
//                            nameRu.toLowerCase().contains("demir") ||
//                            nameRu.toLowerCase().contains("rsk") ||
//                            nameRu.toLowerCase().contains("dos") ||
//                            nameRu.toLowerCase().contains("айыл") ||
//                            nameRu.toLowerCase().contains("кыргызстан") ||
//                            nameRu.toLowerCase().contains("компаньон") ||
//                            nameRu.toLowerCase().contains("halyk") ||
//                            nameRu.toLowerCase().contains("mbank");
//                })
//                .filter(service -> "ENABLED".equals(service.getStatus())) // Только активные
//                .toList();
//
//        allServices.setServices(filteredServices);
//        allServices.setTotal(filteredServices.size());
//
//        log.info("Found {} payment services", filteredServices.size());

        return allServices;
    }

    /**
     * Форматирование номера телефона
     * Преобразует в формат +996XXXXXXXXX
     */
    private String formatPhoneNumber(String phone) {
        // Убираем все кроме цифр
        String cleaned = phone.replaceAll("[^0-9]", "");

        // Если начинается с 996, добавляем +
        if (cleaned.startsWith("996")) {
            return "+" + cleaned;
        }

        // Если начинается с 0, заменяем на +996
        if (cleaned.startsWith("0")) {
            return "+996" + cleaned.substring(1);
        }

        // Если только 9 цифр, добавляем +996
        if (cleaned.length() == 9) {
            return "+996" + cleaned;
        }

        return "+" + cleaned;
    }
}
