package job.search.kg.repo;

import job.search.kg.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByUserTelegramIdOrderByCreatedAtDesc(Long telegramId);

    List<Payment> findByStatusAndCreatedAtBefore(Payment.PaymentStatus paymentStatus, LocalDateTime thirtyMinutesAgo);

    Optional<Payment> findByTransactionId(String transactionId);
}
