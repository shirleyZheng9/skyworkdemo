package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统消息
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class SystemMessage extends Message {
  /** 参与者名称 */
  private String name;
  /** 消息内容 */
  private String content;

  public SystemMessage() {
    super(MessageRole.SYSTEM);
  }

  public SystemMessage(String content) {
    this();
    this.content = content;
  }
}
