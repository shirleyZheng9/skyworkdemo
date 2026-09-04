package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 推理内容文本事件
 *
 * @author chen.linfa
 * @since 2025-06-03
 */
@Getter
@Setter
@ToString
public class ReasoningSseEvent extends SseEvent {
  /** 推理内容 */
  private final String text;

  public ReasoningSseEvent(String text) {
    this.text = text;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.REASONING;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    return text;
  }
}
