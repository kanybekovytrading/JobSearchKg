package job.search.kg.repo;

import job.search.kg.entity.ResumeStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeStatisticsRepository extends JpaRepository<ResumeStatistics, Long> {
    Optional<ResumeStatistics> findByResumeId(Long resumeId);
}
