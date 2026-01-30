package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.UpdatePointsRequest;
import job.search.kg.dto.response.admin.PointsStatsResponse;
import job.search.kg.dto.response.admin.UserBalanceDTO;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.User;
import job.search.kg.service.admin.AdminPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/points")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointsController {

    private final AdminPointsService adminPointsService;

    @GetMapping("/stats")
    public ResponseEntity<PointsStatsResponse> getPointsStats() {
        PointsStatsResponse stats = adminPointsService.getPointsStats();
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUserPoints(
            @PathVariable Long userId,
            @RequestBody UpdatePointsRequest request) {
        User user = adminPointsService.updateUserPoints(userId, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<List<PointsTransaction>> getUserTransactions(@PathVariable Long userId) {
        List<PointsTransaction> transactions = adminPointsService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserBalanceDTO>> getAllUsersBalances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<UserBalanceDTO> users = adminPointsService.getAllUsersBalances(page, size);
        return ResponseEntity.ok(users);
    }
}
