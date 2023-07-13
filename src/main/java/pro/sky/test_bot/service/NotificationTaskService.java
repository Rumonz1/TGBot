package pro.sky.test_bot.service;

import org.springframework.stereotype.Service;
import pro.sky.test_bot.entity.NotificationTask;
import pro.sky.test_bot.repository.NotificationTaskRepository;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }
    public void save(NotificationTask notificationTask) {
        notificationTaskRepository.save(notificationTask);

    }
}
