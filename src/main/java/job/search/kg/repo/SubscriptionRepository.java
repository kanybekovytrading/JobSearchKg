package job.search.kg.repo;
import job.search.kg.entity.Subscription;
import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUser(User user);

    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.isActive = true AND s.endDate > :now ORDER BY s.endDate DESC")
    Optional<Subscription> findActiveSubscription(@Param("user") User user, @Param("now") LocalDateTime now);

    Long countByIsActive(Boolean isActive);

    Optional<Subscription> findFirstByUserIdAndIsActiveTrueOrderByEndDateDesc(Long userId);
}