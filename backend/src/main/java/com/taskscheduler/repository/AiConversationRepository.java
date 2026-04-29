package com.taskscheduler.repository;

import com.taskscheduler.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AiConversation> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @Modifying
    void deleteByTaskId(Long taskId);
}
