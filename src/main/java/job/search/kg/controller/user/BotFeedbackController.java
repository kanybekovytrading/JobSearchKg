package job.search.kg.controller.user;

import job.search.kg.dto.request.user.FeedbackRequest;
import job.search.kg.entity.Feedback;
import job.search.kg.service.user.BotFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/feedback")
@RequiredArgsConstructor
public class BotFeedbackController {

    private final BotFeedbackService botFeedbackService;

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(
            @RequestParam Long telegramId,
            @RequestBody FeedbackRequest request) {
        Feedback feedback = botFeedbackService.createFeedback(telegramId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }
}
