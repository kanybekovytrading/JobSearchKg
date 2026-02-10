package job.search.kg.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import job.search.kg.entity.VacancyReview;
import job.search.kg.service.user.VacancyReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bot/vacancy-feedback")
@RequiredArgsConstructor
public class BotVacancyFeedbackController {

    private final VacancyReviewService reviewService;

    @Operation(
            summary = "Оставить отзыв на вакансию",
            description = "Оставить рейтинг (1-5 звезд) и комментарий на вакансию. " +
                    "Нельзя оценивать свою вакансию и оставлять повторные отзывы."
    )
    @PostMapping("/vacancies/{vacancyId}/reviews")
    public ResponseEntity<VacancyReview> createReview(
            @RequestParam Long telegramId,
            @PathVariable Long vacancyId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment
    ) {
        return ResponseEntity.ok(
                reviewService.createReview(telegramId, vacancyId, rating, comment)
        );
    }

    @Operation(
            summary = "Обновить отзыв",
            description = "Редактировать свой отзыв (только автор может редактировать)"
    )
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<VacancyReview> updateReview(
            @RequestParam Long telegramId,
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(telegramId, reviewId, rating, comment)
        );
    }

    @Operation(
            summary = "Удалить отзыв",
            description = "Удалить свой отзыв (только автор может удалить)"
    )
    @ApiResponse(responseCode = "200", description = "Отзыв удален")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @RequestParam Long telegramId,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(telegramId, reviewId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Пометить отзыв как спам",
            description = "Пометить отзыв как спам (только владелец вакансии)"
    )
    @ApiResponse(responseCode = "200", description = "Отзыв помечен как спам")
    @PostMapping("/reviews/{reviewId}/spam")
    public ResponseEntity<VacancyReview> markReviewAsSpam(
            @RequestParam Long telegramId,
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.markAsSpam(telegramId, reviewId));
    }

    @Operation(
            summary = "Получить отзывы вакансии",
            description = "Получить все отзывы на вакансию с возможностью фильтрации спама"
    )
    @ApiResponse(responseCode = "200", description = "Список отзывов получен")
    @GetMapping("/vacancies/{vacancyId}/reviews")
    public ResponseEntity<List<VacancyReview>> getVacancyReviews(
            @PathVariable Long vacancyId,
            @RequestParam(defaultValue = "false") boolean includeSpam
    ) {
        return ResponseEntity.ok(reviewService.getVacancyReviews(vacancyId, includeSpam));
    }

    @Operation(
            summary = "Получить средний рейтинг вакансии",
            description = "Получить средний рейтинг и общее количество отзывов на вакансию"
    )
    @ApiResponse(responseCode = "200", description = "Рейтинг получен")
    @GetMapping("/vacancies/{vacancyId}/rating")
    public ResponseEntity<VacancyReviewService.VacancyRatingInfo> getVacancyRating(
            @PathVariable Long vacancyId
    ) {
        return ResponseEntity.ok(reviewService.getVacancyRating(vacancyId));
    }

}
