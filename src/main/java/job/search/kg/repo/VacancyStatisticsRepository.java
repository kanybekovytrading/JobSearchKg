package job.search.kg.repo;

import job.search.kg.entity.VacancyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VacancyStatisticsRepository extends JpaRepository<VacancyStatistics, Long> {
    Optional<VacancyStatistics> findByVacancyId(Long vacancyId);
}
