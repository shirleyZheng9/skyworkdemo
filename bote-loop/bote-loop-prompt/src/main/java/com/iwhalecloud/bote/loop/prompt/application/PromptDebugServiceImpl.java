package com.iwhalecloud.bote.loop.prompt.application;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ModelClientUtil;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.loop.client.prompt.debug.PromptDebugService;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.DebugStreamingRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.GetDebugContextRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.GetDebugContextResponse;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.ListDebugHistoryRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.ListDebugHistoryResponse;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.SaveDebugContextRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.SaveDebugContextResponse;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MockToolDTO;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bote.loop.prompt.application.convertor.DebugContextConvertor;
import com.iwhalecloud.bote.loop.prompt.application.convertor.DebugLogConvertor;
import com.iwhalecloud.bote.loop.prompt.application.convertor.LlmConvertor;
import com.iwhalecloud.bote.loop.prompt.application.convertor.ManageConvertor;
import com.iwhalecloud.bote.loop.prompt.domain.component.llm.SseEmitterConsumer;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugContext;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugLog;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugMessage;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptCommit;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDetail;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDraft;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Tool;
import com.iwhalecloud.bote.loop.prompt.domain.entity.ToolChoiceType;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableVal;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IDebugContextRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IDebugLogRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListDebugHistoryParam;
import com.iwhalecloud.bote.loop.prompt.domain.service.IPromptService;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Prompt调试服务实现
 * 迁移对应关系: Go语言modules/prompt/application.PromptDebugApplicationImpl
 * - 功能: 提供Prompt调试相关服务
 * - 主要方法:
 * * debugStreaming - 流式调试Prompt
 * * saveDebugContext - 保存调试上下文
 * * getDebugContext - 获取调试上下文
 * * listDebugHistory - 列出调试历史
 * <p>
 * Java实现说明:
 * - 对应Go的PromptDebugApplicationImpl结构体
 * - 使用Spring服务注解
 * - 实现流式调试功能
 * - 支持调试上下文管理
 * - 集成链路追踪
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go context.Context -> Java方法参数
 * - Go goroutine -> Java CompletableFuture
 * - Go channel -> Java Stream/CompletableFuture
 * - Go error返回 -> Java异常处理
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class PromptDebugServiceImpl implements PromptDebugService {
  private static final Logger logger = LoggerFactory.getLogger(PromptDebugServiceImpl.class);

  private final IDebugLogRepo debugLogRepo;
  private final IDebugContextRepo debugContextRepo;
  private final IPromptService promptService;
  private final LargeModelManageMapper largeModelManageMapper;

  @Override
  public SseEmitter debugStreaming(DebugStreamingRequest request) {
    SessionUtil.getOptionalUserId();
    String userId = SessionContext.getCurrentUserId();

    // 参数验证
    validateDebugStreamingRequest(request);

    // TODO 会话信息
    if (request.getPrompt().getId() == null || request.getPrompt().getId() == 0) {
      request.getPrompt().setPromptKey("playground-" + userId);
//                auth.checkSpacePermission(request.getPrompt().getWorkspaceId(), "workspace_create_loop_prompt");
    }

    // TODO 创建链路追踪

    // 执行流式调试
    return doDebugStreaming(request);
  }

  /**
   * 验证调试流式请求参数
   * 迁移对应关系: Go语言validateDebugStreamingRequest
   */
  private void validateDebugStreamingRequest(DebugStreamingRequest request) {
    validateRequestNotNull(request);
    validatePromptNotNull(request);
    validatePromptDetail(request);
    validateMessages(request);
  }

  private void validateRequestNotNull(DebugStreamingRequest request) {
    if (request == null) {
      throw new BssException("请求参数不能为空");
    }
  }

  private void validatePromptNotNull(DebugStreamingRequest request) {
    if (request.getPrompt() == null) {
      throw new BssException("Prompt不能为空");
    }
    if (request.getPrompt().getWorkspaceId() == null) {
      throw new BssException("Prompt.WorkspaceID不能为空");
    }
    if (request.getPrompt().getPromptDraft() == null && request.getPrompt().getPromptCommit() == null) {
      throw new BssException("Prompt.Draft和Prompt.Commit不能同时为空");
    }
  }

  private void validatePromptDetail(DebugStreamingRequest request) {
    var promptDetail = getPromptDetail(request);
    if (promptDetail == null) {
      throw new BssException("PromptDetail不能为空");
    }
    validatePromptTemplate(promptDetail);
    validateModelConfig(promptDetail);
  }

  private PromptDetail getPromptDetail(DebugStreamingRequest request) {
    return request.getPrompt().getPromptDraft() != null ?
      ManageConvertor.promptDetailDTO2DO(request.getPrompt().getPromptDraft().getDetail()) : ManageConvertor.promptDetailDTO2DO(request.getPrompt().getPromptCommit().getDetail());
  }

  private void validatePromptTemplate(PromptDetail promptDetail) {
    if (promptDetail.getPromptTemplate() == null) {
      throw new BssException("PromptDetail.PromptTemplate不能为空");
    }
    if (promptDetail.getPromptTemplate().getTemplateType() == null) {
      throw new BssException("PromptDetail.PromptTemplate.TemplateType不能为空");
    }
  }

  private void validateModelConfig(PromptDetail promptDetail) {
    if (promptDetail.getModelConfig() == null) {
      throw new BssException("PromptDetail.ModelConfig不能为空");
    }
  }

  private void validateMessages(DebugStreamingRequest request) {
    List<MessageDTO> messages = collectAllMessages(request);
    for (MessageDTO message : messages) {
      validateMessage(message);
    }
  }

  private List<MessageDTO> collectAllMessages(DebugStreamingRequest request) {
    List<MessageDTO> messages = new ArrayList<>();
    var promptDetail = getPromptDetail(request);

    if (promptDetail.getPromptTemplate().getMessages() != null) {
      promptDetail.getPromptTemplate().getMessages().forEach(message -> {
        messages.add(ManageConvertor.messageDO2DTO(message));
      });
    }
    if (request.getMessages() != null) {
      messages.addAll(request.getMessages());
    }
    return messages;
  }

  private void validateMessage(MessageDTO message) {
    if (message == null) {
      throw new BssException("至少有一个Message为空");
    }
    if (message.getRole() == null) {
      throw new BssException("至少有一个Message.Role为空");
    }
  }

  /**
   * 执行流式调试
   * 迁移对应关系: Go语言doDebugStreaming
   */
  private SseEmitter doDebugStreaming(DebugStreamingRequest request) {

    Prompt prompt = ManageConvertor.promptDTO2DO(request.getPrompt());

    PromptDraft promptDraft = prompt.getPromptDraft();
    PromptCommit promptCommit = prompt.getPromptCommit();
    PromptDetail promptDetail = promptDraft == null ? promptCommit.getPromptDetail() : promptDraft.getPromptDetail();
    ModelConfig modelConfig = promptDetail.getModelConfig();
    Long modelId = modelConfig.getModelId();
    SimpleLargeModelDTO model = largeModelManageMapper.selectLargeModelById(request.getPrompt().getWorkspaceId(), modelId);
    if (model == null) {
      throw new BssException("Model not found, modelId=" + modelId);
    }
    LlmConvertor.toModel(modelConfig, model);

    LlmProperties properties = ModelConfigUtil.buildLlmProperties(model);
    LlmClient llmClient = ModelClientUtil.createLlmClient(model.getProtocolType(), properties);
    String clientId = request.getClientId();

    List<Message> messages = ManageConvertor.batchMessageDTO2DO(request.getMessages());
    // 完成多模态文件URI到URL的转换
    promptService.mCompleteMultiModalFileURL(messages);

    List<VariableVal> variableVals = ManageConvertor.batchVariableValDTO2DO(request.getVariableVals());

    messages = promptService.formatPrompt(prompt, messages, variableVals);

    List<com.iwhalecloud.bote.llm.client.dto.message.Message> boteMessages = LlmConvertor.toMessage(messages);

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().messages(boteMessages).build();
    chatCompletionRequest.setTopP(modelConfig.getTopP());
    chatCompletionRequest.setTemperature(modelConfig.getTemperature());
    chatCompletionRequest.setMaxTokens(model.getMaxTokens());
    chatCompletionRequest.setFrequencyPenalty(modelConfig.getFrequencyPenalty());
    if (ToolChoiceType.AUTO.equals(promptDetail.getToolCallConfig().getToolChoice())) {
      chatCompletionRequest.setTools(convertTools(promptDetail.getTools()));
    }
    LocalDateTime startTime = LocalDateTime.now();

    String userId = SessionContext.getCurrentUserId();

    SseEmitterConsumer consumer = SseEmitterConsumer.builder()
      .chatCompletionRequest(chatCompletionRequest)
      .llmClient(llmClient)
      .mockTools(buildMockTools(request.getMockTools()))
      .singleStepDebug(request.getSingleStepDebug())
      .replyConsumer(reply -> saveDebugLog(prompt, startTime, reply, null, request.getSingleStepDebug(), userId))
      .build();

    return SseUtil.createSseEmitter(clientId, emitter -> {
      try {
        consumer.accept(emitter);
        SseUtil.sendText(emitter, ChatMessageType.DONE, ChatConsts.COMPLETIONS_DONE);
      }
      catch (Exception e) {
        logger.error("Failed to debug prompt", e);
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    });
  }

  private Map<String, String> buildMockTools(List<MockToolDTO> mockTools) {
    if (mockTools == null) {
      return null;
    }
    Map<String, String> mockTool = new HashMap<>();
    for (MockToolDTO tool : mockTools) {
      mockTool.put(tool.getName(), tool.getMockResponse());
    }
    return mockTool;
  }

  private List<com.iwhalecloud.bote.llm.client.dto.Tool> convertTools(List<Tool> tools) {
    if (tools == null || tools.isEmpty()) {
      return null;
    }
    List<com.iwhalecloud.bote.llm.client.dto.Tool> toolList = new ArrayList<>();
    for (Tool tool : tools) {
      Function function = new Function();
      function.setName(tool.getFunction().getName());
      function.setDescription(tool.getFunction().getDescription());
      function.setParameters(JsonUtil.parseJsonRequired(tool.getFunction().getParameters(), JsonSchemaNode.class));
      toolList.add(new com.iwhalecloud.bote.llm.client.dto.Tool(function));
    }
    return toolList;
  }

  /**
   * 保存调试日志
   * 迁移对应关系: Go语言saveDebugLog
   */
  private void saveDebugLog(Prompt prompt, LocalDateTime startTime, Reply result,
                            Exception err, Boolean singleStepDebug, String userId) {
    try {
      int errCode = 0;
      if (err != null) {
        errCode = 500; // 内部错误码
        // 可以进一步解析业务错误码
      }

      long inputTokens = 0;
      long outputTokens = 0;
      long debugID = 0;
      int debugStep = 1;

      if (result != null) {
        if (result.getItem() != null && result.getItem().getTokenUsage() != null) {
          inputTokens = result.getItem().getTokenUsage().getInputTokens();
          outputTokens = result.getItem().getTokenUsage().getOutputTokens();
        }
        debugID = result.getDebugId() == null ? 999999999L : result.getDebugId();
        debugStep = result.getDebugStep() == null ? 0 : result.getDebugStep();
      }

      // 非单步调试，debug step记录为1，方便查询调试历史列表
      if (singleStepDebug == null || !singleStepDebug) {
        debugStep = 1;
      }

      LocalDateTime endTime = LocalDateTime.now();
      long costMS = java.time.Duration.between(startTime, endTime).toMillis();

      DebugLog debugLog = DebugLog.builder()
        .promptId(prompt.getId())
        .spaceId(prompt.getSpaceId())
        .promptKey(prompt.getPromptKey())
        .version(prompt.getVersion())
        .inputTokens(inputTokens)
        .outputTokens(outputTokens)
        .startedAt(startTime)
        .endedAt(endTime)
        .costMs(costMS)
        .statusCode(errCode)
        .debuggedBy(userId)
        .debugId(debugID)
        .debugStep(debugStep)
        .build();

      debugLogRepo.saveDebugLog(debugLog);

    }
    catch (Exception e) {
      // 记录日志保存失败，但不影响主流程
      logger.error("保存调试日志失败: {}", e.getMessage(), e);
    }
  }

  @Override
  public SaveDebugContextResponse saveDebugContext(SaveDebugContextRequest request) {
    // TODO 权限校验

    // TODO 会话信息
    String userId = SessionContext.getCurrentUserId();

    DebugContext debugContext = DebugContextConvertor.debugContextDTO2DO(
      request.getPromptId(), userId, request.getDebugContext());

    debugContextRepo.saveDebugContext(debugContext);

    return SaveDebugContextResponse.builder().build();
  }

  @Override
  public GetDebugContextResponse getDebugContext(GetDebugContextRequest request) {
    String userId = SessionContext.getCurrentUserId();
    DebugContext debugContext = debugContextRepo.getDebugContext(request.getPromptId(), userId);
    if (debugContext == null) {
      return null;
    }
    // 完成多模态文件URL
    mCompleteDebugContextMultiModalFileURL(debugContext);
    return GetDebugContextResponse.builder()
      .debugContext(DebugContextConvertor.debugContextDO2DTO(debugContext))
      .build();
  }

  /**
   * 完成调试上下文多模态文件URL
   * 迁移对应关系: Go语言mCompleteDebugContextMultiModalFileURL
   */
  private void mCompleteDebugContextMultiModalFileURL(DebugContext debugContext) {
    if (debugContext == null) {
      return;
    }

    List<DebugMessage> messages = collectAllDebugMessages(debugContext);
    List<String> fileKeys = extractFileKeysFromMessages(messages);

    if (fileKeys.isEmpty()) {
      return;
    }

    Map<String, String> urlMap = getFileUrlMap(fileKeys);
    fillBackUrlsToMessages(messages, urlMap);
  }

  private List<DebugMessage> collectAllDebugMessages(DebugContext debugContext) {
    List<DebugMessage> messages = new ArrayList<>();

    collectDebugCoreMessages(debugContext, messages);
    collectCompareConfigMessages(debugContext, messages);

    return messages;
  }

  private void collectDebugCoreMessages(DebugContext debugContext, List<DebugMessage> messages) {
    if (debugContext.getDebugCore() != null && debugContext.getDebugCore().getMockContexts() != null) {
      messages.addAll(debugContext.getDebugCore().getMockContexts());
    }
  }

  private void collectCompareConfigMessages(DebugContext debugContext, List<DebugMessage> messages) {
    if (debugContext.getCompareConfig() != null && debugContext.getCompareConfig().getGroups() != null) {
      for (var group : debugContext.getCompareConfig().getGroups()) {
        if (group != null && group.getDebugCore() != null) {
          messages.addAll(group.getDebugCore().getMockContexts());
        }
      }
    }
  }

  private List<String> extractFileKeysFromMessages(List<DebugMessage> messages) {
    List<String> fileKeys = new ArrayList<>();

    for (DebugMessage message : messages) {
      if (message == null || message.getParts() == null || message.getParts().isEmpty()) {
        continue;
      }
      for (var part : message.getParts()) {
        if (part != null && part.getImageUrl() != null) {
          fileKeys.add(part.getImageUrl().getUri());
        }
      }
    }

    return fileKeys;
  }

  private Map<String, String> getFileUrlMap(List<String> fileKeys) {
    // TODO 文件实现
    logger.warn("文件未实现{}", fileKeys);
    // Map<String, String> urlMap = file.mGetFileURL(fileKeys);
    return Maps.newHashMap();
  }

  private void fillBackUrlsToMessages(List<DebugMessage> messages, Map<String, String> urlMap) {
    for (DebugMessage message : messages) {
      if (message == null || message.getParts() == null || message.getParts().isEmpty()) {
        continue;
      }
      fillBackUrlsToMessageParts(message, urlMap);
    }
  }

  private void fillBackUrlsToMessageParts(DebugMessage message, Map<String, String> urlMap) {
    for (var part : message.getParts()) {
      if (part != null && part.getImageUrl() != null) {
        part.getImageUrl().setUri(urlMap.get(part.getImageUrl().getUri()));
      }
    }
  }

  @Override
  public ListDebugHistoryResponse listDebugHistory(ListDebugHistoryRequest request) {
    String userId = SessionContext.getCurrentUserId();
    ListDebugHistoryParam param = ListDebugHistoryParam.builder()
      .promptId(request.getPromptId())
      .userId(userId)
      .daysLimit(request.getDaysLimit())
      .pageSize(request.getPageSize())
      .build();

    var result = debugLogRepo.listDebugHistory(param);

    return ListDebugHistoryResponse.builder()
      .debugHistory(DebugLogConvertor.batchDebugLogDO2DTO(result.getDebugHistory()))
      .hasMore(result.getHasMore())
      .nextPageToken(String.valueOf(result.getNextPageToken()))
      .build();
  }
}
