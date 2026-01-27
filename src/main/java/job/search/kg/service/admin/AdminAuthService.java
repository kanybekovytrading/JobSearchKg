package job.search.kg.service.admin;

import job.search.kg.config.JwtTokenProvider;
import job.search.kg.dto.request.admin.LoginRequest;
import job.search.kg.dto.request.admin.RefreshTokenRequest;
import job.search.kg.dto.response.admin.LoginResponse;
import job.search.kg.entity.Admin;
import job.search.kg.entity.RefreshToken;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!admin.getIsActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Обновление времени последнего входа
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        // Генерация токенов
        String accessToken = jwtTokenProvider.generateToken(admin.getEmail(), "ROLE_" + admin.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(admin);

        log.info("Admin logged in: {}", admin.getEmail());

        return buildLoginResponse(admin, accessToken, refreshToken.getToken());
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        // Проверка истечения и отзыва
        refreshTokenService.verifyExpiration(refreshToken);

        Admin admin = refreshToken.getAdmin();

        if (!admin.getIsActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        // Генерация нового Access Token
        String accessToken = jwtTokenProvider.generateToken(admin.getEmail(), "ROLE_" + admin.getRole().name());

        log.info("Token refreshed for admin: {}", admin.getEmail());

        // Возвращаем тот же refresh token (или можно сгенерировать новый для rotation)
        return buildLoginResponse(admin, accessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        refreshTokenService.deleteByAdmin(admin);

        log.info("Admin logged out: {}", email);
    }

    @Transactional(readOnly = true)
    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + email));
    }

    // Вспомогательный метод для построения ответа
    private LoginResponse buildLoginResponse(Admin admin, String accessToken, String refreshToken) {
        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setAdminId(admin.getId());
        response.setEmail(admin.getEmail());
        response.setName(admin.getName());
        response.setRole(admin.getRole());
        return response;
    }
}