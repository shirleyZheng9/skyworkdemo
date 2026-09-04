package com.iwhalecloud.bote.common.sse.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 工具调用结果事件
 *
 * @author bianjp
 * @since 2025-05-29
 */
@Getter
@Setter
@ToString
public class ToolCallResultEvent extends SseEvent {
  /** 工具调用标识，对应 {@link ToolCall#getId()} */
  private String toolCallId;
  /** 工具名称 */
  private String toolName;
  /** 出参 */
  private Object output;
  /** 是否成功 */
  private Boolean success;
  /** 耗时(ms) */
  private Long spentTime;

  public ToolCallResultEvent(String toolCallId, String toolName, @Nullable Object output, Boolean success, Long spentTime) {
    this.toolCallId = toolCallId;
    this.toolName = toolName;
    this.output = output;
    this.success = success;
    this.spentTime = spentTime;
  }

  @Override
  @JsonIgnore
  public ChatMessageType getMsgType() {
    return ChatMessageType.TOOL_CALL_RESULT;
  }

  @Override
  @JsonIgnore
  public Object getMsgContent() {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("toolName", toolName);
    data.put("output", output);
    data.put("success", success);
    data.put("spentTime", spentTime);
    return data;
  }
}
