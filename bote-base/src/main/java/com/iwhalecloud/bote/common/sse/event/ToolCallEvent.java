package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 工具调用事件
 *
 * @author bianjp
 * @since 2025-05-29
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolCallEvent extends SseEvent {
  /** 工具调用标识 */
  private String toolCallId;
  /** 工具名称 */
  private String toolName;
  /** 工具描述 */
  private String toolDescription;
  /** 入参 */
  private Map<String, Object> input;

  public ToolCallEvent(String toolCallId, String toolName, String toolDescription, @Nullable Map<String, Object> input) {
    this.toolCallId = toolCallId;
    this.toolName = toolName;
    this.toolDescription = toolDescription;
    this.input = input;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.TOOL_CALL;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("toolName", toolName);
    data.put("toolDescription", toolDescription);
    data.put("input", input);
    return data;
  }
}
