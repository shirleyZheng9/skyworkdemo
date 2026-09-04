package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 工具消息
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class ToolMessage extends Message {
  /** 工具调用标识，对应 {@link ToolCall#getId()} */
  private String toolCallId;
  /** 消息内容 */
  private String content;

  public ToolMessage() {
    super(MessageRole.TOOL);
  }

  public ToolMessage(String toolCallId, String content) {
    this();
    this.toolCallId = toolCallId;
    this.content = StringUtils.defaultIfEmpty(content, "Done");
  }

  public ToolMessage(String toolCallId, Object content) {
    this(toolCallId, content == null || content instanceof String ? (String) content : JsonUtil.toJsonString(content));
  }
}
