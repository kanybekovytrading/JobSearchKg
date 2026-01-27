package job.search.kg.service.user;

import job.search.kg.dto.response.user.BalanceResponse;
import job.search.kg.entity.PointsTransaction;
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

        // Уведомление пользователю
        telegramService.sendMessage(telegramId,
                String.format("💰 Вам начислено +%d баллов!\n\nПричина: %s\n\nВаш баланс: %d баллов",
                        amount, description, user.getBalance()));
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getBalance() >= requiredAmount;
    }
}