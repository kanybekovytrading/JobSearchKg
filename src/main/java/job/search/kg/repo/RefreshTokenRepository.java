package job.search.kg.repo;

import job.search.kg.entity.Admin;
import job.search.kg.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByAdmin(Admin admin);

    void deleteByExpiryDateBefore(LocalDateTime now);

}
