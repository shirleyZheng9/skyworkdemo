package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库参考文档事件
 *
 * @author bianjp
 * @since 2025-01-07
 */
@Getter
@Setter
@ToString
public class ReferencesSseEvent extends SseEvent {
  /** 参考文档列表 */
  private final List<ReferenceDocumentDTO> references;

  public ReferencesSseEvent(List<ReferenceDocumentDTO> references) {
    this.references = references;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.REFERENCES;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    return references;
  }
}
