package job.search.kg.controller.user;

import jakarta.validation.Valid;
import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.Vacancy;
import job.search.kg.service.admin.AdminVacancyService;
import job.search.kg.service.user.BotAccessService;
import job.search.kg.service.user.BotSearchService;
import job.search.kg.service.user.BotVacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot/vacancies")
@RequiredArgsConstructor
public class BotVacancyController {

    private final BotVacancyService botVacancyService;
    private final AdminVacancyService adminVacancyService;
    private final BotSearchService botSearchService;
    private final BotAccessService botAccessService;

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
        VacancyResponse response;
        if (!botAccessService.canSearchEmployees(telegramId)) {
          response =  botSearchService.mapVacancyToResponseWithoutSubs(vacancy);
        }else {
           response =  botSearchService.mapVacancyToResponse(vacancy);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{vacancyId}/update/{telegramId}")
    public ResponseEntity<Vacancy> updateVacancy(
            @PathVariable Long vacancyId,
            @PathVariable Long telegramId,
            @Valid @RequestBody CreateVacancyRequest request) {
        return ResponseEntity.ok(botVacancyService.updateVacancy(vacancyId, telegramId, request));
    }

}
