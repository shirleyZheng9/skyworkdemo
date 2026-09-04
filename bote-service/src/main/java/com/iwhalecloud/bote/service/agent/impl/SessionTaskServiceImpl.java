package com.iwhalecloud.bote.service.agent.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.GeneralAgentConsts;
import com.iwhalecloud.bote.dto.agent.SessionTaskDTO;
import com.iwhalecloud.bote.dto.agent.task.UpdateTaskRequest;
import com.iwhalecloud.bote.entity.agent.SessionTaskEntity;
import com.iwhalecloud.bote.mapper.agent.SessionTaskMapper;
import com.iwhalecloud.bote.service.agent.ISessionTaskService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话任务服务实现
 *
 * @author bianjp
 * @since 2026-04-10
 */
@Service
@RequiredArgsConstructor
public class SessionTaskServiceImpl implements ISessionTaskService {
  private final SessionTaskMapper agentTaskMapper;

  @Override
  @Transactional
  public Integer createTask(SessionTaskEntity task) {
    Integer maxTaskId = agentTaskMapper.selectMaxTaskId(task.getSessionId());
    int newTaskId = maxTaskId == null ? 1 : maxTaskId + 1;

    task.setId(IDUtils.nextId());
    task.setTaskId(newTaskId);
    task.setTaskStatus(GeneralAgentConsts.AGENT_TASK_STATUS_PENDING);
    task.setBlocksJson("[]");
    task.setBlockedByJson("[]");
    task.setIsDeleted(0);

    agentTaskMapper.insertTask(task);
    return newTaskId;
  }

  @Override
  @Transactional
  public ResultVO<Void> updateTask(UpdateTaskRequest request, Long sessionId) {
    SessionTaskEntity existing = agentTaskMapper.selectTaskBySessionIdAndTaskId(sessionId, request.getTaskId());
    if (existing == null) {
      return ResultVO.fail("Task not found");
    }

    // 复制字段进行更新
    SessionTaskEntity updateEntity = new SessionTaskEntity();
    updateEntity.setId(existing.getId());
    updateEntity.setSubject(StringUtils.trimToNull(request.getSubject()));
    updateEntity.setDescription(StringUtils.trimToNull(request.getDescription()));
    updateEntity.setActiveForm(StringUtils.trimToNull(request.getActiveForm()));
    updateEntity.setTaskStatus(StringUtils.trimToNull(request.getStatus()));

    // 更新依赖关系
    if (CollectionUtils.isNotEmpty(request.getAddBlocks())) {
      List<Integer> currentBlocks = parseTaskIdsJson(existing.getBlocksJson());
      List<Integer> newBlocks = new ArrayList<>(currentBlocks);
      updateTaskBlocks(request, sessionId, currentBlocks, newBlocks);
      if (!newBlocks.equals(currentBlocks)) {
        updateEntity.setBlocksJson(JsonUtil.toJsonString(newBlocks));
      }
    }
    if (CollectionUtils.isNotEmpty(request.getAddBlockedBy())) {
      List<Integer> currentBlockedBy = parseTaskIdsJson(existing.getBlockedByJson());
      List<Integer> newBlockedBy = new ArrayList<>(currentBlockedBy);
      updateTaskBlockedBy(request, sessionId, newBlockedBy, currentBlockedBy);
      if (!newBlockedBy.equals(currentBlockedBy)) {
        updateEntity.setBlockedByJson(JsonUtil.toJsonString(newBlockedBy));
      }
    }

    // 更新数据库
    agentTaskMapper.updateTaskById(updateEntity);

    return ResultVO.success();
  }

  /**
   * 更新任务的阻塞关系
   */
  private void updateTaskBlocks(UpdateTaskRequest request, Long sessionId, List<Integer> currentBlocks, List<Integer> newBlocks) {
    for (Integer blockedTaskId : request.getAddBlocks()) {
      // 忽略 null 值和重复值
      if (blockedTaskId == null || currentBlocks.contains(blockedTaskId)) {
        continue;
      }
      newBlocks.add(blockedTaskId);
      // 反向更新依赖关系
      SessionTaskEntity blockedTask = agentTaskMapper.selectTaskBySessionIdAndTaskId(sessionId, blockedTaskId);
      if (blockedTask != null) {
        String blockedByJson = addToTaskIdsJson(blockedTask.getBlockedByJson(), request.getTaskId());
        if (!blockedByJson.equals(blockedTask.getBlockedByJson())) {
          agentTaskMapper.updateTaskBlockedBy(blockedTask.getId(), blockedByJson);
        }
      }
    }
  }

