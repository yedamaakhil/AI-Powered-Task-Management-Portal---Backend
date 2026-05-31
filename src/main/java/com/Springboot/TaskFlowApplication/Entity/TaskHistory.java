package com.Springboot.TaskFlowApplication.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private String action;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "block_hash", nullable = false, length = 64)
    private String blockHash;

    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "block_index", nullable = false)
    private Long blockIndex;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Constructors
    public TaskHistory() {}

    // Getters
    public Long getId() { return id; }
    public Long getTaskId() { return taskId; }
    public String getAction() { return action; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getBlockHash() { return blockHash; }
    public String getPreviousHash() { return previousHash; }
    public String getChangedBy() { return changedBy; }
    public Long getBlockIndex() { return blockIndex; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public void setAction(String action) { this.action = action; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public void setBlockIndex(Long blockIndex) { this.blockIndex = blockIndex; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}