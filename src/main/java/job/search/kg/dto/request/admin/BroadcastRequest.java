package job.search.kg.dto.request.admin;

import lombok.Data;

@Data
public class BroadcastRequest {
    private String emoji;       // 🚀
    private String title;       // Важное обновление!
    private String subtitle;    // От создателя этого бота
    private String message;     // тело сообщения
    private String buttonText;  // Подписаться → (опционально)
    private String buttonUrl;   // https://instagram.com/... (опционально)
    private String footer;      // Спасибо, что ты с нами ❤️ (опционально)
}
