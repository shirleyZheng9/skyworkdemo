package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 会话补全响应
 *
 * @author bianjp
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/create">Create chat completion</a>
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class ChatCompletionResponse {
  /** 标识本次调用的 ID */
  private String id;
  /** 对象类型(流式为 chat.completion.chunk, 非流式为 chat.completion) */
  private String object;
  /** 模型 */
  private String model;
  /** 补全选项列表。只有当请求参数中的 n 大于 1 时这个列表数量才会大于 1 */
  private List<ChatCompletionChoice> choices;
  /** 服务等级 */
  private String serviceTier;
  /** 计量信息 */
  private Usage usage;
  /** 配置版本 */
  private String systemFingerprint;
  /** 创建时间(时间戳，秒) */
  private Long created;

  /**
   * 构造非流式响应
   */
  public static ChatCompletionResponse ofMessage(AssistantMessage message) {
    ChatCompletionChoice choice = new ChatCompletionChoice();
    choice.setMessage(message);
    choice.setFinishReason("stop");
    ChatCompletionResponse response = new ChatCompletionResponse();
    response.setChoices(Collections.singletonList(choice));
    return response;
  }

  /**
   * 构造流式响应
   */
  public static ChatCompletionResponse ofDelta(AssistantMessage message, boolean finish) {
    ChatCompletionChoice choice = new ChatCompletionChoice();
    choice.setDelta(message);
    if (finish) {
      choice.setFinishReason("stop");
    }
    ChatCompletionResponse response = new ChatCompletionResponse();
    response.setChoices(Collections.singletonList(choice));
    return response;
  }

  /**
   * 校验调用成功
   */
  public void assertSuccess() {
    assertSuccess(false);
  }

  /**
   * 校验调用成功
   */
  public void assertSuccess(boolean allowLengthLimit) {
    // 流式输出时可能不返回 choices, 比如流式输出末尾返回 usage
    if (CollectionUtils.isEmpty(choices)) {
      return;
    }
    String finishReason = choices.get(0).getFinishReason();
    // 使用黑名单方式检查，避免误判
    if (!allowLengthLimit && "length".equals(finishReason)) {
      throw new BssException("大模型输出长度超出限制");
    }
    if ("content_filter".equals(finishReason)) {
      throw new BssException("大模型输出触发内容过滤");
    }
  }

  /**
   * 获取非流式输出的消息
   */
  @JsonIgnore
  public AssistantMessage getMessage() {
    return choices.get(0).getMessage();
  }

  /**
   * 获取非流式输出的消息内容
   */
  @JsonIgnore
  public String getMessageContent() {
    return StringUtils.defaultString(choices.get(0).getMessage().getContent());
  }

  /**
   * 获取非流式输出的推理内容
   */
  @JsonIgnore
  public String getReasoningContent() {
    return StringUtils.defaultString(choices.get(0).getMessage().getReasoningContent());
  }

  /**
   * 获取流式输出的消息
   */
  @JsonIgnore
  @Nullable
  public AssistantMessage getDelta() {
    return CollectionUtils.isEmpty(choices) ? null : choices.get(0).getDelta();
  }

  /**
   * 获取流式输出的消息内容
   */
  @JsonIgnore
  public String getDeltaContent() {
    AssistantMessage delta = getDelta();
    return delta != null ? StringUtils.defaultString(delta.getContent()) : "";
  }

  /**
   * 获取流式输出的推理内容
   */
  @JsonIgnore
  public String getDeltaReasoningContent() {
    AssistantMessage delta = getDelta();
    return delta != null ? StringUtils.defaultString(delta.getReasoningContent()) : "";
  }
}
