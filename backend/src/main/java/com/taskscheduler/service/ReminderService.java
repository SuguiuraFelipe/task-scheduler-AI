package com.taskscheduler.service;

import com.taskscheduler.entity.ReminderLog;
import com.taskscheduler.entity.Task;
import com.taskscheduler.repository.ReminderLogRepository;
import com.taskscheduler.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    private final TaskRepository taskRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final EmailService emailService;

    @Transactional
    public void checkAndSendReminders() {
        log.info("Starting reminder check job");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayLater = now.plusDays(1);
        LocalDateTime oneHourLater = now.plusHours(1);

        // Check tasks due in 1 day (if not already sent)
        List<Task> tasksInOneDay = taskRepository.findTasksDueBetween(now, oneDayLater);
        for (Task task : tasksInOneDay) {
            if (!reminderLogRepository.existsByTaskIdAndReminderType(task.getId(), ReminderLog.ReminderType.DAY_BEFORE)) {
                sendReminder(task, ReminderLog.ReminderType.DAY_BEFORE);
            }
        }

        // Check tasks due in 1 hour (if not already sent)
        List<Task> tasksInOneHour = taskRepository.findTasksDueBetween(now, oneHourLater);
        for (Task task : tasksInOneHour) {
            if (!reminderLogRepository.existsByTaskIdAndReminderType(task.getId(), ReminderLog.ReminderType.HOUR_BEFORE)) {
                sendReminder(task, ReminderLog.ReminderType.HOUR_BEFORE);
            }
        }

        log.info("Reminder check job completed");
    }

    private void sendReminder(Task task, ReminderLog.ReminderType reminderType) {
        try {
            // Extract data while still inside the transaction to avoid LazyInitializationException
            // in the async email thread.
            emailService.sendReminderEmail(
                    task.getUser().getEmail(),
                    task.getUser().getName(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    task.getPriority(),
                    reminderType
            );

            ReminderLog reminderLog = new ReminderLog();
            reminderLog.setTask(task);
            reminderLog.setReminderType(reminderType);
            reminderLogRepository.save(reminderLog);

            log.info("Reminder scheduled for task: {} (type: {})", task.getId(), reminderType);
        } catch (Exception e) {
            log.error("Error scheduling reminder for task: {}", task.getId(), e);
        }
    }
}
