package job.search.kg.dto.response.admin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceDTO {
    private Long userId;
    private Long telegramId;
    private Integer balance;
    private Integer earned;      // Заработано
    private Integer spent;       // Потрачено
    private LocalDateTime createdAt;
}