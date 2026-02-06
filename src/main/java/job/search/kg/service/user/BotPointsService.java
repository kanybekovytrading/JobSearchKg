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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotPointsService {

    private final UserRepository userRepository;
    private final PointsTransactionRepository transactionRepository;
    private final TelegramService telegramService;
    private final BotSubscriptionService botSubscriptionService;
    private final WithdrawalService withdrawalService;

    // Константы обмена
    private static final int POINTS_PER_SOM = 10;           // 10 баллов = 1 сом
    private static final int MIN_POINTS_FOR_WITHDRAWAL = 1000;  // Минимум 1000 баллов = 100 сом
    private static final int MAX_WITHDRAWAL_SOMS = 1000;    // Максимум 1000 сом


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

    /**
     * Покупка подписки за баллы
     * 1500 баллов = 150 сом
     */
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
     * 50 баллов = 5 сом
     * Минимум: 1000 баллов = 100 сом
     * Максимум: 10000 баллов = 1000 сом
     */
    @Transactional
    public void withdrawPointsToMoney(
            Long telegramId,
            Integer pointsAmount,
            String serviceId,
            String serviceName,
            String recipientPhone
    ) throws Exception {

        // Валидация баллов
        if (pointsAmount < MIN_POINTS_FOR_WITHDRAWAL) {
            throw new IllegalArgumentException(
                    String.format("Minimum withdrawal is %d points (%d KGS)",
                            MIN_POINTS_FOR_WITHDRAWAL,
                            MIN_POINTS_FOR_WITHDRAWAL / POINTS_PER_SOM)
            );
        }

        // Проверяем баланс
        if (!hasEnoughPoints(telegramId, pointsAmount)) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        // Конвертируем баллы в сомы
        BigDecimal amountInSoms = BigDecimal.valueOf(pointsAmount)
                .divide(BigDecimal.valueOf(POINTS_PER_SOM));

        // Проверяем максимум
        if (amountInSoms.compareTo(BigDecimal.valueOf(MAX_WITHDRAWAL_SOMS)) > 0) {
            throw new IllegalArgumentException(
                    String.format("Maximum withdrawal is %d KGS (%d points)",
                            MAX_WITHDRAWAL_SOMS,
                            MAX_WITHDRAWAL_SOMS * POINTS_PER_SOM)
            );
        }

        // Списываем баллы
       long pointsTransactionId =  deductPoints(
                telegramId,
                pointsAmount,
                PointsTransaction.TransactionType.WITHDRAWAL,
                String.format("Вывод %s сом на %s", amountInSoms, recipientPhone)
        );

        try {
            // Создаем запрос на вывод через WithdrawalService
            withdrawalService.createWithdrawal(
                    telegramId,
                    serviceId,
                    serviceName,
                    recipientPhone,
                    amountInSoms,
                    pointsTransactionId
            );
        } catch (Exception e) {
            // В случае ошибки возвращаем баллы
            addPoints(
                    telegramId,
                    pointsAmount,
                    PointsTransaction.TransactionType.REFUND,
                    "Возврат баллов после неудачного вывода"
            );
            throw e;
        }
    }

    /**
     * Информация о доступном выводе
     */
    @Transactional(readOnly = true)
    public WithdrawalInfo getWithdrawalInfo(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
            case ONE_WEEK -> 1500;      // 150 сом = 1500 баллов
            case ONE_MONTH -> 5000;     // 500 сом = 5000 баллов
            case THREE_MONTHS -> 12000; // 1200 сом = 12000 баллов
        };
    }
}