package job.search.kg.service.user;

import job.search.kg.entity.User;
import job.search.kg.entity.Vacancy;
import job.search.kg.entity.VacancyReview;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.UserRepository;
import job.search.kg.repo.VacancyRepository;
import job.search.kg.repo.VacancyReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyReviewService {

    private final VacancyReviewRepository reviewRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;

    /**
     * Оставить отзыв на вакансию
     */
    @Transactional
    public VacancyReview createReview(
            Long telegramId,
            Long vacancyId,
            Integer rating,
            String comment
    ) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy not found"));

        // Проверка: рейтинг от 1 до 5
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Проверка: пользователь не может оценивать свою вакансию
        if (vacancy.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You cannot review your own vacancy");
        }

        // Проверка на дубликаты
        if (reviewRepository.existsByVacancyIdAndUserId(vacancyId, user.getId())) {
            throw new IllegalStateException("You have already reviewed this vacancy");
        }

        VacancyReview review = VacancyReview.builder()
                .vacancy(vacancy)
                .user(user)
                .rating(rating)
                .comment(comment)
                .isSpam(false)
                .build();

        return reviewRepository.save(review);
    }

    /**
     * Обновить отзыв
     */
    @Transactional
    public VacancyReview updateReview(
            Long telegramId,
            Long reviewId,
            Integer rating,
            String comment
    ) {
        VacancyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Проверка: только автор может редактировать
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only edit your own reviews");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    /**
     * Удалить отзыв
     */
    @Transactional
    public void deleteReview(Long telegramId, Long reviewId) {
        VacancyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    /**
     * Пометить отзыв как спам
     */
    @Transactional
    public VacancyReview markAsSpam(Long telegramId, Long reviewId) {
        VacancyReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Только владелец вакансии может помечать как спам
        if (!review.getVacancy().getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Only vacancy owner can mark reviews as spam");
        }

        review.setIsSpam(true);
        return reviewRepository.save(review);
    }

    /**
     * Получить отзывы на вакансию
     */
    @Transactional(readOnly = true)
    public List<VacancyReview> getVacancyReviews(Long vacancyId, boolean includeSpam) {
        if (includeSpam) {
            return reviewRepository.findByVacancyIdOrderByCreatedAtDesc(vacancyId);
        } else {
            return reviewRepository.findByVacancyIdAndIsSpamFalseOrderByCreatedAtDesc(vacancyId);
        }
    }

    /**
     * Получить средний рейтинг вакансии
     */
    @Transactional(readOnly = true)
    public VacancyRatingInfo getVacancyRating(Long vacancyId) {
        List<VacancyReview> reviews = reviewRepository
                .findByVacancyIdAndIsSpamFalseOrderByCreatedAtDesc(vacancyId);

        if (reviews.isEmpty()) {
            return VacancyRatingInfo.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }

        double average = reviews.stream()
                .mapToInt(VacancyReview::getRating)
                .average()
                .orElse(0.0);

        return VacancyRatingInfo.builder()
                .averageRating(Math.round(average * 10.0) / 10.0)
                .totalReviews(reviews.size())
                .build();
    }

    /**
     * Получить отзывы пользователя
     */
    @Transactional(readOnly = true)
    public List<VacancyReview> getUserReviews(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @lombok.Data
    @lombok.Builder
    public static class VacancyRatingInfo {
        private Double averageRating;
        private Integer totalReviews;
    }
}
