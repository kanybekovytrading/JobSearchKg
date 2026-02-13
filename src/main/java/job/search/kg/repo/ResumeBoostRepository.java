package job.search.kg.repo;

import job.search.kg.entity.ResumeBoost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ResumeBoostRepository extends JpaRepository<ResumeBoost, Long> {
    boolean existsByResumeIdAndIsActiveTrueAndExpiresAtAfter(Long resumeId, LocalDateTime now);

    List<ResumeBoost> findByResumeIdAndIsActiveTrue(Long resumeId);

    @Query("SELECT rb.resume.id FROM ResumeBoost rb " +
            "WHERE rb.isActive = true " +
            "AND rb.expiresAt > :now")
    Set<Long> findActiveBoostResumeIds(@Param("now") LocalDateTime now);
    Optional<ResumeBoost> findByPaymentId(String paymentId);

    List<ResumeBoost> findByIsActiveTrueAndExpiresAtBefore(LocalDateTime dateTime);

    List<ResumeBoost> findByIsActiveTrueAndExpiresAtBetween(LocalDateTime start, LocalDateTime end);

    List<ResumeBoost> findByIsActiveFalseAndCreatedAtBefore(LocalDateTime dateTime);

}