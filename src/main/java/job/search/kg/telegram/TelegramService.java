package job.search.kg.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public void sendMessage(Long chatId, String text) {
        String url = TELEGRAM_API_URL + botToken + "/sendMessage";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("text", text);
        request.put("parse_mode", "HTML");

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("Message sent to user {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send message to user {}: {}", chatId, e.getMessage());
        }
    }

    public boolean checkSubscription(Long userId, String channelId) {
        String url = TELEGRAM_API_URL + botToken + "/getChatMember";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", channelId);
        request.put("user_id", userId);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                String status = (String) result.get("status");

                return "member".equals(status) ||
                        "administrator".equals(status) ||
                        "creator".equals(status);
            }
        } catch (Exception e) {
            log.error("Failed to check subscription for user {} in channel {}: {}",
                    userId, channelId, e.getMessage());
        }

        return false;
    }

    public void deleteMessage(Long chatId, Integer messageId) {
        String url = TELEGRAM_API_URL + botToken + "/deleteMessage";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", chatId);
        request.put("message_id", messageId);

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("Message {} deleted for user {}", messageId, chatId);
        } catch (Exception e) {
            log.error("Failed to delete message {} for user {}: {}", messageId, chatId, e.getMessage());
        }
    }
}
