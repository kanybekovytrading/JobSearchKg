package job.search.kg.repo;


import job.search.kg.entity.VacancyBoost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacancyBoostRepository extends JpaRepository<VacancyBoost, Long> {
    boolean existsByVacancyIdAndIsActiveTrueAndExpiresAtAfter(Long vacancyId, LocalDateTime now);
    List<VacancyBoost> findByVacancyIdAndIsActiveTrue(Long vacancyId);
    List<VacancyBoost> findByUserTelegramIdOrderByCreatedAtDesc(Long telegramId);

    Optional<VacancyBoost> findByPaymentId(String paymentId);
    List<VacancyBoost> findByIsActiveTrueAndExpiresAtBefore(LocalDateTime dateTime);

    List<VacancyBoost> findByIsActiveTrueAndExpiresAtBetween(LocalDateTime start, LocalDateTime end);

    List<VacancyBoost> findByIsActiveFalseAndCreatedAtBefore(LocalDateTime dateTime);

}