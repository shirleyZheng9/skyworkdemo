package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息抽象类
 *
 * <p>流式输出时，服务器在后面的消息中可能不会返回 role 属性，因此需要</p>
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@JsonTypeInfo(use = Id.NAME, property = "role", include = JsonTypeInfo.As.EXISTING_PROPERTY, defaultImpl = AssistantMessage.class)
@JsonSubTypes({
  @Type(name = "system", value = SystemMessage.class),
  @Type(name = "user", value = UserMessage.class),
  @Type(name = "assistant", value = AssistantMessage.class),
  @Type(name = "tool", value = ToolMessage.class),
  @Type(name = "function", value = FunctionMessage.class)
})
public abstract class Message {
  /** 角色 */
  protected final MessageRole role;
  /** token 数量。实现会话窗口使用，缓存数量以避免重复计算 */
  @JsonIgnore
  protected Integer tokenCount;

  protected Message(MessageRole role) {
    this.role = role;
  }

  /**
   * 是否是工具或函数消息
   */
  @JsonIgnore
  public boolean isToolOrFunction() {
    return this instanceof ToolMessage || this instanceof FunctionMessage;
  }
}
