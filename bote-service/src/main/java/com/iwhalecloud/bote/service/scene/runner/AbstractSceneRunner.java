package com.iwhalecloud.bote.service.scene.runner;

import com.iwhalecloud.bote.agent.tool.InternalToolsLoader;
import com.iwhalecloud.bote.agent.tools.MemoryTools;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.ScenePromptUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.MemoryToolStep;
import com.iwhalecloud.bote.dto.scene.SceneChatContext;
import com.iwhalecloud.bote.dto.scene.SceneChatMessageDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.entity.scene.SceneChatMessageEntity;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.mapper.scene.SceneChatMessageMapper;
import com.iwhalecloud.bote.service.skill.IFlowRunLogService;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 智能体执行器抽象类
 *
 * @author bianjp
 * @since 2026-04-24
 */
public abstract class AbstractSceneRunner implements SceneRunner {
  @Autowired
  protected SceneCache sceneCache;
  @Autowired
  protected ModelClientCache modelClientCache;
  @Autowired
  protected TenantSettingInfoCache tenantSettingInfoCache;
  @Autowired
  protected IFlowRunLogService flowTraceLogService;
  @Autowired
  protected SceneChatMessageMapper sceneChatMessageMapper;

  /**
   * 获取大模型 ID
   */
  protected final Long resolveModelId(SceneChatParamsDTO sceneChatParams) {
    Long modelId = sceneChatParams.getModelId();
    if (modelId == null || ModelConsts.DEFAULT_MODEL.equals(modelId)) {
      modelId = tenantSettingInfoCache.getModelId(sceneChatParams.getTenantId());
    }
    return modelId;
  }


  /**
   * 保存消息记录
   */
  protected final void saveNewMessages(SceneChatContext context) {
    List<SceneChatMessageEntity> newMessages = context.getNewMessages();
    if (newMessages.isEmpty()) {
      return;
    }
    for (SceneChatMessageEntity message : newMessages) {
      message.setMessageId(Sequences.CHAT_MESSAGE_ID.next());
    }
    TransactionUtil.execute(() -> {
      sceneChatMessageMapper.batchInsertMessage(newMessages);
    });
    newMessages.clear();
  }

  /**
   * 模拟一个编排引擎上下文对象，解析提示词中的表达式时使用
   */
  protected final SceneOrchestrationContext mockOrchestrationContext(SceneChatParamsDTO sceneChatParams) {
    SceneOrchestrationContext context = new SceneOrchestrationContext();
    context.setRequest(new OrchestrationEngineRequest(sceneChatParams, sceneChatParams.getSceneId(), null, null));
    return context;
  }

