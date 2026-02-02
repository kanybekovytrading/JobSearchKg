package job.search.kg.service.user;

import job.search.kg.dto.response.user.BalanceResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.InsufficientBalanceException;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotPointsService {

    private final UserRepository userRepository;
    private final PointsTransactionRepository transactionRepository;
    private final TelegramService telegramService;
    private final BotSubscriptionService botSubscriptionService;

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
    public void deductPoints(Long telegramId, Integer amount, PointsTransaction.TransactionType type, String description) {
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

    private int getSubscriptionPointsCost(Subscription.PlanType planType) {
        return switch (planType) {
            case ONE_WEEK -> 1500;      // 150 сом = 1500 баллов
            case ONE_MONTH -> 5000;     // 500 сом = 5000 баллов
            case THREE_MONTHS -> 12000; // 1200 сом = 12000 баллов
        };
    }
}