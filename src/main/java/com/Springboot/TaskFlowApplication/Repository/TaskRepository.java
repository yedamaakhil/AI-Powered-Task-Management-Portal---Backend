package com.Springboot.TaskFlowApplication.Repository;

import com.Springboot.TaskFlowApplication.Entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository   
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Task> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> findByFilters(@Param("userId") Long userId,
                            @Param("status") Task.Status status,
                            @Param("priority") Task.Priority priority,
                            @Param("search") String search,
                            Pageable pageable);
}