package job.search.kg.controller.admin;

import job.search.kg.dto.response.admin.DashboardResponse;
import job.search.kg.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> getDashboardStats() {
        DashboardResponse stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
