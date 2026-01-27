package job.search.kg.repo;


import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long>, JpaSpecificationExecutor<Vacancy> {

    List<Vacancy> findByUser(User user);

    List<Vacancy> findByUserAndIsActive(User user, Boolean isActive);

    Long countByIsActive(Boolean isActive);
}