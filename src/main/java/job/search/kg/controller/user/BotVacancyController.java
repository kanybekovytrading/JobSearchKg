package job.search.kg.controller.user;

import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.VacancyStatsResponse;
import job.search.kg.entity.Vacancy;
import job.search.kg.mapper.VacancyMapper;
import job.search.kg.service.admin.AdminVacancyService;
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
    private final VacancyMapper vacancyMapper;

    @PostMapping
    public ResponseEntity<Vacancy> createVacancy(
            @RequestParam Long telegramId,
            @RequestBody CreateVacancyRequest request) {
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
            @RequestParam Long telegramId){
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

    @GetMapping("/{vacancyId}")
    public ResponseEntity<job.search.kg.dto.response.admin.VacancyResponse> getVacancyById(
            @PathVariable Long vacancyId) {
        Vacancy vacancy = adminVacancyService.getVacancyById(vacancyId);
        job.search.kg.dto.response.admin.VacancyResponse response = vacancyMapper.toResponse(vacancy, vacancy.getUser().getLanguage());
        return ResponseEntity.ok(response);
    }
}
