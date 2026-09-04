package com.iwhalecloud.bote.service.scene.runner;

import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;

/**
 * 智能体执行器
 *
 * @author bianjp
 * @since 2026-04-24
 */
public interface SceneRunner {

  /**
   * 运行
   *
   * @param scene 智能体
   * @param sceneChatParams 会话参数
   * @return 执行结果
   */
  OrchestrationEngineResponse run(SimpleBotSceneDTO scene, SceneChatParamsDTO sceneChatParams);

}
