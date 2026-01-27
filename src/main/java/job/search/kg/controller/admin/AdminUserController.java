package job.search.kg.controller.admin;

import job.search.kg.entity.User;
import job.search.kg.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/ban")
    public ResponseEntity<User> banUser(@PathVariable Long id) {
        User user = adminUserService.banUser(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/unban")
    public ResponseEntity<User> unbanUser(@PathVariable Long id) {
        User user = adminUserService.unbanUser(id);
        return ResponseEntity.ok(user);
    }
}
