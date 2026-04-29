package com.taskscheduler.repository;

import com.taskscheduler.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    boolean existsByTaskIdAndReminderType(Long taskId, ReminderLog.ReminderType reminderType);

    @Modifying
    void deleteByTaskId(Long taskId);
}
