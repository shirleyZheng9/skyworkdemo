package com.iwhalecloud.bote.dto.chat.agent;

import com.iwhalecloud.bote.agent.tools.A2uiTools.A2uiEvent;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流 Agent 节点的 A2UI 回复
 *
 * @author bianjp
 * @since 2026-03-21
 */
@Getter
@Setter
@ToString
public class AgentReplyA2uiPart extends AgentReplyPart {
  /** A2UI 事件列表 */
  private List<A2uiEvent> events;

  public AgentReplyA2uiPart() {
    super("a2ui");
  }

  public AgentReplyA2uiPart(List<A2uiEvent> events) {
    this();
    this.events = events;
  }
}
