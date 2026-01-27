package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.LoginRequest;
import job.search.kg.dto.request.admin.RefreshTokenRequest;
import job.search.kg.dto.response.admin.LoginResponse;
import job.search.kg.service.admin.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        LoginResponse response = adminAuthService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        String email = authentication.getName();
        adminAuthService.logout(email);
        return ResponseEntity.ok().build();
    }
}