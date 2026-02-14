package job.search.kg.repo;

import job.search.kg.entity.VacancyMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyMediaRepository extends JpaRepository<VacancyMedia, Long> {

    List<VacancyMedia> findByVacancyIdOrderByDisplayOrderAsc(Long vacancyId);
}