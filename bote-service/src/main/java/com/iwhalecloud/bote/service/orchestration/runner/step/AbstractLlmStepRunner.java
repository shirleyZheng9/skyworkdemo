package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeAnswerHelper;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.model.VisionConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.file.AbstractFile;
import com.iwhalecloud.bote.dto.orchestration.file.DataUrlFile;
import com.iwhalecloud.bote.dto.orchestration.file.FileServerFile;
import com.iwhalecloud.bote.dto.orchestration.file.UrlFile;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep.LlmMessage;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ImageUrl;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.MessageContent;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 大模型步骤执行器抽象类
 *
 * @author bianjp
 * @since 2025-02-08
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractLlmStepRunner<T extends AbstractStep> extends AbstractStepRunner<T> {
  /** 视觉模型支持的最大图片大小: 10M */
  private static final int MAX_IMAGE_SIZE = 1024 * 1024 * 10;
  /**
   * 视觉模型支持的图片类型，只支持常用图片格式
   *
   * @see <a href="https://help.aliyun.com/zh/model-studio/user-guide/vision/#afa499b5b1rl5">Qwen</a>
   * @see <a href="https://platform.openai.com/docs/guides/vision#what-type-of-files-can-i-upload">OpenAI</a>
   */
  private static final List<String> ALLOWED_IMAGE_TYPES = List.of("png", "apng", "jpg", "jpeg", "webp", "bmp");

  protected static final ModelClientCache modelClientCache = SpringUtil.getBean(ModelClientCache.class);
  protected static final KnowledgeAnswerHelper knowledgeAnswerHelper = SpringUtil.getBean(KnowledgeAnswerHelper.class);

  /**
   * 调用工具
   *
   * @param sceneChatParams 场景会话参数
   * @param skillTool 技能工具
   * @param parameters 工具参数
   * @return 工具调用结果
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Nullable
  protected final Object invokeTool(SceneChatParamsDTO sceneChatParams, SkillToolDTO skillTool, @Nullable Map<String, Object> parameters) {
    AbstractStep step = skillTool.getStep();
    AbstractStepRunner runner = SceneStepRegistry.getRunner(skillTool.getSkillType());
    return runner.runAsTool(sceneChatParams, step, "", parameters, Optional.empty());
  }

  /**
   * 合并自定义参数
   *
   * @param skillTool 技能配置，可能包含自定义参数
   * @param parameters 大模型生成的参数
   * @return 合并后的参数
   */
  @Nullable
  protected final Map<String, Object> mergeCustomParameters(SkillToolDTO skillTool, @Nullable Map<String, Object> parameters) {
    // 有自定义参数时，解析自定义参数，并与大模型生成的工具参数做深度合并
    ParameterSpec customParameterSpec = skillTool.getCustomParameters();
    if (customParameterSpec != null) {
      Map<String, Object> customParameters = resolveObjectParameterValue("", customParameterSpec);
      if (MapUtils.isNotEmpty(customParameters)) {
        return MapUtils.isEmpty(parameters) ? customParameters : deepMergeMap(parameters, customParameters);
      }
    }
    return parameters;
  }

  /**
   * 深度合并 Map
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> deepMergeMap(Map<String, Object> target, Map<String, Object> source) {
    Map<String, Object> result = new LinkedHashMap<>(target);
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (ObjectUtils.isEmpty(value)) {
        continue;
      }
      // 递归处理 Map
      if (value instanceof Map) {
        Object targetMap = target.get(key);
        if (targetMap == null) {
          result.put(key, value);
        }
        else if (targetMap instanceof Map) {
          result.put(key, deepMergeMap((Map<String, Object>) targetMap, (Map<String, Object>) value));
        }
        // 类型不一致时不处理
      }
      else {
        result.put(key, value);
      }
    }
    return result;
  }

  /**
   * 调用大模型，流式输出
   */
  @SuppressWarnings("PMD.AvoidInstanceofChecksInCatchClause")
  protected final ChatCompletionResponse invokeLlmStream(SceneOrchestrationContext context, AbstractStep step, LlmClient modelClient, List<Message> messages,
                                                         Consumer<ChatCompletionResponse> partialHandler, CustomModelConfig customModelConfig) {
    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().tenantId(context.getTenantId()).messages(messages).customModelConfig(customModelConfig).build();
    try {
      return modelClient.chatCompletionStreamBlockingAndCollect(chatCompletionRequest, partialHandler, SseUtil.requestListener);
    }
    catch (Exception e) {
      logger.error("Failed to invoke llm stream: service={}|{}, step={}|{}",
        context.getDsl().getCode(),
        context.getDsl().getName(),
        step.getCode(),
        step.getName(),
        e);
      if (e instanceof BssException) {
        // 确保报错信息中包含服务名称和步骤名称
        ((BssException) e).setFailMsg(buildFailMsg(context, e.getMessage(), step.getName()));
        throw (BssException) e;
      }
      throw new BssException(buildFailMsg(context, ExpUtil.getMsg(e), step.getName()), e);
    }
  }

  /**
   * 加载历史消息
   */
  protected final List<Message> loadHistoryMessages(SceneOrchestrationContext context, @Nullable MemoryConfig memoryConfig) {
    if (memoryConfig == null || !Boolean.TRUE.equals(memoryConfig.getEnabled()) || context.getRequest().getHistoryMessagesLoader() == null) {
      return Collections.emptyList();
    }
    List<Message> messages = context.getRequest().getHistoryMessagesLoader().get();
    if (!messages.isEmpty() && Boolean.TRUE.equals(memoryConfig.getWindowEnabled())) {
      int windowSize = memoryConfig.getWindowSize() != null ? memoryConfig.getWindowSize() : 0;
      if (windowSize > 0 && windowSize < messages.size()) {
        messages = messages.subList(messages.size() - windowSize, messages.size());
      }
    }
    return messages;
  }

  /**
   * 格式化消息列表，以方便调试日志展示
   */
  protected final List<Object> formatMessagesForDebugLog(List<Message> messages) {
    List<Object> result = new ArrayList<>(messages.size());
    for (int i = 0; i < messages.size(); i++) {
      Message message = messages.get(i);
      if (i == messages.size() - 1 && message instanceof UserMessage) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("role", message.getRole());
        map.put("content", JsonUtil.readTree((String) ((UserMessage) message).getContent()));
        result.add(map);
      }
      else {
        result.add(message);
      }
    }
    return result;
  }

  /**
   * 解析视觉配置中的图片
   */
  protected final List<MessageContent> parseVisonImages(@Nullable VisionConfig vision, LlmClient modelClient, Long modelId) {
    if (vision != null && Boolean.TRUE.equals(vision.getEnabled()) && StringUtils.isNotEmpty(vision.getFiles())) {
      Assert.isTrue(modelClient.supportsVision(), () -> String.format("大模型 %s(%s) 不支持视觉", modelClient.defaultModel(), modelId));
      return buildFileMessageContents(vision.getFiles());
    }
    return List.of();
  }

  /**
   * 添加节点配置的消息列表
   */
  protected final void addMessages(Long tenantId, Long modelId, List<LlmMessage> messages, List<Message> result) {
    for (LlmMessage message : messages) {
      Assert.notNull(message.getRole(), "消息角色不能为空");
      String content = resolvePrompt(tenantId, modelId, message.getPromptId(), message.getPromptParameters(), message.getContent());
      // 忽略空消息
      if (StringUtils.isEmpty(content)) {
        continue;
      }
      switch (message.getRole()) {
        case SYSTEM:
          result.add(new SystemMessage(content));
          break;
        case USER:
          result.add(new UserMessage(content));
          break;
        case ASSISTANT:
          result.add(new AssistantMessage(content));
          break;
        default:
          throw new IllegalStateException("不支持的角色: " + message.getRole());
      }
    }
  }

  /**
   * 构造用户消息
   */
  protected final UserMessage buildUserMessage(Long tenantId, Long modelId,
                                               @Nullable Long promptId, @Nullable List<ParameterSpec> promptParameters, @Nullable String messageText,
                                               @Nullable List<MessageContent> messageContents) {
    String userMessageText = resolvePrompt(tenantId, modelId, promptId, promptParameters, messageText);
    return buildUserMessage(messageContents, userMessageText);
  }

  /**
   * 构造用户消息
   *
   * @param messageContents 文件消息内容列表
   * @param messageText 消息文本
   */
  protected final UserMessage buildUserMessage(@Nullable List<MessageContent> messageContents, @Nullable String messageText) {
    if (CollectionUtils.isEmpty(messageContents)) {
      return new UserMessage(messageText);
    }
    List<MessageContent> contentList = new ArrayList<>(messageContents.size() + 1);
    contentList.addAll(messageContents);
    contentList.add(new MessageContent(messageText));
    return new UserMessage(contentList);
  }

  /**
   * 构造文件消息内容列表
   *
   * @param filesSpec 文件地址/ID 的取值表达式
   */
  protected final List<MessageContent> buildFileMessageContents(@Nullable String filesSpec) {
    List<AbstractFile> files = resolveFiles(filesSpec);
    if (files.isEmpty()) {
      return List.of();
    }
    List<MessageContent> result = new ArrayList<>(files.size());
    for (AbstractFile file : files) {
      switch (file) {
        case UrlFile urlFile -> result.add(new MessageContent(new ImageUrl(urlFile.getUrl().toString())));
        case DataUrlFile dataUrlFile -> result.add(new MessageContent(new ImageUrl(dataUrlFile.getUrl())));
        case FileServerFile fileServerFile -> result.add(buildFileMessageContentByFileId(fileServerFile));
        default -> throw new BssException("不支持的文件类型: " + file.getClass().getName());
      }
    }
    return result;
  }

  /**
   * 根据文件 ID 构造文件消息内容
   */
  private MessageContent buildFileMessageContentByFileId(FileServerFile file) {
    FileInfoVO fileInfo = file.getFileInfo();
    Long fileId = fileInfo.getFileId();
    // 限制文件大小
    Assert.isTrue(fileInfo.getFileSize() == null || fileInfo.getFileSize() <= MAX_IMAGE_SIZE, () -> "文件大小超出限制: " + fileId);
    // 文件类型必须是图片
    String fileType = file.getFileType();
    String mimeType = file.getMimeType();
    Assert.isTrue(fileType != null && mimeType != null, () -> "文件类型未知: " + fileId);
    Assert.isTrue(ALLOWED_IMAGE_TYPES.contains(fileType) && mimeType.startsWith("image/"), "不支持的图片格式: " + fileType);

    // 下载文件，构造 data URI 字符串
    byte[] bytes = fileStoreService.downloadFile(fileId);
    Assert.isTrue(bytes != null && bytes.length > 0, () -> "文件不存在: " + fileId);
    String dataUri = "data:" + mimeType + ";base64," + Base64.encodeBase64String(bytes);
    return new MessageContent(new ImageUrl(dataUri));
  }
}
