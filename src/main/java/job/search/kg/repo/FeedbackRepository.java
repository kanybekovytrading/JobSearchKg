package job.search.kg.repo;

import job.search.kg.entity.Feedback;
import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByUser(User user);

    List<Feedback> findByStatusOrderByCreatedAtDesc(Feedback.FeedbackStatus status);

    Long countByStatus(Feedback.FeedbackStatus status);
}
