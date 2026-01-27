package job.search.kg.repo;

import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {

    List<PointsTransaction> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PointsTransaction pt WHERE pt.amount > 0")
    Integer getTotalEarned();

    @Query("SELECT COALESCE(SUM(ABS(pt.amount)), 0) FROM PointsTransaction pt WHERE pt.amount < 0")
    Integer getTotalSpent();
}