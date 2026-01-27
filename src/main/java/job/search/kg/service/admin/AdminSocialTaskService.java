package job.search.kg.service.admin;

import job.search.kg.dto.request.admin.CreateSocialTaskRequest;
import job.search.kg.entity.SocialTask;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.SocialTaskRepository;
import job.search.kg.repo.UserTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSocialTaskService {

    private final SocialTaskRepository taskRepository;
    private final UserTaskRepository userTaskRepository;

    @Transactional(readOnly = true)
    public List<SocialTask> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SocialTask getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    @Transactional
    public SocialTask createTask(CreateSocialTaskRequest request) {
        SocialTask task = new SocialTask();
        task.setType(request.getType());
        task.setTitle(request.getTitle());
        task.setLink(request.getLink());
        task.setChannelId(request.getChannelId());
        task.setReward(request.getReward());
        task.setIsActive(true);

        return taskRepository.save(task);
    }

    @Transactional
    public SocialTask updateTask(Integer id, CreateSocialTaskRequest request) {
        SocialTask task = getTaskById(id);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getLink() != null) {
            task.setLink(request.getLink());
        }
        if (request.getChannelId() != null) {
            task.setChannelId(request.getChannelId());
        }
        if (request.getReward() != null) {
            task.setReward(request.getReward());
        }
        if (request.getIsActive() != null) {
            task.setIsActive(request.getIsActive());
        }

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Integer id) {
        SocialTask task = getTaskById(id);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Long countCompletions(Integer taskId) {
        SocialTask task = getTaskById(taskId);
        return userTaskRepository.countByTask(task);
    }
}
