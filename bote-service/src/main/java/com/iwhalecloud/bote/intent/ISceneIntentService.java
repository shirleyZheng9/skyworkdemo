package com.iwhalecloud.bote.intent;

import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SimpleUserMessageDTO;
import java.util.Optional;
import org.springframework.lang.Nullable;

/**
 * @author chen.linfa
 * @since 2024-10-08
 */
public interface ISceneIntentService {
  /**
   * 意图识别智能体
   *
   * @param tenantId 租户 ID
   * @param botId 当前的机器人 ID
   * @param message 用户消息
   * @return 结果
   */
  @Nullable
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  SceneIntentResultDTO recognize(Long tenantId, @Nullable Long botId, SimpleUserMessageDTO message, Optional<ChatTraceLogBuilder> log);

  /**
   * 租户欢迎页，意图识别应用
   *
   * @param tenantId 租户 ID
   * @param message 用户消息
   * @return 结果
   */
  SceneIntentResultDTO recognizeBot(Long tenantId, String message);

  /**
   * 通过规划，生成计划
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param message 用户消息
   * @return 结果
   */
  @Nullable
  SceneIntentResultDTO recognizePlan(Long tenantId, Long botId, SimpleUserMessageDTO message, Optional<ChatTraceLogBuilder> log);
}
