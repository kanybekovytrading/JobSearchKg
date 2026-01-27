package job.search.kg.service.admin;

import job.search.kg.config.JwtTokenProvider;
import job.search.kg.entity.Admin;
import job.search.kg.entity.RefreshToken;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RefreshToken createRefreshToken(Admin admin) {
        // Удаляем старые refresh tokens этого админа
        refreshTokenRepository.deleteByAdmin(admin);

        String token = jwtTokenProvider.generateRefreshToken(admin.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .admin(admin)
                .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshExpirationMs())))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token == null) {
            throw new ResourceNotFoundException("Refresh token not found");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new login request");
        }

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token was revoked");
        }

        return token;
    }

    @Transactional
    public void deleteByAdmin(Admin admin) {
        refreshTokenRepository.deleteByAdmin(admin);
    }

    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // Удаление просроченных токенов (можно вызывать по расписанию)
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}