  /**
   * 构造会话补全请求对象
   */
  protected final SceneChatContext buildContext(SimpleBotSceneDTO scene, SceneChatParamsDTO sceneChatParams, @Nullable LlmClient modelClient, @Nullable Long modelId) {
    Long sceneId = sceneChatParams.getSceneId();
    Long conversationId = sceneChatParams.getConversationId();
    String contextId = sceneChatParams.getContextId();
    Assert.hasLength(contextId, "contextId 不能为空");
    SceneChatContext context;
    if (modelClient == null) {
      context = new SceneChatContext(sceneChatParams, null, 0);
    }
    else {
      context = new SceneChatContext(sceneChatParams, modelClient.defaultModel(), modelClient.contextLength());
    }
    context.setSceneId(sceneId);
    context.setConversationId(conversationId);
    context.setContextId(contextId);

    // 查询工具
    if (modelId != null) {
      context.setSceneTools(sceneCache.getSkillTools(scene, sceneChatParams.getTenantId(), modelId, Boolean.TRUE.equals(sceneChatParams.getDebug())));
    }
    else {
      context.setSceneTools(Collections.emptyList());
    }
    // 添加记忆工具
    appendMemoryTools(context, sceneChatParams);

    context.setParams(sceneChatParams.getParams());

    // 添加历史消息
    List<SceneChatMessageDTO> historyMessages = sceneChatMessageMapper.selectSimpleMessagesByContextId(contextId);
    context.setLastSeq(historyMessages.isEmpty() ? 0 : historyMessages.getLast().getSeq());
    List<Message> messages = historyMessages.stream().map(SceneChatMessageDTO::toMessage).collect(Collectors.toList());

    // 没有历史消息时需要添加场景提示词，用作 system 角色
    if (modelId != null && messages.isEmpty()) {
      String scenePrompt = ScenePromptUtil.resolveScenePrompt(mockOrchestrationContext(sceneChatParams), sceneChatParams.getTenantId(), sceneId, modelId, context.getSceneTools());
      if (StringUtils.isNotEmpty(scenePrompt)) {
        SystemMessage systemMessage = new SystemMessage(scenePrompt);
        messages.add(systemMessage);
        context.addNewMessage(systemMessage);
      }
    }

    // 添加工具消息
    String toolCallId = sceneChatParams.getToolCallId();
    if (StringUtils.isNotEmpty(toolCallId)) {
      SceneChatMessageDTO assistantMessage = IterableUtils.find(historyMessages, m -> m.isAssistant() && toolCallId.equals(m.getToolCallId()));
      Assert.notNull(assistantMessage, () -> "未找到助手消息: toolCallId=" + toolCallId);
      SkillToolDTO tool = IterableUtils.find(context.getSceneTools(), t -> t.getToolCode().equals(assistantMessage.getToolName()));
      Assert.notNull(tool, () -> "未找到工具: toolCode=" + assistantMessage.getToolName());
      ToolMessage toolMessage = new ToolMessage(toolCallId, sceneChatParams.getMessageContent());
      messages.add(toolMessage);
      context.addNewMessage(toolMessage);
    }
    else {
      // 如果用户上传了文件，将文件 ID 告诉大模型，以便大模型组装 function calling 的参数时使用
      if (CollectionUtils.isNotEmpty(sceneChatParams.getFileIds())) {
        UserMessage fileMessage = new UserMessage("用户上传的文件列表(fileIds): " + StringUtils.join(sceneChatParams.getFileIds(), ','));
        messages.add(fileMessage);
        context.addNewMessage(fileMessage);
      }
      // 添加用户消息
      UserMessage userMessage = new UserMessage(sceneChatParams.getMessageContent());
      messages.add(userMessage);
      context.addNewMessage(userMessage);
    }
    context.setHistoryMessages(messages);
    return context;
  }

  /**
   * 添加记忆工具
   */
  private void appendMemoryTools(SceneChatContext context, SceneChatParamsDTO sceneChatParams) {
    // 启用长期记忆时，运行时自动注入记忆工具
    if (BaseConsts.TRUE.equals(sceneChatParams.getLongTermMemoryEnabled())) {
      List<SkillToolDTO> sceneTools = context.getSceneTools();
      if (sceneTools.isEmpty()) {
        sceneTools = new ArrayList<>();
      }
      List<SkillToolDTO> memoryTools = buildMemorySkillTools();
      sceneTools.addAll(memoryTools);
      context.setSceneTools(sceneTools);
    }
  }

  /**
   * 构建长期记忆 SkillToolDTO 列表
   *
   * <p>将 {@link MemoryTools} 中所有带 {@code @Tool} 注解的静态方法通过反射加载为
   * {@link SkillToolDTO}，供简单场景在启用长期记忆时动态注入到工具列表中。</p>
   */
  private List<SkillToolDTO> buildMemorySkillTools() {
    return InternalToolsLoader.loadTools(MemoryTools.class).stream()
      .map(callback -> {
        MemoryToolStep step = new MemoryToolStep(callback);
        return SkillToolDTO.builder(StepType.MEMORY_TOOL, null, step)
          .tool(callback.getTool().getFunction().getName(),
            callback.getTool().getFunction().getDescription(),
            callback.getTool().getFunction().getParameters(),
            null)
          .build();
      })
      .collect(Collectors.toList());
  }
}
