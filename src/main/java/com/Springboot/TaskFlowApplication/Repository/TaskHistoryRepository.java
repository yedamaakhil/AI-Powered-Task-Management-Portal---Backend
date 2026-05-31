package com.Springboot.TaskFlowApplication.Repository;

import com.Springboot.TaskFlowApplication.Entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    
    List<TaskHistory> findByTaskIdOrderByBlockIndexAsc(Long taskId);
    
    @Query(value = "SELECT * FROM task_history WHERE task_id = :taskId ORDER BY block_index DESC LIMIT 1", nativeQuery = true)
    Optional<TaskHistory> findTopByTaskIdOrderByBlockIndexDesc(@Param("taskId") Long taskId);
    
    @Query("SELECT COUNT(th) FROM TaskHistory th WHERE th.taskId = :taskId")
    int countByTaskId(@Param("taskId") Long taskId);
    
    List<TaskHistory> findAllByOrderByBlockIndexAsc();
}