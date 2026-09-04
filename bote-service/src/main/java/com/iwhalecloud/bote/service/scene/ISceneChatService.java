package com.iwhalecloud.bote.service.scene;

import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.FlowSceneProcessDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import org.springframework.lang.Nullable;

/**
 * 场景会话服务
 *
 * @author bianjp
 * @since 2024-08-05
 */
public interface ISceneChatService {

  /**
   * 执行场景
   * <p>适用于简单场景、复杂场景、对话型工作流，不用于任务型工作流。</p>
   *
   * @param sceneChatParams 场景会话参数
   * @return 执行结果
   */
  OrchestrationEngineResponse run(SceneChatParamsDTO sceneChatParams);

  /**
   * 获取智能体流程进度，目前只有嵌套调用的智能体，才会记录流程进度
   *
   * @param contextId 上下文 ID
   * @return 流程进度
   */
  @Nullable
  FlowSceneProcessDTO getFlowSceneProcess(String contextId);
}
