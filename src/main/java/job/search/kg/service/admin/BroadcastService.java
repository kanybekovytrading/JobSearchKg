package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.BroadcastRequest;
import job.search.kg.entity.User;
import job.search.kg.repo.UserRepository;
import job.search.kg.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastService {

    private final UserRepository userRepository;
    private final TelegramService telegramService;

    public BroadcastResult sendToAll(BroadcastRequest req) {
        List<User> users = userRepository.findAll();
        String text = buildMessage(req);

        AtomicInteger sent = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        for (User user : users) {
            if (user.getTelegramId() == null) continue;
            try {
                telegramService.sendMessageWithButton(
                        user.getTelegramId(),
                        text,
                        req.getButtonText(),
                        req.getButtonUrl()
                );
                sent.incrementAndGet();
            } catch (Exception e) {
                log.warn("Broadcast failed for user {}: {}", user.getTelegramId(), e.getMessage());
                failed.incrementAndGet();
            }
            // небольшая пауза чтобы не словить flood limit (30 msg/sec)
            try { Thread.sleep(35); } catch (InterruptedException ignored) {}
        }

        log.info("Broadcast complete: sent={}, failed={}", sent.get(), failed.get());
        return new BroadcastResult(sent.get(), failed.get(), users.size());
    }

    private String buildMessage(BroadcastRequest req) {
        StringBuilder sb = new StringBuilder();

        // Заголовок
        if (req.getEmoji() != null && !req.getEmoji().isBlank()) {
            sb.append(req.getEmoji()).append("\n");
        }
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            sb.append("<b>").append(req.getTitle()).append("</b>\n");
        }
        if (req.getSubtitle() != null && !req.getSubtitle().isBlank()) {
            sb.append("<i>").append(req.getSubtitle()).append("</i>\n");
        }

        // Тело
        if (req.getMessage() != null && !req.getMessage().isBlank()) {
            sb.append("\n").append(req.getMessage());
        }

        // Футер
        if (req.getFooter() != null && !req.getFooter().isBlank()) {
            sb.append("\n\n─────────────────\n");
            sb.append("<i>").append(req.getFooter()).append("</i>");
        }

        return sb.toString();
    }

    public record BroadcastResult(int sent, int failed, int total) {}
}
