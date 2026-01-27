package job.search.kg.repo;

import job.search.kg.entity.Payment;
import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByUser(User user);

    List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);
}
