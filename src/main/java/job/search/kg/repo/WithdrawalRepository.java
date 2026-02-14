package job.search.kg.repo;

import job.search.kg.entity.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    /**
     * Поиск вывода по transactionId
     */
    Optional<Withdrawal> findByTransactionId(String transactionId);

    /**
     * История выводов пользователя
     */
    List<Withdrawal> findByUserTelegramIdOrderByCreatedAtDesc(Long telegramId);

    /**
     * Получение всех выводов со статусом PROCESSING для проверки
     */
    List<Withdrawal> findByStatus(Withdrawal.WithdrawalStatus status);

    Page<Withdrawal> findByStatus(Withdrawal.WithdrawalStatus status, Pageable pageable);

}
