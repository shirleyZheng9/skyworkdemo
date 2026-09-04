package com.iwhalecloud.bote.dto.beyond;

import com.iwhalecloud.bote.common.enums.BeyondContentType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionChoice;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应会话响应对象
 *
 * @author bianjp
 * @since 2025-07-17
 */
@Getter
@Setter
@ToString
@Builder
public class BeyondChatResponse {
  /** 标识本次调用的 ID */
  private String id;
  /** 补全选项列表。只有当请求参数中的 n 大于 1 时这个列表数量才会大于 1 */
  private List<ChatCompletionChoice> choices;
  /** 创建时间(时间戳，秒) */
  private Long created;
  /** 内容类型 */
  private BeyondContentType contentType;

  /**
   * 百应会话响应对象构造器
   */
  public static final class BeyondChatResponseBuilder {
    private BeyondChatResponseBuilder() {
      this.created = Instant.now().getEpochSecond();
    }

    /**
     * 设置响应消息
     */
    public BeyondChatResponseBuilder message(AssistantMessage message) {
      ChatCompletionChoice choice = new ChatCompletionChoice();
      choice.setDelta(message);
      this.choices = Collections.singletonList(choice);
      return this;
    }
  }

}
