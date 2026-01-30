package job.search.kg.repo;

import job.search.kg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);

    Long countByReferrer(User referrer);

    Optional<User> findByReferralCode(String referralCode);

    boolean existsByTelegramId(Long telegramId);

    boolean existsByPhone(String phone);

    Optional<User> findByUsername(String username);
}
