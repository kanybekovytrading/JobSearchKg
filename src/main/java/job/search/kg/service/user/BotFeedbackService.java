package job.search.kg.service.user;

import job.search.kg.dto.request.user.FeedbackRequest;
import job.search.kg.entity.Feedback;
import job.search.kg.entity.User;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.FeedbackRepository;
import job.search.kg.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final TelegramService telegramService;

    @Transactional
    public Feedback createFeedback(Long telegramId, FeedbackRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setMessage(request.getMessage());
        feedback.setStatus(Feedback.FeedbackStatus.PENDING);

        feedbackRepository.save(feedback);

        // Уведомление пользователю
        telegramService.sendMessage(
                telegramId,
                "✅ Спасибо! Ваше сообщение отправлено.\n\nМы свяжемся с вами в ближайшее время."
        );

        return feedback;
    }
}
