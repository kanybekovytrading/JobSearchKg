package job.search.kg.repo;


import job.search.kg.entity.VacancyBoost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VacancyBoostRepository extends JpaRepository<VacancyBoost, Long> {
    boolean existsByVacancyIdAndIsActiveTrueAndExpiresAtAfter(Long vacancyId, LocalDateTime now);

    List<VacancyBoost> findByVacancyIdAndIsActiveTrue(Long vacancyId);

    @Query("SELECT vb.vacancy.id FROM VacancyBoost vb " +
            "WHERE vb.isActive = true " +
            "AND vb.expiresAt > :now")
    Set<Long> findActiveBoostVacancyIds(@Param("now") LocalDateTime now);

    Optional<VacancyBoost> findByPaymentId(String paymentId);

    List<VacancyBoost> findByIsActiveTrueAndExpiresAtBefore(LocalDateTime dateTime);

    List<VacancyBoost> findByIsActiveTrueAndExpiresAtBetween(LocalDateTime start, LocalDateTime end);

    List<VacancyBoost> findByIsActiveFalseAndCreatedAtBefore(LocalDateTime dateTime);

}