package job.search.kg.controller.admin;

import job.search.kg.dto.request.admin.AnswerFeedbackRequest;
import job.search.kg.entity.Feedback;
import job.search.kg.service.admin.AdminFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    @GetMapping
    public ResponseEntity<Page<Feedback>> getAllFeedback(Pageable pageable) {
        Page<Feedback> feedback = adminFeedbackService.getAllFeedback(pageable);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Feedback>> getPendingFeedback() {
        List<Feedback> feedback = adminFeedbackService.getPendingFeedback();
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        Feedback feedback = adminFeedbackService.getFeedbackById(id);
        return ResponseEntity.ok(feedback);
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<Feedback> answerFeedback(
            @PathVariable Long id,
            @RequestBody AnswerFeedbackRequest request,
            Authentication authentication) {
        String adminEmail = authentication.getName();
        Feedback feedback = adminFeedbackService.answerFeedback(id, request, adminEmail);
        return ResponseEntity.ok(feedback);
    }
}
