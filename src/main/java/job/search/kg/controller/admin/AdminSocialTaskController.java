package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.CreateSocialTaskRequest;
import job.search.kg.entity.SocialTask;
import job.search.kg.service.admin.AdminSocialTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/social-tasks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSocialTaskController {

    private final AdminSocialTaskService adminSocialTaskService;

    @GetMapping
    public ResponseEntity<List<SocialTask>> getAllTasks() {
        List<SocialTask> tasks = adminSocialTaskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocialTask> getTaskById(@PathVariable Integer id) {
        SocialTask task = adminSocialTaskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<SocialTask> createTask(@RequestBody CreateSocialTaskRequest request) {
        SocialTask task = adminSocialTaskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocialTask> updateTask(
            @PathVariable Integer id,
            @RequestBody CreateSocialTaskRequest request) {
        SocialTask task = adminSocialTaskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        adminSocialTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/completions")
    public ResponseEntity<Long> countCompletions(@PathVariable Integer id) {
        Long count = adminSocialTaskService.countCompletions(id);
        return ResponseEntity.ok(count);
    }
}