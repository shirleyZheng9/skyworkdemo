package com.iwhalecloud.bote.service.a2a.helper;

import com.iwhalecloud.bote.cache.A2aTaskInfoCache;
import com.iwhalecloud.bote.common.util.A2aUtil;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aTaskInfo;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.a2a.IA2aAgentManageService;
import io.a2a.spec.FilePart;
import io.a2a.spec.Message;
import io.a2a.spec.TaskState;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A2A 智能体运行引擎
 *
 * @author bianjp
 * @since 2025-09-12
 */
@Service
@RequiredArgsConstructor
public class A2aAgentEngine {
  private final IA2aAgentManageService a2aAgentManageService;
  private final A2aTaskInfoCache taskInfoCache;

  /**
   * 执行 A2A 服务（外系统的服务）
   *
   * @param sceneChatParams 场景会话参数
   * @return 执行结果
   */
  public OrchestrationEngineResponse execute(SceneChatParamsDTO sceneChatParams) {
    Date startTime = new Date();
    Long tenantId = sceneChatParams.getTenantId();
    Long agentId = sceneChatParams.getSceneId();
    // 查询 A2A 服务
    A2aAgentDTO agent = a2aAgentManageService.findA2aAgent(tenantId, agentId);
    agent.parseJsonConfig();

    // 查询当前会话关联的任务
    String taskCacheKey = sceneChatParams.getContextId() + ":" + agentId;
    A2aTaskInfo taskInfo = taskInfoCache.get(taskCacheKey);

    // 构造消息
    String messageText = sceneChatParams.getMessageContent();
    List<FilePart> messageFiles = A2aUtil.buildMessageFilesByFileIds(sceneChatParams.getFileIds());
    Message userMessage = A2aUtil.buildUserMessage(messageText, null, messageFiles, null, taskInfo);

    // 调用 A2A 服务
    A2aInvokeHandler handler = A2aInvokeHandler.builder()
      .agent(agent)
      .clientId(sceneChatParams.getClientId())
      .replyHandler(sceneChatParams.getReplyHandler())
      .taskInfo(taskInfo)
      .skipAuthRequiredMessage(false)
      .build();
    handler.initialize();
    handler.invoke(userMessage);

    // 构造响应对象
    TaskState taskState = taskInfo.getTaskState();
    OrchestrationEngineResponse response = new OrchestrationEngineResponse();
    response.setSuccess(true);
    response.setTimeSpent(System.currentTimeMillis() - startTime.getTime());
    response.setSceneFinished(taskInfo.getTaskId() == null || (taskState != null && taskState.isFinal()));
    response.setReplies(sceneChatParams.getReplyHandler().getReplies());
    return response;
  }

}
