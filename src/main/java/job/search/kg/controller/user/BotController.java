package job.search.kg.controller.user;
import job.search.kg.dto.request.user.SaveMessageRequest;
import job.search.kg.dto.response.admin.ChatHistoryResponse;
import job.search.kg.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotController {

    private final ChatMessageService chatMessageService;

    /**
     * Сохранение сообщения от пользователя или бота
     * POST /api/bot/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<Void> saveMessage(@RequestBody SaveMessageRequest request) {
        chatMessageService.saveMessage(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/chat")
    public ResponseEntity<ChatHistoryResponse> getUserChatHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ChatHistoryResponse response = chatMessageService.getUserChatHistory(userId, page, size);
        return ResponseEntity.ok(response);
    }
}