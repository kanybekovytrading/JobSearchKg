package job.search.kg.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import job.search.kg.dto.request.admin.BroadcastRequest;
import job.search.kg.service.admin.BroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/broadcast")
@Tag(name = "Admin Broadcast", description = "Рассылка сообщений всем пользователям бота")
@RequiredArgsConstructor
public class AdminBroadcastController {

    private final BroadcastService broadcastService;

    @Operation(summary = "Отправить сообщение всем пользователям бота")
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendBroadcast(@RequestBody BroadcastRequest request) {
        log.info("Broadcast requested: title={}", request.getTitle());
        BroadcastService.BroadcastResult result = broadcastService.sendToAll(request);
        return ResponseEntity.ok(Map.of(
                "total", result.total(),
                "sent", result.sent(),
                "failed", result.failed()
        ));
    }
}
