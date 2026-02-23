package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.Vacancy;
import job.search.kg.repo.FreeAccessTrackingRepository;
import job.search.kg.service.admin.AdminVacancyService;
import job.search.kg.service.user.BotAccessService;
import job.search.kg.service.user.BotSearchService;
import job.search.kg.service.user.BotVacancyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/bot/vacancies")
@RequiredArgsConstructor
public class BotVacancyController {

    private final BotVacancyService botVacancyService;
    private final AdminVacancyService adminVacancyService;
    private final BotSearchService botSearchService;
    private final BotAccessService botAccessService;
    private final BotVacancyService vacancyService;
    private final FreeAccessTrackingRepository freeAccessTrackingRepository;


    @PostMapping
    public ResponseEntity<Vacancy> createVacancy(
            @RequestParam Long telegramId,
            @Valid @RequestBody CreateVacancyRequest request) {
        Vacancy vacancy = botVacancyService.createVacancy(telegramId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancy);
    }

    @GetMapping("/user/{telegramId}")
    public ResponseEntity<List<VacancyResponse>> getUserVacancies(@PathVariable Long telegramId) {
        List<VacancyResponse> vacancies = botVacancyService.getUserVacancies(telegramId);
        return ResponseEntity.ok(vacancies);
    }

    @DeleteMapping("/{vacancyId}")
    public ResponseEntity<Void> deleteVacancy(
            @PathVariable Long vacancyId,
            @RequestParam Long telegramId) {
        botVacancyService.deleteVacancy(vacancyId, telegramId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{telegramId}/stats")
    public ResponseEntity<VacancyStatsResponse> getUserVacancyStats(@PathVariable Long telegramId) {
        VacancyStatsResponse stats = botVacancyService.getUserVacancyStats(telegramId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{vacancyId}/status")
    public ResponseEntity<Vacancy> updateVacancyStatus(
            @PathVariable Long vacancyId,
            @RequestParam Long telegramId,
            @RequestParam Boolean isActive) {
        Vacancy vacancy = botVacancyService.updateVacancyStatus(vacancyId, telegramId, isActive);
        return ResponseEntity.ok(vacancy);
    }

    @GetMapping("/{vacancyId}/{telegramId}")
    public ResponseEntity<VacancyResponse> getVacancyById(
            @PathVariable Long vacancyId,
            @PathVariable Long telegramId,
            @RequestParam boolean isProfile) {

        Vacancy vacancy = adminVacancyService.getVacancyById(vacancyId);
        List<MediaResponse> mediaResponses = botVacancyService.getVacancyMedia(vacancyId);

        VacancyResponse response;

        if (!isProfile && !botAccessService.canSearchJobs(telegramId)) {

            boolean isFree = freeAccessTrackingRepository
                    .existsByTelegramIdAndEntityIdAndDate(telegramId, vacancyId, LocalDate.now(), "VACANCY");

            if (isFree) {
                response = botSearchService.mapVacancyToResponse(vacancy, null, null, true, false);
            } else {
                response = botSearchService.mapVacancyToResponseWithoutSubs(vacancy, null, null, false, false);
            }

        } else {
            response = botSearchService.mapVacancyToResponse(vacancy, null, null, false, false);
        }

        response.setMedia(mediaResponses);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{vacancyId}/update/{telegramId}")
    public ResponseEntity<Vacancy> updateVacancy(
            @PathVariable Long vacancyId,
            @PathVariable Long telegramId,
            @Valid @RequestBody CreateVacancyRequest request) {
        return ResponseEntity.ok(botVacancyService.updateVacancy(vacancyId, telegramId, request));
    }

    @PostMapping(value = "/{vacancyId}/media/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadMediaBatch(
            @PathVariable Long vacancyId,
            @RequestParam("telegramId") Long telegramId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        try {
            // Читаем байты ДО того как вернём ответ — потом файлы недоступны
            List<FileData> fileDataList = files.stream()
                    .map(file -> {
                        try {
                            return new FileData(file.getBytes(), file.getOriginalFilename(),
                                    file.getContentType(), file.getSize());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            // Запускаем в фоне и сразу отвечаем
            vacancyService.addVacancyMediaBatchAsync(vacancyId, telegramId, fileDataList);
            return ResponseEntity.accepted().body(Map.of("message", "Файлы загружаются"));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Удалить медиа файл
     * DELETE /api/bot/vacancies/media/{mediaId}
     */
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long mediaId,
            @RequestParam("telegramId") Long telegramId
    ) {
        try {
            vacancyService.deleteVacancyMedia(mediaId, telegramId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    public record FileData(byte[] bytes, String fileName, String contentType, long size) {}

}
