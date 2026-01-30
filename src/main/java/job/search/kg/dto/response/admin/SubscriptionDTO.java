package job.search.kg.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private String status; // "Активна" / "Неактивна"
    private String subscriptionHistory; // "1 неделя"
    private LocalDateTime subscriptionDate;
    private Integer totalMessages;
}
