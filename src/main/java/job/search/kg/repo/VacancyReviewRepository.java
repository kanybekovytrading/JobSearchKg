package job.search.kg.repo;

import job.search.kg.entity.VacancyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyReviewRepository extends JpaRepository<VacancyReview, Long> {
    boolean existsByVacancyIdAndUserId(Long vacancyId, Long userId);

    List<VacancyReview> findByVacancyIdOrderByCreatedAtDesc(Long vacancyId);

    List<VacancyReview> findByVacancyIdAndIsSpamFalseOrderByCreatedAtDesc(Long vacancyId);

    List<VacancyReview> findByUserIdOrderByCreatedAtDesc(Long userId);
}