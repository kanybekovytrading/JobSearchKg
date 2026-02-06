package job.search.kg.controller;

import io.swagger.v3.oas.annotations.Operation;
import job.search.kg.controller.user.BotSearchController;
import job.search.kg.service.user.BotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
