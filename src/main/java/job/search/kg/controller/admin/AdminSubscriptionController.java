package job.search.kg.controller.admin;
import job.search.kg.dto.request.admin.GrantSubscriptionRequest;
import job.search.kg.entity.Subscription;
import job.search.kg.service.admin.AdminSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    @GetMapping
    public ResponseEntity<Page<Subscription>> getAllSubscriptions(Pageable pageable) {
        Page<Subscription> subscriptions = adminSubscriptionService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/grant")
    public ResponseEntity<Subscription> grantSubscription(
            @RequestBody GrantSubscriptionRequest request,
            Authentication authentication) {
        String adminEmail = authentication.getName();
        Subscription subscription = adminSubscriptionService.grantSubscription(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateSubscription(@PathVariable Long id) {
        adminSubscriptionService.deactivateSubscription(id);
        return ResponseEntity.ok().build();
    }
}
