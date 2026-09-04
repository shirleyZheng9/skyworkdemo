package com.iwhalecloud.bote.service.agent;

import com.iwhalecloud.bote.dto.agent.SessionTaskDTO;
import com.iwhalecloud.bote.dto.agent.task.UpdateTaskRequest;
import com.iwhalecloud.bote.entity.agent.SessionTaskEntity;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 会话任务服务
 *
 * @author bianjp
 * @since 2026-04-10
 */
public interface ISessionTaskService {

  /**
   * 创建任务
   */
  Integer createTask(SessionTaskEntity task);

  /**
   * 更新任务
   *
   * @param request 更新请求
   * @param sessionId 会话 ID
   * @return 更新结果
   */
  ResultVO<Void> updateTask(UpdateTaskRequest request, Long sessionId);

  /**
   * 列出会话的所有任务
   *
   * @param sessionId 会话 ID
   * @return 任务列表
   */
  List<SessionTaskDTO> listTasks(Long sessionId);

  /**
   * 获取单个任务
   *
   * @param sessionId 会话 ID
   * @param taskId 任务 ID
   * @return 任务详情
   */
  @Nullable
  SessionTaskDTO getTask(Long sessionId, Integer taskId);

  /**
   * 删除任务
   *
   * @param sessionId 会话 ID
   * @param taskId 任务 ID
   * @return 删除结果
   */
  ResultVO<Void> deleteTask(Long sessionId, Integer taskId);
}
