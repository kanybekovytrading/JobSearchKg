package job.search.kg.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveMessageRequest {
    private Long telegramUserId;
    private String senderType; // "USER" или "BOT"
    private String messageText;
    private Integer telegramMessageId;
    private String messageType; // "TEXT", "COMMAND", "CONTACT" и т.д.
}
