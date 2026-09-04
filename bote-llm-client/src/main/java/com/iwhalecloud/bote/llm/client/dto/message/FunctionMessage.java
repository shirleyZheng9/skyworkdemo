package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 函数消息
 *
 * @author bianjp
 * @since 2024-10-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class FunctionMessage extends Message {
  /** 函数名称 */
  private String name;
  /** 消息内容 */
  private String content;

  public FunctionMessage() {
    super(MessageRole.FUNCTION);
  }

  public FunctionMessage(String name, String content) {
    this();
    this.name = name;
    this.content = content;
  }

  public FunctionMessage(String name, Object content) {
    this(name, content instanceof String ? (String) content : JsonUtil.toJsonString(content));
  }
}
