package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.UpdatePointsRequest;
import job.search.kg.dto.response.admin.PointsStatsResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.PointsTransactionRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPointsService {

    private final UserRepository userRepository;
    private final PointsTransactionRepository transactionRepository;
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public PointsStatsResponse getPointsStats() {
        Integer totalInSystem = userRepository.findAll().stream()
                .mapToInt(User::getBalance)
                .sum();

        Integer totalEarned = transactionRepository.getTotalEarned();
        Integer totalSpent = transactionRepository.getTotalSpent();

        PointsStatsResponse response = new PointsStatsResponse();
        response.setTotalInSystem(totalInSystem);
        response.setTotalEarned(totalEarned);
        response.setTotalSpent(totalSpent);

        return response;
    }

    @Transactional
    public User updateUserPoints(Long userId, UpdatePointsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int oldBalance = user.getBalance();
        int difference = request.getNewBalance() - oldBalance;

        user.setBalance(request.getNewBalance());
        userRepository.save(user);

        // Записать транзакцию
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUser(user);
        transaction.setAmount(difference);
        transaction.setType(PointsTransaction.TransactionType.ADMIN_GRANT);
        transaction.setDescription(request.getReason() != null ? request.getReason() : "Корректировка администратором");
        transactionRepository.save(transaction);

        // Уведомление пользователю
        if (difference > 0) {
            telegramService.sendMessage(
                    user.getTelegramId(),
                    String.format("💰 Вам начислено +%d баллов!\n\nПричина: %s\n\nВаш баланс: %d баллов",
                            difference, transaction.getDescription(), user.getBalance())
            );
        } else if (difference < 0) {
            telegramService.sendMessage(
                    user.getTelegramId(),
                    String.format("⚠️ С вашего счёта списано %d баллов.\n\nПричина: %s\n\nВаш баланс: %d баллов",
                            Math.abs(difference), transaction.getDescription(), user.getBalance())
            );
        }

        return user;
    }

    @Transactional(readOnly = true)
    public List<PointsTransaction> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
