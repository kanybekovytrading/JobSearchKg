package job.search.kg.repo;

import job.search.kg.entity.SocialTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialTaskRepository extends JpaRepository<SocialTask, Integer> {

    List<SocialTask> findByIsActive(Boolean isActive);
}
