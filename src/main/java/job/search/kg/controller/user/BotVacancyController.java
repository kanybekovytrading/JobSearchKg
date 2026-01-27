package job.search.kg.controller.user;

import job.search.kg.dto.request.user.CreateVacancyRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.entity.Vacancy;
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
}
