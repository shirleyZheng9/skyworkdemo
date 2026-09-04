package com.iwhalecloud.bote.agent.memory.helper;

import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.dto.agent.MemoryMessage;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.helper.TokenCounter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.Yaml;

/**
 * 记忆摘要生成器
 *
 * @author bianjp
 * @since 2026-03-11
 */
public final class MemoryCompactor {
  /** 系统提示词 */
  private static String systemPrompt;
  /** 生成摘要的提示词模板 */
  private static String summaryPromptTemplate;
  /** 更新摘要的提示词模板 */
  private static String updateSummaryPromptTemplate;
  /** 每日记忆摘要的提示词模板（用于触发压缩时同步生成每日记忆条目） */
  private static String dailySummaryPromptTemplate;
  /** 每日记忆系统提示词（专用于每日记忆生成） */
  private static String dailySystemPrompt;
  /** 每日记忆更新提示词模板（用于结合已有内容做智能更新） */
  private static String dailyUpdatePromptTemplate;
  /** 默认上下文长度。获取不到模型的上下文长度时用作默认值 */
  private static final int DEFAULT_CONTEXT_LENGTH = 32 * 1024;
  /** 记忆压缩比例（消息列表的 token 数量占上下文长度的比例超过这个数值时触发压缩） */
  private static final double MEMORY_COMPACT_RATIO = 0.7;
  /** 保留最近的消息数量（不做摘要） */
  private static final int KEEP_RECENT = 5;

  static {
    loadPrompts();
  }

  private MemoryCompactor() {
  }

  /**
   * 加载提示词
   */
  private static void loadPrompts() {
    try (InputStream inputStream = new ClassPathResource("agent/memory/prompt/compactor.yml").getInputStream()) {
      Map<String, String> prompts = new Yaml().load(inputStream);
      systemPrompt = prompts.get("system_prompt");
      summaryPromptTemplate = prompts.get("summary_prompt_template");
      updateSummaryPromptTemplate = prompts.get("update_summary_prompt_template");
      dailySummaryPromptTemplate = prompts.get("daily_summary_prompt_template");
      dailySystemPrompt = prompts.get("daily_system_prompt");
      dailyUpdatePromptTemplate = prompts.get("daily_update_prompt_template");
    }
    catch (IOException e) {
      throw new BssException("加载记忆压缩提示词失败: " + e.getMessage(), e);
    }
  }

  /**
   * 自动摘要，仅在消息列表的 token 数量占上下文长度的比例超出限制时触发
   *
   * @param modelClient 大模型客户端
   * @param messages 消息列表
   * @param previousSummary 之前的摘要
   * @return (最新摘要, 被压缩的消息列表)，如果不需要压缩则返回 null
   */
  @Nullable
  public static Pair<String, List<MemoryMessage>> autoCompact(LlmClient modelClient, List<MemoryMessage> messages, @Nullable String previousSummary) {
    // 可压缩消息的起始索引（包含），排除系统消息
    int compressibleMsgStart = messages.getFirst().message() instanceof SystemMessage ? 1 : 0;
    // 可压缩消息的结束索引（不包含），保留最近的几条消息不做压缩
    int compressibleMsgEnd = messages.size() - KEEP_RECENT;
    // 如果最新消息中第一条是工具调用，需要一并保留前一条助手消息，以避免调用大模型时报错
    if (compressibleMsgEnd > compressibleMsgStart && messages.get(compressibleMsgEnd).message() instanceof ToolMessage) {
      compressibleMsgEnd--;
    }
    // 没有可压缩的消息时不处理，只有一条需要压缩时也不处理
    if (compressibleMsgEnd - compressibleMsgStart <= 1) {
      return null;
    }

    // 检查 token 比例是否超出限制
    int totalTokens = 0;
    for (MemoryMessage message : messages) {
      totalTokens += TokenCounter.estimate(modelClient.defaultModel(), message.message());
      // 系统提醒单独挂在 MemoryMessage 上，不参与摘要 prompt 文本，但计入体积以避免阈值失真
      totalTokens += calculateReminderTokens(modelClient, message);
    }
    // 未超出限制时不压缩
    int threshold = (int) ((modelClient.contextLength() > 0 ? modelClient.contextLength() : DEFAULT_CONTEXT_LENGTH) * MEMORY_COMPACT_RATIO);
    if (totalTokens < threshold) {
      return null;
    }

    List<MemoryMessage> compressibleMessages = new ArrayList<>(messages.subList(compressibleMsgStart, compressibleMsgEnd));
    String summary = compact(modelClient, compressibleMessages.stream().map(MemoryMessage::message).toList(), previousSummary);
    return Pair.of(summary, compressibleMessages);
  }

