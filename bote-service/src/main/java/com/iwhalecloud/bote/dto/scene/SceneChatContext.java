package com.iwhalecloud.bote.dto.scene;

import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.entity.scene.SceneChatMessageEntity;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.helper.TokenCounter;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.ListUtils;
import org.springframework.util.Assert;

/**
 * 场景会话上下文
 *
 * @author bianjp
 * @since 2024-08-06
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class SceneChatContext {
  /** 场景会话参数 */
  private final SceneChatParamsDTO sceneChatParams;
  /** 大模型名称 */
  private final String modelName;
  /** 上下文长度 */
  private final int contextLength;
  /** 场景标识 */
  private Long sceneId;
  /** 对话标识 */
  private Long conversationId;
  /** 场景会话标识 */
  private String contextId;
  /** 登录用户 ID */
  private Long userId;
  /** 最后一条消息的序号，从 0 开始 */
  private int lastSeq;
  /** 场景工具列表 */
  private List<SkillToolDTO> sceneTools;
  /** 发送给大模型的工具列表 */
  private List<Tool> tools;
  /** 工具的 token 数量 */
  private int toolsTokenCount;
  /** 需要保存到数据库的新消息列表 */
  private List<SceneChatMessageEntity> newMessages = new ArrayList<>();
  /** 发送给大模型的消息列表 */
  private List<Message> messages = new ArrayList<>();
  private Map<String, Object> params;

  /** 步骤执行日志。只在调试时生成；只包含已执行的步骤，按执行顺序排序 */
  private List<OrchestrationStepRunLog> stepLogs;

  /**
   * 检查是否启用上下文窗口
   */
  private boolean isContextWindowEnabled() {
    return contextLength > 0;
  }

  /**
   * 设置历史消息
   */
  public void setHistoryMessages(List<Message> historyMessages) {
    // 工具消息映射，key 为工具调用标识
    Map<String, ToolMessage> toolMessageMap = historyMessages.stream()
      .filter(m -> m instanceof ToolMessage)
      .map(m -> (ToolMessage) m)
      .collect(Collectors.toMap(ToolMessage::getToolCallId, Function.identity()));
    for (Message message : historyMessages) {
      // 工具消息由对应的助手消息添加，这里不处理
      if (message instanceof ToolMessage) {
        continue;
      }
      // 没有工具调用时直接添加即可
      if (!(message instanceof AssistantMessage) || !((AssistantMessage) message).hasToolCall()) {
        messages.add(message);
        continue;
      }

      // 如果助手消息有工具调用，但我们还没生成工具的调用结果，需要暂时忽略这条消息，否则大模型会报错:
      // An assistant message with 'tool_calls' must be followed by tool messages responding to each 'tool_call_id'

      // 有工具调用结果时，对应的工具消息必须紧跟在助手消息后面，中间不能有别的消息
      String toolCallId = ((AssistantMessage) message).getToolCall().getId();
      if (toolMessageMap.containsKey(toolCallId)) {
        messages.add(message);
        messages.add(toolMessageMap.get(toolCallId));
      }
      else {
        // 把对应的用户消息也忽略掉
        if (!messages.isEmpty() && messages.get(messages.size() - 1).getRole() == MessageRole.USER) {
          messages.remove(messages.size() - 1);
        }
      }
    }

    // 计算每条消息的 token 数量
    fillTokenCount();
  }

  /**
   * 计算每条消息的 token 数量
   */
  private void fillTokenCount() {
    if (!isContextWindowEnabled()) {
      return;
    }
    for (Message message : messages) {
      if (message.getTokenCount() == null) {
        message.setTokenCount(TokenCounter.estimate(modelName, message));
      }
    }
  }

  /**
   * 添加消息
   */
  public void addMessage(Message message) {
    messages.add(message);
    addNewMessage(message);
  }

  /**
   * 添加新消息，仅保存到数据库，不添加到 messages 中
   */
  public void addNewMessage(Message message) {
    if (isContextWindowEnabled()) {
      message.setTokenCount(TokenCounter.estimate(modelName, message));
    }
    SceneChatMessageEntity entity = new SceneChatMessageEntity();
    entity.setSceneId(sceneId);
    entity.setConversationId(conversationId);
    entity.setContextId(contextId);
    entity.setUserId(userId);
    entity.setSeq(++lastSeq);
    entity.setMessage(message);
    entity.setCreateTime(new Date());
    newMessages.add(entity);
  }

  /**
   * 设置场景工具
   */
  public void setSceneTools(List<SkillToolDTO> sceneTools) {
    this.sceneTools = ListUtils.emptyIfNull(sceneTools);
    if (!this.sceneTools.isEmpty()) {
      this.tools = sceneTools.stream().map(skill -> {
        // 简单场景中调用知识问答不需要参数，构造一个新对象，避免影响缓存
        if (StepType.KNOWLEDGE_CHAT.equals(skill.getSkillType())) {
          return new Tool(skill.getTool().getFunction().getName(), skill.getTool().getFunction().getDescription(), null);
        }
        return skill.getTool();
      }).collect(Collectors.toList());
      if (isContextWindowEnabled()) {
        this.toolsTokenCount = TokenCounter.estimateTools(this.modelName, this.tools);
      }
    }
  }

  /**
   * 创建会话补全请求
   */
  public ChatCompletionRequest newRequest() {
    List<Message> finalMessages;
    if (!isContextWindowEnabled()) {
      // 拷贝一份以避免这里和外面的修改相互影响
      finalMessages = new ArrayList<>(messages);
    }
    else {
      finalMessages = buildMessagesWithContextWindow();
    }
    // 自定义模型配置
    CustomModelConfig customModelConfig = sceneChatParams == null ? null : sceneChatParams.getCustomModelConfig();
    ChatCompletionRequest request = ChatCompletionRequest.builder().messages(finalMessages).tools(tools).customModelConfig(customModelConfig).build();
    if (sceneChatParams != null) {
      request.setTenantId(sceneChatParams.getTenantId());
      request.setBotId(sceneChatParams.getBotId());
      request.setSceneId(sceneChatParams.getSceneId());
      request.setSessionId(sceneChatParams.getConversationId());
    }
    request.setSourceFrom(ModelConsts.SOURCE_AGENT);
    return request;
  }

  /**
   * 使用上下文窗口机制构造消息列表
   */
  private List<Message> buildMessagesWithContextWindow() {
    // 预留一些 token 给响应使用（响应也要占用上下文长度）
    int availableTokens = contextLength - 1000 - toolsTokenCount;
    int messagesTokenCount = messages.stream().mapToInt(message -> {
      Integer tokenCount = message.getTokenCount();
      Assert.notNull(tokenCount, () -> "消息的 tokenCount 为空: " + message);
      return tokenCount;
    }).sum();
    // 如果未超出可用 token 数，不需要处理
    if (messagesTokenCount < availableTokens) {
      // 拷贝一份以避免这里和外面的修改相互影响
      return new ArrayList<>(messages);
    }

    // 最终消息列表，先按倒序添加，最后再翻转顺序
    List<Message> finalMessages = new ArrayList<>();
    // 开始裁剪历史的最小索引（排除必须保留的第一条系统消息）
    int minPos;
    // 结束裁剪历史的最大索引（排除必须保留的最后一条消息，如果是工具消息，还需排除倒数第二条消息）
    int maxPos;
    // 如果第一条是系统消息（场景提示词），必须保留
    Message systemMessage;
    if (messages.get(0) instanceof SystemMessage) {
      systemMessage = messages.get(0);
      minPos = 1;
      availableTokens -= systemMessage.getTokenCount();
      Assert.isTrue(availableTokens > 0, "系统提示词过长，上下文长度不足");
    }
    else {
      systemMessage = null;
      minPos = 0;
    }

    // 最后一条消息必须保留（这是大模型需要回复的问题），如果是工具消息，还需保留对应的助手消息
    Message lastMessage = messages.get(messages.size() - 1);
    Assert.isTrue(!(lastMessage instanceof AssistantMessage), "最后一条消息不能是助手消息");
    finalMessages.add(lastMessage);
    if (lastMessage.isToolOrFunction()) {
      Message assistantMessage = messages.get(messages.size() - 2);
      Assert.isTrue(assistantMessage instanceof AssistantMessage, "工具消息的前一条消息必须是助手消息");
      maxPos = messages.size() - 2;
      availableTokens -= lastMessage.getTokenCount();
      availableTokens -= assistantMessage.getTokenCount();
      finalMessages.add(assistantMessage);
      Assert.isTrue(availableTokens > 0, "上下文长度不足");
    }
    else {
      maxPos = messages.size() - 1;
      availableTokens -= lastMessage.getTokenCount();
      Assert.isTrue(availableTokens > 0, "上下文长度不足");
    }

    // 添加历史消息
    addHistoryMessages(maxPos, minPos, availableTokens, finalMessages);

    if (systemMessage != null) {
      finalMessages.add(systemMessage);
    }
    Collections.reverse(finalMessages);
    return finalMessages;
  }

  /**
   * 添加历史消息
   */
  private void addHistoryMessages(int maxPos, int minPos, int availableTokens, List<Message> finalMessages) {
    // 从后往前逐条添加消息
    int i = maxPos;
    while (i >= minPos) {
      Message message = messages.get(i);
      // 工具消息和对应的助手消息要么都保留，要么都删除，不能只保留一个
      if (message.isToolOrFunction()) {
        Message assistantMessage = messages.get(i - 1);
        Assert.isTrue(assistantMessage instanceof AssistantMessage, "工具消息的前一条消息必须是助手消息");
        if (availableTokens - message.getTokenCount() - assistantMessage.getTokenCount() >= 0) {
          finalMessages.add(message);
          finalMessages.add(assistantMessage);
          availableTokens -= message.getTokenCount() + assistantMessage.getTokenCount();
          i--;
        }
        else {
          // token 不足时直接退出，不再处理前面的消息，避免消息历史混乱
          break;
        }
      }
      else if (availableTokens > message.getTokenCount()) {
        finalMessages.add(message);
        availableTokens -= message.getTokenCount();
      }
      else {
        // token 不足时直接退出，不再处理前面的消息，避免消息历史混乱
        break;
      }
      i--;
    }
  }

  /**
   * 构造步骤日志
   */
  public Optional<OrchestrationStepRunLog> newStepLog(String stepType, String stepName) {
    Optional<OrchestrationStepRunLog> optionalLog = newStepLog();
    optionalLog.ifPresent(log -> {
      log.setStepName(stepName);
      log.setStepCode(stepType);
      log.setStepType(stepType);
    });
    return optionalLog;
  }

  /**
   * 构造步骤日志
   */
  public Optional<OrchestrationStepRunLog> newStepLog(AbstractStep step) {
    Optional<OrchestrationStepRunLog> optionalLog = newStepLog();
    optionalLog.ifPresent(log -> {
      log.setStepName(step.getName());
      log.setStepCode(step.getCode());
      log.setStepType(step.getType());
    });
    return optionalLog;
  }

  /**
   * 构造步骤日志
   */
  private Optional<OrchestrationStepRunLog> newStepLog() {
    if (!Boolean.TRUE.equals(sceneChatParams.getDebug()) && !Boolean.TRUE.equals(sceneChatParams.getLogEnabled())) {
      return Optional.empty();
    }
    OrchestrationStepRunLog log = new OrchestrationStepRunLog();
    log.setLogId(IDUtils.nextId());
    log.setStartTime(new Date());

    if (stepLogs == null) {
      stepLogs = new ArrayList<>();
    }
    stepLogs.add(log);
    return Optional.of(log);
  }
}
