package com.Springboot.TaskFlowApplication.service;

import com.Springboot.TaskFlowApplication.Entity.Task;
import com.Springboot.TaskFlowApplication.Entity.TaskHistory;
import com.Springboot.TaskFlowApplication.Repository.TaskHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    private final TaskHistoryRepository taskHistoryRepository;

    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    public BlockchainService(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }

    @Transactional
    public TaskHistory recordTaskEvent(
            Task task,
            String action,
            String previousStatus,
            String newStatus,
            String changedBy) {

        log.info("Recording blockchain event for task {}: {}", task.getId(), action);

        // Get the last block for THIS SPECIFIC TASK
        Optional<TaskHistory> lastBlock = taskHistoryRepository
                .findTopByTaskIdOrderByBlockIndexDesc(task.getId());
        
        String previousHash = GENESIS_HASH;
        long blockIndex = 1;
        
        if (lastBlock.isPresent()) {
            previousHash = lastBlock.get().getBlockHash();
            blockIndex = lastBlock.get().getBlockIndex() + 1;
            log.info("Previous block found - index: {}, hash: {}", lastBlock.get().getBlockIndex(), previousHash.substring(0, 8));
        } else {
            log.info("No previous block found - this is the first block for task {}", task.getId());
        }

        // Build block data
        String blockData = task.getId() + "|"
                + action + "|"
                + (previousStatus != null ? previousStatus : "null") + "|"
                + (newStatus != null ? newStatus : "null") + "|"
                + changedBy + "|"
                + blockIndex + "|"
                + previousHash + "|"
                + LocalDateTime.now();

        String blockHash = computeHash(blockData);
        log.info("Generated block hash: {}", blockHash.substring(0, 8) + "...");

        // Create history record
        TaskHistory history = new TaskHistory();
        history.setTaskId(task.getId());
        history.setAction(action);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setBlockHash(blockHash);
        history.setPreviousHash(previousHash);
        history.setChangedBy(changedBy);
        history.setBlockIndex(blockIndex);
        history.setTimestamp(LocalDateTime.now());

        // Update task with the latest block hash
        task.setTaskHash(blockHash);
        
        log.info("✅ Block #{} recorded for task {} | Action: {}", blockIndex, task.getId(), action);

        return taskHistoryRepository.save(history);
    }

    public List<TaskHistory> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findByTaskIdOrderByBlockIndexAsc(taskId);
    }
    
    public int getBlockCount(Long taskId) {
        return taskHistoryRepository.countByTaskId(taskId);
    }

    public boolean verifyChain() {
        List<TaskHistory> allBlocks = taskHistoryRepository.findAllByOrderByBlockIndexAsc();
        
        if (allBlocks.isEmpty()) {
            log.info("Blockchain is empty - valid");
            return true;
        }
        
        // Group by taskId
        Map<Long, List<TaskHistory>> taskBlocks = new HashMap<>();
        for (TaskHistory block : allBlocks) {
            taskBlocks.computeIfAbsent(block.getTaskId(), k -> new java.util.ArrayList<>()).add(block);
        }
        
        for (Map.Entry<Long, List<TaskHistory>> entry : taskBlocks.entrySet()) {
            List<TaskHistory> blocks = entry.getValue();
            
            // Check block indices are sequential starting from 1
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).getBlockIndex() != i + 1) {
                    log.warn("Invalid block index for task {}", entry.getKey());
                    return false;
                }
            }
            
            // Check genesis block
            if (!GENESIS_HASH.equals(blocks.get(0).getPreviousHash())) {
                log.warn("Invalid genesis block for task {}", entry.getKey());
                return false;
            }
            
            // Check chain linkage
            for (int i = 1; i < blocks.size(); i++) {
                if (!blocks.get(i).getPreviousHash().equals(blocks.get(i-1).getBlockHash())) {
                    log.warn("Broken chain for task {}", entry.getKey());
                    return false;
                }
            }
        }
        
        log.info("✅ Blockchain verification passed - {} total blocks", allBlocks.size());
        return true;
    }

    public Map<String, Object> getBlockchainStats() {
        List<TaskHistory> allBlocks = taskHistoryRepository.findAllByOrderByBlockIndexAsc();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBlocks", allBlocks.size());
        stats.put("totalTasks", allBlocks.stream().map(TaskHistory::getTaskId).distinct().count());
        stats.put("createdEvents", allBlocks.stream().filter(b -> "CREATED".equals(b.getAction())).count());
        stats.put("updatedEvents", allBlocks.stream().filter(b -> "STATUS_UPDATED".equals(b.getAction())).count());
        stats.put("deletedEvents", allBlocks.stream().filter(b -> "DELETED".equals(b.getAction())).count());
        
        return stats;
    }

    private String computeHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}