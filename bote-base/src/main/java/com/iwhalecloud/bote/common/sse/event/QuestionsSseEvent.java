package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库追问问题事件
 *
 * @author bianjp
 * @since 2025-01-07
 */
@Getter
@Setter
@ToString
public class QuestionsSseEvent extends SseEvent {
  /** 追问问题列表 */
  private final List<String> questions;

  public QuestionsSseEvent(List<String> questions) {
    this.questions = questions;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.QUESTIONS;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    return questions;
  }
}
