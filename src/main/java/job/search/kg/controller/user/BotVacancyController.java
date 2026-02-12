package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.MediaResponse;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.Vacancy;
import job.search.kg.service.admin.AdminVacancyService;
import job.search.kg.service.user.BotAccessService;
import job.search.kg.service.user.BotSearchService;
import job.search.kg.service.user.BotVacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bot/vacancies")
@RequiredArgsConstructor
public class BotVacancyController {

    private final BotVacancyService botVacancyService;
    private final AdminVacancyService adminVacancyService;
    private final BotSearchService botSearchService;
    private final BotAccessService botAccessService;
    private final BotVacancyService vacancyService;


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
    public ResponseEntity<job.search.kg.dto.response.VacancyResponse> getVacancyById(
            @PathVariable Long vacancyId,  @PathVariable Long telegramId) {
        Vacancy vacancy = adminVacancyService.getVacancyById(vacancyId);
       List<MediaResponse> mediaResponses = botVacancyService.getVacancyMedia(vacancyId);
        VacancyResponse response;
        if (!botAccessService.canSearchEmployees(telegramId)) {
          response =  botSearchService.mapVacancyToResponseWithoutSubs(vacancy);
        }else {
           response =  botSearchService.mapVacancyToResponse(vacancy);
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

    /**
     * Получить все медиа файлы вакансии
     * GET /api/bot/vacancies/{vacancyId}/media
     */
    @GetMapping("/{vacancyId}/media")
    public ResponseEntity<List<MediaResponse>> getVacancyMedia(@PathVariable Long vacancyId) {
        List<MediaResponse> media = vacancyService.getVacancyMedia(vacancyId);
        return ResponseEntity.ok(media);
    }

    /**
     * Загрузить фото к вакансии
     * POST /api/bot/vacancies/{vacancyId}/media/photo
     */
    @PostMapping(value="/{vacancyId}/media/photo",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadPhoto(
            @PathVariable Long vacancyId,
            @RequestParam("telegramId") Long telegramId,
            @Parameter(
                    description = "Фото файл (jpg, png, max 10MB)",
                    required = true
            )
            @RequestPart("file") MultipartFile file
    ) {
        try {
            MediaResponse response = vacancyService.addVacancyPhoto(vacancyId, telegramId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Загрузить видео к вакансии
     * POST /api/bot/vacancies/{vacancyId}/media/video
     */
    @PostMapping(value = "/{vacancyId}/media/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadVideo(
            @PathVariable Long vacancyId,
            @RequestParam("telegramId") Long telegramId,
            @Parameter(
                    description = "Видео файл (mp4, mov, max 100MB)",
                    required = true
            )
            @RequestPart("file") MultipartFile file
    ) {
        try {
            MediaResponse response = vacancyService.addVacancyVideo(vacancyId, telegramId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    /**
     * Изменить порядок отображения медиа
     * PATCH /api/bot/vacancies/media/{mediaId}/order
     */
    @PatchMapping("/media/{mediaId}/order")
    public ResponseEntity<Void> updateMediaOrder(
            @PathVariable Long mediaId,
            @RequestParam("telegramId") Long telegramId,
            @RequestParam("newOrder") Integer newOrder
    ) {
        vacancyService.updateMediaOrder(mediaId, telegramId, newOrder);
        return ResponseEntity.ok().build();
    }

}
