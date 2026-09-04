package com.iwhalecloud.bote.llm.client.util;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionChoice;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.FunctionCall;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 大模型响应工具类
 *
 * @author bianjp
 * @since 2025-12-17
 */
public final class ModelResponseUtil {
  private ModelResponseUtil() {
  }

  /**
   * 修复并行工具调用
   */
  public static void fixParallelToolCalls(ChatCompletionResponse response) {
    // 如果返回了多个工具调用（可能未正确支持 parallel_tool_calls 参数），只保留第一个
    AssistantMessage message = response.getChoices().getFirst().getMessage();
    if (CollectionUtils.size(message.getToolCalls()) > 1) {
      message.setToolCalls(Collections.singletonList(message.getToolCalls().getFirst()));
    }
  }

  /**
   * 合并流式输出的响应（流式转非流式）
   *
   * @param partialResponses 流式输出的响应列表
   * @return 非流式响应
   */
  public static ChatCompletionResponse mergeStreamResponses(List<ChatCompletionResponse> partialResponses) {
    Assert.notEmpty(partialResponses, "流式响应列表不能为空");
    List<ChatCompletionChoice> choices = partialResponses.stream()
      .map(ChatCompletionResponse::getChoices)
      .filter(CollectionUtils::isNotEmpty)
      .map(List::getFirst)
      .toList();
    List<AssistantMessage> messages = choices.stream().map(ChatCompletionChoice::getDelta).filter(Objects::nonNull).toList();
    // 思考内容
    String reasoning = messages.stream().map(AssistantMessage::getReasoningContent).filter(StringUtils::isNotEmpty).collect(Collectors.joining());
    // 回复内容
    String content = messages.stream().map(AssistantMessage::getContent).filter(StringUtils::isNotEmpty).collect(Collectors.joining());
    // 工具调用列表
    List<ToolCall> toolCalls = extractToolCalls(messages);
    // 函数调用
    FunctionCall functionCall = toolCalls == null ? extractFunctionCall(messages) : null;

    AssistantMessage message = new AssistantMessage();
    message.setReasoningContent(reasoning.isEmpty() ? null : reasoning);
    message.setContent(content.isEmpty() ? null : content);
    message.setToolCalls(toolCalls);
    message.setFunctionCall(functionCall);

    ChatCompletionChoice choice = new ChatCompletionChoice();
    choice.setIndex(0);
    // finish_reason 通常在最后一条消息中返回
    choice.setFinishReason(findFirstValue(choices.reversed(), ChatCompletionChoice::getFinishReason, "stop"));
    choice.setMessage(message);

    ChatCompletionResponse response = new ChatCompletionResponse();
    response.setId(findFirstValue(partialResponses, ChatCompletionResponse::getId));
    response.setObject("chat.completion");
    response.setModel(findFirstValue(partialResponses, ChatCompletionResponse::getModel));
    response.setChoices(List.of(choice));
    response.setServiceTier(findFirstValue(partialResponses, ChatCompletionResponse::getServiceTier));
    // usage 通常在最后一条消息中返回
    response.setUsage(findFirstValue(partialResponses.reversed(), ChatCompletionResponse::getUsage));
    response.setSystemFingerprint(findFirstValue(partialResponses, ChatCompletionResponse::getSystemFingerprint));
    response.setCreated(findFirstValue(partialResponses, ChatCompletionResponse::getCreated));
    return response;
  }

  /**
   * 提取工具调用
   */
  @Nullable
  private static List<ToolCall> extractToolCalls(List<AssistantMessage> messages) {
    // 可能会有多个工具调用，根据 index 分组
    Map<Integer, List<ToolCall>> toolCallsMap = messages.stream()
      .map(AssistantMessage::getToolCalls)
      .filter(CollectionUtils::isNotEmpty)
      .flatMap(List::stream)
      // 使用 LinkedHashMap 以确保根据 index 排序
      .collect(Collectors.groupingBy(t -> ObjectUtils.getIfNull(t.getIndex(), 0), LinkedHashMap::new, Collectors.toList()));
    if (toolCallsMap.isEmpty()) {
      return null;
    }
    List<ToolCall> finalToolCalls = new ArrayList<>();
    for (Entry<Integer, List<ToolCall>> entry : toolCallsMap.entrySet()) {
      Integer index = entry.getKey();
      List<ToolCall> toolCalls = entry.getValue();
      List<FunctionCall> functions = toolCalls.stream().map(ToolCall::getFunction).filter(Objects::nonNull).toList();
      String name = functions.stream().map(FunctionCall::getName).filter(StringUtils::isNotEmpty).findFirst().orElse(null);
      if (StringUtils.isEmpty(name)) {
        throw new BssException("工具调用的工具名称不能为空");
      }
      String arguments = functions.stream().map(FunctionCall::getArguments).filter(StringUtils::isNotEmpty).collect(Collectors.joining());
      String id = toolCalls.stream().map(ToolCall::getId).filter(StringUtils::isNotEmpty).findFirst().orElse(null);
      finalToolCalls.add(new ToolCall(index, id, name, arguments.isEmpty() ? null : arguments));
    }
    return finalToolCalls;
  }

  /**
   * 提取函数调用
   */
  @Nullable
  private static FunctionCall extractFunctionCall(List<AssistantMessage> messages) {
    List<FunctionCall> functionCalls = messages.stream().map(AssistantMessage::getFunctionCall).filter(Objects::nonNull).toList();
    if (functionCalls.isEmpty()) {
      return null;
    }
    String name = functionCalls.stream().map(FunctionCall::getName).filter(StringUtils::isNotEmpty).findFirst().orElse(null);
    if (StringUtils.isEmpty(name)) {
      throw new BssException("函数调用的函数名称不能为空");
    }
    String arguments = functionCalls.stream().map(FunctionCall::getArguments).filter(StringUtils::isNotEmpty).collect(Collectors.joining());
    return new FunctionCall(name, arguments.isEmpty() ? null : arguments);
  }

  /**
   * 获取第一个非空的属性值
   */
  @SuppressWarnings("SameParameterValue")
  private static <T, V> V findFirstValue(List<T> list, Function<T, V> getter, V defaultValue) {
    V value = findFirstValue(list, getter);
    return value != null ? value : defaultValue;
  }

  /**
   * 获取第一个非空的属性值
   */
  @Nullable
  private static <T, V> V findFirstValue(List<T> list, Function<T, V> getter) {
    for (T item : list) {
      V value = getter.apply(item);
      if (ObjectUtils.isNotEmpty(value)) {
        return value;
      }
    }
    return null;
  }

}
