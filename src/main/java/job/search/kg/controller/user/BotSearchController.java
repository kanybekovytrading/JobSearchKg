package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import job.search.kg.dto.request.user.SearchRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.SearchResultResponse;
import job.search.kg.service.user.BotSearchService;
import job.search.kg.service.user.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotSearchController {

    private final BotSearchService botSearchService;
    private final RecommendationService recommendationService;

    @Operation(
            summary = "Получить рекомендованные вакансии",
            description = "Рекомендации вакансий на основе резюме пользователя. " +
                    "Если у пользователя есть резюме, вакансии фильтруются по подкатегориям и городу из резюме. " +
                    "Алгоритм учитывает: свежесть (25%), вовлеченность CTR (20%), " +
                    "совпадение города (25%), совпадение подкатегории (30%), активный Boost (20%)"
    )
    @GetMapping("/recommended/vacancies")
    public ResponseEntity<List<VacancyResponse>> getRecommendedVacancies(
            @RequestParam Long telegramId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getRecommendedVacancies(telegramId, limit));
    }

    @Operation(
            summary = "Поиск резюме",
            description = "Поиск резюме по параметрам: город, категория, подкатегория. " +
                    "Результаты отсортированы: сначала с активным Boost, затем по дате создания."
    )
    @PostMapping("/search/resumes")
    public ResponseEntity<SearchResultResponse<ResumeResponse>> searchResumes(
            @RequestParam Long telegramId,
            @RequestBody SearchRequest request) {
        return ResponseEntity.ok(botSearchService.searchResumes(telegramId, request));
    }

    @Operation(
            summary = "Поиск вакансий",
            description = "Поиск вакансий по параметрам: город, сфера, категория, подкатегория. " +
                    "Результаты отсортированы: сначала с активным Boost, затем по дате создания."
    )
    @PostMapping("/search/vacancies")
    public ResponseEntity<SearchResultResponse<VacancyResponse>> searchVacancies(
            @RequestParam Long telegramId,
            @RequestBody SearchRequest request,
            @RequestParam(required = false)  Double userLatitude,
            @RequestParam(required = false)  Double userLongitude) {
        return ResponseEntity.ok(botSearchService.searchVacancies(telegramId, request,  userLatitude, userLongitude));
    }
}