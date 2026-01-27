package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.AnswerFeedbackRequest;
import job.search.kg.entity.Admin;
import job.search.kg.entity.Feedback;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.AdminRepository;
import job.search.kg.repo.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AdminRepository adminRepository;
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public Page<Feedback> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Feedback> getPendingFeedback() {
        return feedbackRepository.findByStatusOrderByCreatedAtDesc(Feedback.FeedbackStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found"));
    }

    @Transactional
    public Feedback answerFeedback(Long feedbackId, AnswerFeedbackRequest request, String adminEmail) {
        Feedback feedback = getFeedbackById(feedbackId);
        Admin admin = adminRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        feedback.setAdminResponse(request.getResponse());
        feedback.setAnsweredBy(admin);
        feedback.setStatus(Feedback.FeedbackStatus.ANSWERED);
        feedback.setAnsweredAt(LocalDateTime.now());

        feedbackRepository.save(feedback);

        // Отправка ответа пользователю
        telegramService.sendMessage(
                feedback.getUser().getTelegramId(),
                String.format(
                        "💬 Ответ администратора:\n\n" +
                                "Ваше сообщение:\n\"%s\"\n\n" +
                                "Ответ:\n%s",
                        feedback.getMessage(),
                        request.getResponse()
                )
        );

        return feedback;
    }

    @Transactional(readOnly = true)
    public Long countPendingFeedback() {
        return feedbackRepository.countByStatus(Feedback.FeedbackStatus.PENDING);
    }
}