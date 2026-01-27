package job.search.kg.dto.response.user;

import job.search.kg.entity.SocialTask;
import job.search.kg.entity.UserTask;
import lombok.Data;

import java.util.List;

@Data
public class TaskListResponse {
    private List<SocialTask> availableTasks;
    private List<UserTask> completedTasks;
}
