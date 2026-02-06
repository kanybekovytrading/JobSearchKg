package job.search.kg.controller;

import io.swagger.v3.oas.annotations.Operation;
import job.search.kg.controller.user.BotSearchController;
import job.search.kg.dto.response.user.ResumeStatisticsResponse;
import job.search.kg.dto.response.user.VacancyStatisticsResponse;
import job.search.kg.service.user.BotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistic")
@RequiredArgsConstructor
public class StatisticController {

    private final BotSearchService searchService;

    @Operation(
            summary = "Отметить просмотр вакансии",
            description = "Увеличивает счетчик просмотров вакансии на 1"
    )
    @PostMapping("/vacancies/{vacancyId}/view")
    public ResponseEntity<Void> trackVacancyView(@PathVariable Long vacancyId) {
        searchService.trackVacancyView(vacancyId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Отметить клик по контактам вакансии",
            description = "Увеличивает счетчик кликов по контактам вакансии на 1"
    )
    @PostMapping("/vacancies/{vacancyId}/contact-click")
    public ResponseEntity<Void> trackVacancyContactClick(@PathVariable Long vacancyId) {
        searchService.trackVacancyContactClick(vacancyId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Отметить просмотр резюме",
            description = "Увеличивает счетчик просмотров резюме на 1"
    )
    @PostMapping("/resumes/{resumeId}/view")
    public ResponseEntity<Void> trackResumeView(@PathVariable Long resumeId) {
        searchService.trackResumeView(resumeId);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "Отметить клик по контактам резюме",
            description = "Увеличивает счетчик кликов по контактам резюме на 1"
    )
    @PostMapping("/resumes/{resumeId}/contact-click")
    public ResponseEntity<Void> trackResumeContactClick(@PathVariable Long resumeId) {
        searchService.trackResumeContactClick(resumeId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получить статистику вакансии",
            description = "Возвращает статистику просмотров, кликов и откликов для конкретной вакансии"
    )
    @GetMapping("/vacancies/{vacancyId}")
    public ResponseEntity<VacancyStatisticsResponse> getVacancyStatistics(@PathVariable Long vacancyId) {
        VacancyStatisticsResponse statistics = searchService.getVacancyStatistics(vacancyId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Получить статистику резюме",
            description = "Возвращает статистику просмотров, кликов и приглашений для конкретного резюме"
    )
    @GetMapping("/resumes/{resumeId}")
    public ResponseEntity<ResumeStatisticsResponse> getResumeStatistics(@PathVariable Long resumeId) {
        ResumeStatisticsResponse statistics = searchService.getResumeStatistics(resumeId);
        return ResponseEntity.ok(statistics);
    }

}
