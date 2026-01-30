package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.UpdatePointsRequest;
import job.search.kg.dto.response.admin.PointsStatsResponse;
import job.search.kg.dto.response.admin.UserBalanceDTO;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.PointsTransactionRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<UserBalanceDTO> getAllUsersBalances(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageRequest);

        return users.stream()
                .map(user -> {
                    List<PointsTransaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user);

                    int earned = transactions.stream()
                            .filter(t -> t.getAmount() > 0)
                            .mapToInt(PointsTransaction::getAmount)
                            .sum();

                    int spent = transactions.stream()
                            .filter(t -> t.getAmount() < 0)
                            .mapToInt(t -> Math.abs(t.getAmount()))
                            .sum();

                    return new UserBalanceDTO(
                            user.getId(),
                            user.getTelegramId(),
                            user.getBalance(),
                            earned,
                            spent,
                            user.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public User updateUserPoints(Long userId, UpdatePointsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        return user;
    }

    @Transactional(readOnly = true)
    public List<PointsTransaction> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
