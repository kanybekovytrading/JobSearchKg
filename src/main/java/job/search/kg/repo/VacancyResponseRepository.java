package job.search.kg.repo;

import job.search.kg.entity.VacancyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyResponseRepository extends JpaRepository<VacancyResponse, Long> {
    boolean existsByVacancyIdAndResumeId(Long vacancyId, Long resumeId);

    List<VacancyResponse> findByVacancyIdOrderByCreatedAtDesc(Long vacancyId);
}