  /**
   * 更新任务的依赖关系
   */
  private void updateTaskBlockedBy(UpdateTaskRequest request, Long sessionId, List<Integer> newBlockedBy, List<Integer> currentBlockedBy) {
    for (Integer blockingTaskId : request.getAddBlockedBy()) {
      if (!newBlockedBy.contains(blockingTaskId)) {
        // 忽略 null 值和重复值
        if (blockingTaskId == null || currentBlockedBy.contains(blockingTaskId)) {
          continue;
        }
        newBlockedBy.add(blockingTaskId);
      }
      SessionTaskEntity blockingTask = agentTaskMapper.selectTaskBySessionIdAndTaskId(sessionId, blockingTaskId);
      if (blockingTask != null) {
        String blocksJson = addToTaskIdsJson(blockingTask.getBlocksJson(), request.getTaskId());
        if (!blocksJson.equals(blockingTask.getBlocksJson())) {
          agentTaskMapper.updateTaskBlocks(blockingTask.getId(), blocksJson);
        }
      }
    }
  }

  @Override
  public List<SessionTaskDTO> listTasks(Long sessionId) {
    List<SessionTaskEntity> entities = agentTaskMapper.selectTasksBySessionId(sessionId);
    List<SessionTaskDTO> tasks = entities.stream()
      .map(SessionTaskDTO::new)
      .collect(Collectors.toList());

    // 过滤掉 blockedBy 中已经完成的任务
    Set<Integer> completedTaskIds = entities.stream()
      .filter(entity -> GeneralAgentConsts.AGENT_TASK_STATUS_COMPLETED.equals(entity.getTaskStatus()))
      .map(SessionTaskEntity::getTaskId).collect(Collectors.toSet());
    for (SessionTaskDTO dto : tasks) {
      if (CollectionUtils.isNotEmpty(dto.getBlockedBy())) {
        dto.setBlockedBy(dto.getBlockedBy().stream().filter(taskId -> !completedTaskIds.contains(taskId)).toList());
      }
    }
    return tasks;
  }

  @Override
  @Nullable
  public SessionTaskDTO getTask(Long sessionId, Integer taskId) {
    SessionTaskEntity entity = agentTaskMapper.selectTaskBySessionIdAndTaskId(sessionId, taskId);
    if (entity == null) {
      return null;
    }
    return new SessionTaskDTO(entity);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteTask(Long sessionId, Integer taskId) {
    SessionTaskEntity entity = agentTaskMapper.selectTaskBySessionIdAndTaskId(sessionId, taskId);
    if (entity == null) {
      return ResultVO.fail("Task not found");
    }
    agentTaskMapper.deleteTaskById(entity.getId());
    cleanupTaskDependencies(sessionId, taskId);
    return ResultVO.success();
  }

  /**
   * 清理任务的依赖引用
   */
  private void cleanupTaskDependencies(Long sessionId, Integer taskId) {
    List<SessionTaskEntity> allTasks = agentTaskMapper.selectTasksBySessionId(sessionId);

    for (SessionTaskEntity task : allTasks) {
      List<Integer> blocks = parseTaskIdsJson(task.getBlocksJson());
      if (blocks.contains(taskId)) {
        String blocksJson = JsonUtil.toJsonString(blocks.stream().filter(id -> !id.equals(taskId)).toList());
        agentTaskMapper.updateTaskBlocks(task.getId(), blocksJson);
      }
      else {
        List<Integer> blockedBy = parseTaskIdsJson(task.getBlockedByJson());
        String blockedByJson = JsonUtil.toJsonString(blockedBy.stream().filter(id -> !id.equals(taskId)).toList());
        agentTaskMapper.updateTaskBlockedBy(task.getId(), blockedByJson);
      }
    }
  }

  private String addToTaskIdsJson(@Nullable String existingTaskIdsJson, Integer taskId) {
    List<Integer> existingTaskIds = parseTaskIdsJson(existingTaskIdsJson);
    if (existingTaskIds.contains(taskId)) {
      return existingTaskIdsJson;
    }
    List<Integer> result;
    if (existingTaskIds.isEmpty()) {
      result = List.of(taskId);
    }
    else {
      result = new ArrayList<>(existingTaskIds);
      result.add(taskId);
    }
    return JsonUtil.toJsonString(result);
  }

  /**
   * 解析 JSON 列表
   *
   * @param json JSON 字符串
   * @return 列表
   */
  private List<Integer> parseTaskIdsJson(@Nullable String json) {
    if (json == null || json.isEmpty() || "[]".equals(json)) {
      return List.of();
    }
    List<Integer> taskIds = JsonUtil.parseJson(json, new TypeReference<>() {
    });
    return taskIds != null ? taskIds : List.of();
  }
}
