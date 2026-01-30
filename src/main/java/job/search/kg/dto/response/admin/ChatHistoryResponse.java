package job.search.kg.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    private List<ChatMessageDTO> messages;
    private int currentPage;
    private int totalPages;
    private long totalMessages;
}