  /**
   * 计算系统提醒所占 token 数量
   */
  private static int calculateReminderTokens(LlmClient modelClient, MemoryMessage message) {
    List<SystemReminder> reminders = message.reminders();
    if (CollectionUtils.isNotEmpty(reminders)) {
      int totalTokens = 0;
      for (SystemReminder reminder : reminders) {
        if (reminder.type().isPersistent()) {
          totalTokens += TokenCounter.estimate(modelClient.defaultModel(), new UserMessage(reminder.text()));
        }
      }
      return totalTokens;
    }
    return 0;
  }

  /**
   * 生成摘要
   *
   * @param modelClient 大模型客户端
   * @param messages 消息列表
   * @param previousSummary 之前的摘要
   * @return 最新摘要
   */
  public static String compact(LlmClient modelClient, List<Message> messages, @Nullable String previousSummary) {
    String formattedMessages = formatHistoryMessages(messages);
    String userPrompt;
    // 参考 https://github.com/agentscope-ai/ReMe/blob/v0.3.0.6b3/reme/memory/file_based/components/compactor.py#L54
    if (StringUtils.isNotEmpty(previousSummary)) {
      userPrompt = updateSummaryPromptTemplate.formatted(formattedMessages, previousSummary);
    }
    else {
      userPrompt = summaryPromptTemplate.formatted(formattedMessages);
    }

    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage(systemPrompt)
      .addUserMessage(userPrompt)
      .build();
    return modelClient.chatCompletion(request, SseUtil.requestListener).getMessageContent();
  }

  /**
   * 为每日记忆生成摘要条目
   *
   * <p>在记忆压缩触发时调用，将被压缩的对话消息总结为一段每日记忆文本。
   * 生成的摘要将被异步追加到 MEMORY-YYYY-MM-DD.md 文件中。</p>
   *
   * @param modelClient 大模型客户端
   * @param messages    被压缩的消息列表
   * @return 每日记忆摘要文本
   */
  public static String summaryForDailyMemory(LlmClient modelClient, List<Message> messages) {
    String formattedMessages = formatHistoryMessages(messages);
    String userPrompt = dailySummaryPromptTemplate.formatted(formattedMessages);
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage(dailySystemPrompt)
      .addUserMessage(userPrompt)
      .build();
    return modelClient.chatCompletion(request, SseUtil.requestListener).getMessageContent();
  }

  /**
   * 更新每日记忆（结合已有内容做智能更新）
   *
   * <p>当当天已有记忆时，调用此方法让大模型结合已有内容和新对话做智能更新，
   * 而不是简单追加。</p>
   *
   * @param modelClient    大模型客户端
   * @param existingMemory 已有的每日记忆内容
   * @param messages       新对话消息列表
   * @return 更新后的每日记忆内容
   */
  public static String updateDailyMemory(LlmClient modelClient, String existingMemory, List<Message> messages) {
    String formattedMessages = formatHistoryMessages(messages);
    String userPrompt = dailyUpdatePromptTemplate.formatted(existingMemory, formattedMessages);
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage(dailySystemPrompt)
      .addUserMessage(userPrompt)
      .build();
    return modelClient.chatCompletion(request, SseUtil.requestListener).getMessageContent();
  }

  /**
   * 格式化消息列表（仅使用 {@link Message} 的可见内容；系统提醒在 SessionBasedMemory 侧单独存储，不在此参与摘要）
   */
  private static String formatHistoryMessages(List<Message> messages) {
    StringBuilder sb = new StringBuilder();
    Map<String, String> toolCallId2NameMap = messages.stream()
      .filter(m -> m instanceof AssistantMessage && ((AssistantMessage) m).hasToolCall())
      .map(m -> ((AssistantMessage) m).getToolCall())
      .collect(Collectors.toMap(ToolCall::getId, t -> t.getFunction().getName(), (a, b) -> b));
    // 参考 https://github.com/agentscope-ai/ReMe/blob/v0.3.0.6b3/reme/core/schema/as_msg_stat.py#L36
    for (Message message : messages) {
      sb.append(message.getRole()).append(": ");
      switch (message) {
        case UserMessage userMessage -> sb.append(Objects.toString(userMessage.getContent(), ""));
        case AssistantMessage assistantMessage -> {
          if (StringUtils.isNotEmpty(assistantMessage.getContent())) {
            sb.append(assistantMessage.getContent());
          }
          if (assistantMessage.hasToolCall()) {
            if (StringUtils.isNotEmpty(assistantMessage.getContent())) {
              sb.append("\n");
            }
            ToolCall toolCall = assistantMessage.getToolCall();
            sb.append("<tool_use>").append(toolCall.getFunction().getName())
              .append(" params=").append(toolCall.getFunction().getArguments())
              .append("</tool_use>");
          }
        }
        case ToolMessage toolMessage -> {
          String toolName = toolCallId2NameMap.getOrDefault(toolMessage.getToolCallId(), "");
          sb.append("<tool_result>").append(toolName)
            .append(" output=").append(Objects.toString(toolMessage.getContent(), ""))
            .append("</tool_result>");
        }
        default -> {
          // 忽略未知消息类型
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
