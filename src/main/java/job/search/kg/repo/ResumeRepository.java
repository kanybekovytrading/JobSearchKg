package job.search.kg.repo;

import job.search.kg.entity.Resume;
import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>, JpaSpecificationExecutor<Resume> {

    List<Resume> findByUser(User user);

    List<Resume> findByUserAndIsActive(User user, Boolean isActive);

    Long countByIsActive(Boolean isActive);

    Optional<Resume> findFirstByUserIdOrderByCreatedAtAsc(Long userId);
}