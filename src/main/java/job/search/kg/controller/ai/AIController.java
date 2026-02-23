package job.search.kg.controller.ai;

import job.search.kg.ai.GeminiService;
import job.search.kg.dto.response.ai.CoverLetter;
import job.search.kg.dto.response.ai.ResumeAnalysis;
import job.search.kg.dto.response.ai.SalaryPrediction;
import job.search.kg.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bot/ai")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;

    @PostMapping("/analyze-resume/{resumeId}/{telegramId}")
    public ResponseEntity<ResumeAnalysis> analyzeResume(
            @PathVariable Long resumeId,
            @PathVariable Long telegramId
    ) {
        try {
            ResumeAnalysis analysis = geminiService.analyzeResume(resumeId, telegramId);
            return ResponseEntity.ok(analysis);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error analyzing resume", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/predict-salary/{vacancyId}/{telegramId}")
    public ResponseEntity<SalaryPrediction> predictSalary(@PathVariable Long vacancyId, @PathVariable Long telegramId) {
        return ResponseEntity.ok(geminiService.predictSalary(vacancyId, telegramId));
    }

    @PostMapping("/generate-cover-letter/{resumeId}/{vacancyId}/{telegramId}")
    public ResponseEntity<CoverLetter> generateCoverLetter(
            @PathVariable Long resumeId, @PathVariable Long vacancyId, @PathVariable Long telegramId
    ) {
        return ResponseEntity.ok(geminiService.generateCoverLetter(
                resumeId, vacancyId, telegramId
        ));
    }
}