package job.search.kg.dto.request.admin;

import job.search.kg.entity.SocialTask;
import lombok.Data;

@Data
public class CreateSocialTaskRequest {
    private SocialTask.TaskType type;
    private String title;
    private String link;
    private String channelId; // Для Telegram
    private Integer reward;
    private Boolean isActive;
}
