package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文本事件
 *
 * @author bianjp
 * @since 2025-01-07
 */
@Getter
@Setter
@ToString
public class TextSseEvent extends SseEvent {
  /** 文本内容 */
  private final String text;

  public TextSseEvent(String text) {
    this.text = text;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.TEXT;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    return text;
  }
}
