package job.search.kg.controller.admin;

import job.search.kg.dto.request.PageRequestDTO;
import job.search.kg.dto.response.admin.SubscriptionDTO;
import job.search.kg.dto.response.admin.UserProfileDTO;
import job.search.kg.entity.User;
import job.search.kg.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping("/search")
    public ResponseEntity<Page<User>> getAllUsers(@RequestBody PageRequestDTO pageRequest) {
        Sort sort = Sort.unsorted();

        if (pageRequest.getSort() != null && !pageRequest.getSort().isEmpty()) {
            List<Sort.Order> orders = pageRequest.getSort().stream()
                    .map(sortStr -> {
                        String[] parts = sortStr.split(",");
                        String property = parts[0].trim();
                        Sort.Direction direction = parts.length > 1
                                ? Sort.Direction.fromString(parts[1].trim())
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, property);
                    })
                    .collect(Collectors.toList());
            sort = Sort.by(orders);
        }

        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
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

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<Void> toggleBanUser(@PathVariable Long userId) {
        adminUserService.toggleBanUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserProfile(userId));
    }
    /**
     * Получить информацию об активности (подписках)
     */
    @GetMapping("/{userId}/activity")
    public ResponseEntity<SubscriptionDTO> getUserActivity(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserActivity(userId));
    }
}
