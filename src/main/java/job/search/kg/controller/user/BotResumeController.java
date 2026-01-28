package job.search.kg.controller.user;

import job.search.kg.dto.request.user.CreateResumeRequest;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatsResponse;
import job.search.kg.entity.Resume;
import job.search.kg.service.user.BotResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot/resumes")
@RequiredArgsConstructor
public class BotResumeController {

    private final BotResumeService botResumeService;

    @PostMapping
    public ResponseEntity<Resume> createResume(
            @RequestParam Long telegramId,
            @RequestBody CreateResumeRequest request) {
        Resume resume = botResumeService.createResume(telegramId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resume);
    }

    @GetMapping("/user/{telegramId}")
    public ResponseEntity<List<ResumeResponse>> getUserResumes(@PathVariable Long telegramId) {
        List<ResumeResponse> resumes = botResumeService.getUserResumes(telegramId);
        return ResponseEntity.ok(resumes);
    }

    @PutMapping("/{resumeId}/status")
    public ResponseEntity<Resume> updateResumeStatus(
            @PathVariable Long resumeId,
            @RequestParam Long telegramId,
            @RequestParam Boolean isActive) {
        Resume resume = botResumeService.updateResumeStatus(resumeId, telegramId, isActive);
        return ResponseEntity.ok(resume);
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long resumeId,
            @RequestParam Long telegramId) {
        botResumeService.deleteResume(resumeId, telegramId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{telegramId}/stats")
    public ResponseEntity<ResumeStatsResponse> getUserResumeStats(@PathVariable Long telegramId) {
        ResumeStatsResponse stats = botResumeService.getUserResumeStats(telegramId);
        return ResponseEntity.ok(stats);
    }
}