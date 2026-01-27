package job.search.kg.repo;

import job.search.kg.entity.SocialTask;
import job.search.kg.entity.User;
import job.search.kg.entity.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    List<UserTask> findByUser(User user);

    Optional<UserTask> findByUserAndTask(User user, SocialTask task);

    boolean existsByUserAndTask(User user, SocialTask task);

    Long countByTask(SocialTask task);
}
