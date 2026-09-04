package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 推荐智能体事件
 *
 * @author chen.linfa
 * @since 2025-05-28
 */
@Getter
@Setter
@ToString
public class RecommendedScenesSseEvent extends SseEvent {
  /** 参考文档列表 */
  private final List<RecommendedSceneDTO> scenes;

  public RecommendedScenesSseEvent(List<RecommendedSceneDTO> scenes) {
    this.scenes = scenes;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.SELECT_SCENE;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    return ImmutableMap.of("scenes", scenes);
  }
}
