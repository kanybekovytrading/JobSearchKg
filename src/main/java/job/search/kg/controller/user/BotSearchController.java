package job.search.kg.controller.user;

import job.search.kg.dto.request.user.SearchRequest;
import job.search.kg.dto.response.VacancyResponse;
import job.search.kg.dto.response.user.ResumeResponse;
import job.search.kg.dto.response.user.SearchResultResponse;
import job.search.kg.service.user.BotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot/search")
@RequiredArgsConstructor
public class BotSearchController {

    private final BotSearchService botSearchService;

    @PostMapping("/resumes")
    public ResponseEntity<SearchResultResponse<ResumeResponse>> searchResumes(
            @RequestParam Long telegramId,
            @RequestBody SearchRequest request) {
        SearchResultResponse<ResumeResponse> results = botSearchService.searchResumes(telegramId, request);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/vacancies")
    public ResponseEntity<SearchResultResponse<VacancyResponse>> searchVacancies(
            @RequestParam Long telegramId,
            @RequestBody SearchRequest request) {
        SearchResultResponse<VacancyResponse> results = botSearchService.searchVacancies(telegramId, request);
        return ResponseEntity.ok(results);
    }
}