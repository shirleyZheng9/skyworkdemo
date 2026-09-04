package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户消息
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class UserMessage extends Message {
  /** 参与者名称 */
  private String name;
  /** 消息内容，类型为字符串，或者 MessageContent 列表 */
  private Object content;

  public UserMessage() {
    super(MessageRole.USER);
  }

  public UserMessage(String content) {
    this();
    this.content = content;
  }

  public UserMessage(List<MessageContent> content) {
    this();
    this.content = content;
  }
}
