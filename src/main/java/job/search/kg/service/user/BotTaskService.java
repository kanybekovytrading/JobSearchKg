package job.search.kg.service.user;

import job.search.kg.dto.request.user.CompleteTaskRequest;
import job.search.kg.dto.response.user.TaskListResponse;
import job.search.kg.entity.PointsTransaction;
import job.search.kg.entity.SocialTask;
import job.search.kg.entity.User;
import job.search.kg.entity.UserTask;
import job.search.kg.telegram.TelegramService;
import job.search.kg.exceptions.ResourceNotFoundException;
import job.search.kg.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotTaskService {

    private final SocialTaskRepository taskRepository;
    private final UserTaskRepository userTaskRepository;
    private final UserRepository userRepository;
    private final BotPointsService pointsService;
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public TaskListResponse getAvailableTasks(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<SocialTask> allTasks = taskRepository.findByIsActive(true);
        List<UserTask> completedTasks = userTaskRepository.findByUser(user);

        List<Long> completedTaskIds = completedTasks.stream()
                .map(ut -> ut.getTask().getId().longValue())
                .collect(Collectors.toList());

        List<SocialTask> availableTasks = allTasks.stream()
                .filter(task -> !completedTaskIds.contains(task.getId().longValue()))
                .collect(Collectors.toList());

        TaskListResponse response = new TaskListResponse();
        response.setAvailableTasks(availableTasks);
        response.setCompletedTasks(completedTasks);

        return response;
    }

    @Transactional
    public UserTask completeTask(Long telegramId, CompleteTaskRequest request) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SocialTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Проверка, не выполнено ли уже
        if (userTaskRepository.existsByUserAndTask(user, task)) {
            throw new IllegalStateException("Task already completed");
        }

        // Проверка подписки для Telegram
        if (task.getType() == SocialTask.TaskType.TELEGRAM) {
            boolean isSubscribed = telegramService.checkSubscription(telegramId, task.getChannelId());
            if (!isSubscribed) {
                throw new IllegalStateException("Not subscribed to channel");
            }
        }

        // Создание записи о выполнении
        UserTask userTask = new UserTask();
        userTask.setUser(user);
        userTask.setTask(task);
        userTask.setRewardGiven(task.getReward());
        userTaskRepository.save(userTask);

        // Начисление баллов
        pointsService.addPoints(
                telegramId,
                task.getReward(),
                PointsTransaction.TransactionType.TASK,
                "Задание: " + task.getTitle()
        );

        return userTask;
    }

    @Transactional(readOnly = true)
    public boolean isTaskCompleted(Long telegramId, Integer taskId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SocialTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return userTaskRepository.existsByUserAndTask(user, task);
    }
}
