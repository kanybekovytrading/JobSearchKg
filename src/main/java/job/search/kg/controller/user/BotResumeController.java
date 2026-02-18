package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import job.search.kg.dto.request.user.CreateResumeRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.ResumeStatsResponse;
import job.search.kg.entity.Resume;
import job.search.kg.repo.FreeAccessTrackingRepository;
import job.search.kg.service.admin.AdminResumeService;
import job.search.kg.service.user.BotAccessService;
import job.search.kg.service.user.BotResumeService;
import job.search.kg.service.user.BotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bot/resumes")
@RequiredArgsConstructor
public class BotResumeController {

    private final BotResumeService botResumeService;
    private final AdminResumeService adminResumeService;
    private final BotSearchService botSearchService;
    private final BotAccessService botAccessService;
    private final BotResumeService resumeService;
    private final FreeAccessTrackingRepository freeAccessTrackingRepository;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resume> createResume(
            @RequestParam Long telegramId,
            @RequestPart("data") CreateResumeRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        Resume resume = botResumeService.createResume(telegramId, request, photo);
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

    @GetMapping("/{resumeId}/{telegramId}")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable Long resumeId,
            @PathVariable Long telegramId,
            @RequestParam boolean isProfile) {

        Resume resume = adminResumeService.getResumeById(resumeId);
        List<MediaResponse> mediaResponses = botResumeService.getResumeMedia(resumeId);

        ResumeResponse response;

        if (!isProfile && !botAccessService.canSearchEmployees(telegramId)) {

            boolean isFree = freeAccessTrackingRepository
                    .existsByTelegramIdAndEntityIdAndDate(telegramId, resumeId, LocalDate.now(), "RESUME");

            if (isFree) {
                response = botSearchService.mapResumeToResponse(resume, true, false);
            } else {
                response = botSearchService.mapResumeToResponseWithoutSubs(resume, false, false);
            }

        } else {
            response = botSearchService.mapResumeToResponse(resume, false, false);
        }

        response.setMedia(mediaResponses);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{resumeId}/update/{telegramId}")
    public ResponseEntity<Resume> updateResume(
            @PathVariable Long resumeId,
            @PathVariable Long telegramId,
            @Valid @RequestBody CreateResumeRequest request) {
        return ResponseEntity.ok(botResumeService.updateResume(resumeId, telegramId, request));
    }

    @PostMapping(value = "/{resumeId}/media/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadPhoto(
            @PathVariable Long resumeId,
            @RequestParam("telegramId") Long telegramId,
            @Parameter(
                    description = "Фото файл (jpg, png, max 10MB)",
                    required = true
            )
            @RequestPart("file") MultipartFile file
    ) {
        try {
            MediaResponse response = resumeService.addResumePhoto(resumeId, telegramId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Загрузить видео к резюме
     * POST /api/bot/resumes/{resumeId}/media/video
     */
    @PostMapping(value = "/{resumeId}/media/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadVideo(
            @PathVariable Long resumeId,
            @RequestParam("telegramId") Long telegramId,
            @Parameter(
                    description = "Видео файл (mp4, mov, max 100MB)",
                    required = true
            )
            @RequestPart("file") MultipartFile file
    ) {
        try {
            MediaResponse response = resumeService.addResumeVideo(resumeId, telegramId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить медиа файл
     * DELETE /api/bot/resumes/media/{mediaId}
     */
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long mediaId,
            @RequestParam("telegramId") Long telegramId
    ) {
        try {
            resumeService.deleteResumeMedia(mediaId, telegramId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}