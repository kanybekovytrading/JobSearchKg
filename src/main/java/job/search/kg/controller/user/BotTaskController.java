package job.search.kg.controller.user;
import job.search.kg.dto.request.user.CompleteTaskRequest;
import job.search.kg.dto.response.user.TaskListResponse;
import job.search.kg.entity.UserTask;
import job.search.kg.service.user.BotTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/tasks")
@RequiredArgsConstructor
public class BotTaskController {

    private final BotTaskService botTaskService;

    @GetMapping("/{telegramId}")
    public ResponseEntity<TaskListResponse> getAvailableTasks(@PathVariable Long telegramId) {
        TaskListResponse tasks = botTaskService.getAvailableTasks(telegramId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/complete")
    public ResponseEntity<UserTask> completeTask(
            @RequestParam Long telegramId,
            @RequestBody CompleteTaskRequest request) {
        UserTask userTask = botTaskService.completeTask(telegramId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userTask);
    }

    @GetMapping("/{telegramId}/check/{taskId}")
    public ResponseEntity<Boolean> isTaskCompleted(
            @PathVariable Long telegramId,
            @PathVariable Integer taskId) {
        boolean completed = botTaskService.isTaskCompleted(telegramId, taskId);
        return ResponseEntity.ok(completed);
    }
}
