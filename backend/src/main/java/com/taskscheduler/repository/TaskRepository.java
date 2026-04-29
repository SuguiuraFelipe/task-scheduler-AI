package com.taskscheduler.repository;

import com.taskscheduler.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByDueDateAsc(Long userId);

    List<Task> findByUserIdAndStatus(Long userId, Task.TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED'")
    List<Task> findTasksDueBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Task> findByUserIdAndDueDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
