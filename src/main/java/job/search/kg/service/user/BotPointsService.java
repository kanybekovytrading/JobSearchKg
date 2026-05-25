package job.search.kg.service.user;

import job.search.kg.dto.response.payment.WithdrawalInfo;
import job.search.kg.dto.response.user.BalanceResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.payment.WithdrawalService;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.InsufficientBalanceException;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotPointsService {

    // Константы обмена
    // 1 балл = 5 тыйын = 0.05 сом
    // 100 баллов = 5 сом
    // 5000 баллов = 250 сом (минимум на вывод)
    // 20000 баллов = 1000 сом (максимум на вывод)
    private static final int POINTS_PER_SOM = 20;              // 20 баллов = 1 сом
    private static final int MIN_POINTS_FOR_WITHDRAWAL = 2000; // Минимум 5000 баллов = 250 сом
    private static final int MAX_WITHDRAWAL_SOMS = 1000;       // Максимум 1000 сом
    private final UserRepository userRepository;
    private final PointsTransactionRepository transactionRepository;
    private final TelegramService telegramService;
    private final BotSubscriptionService botSubscriptionService;
    private final WithdrawalService withdrawalService;

    private static @NonNull BigDecimal getBigDecimal(Integer pointsAmount) {
        BigDecimal amountInSoms = BigDecimal.valueOf(pointsAmount)
                .divide(BigDecimal.valueOf(POINTS_PER_SOM), 2, RoundingMode.DOWN);

        // Проверяем максимум
        if (amountInSoms.compareTo(BigDecimal.valueOf(MAX_WITHDRAWAL_SOMS)) > 0) {
            throw new IllegalArgumentException(
                    String.format("Maximum withdrawal is %d KGS (%d points)",
                            MAX_WITHDRAWAL_SOMS,
                            MAX_WITHDRAWAL_SOMS * POINTS_PER_SOM)
            );
        }
        return amountInSoms;
    }

    @Transactional
    public void addPoints(Long telegramId, Integer amount, PointsTransaction.TransactionType type, String description) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);

        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    @Transactional
    public long deductPoints(Long telegramId, Integer amount, PointsTransaction.TransactionType type, String description) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setAmount(-amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
        return transaction.getId();
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<PointsTransaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);

        BalanceResponse response = new BalanceResponse();
        response.setBalance(user.getBalance());
        response.setTransactions(transactions);

        return response;
    }

    @Transactional(readOnly = true)
    public boolean hasEnoughPoints(Long telegramId, Integer requiredAmount) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getBalance() >= requiredAmount;
    }

    @Transactional
    public void purchaseSubscriptionWithPoints(Long telegramId, Subscription.PlanType subscriptionType) {
        if (botSubscriptionService.hasActiveSubscription(telegramId)) {
            throw new IllegalStateException("У вас уже есть активная подписка");
        }
        int requiredPoints = getSubscriptionPointsCost(subscriptionType);
        if (!hasEnoughPoints(telegramId, requiredPoints)) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        deductPoints(telegramId, requiredPoints, PointsTransaction.TransactionType.SUBSCRIPTION,
                "Покупка подписки: " + subscriptionType);

        botSubscriptionService.createSubscription(
                telegramId,
                subscriptionType,
                "POINTS_PAYMENT_" + System.currentTimeMillis()
        );
    }

    /**
     * Обмен баллов на деньги и вывод
     * 1 балл = 0.05 сом (5 тыйын)
     * 100 баллов = 5 сом
     * Минимум: 5000 баллов = 250 сом
     * Максимум: 20000 баллов = 1000 сом
     */
    @Transactional
    public void withdrawPointsToMoney(
            Long telegramId,
            Integer pointsAmount,
            String serviceId,
            String recipientPhone
    ) {

        User user = userRepository.findByTelegramId(telegramId).orElseThrow(
                () -> new ResourceNotFoundException("Пользователь не найден")
        );
        // Валидация баллов
        if (pointsAmount < 2000) {
            throw new IllegalArgumentException(
                    String.format("Минимальная сумма вывода — %d баллов (%d сом)",
                            MIN_POINTS_FOR_WITHDRAWAL,
                            MIN_POINTS_FOR_WITHDRAWAL / POINTS_PER_SOM)
            );
        }

        // Проверяем баланс
        if (!hasEnoughPoints(telegramId, pointsAmount)) {
            throw new InsufficientBalanceException("Недостаточно баллов для вывода средств");
        }

        // Конвертируем баллы в сомы: points / 20 = soms
        // Используем BigDecimal для точности
        BigDecimal amountInSoms = getBigDecimal(pointsAmount);

        // Списываем баллы
        long pointsTransactionId = deductPoints(
                telegramId,
                pointsAmount,
                PointsTransaction.TransactionType.WITHDRAWAL,
                String.format("Вывод %s сом на %s", amountInSoms, recipientPhone)
        );

        try {
            // Создаем запрос на вывод через WithdrawalService (REQUIRES_NEW — своя транзакция)
            var withdrawal = withdrawalService.createWithdrawal(
                    telegramId,
                    serviceId,
                    recipientPhone,
                    amountInSoms,
                    "Обмен баллов на деньги",
                    pointsTransactionId
            );
            if (withdrawal.getStatus() == job.search.kg.entity.Withdrawal.WithdrawalStatus.FAILED) {
                addPoints(
                        telegramId,
                        pointsAmount,
                        PointsTransaction.TransactionType.REFUND,
                        "Возврат баллов после неудачного вывода"
                );
                String msg = getWithdrawalFailedMessage(user.getLanguage(), amountInSoms);
                telegramService.sendMessage(user.getTelegramId(), msg);
            }
        } catch (Exception e) {
            // Ошибка до создания записи вывода (неверный банк, получатель не найден и т.д.)
            addPoints(
                    telegramId,
                    pointsAmount,
                    PointsTransaction.TransactionType.REFUND,
                    "Возврат баллов после неудачного вывода"
            );
            String msg = getWithdrawalFailedMessage(user.getLanguage(), amountInSoms);
            telegramService.sendMessage(user.getTelegramId(), msg);
        }
    }

    /**
     * Информация о доступном выводе
     */
    @Transactional(readOnly = true)
    public WithdrawalInfo getWithdrawalInfo(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        int balance = user.getBalance();
        int availableSoms = balance / POINTS_PER_SOM;
        int minSoms = MIN_POINTS_FOR_WITHDRAWAL / POINTS_PER_SOM;
        boolean canWithdraw = balance >= MIN_POINTS_FOR_WITHDRAWAL;

        return WithdrawalInfo.builder()
                .currentPoints(balance)
                .availableSoms(availableSoms)
                .minWithdrawalSoms(minSoms)
                .maxWithdrawalSoms(MAX_WITHDRAWAL_SOMS)
                .canWithdraw(canWithdraw)
                .pointsPerSom(POINTS_PER_SOM)
                .build();
    }

    private int getSubscriptionPointsCost(Subscription.PlanType planType) {
        return switch (planType) {
            case THREE_DAYS -> 5000;      // 50 рефералов
            case ONE_WEEK -> 10000;       // 100 рефералов
            case ONE_MONTH -> 30000;      // 300 рефералов
            case THREE_MONTHS ->
                    throw new IllegalArgumentException("THREE_MONTHS subscription not supported for points purchase");
        };
    }

    private String getWithdrawalFailedMessage(
            User.Language language,
            BigDecimal amount
    ) {
        return switch (language) {
            case RU -> String.format(
                    "❌ <b>Вывод средств не выполнен</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n\n" +
                            "К сожалению, не удалось завершить операцию.\n\n" +
                            "Попробуйте позже или свяжитесь с поддержкой.",
                    amount
            );
            case KY -> String.format(
                    "❌ <b>Акча алуу аткарылган жок</b>\n\n" +
                            "💰 Сумма: <b>%.2f сом</b>\n\n" +
                            "Тилекке каршы, операцияны аяктоо мүмкүн болгон жок.\n\n" +
                            "Кийинчерээк кайра аракет кылыңыз же колдоо кызматына кайрылыңыз.",
                    amount
            );
            case EN -> String.format(
                    "❌ <b>Withdrawal failed</b>\n\n" +
                            "💰 Amount: <b>%.2f som</b>\n\n" +
                            "Unfortunately, the operation could not be completed.\n\n" +
                            "Please try again later or contact support.",
                    amount
            );
        };
    }
}