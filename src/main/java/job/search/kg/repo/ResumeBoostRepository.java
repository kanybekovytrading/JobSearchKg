package job.search.kg.repo;

import job.search.kg.entity.ResumeBoost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeBoostRepository extends JpaRepository<ResumeBoost, Long> {
    boolean existsByResumeIdAndIsActiveTrueAndExpiresAtAfter(Long resumeId, LocalDateTime now);
    List<ResumeBoost> findByResumeIdAndIsActiveTrue(Long resumeId);
    List<ResumeBoost> findByUserTelegramIdOrderByCreatedAtDesc(Long telegramId);

    Optional<ResumeBoost> findByPaymentId(String paymentId);
    List<ResumeBoost> findByIsActiveTrueAndExpiresAtBefore(LocalDateTime dateTime);

    List<ResumeBoost> findByIsActiveTrueAndExpiresAtBetween(LocalDateTime start, LocalDateTime end);

    List<ResumeBoost> findByIsActiveFalseAndCreatedAtBefore(LocalDateTime dateTime);

}