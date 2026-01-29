package job.search.kg.controller.admin;

import job.search.kg.dto.request.PageRequestDTO;
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
