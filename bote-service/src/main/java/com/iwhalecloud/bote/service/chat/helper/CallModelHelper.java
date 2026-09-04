package com.iwhalecloud.bote.service.chat.helper;

import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.dto.search.response.BochaSearchResponse.WebPageInfoGroup;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.service.bot.IBotQueryService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.search.IWebSearchService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 调用大模型辅助类
 *
 * @author chen.linfa
 * @since 2025-03-14
 */
@Component
@RequiredArgsConstructor
public class CallModelHelper {
  // @formatter:off
  private final QuerySessionMsgHelper querySessionMsgHelper;
  private final IWebSearchService webSearchService;
  private final IBotQueryService botQueryService;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final ModelClientCache modelClientCache;
  private final SelectSceneHelper selectSceneHelper;
  // @formatter:on

  /**
   * <p>支持对话上下文</p>
   * <p>判断是否需要联网搜索</p>
   * <p>通过大模型总结召回信息</p>
   */
  public void invoke(ChatContext context, ChatTraceLogBuilder log, boolean firstEnter) {
    List<Message> history = querySessionMsgHelper.getHistory(context.getSessionId(), context.getContextId());
    LlmClient client = getLlmClient(context);

    String query = context.getUserMessage().getContent();
    String userMessage = query;
    WebPageInfoGroup pageInfo = null;
    // 联网搜索开关
    if (BooleanUtils.isTrue(context.getRequest().getWebSearchEnabled())) {
      boolean needSearch = true;
      if (!history.isEmpty()) {
        needSearch = analyzeWebSearch(context, client, history, query);
      }
      if (needSearch) {
        log.addLog("联网搜索问题：" + query);
        ResultVO<List<WebPageInfo>> result = webSearchService.run(query);
        if (!result.isSuccess()) {
          log.failed().addLog(result.getResultMsg());
          context.sendMessage(ChatMessageType.TEXT, result.getResultMsg());
          return;
        }
        // 将召回的页面信息组装到提示词，交由大模型提炼总结
        pageInfo = new WebPageInfoGroup();
        pageInfo.setTitle(query);
        pageInfo.setList(result.getResultObject());
        String template = SystemParameter.WEB_SEARCH_PROMPT.getValueFromDb();
        Map<String, Object> params = new HashMap<>(2);
        params.put("pageInfos", JsonUtil.toJsonString(pageInfo.getList()));
        params.put("question", query);
        userMessage = FreemarkerUtil.process(template, params);
      }
    }
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addMessages(history)
      .addUserMessage(userMessage)
      .tenantId(context.getTenantId())
      .botId(context.getBotId())
      .sessionId(context.getSessionId())
      .sourceFrom(ModelConsts.SOURCE_LLM)
      .build();
    // 首次大模型对话，带上推荐智能体列表
    List<RecommendedSceneDTO> scenes = new ArrayList<>();
    if (firstEnter && BooleanUtils.isNotTrue(context.getRequest().getCommonFlag())) {
      scenes = selectSceneHelper.invoke(context, log);
    }
    chat(context, client, request, pageInfo, scenes, log);
  }

  /**
   * 分析使用的大模型
   * <p>1. 首页对话，使用平台预置的大模型 </p>
   * <p>2. 应用对话，使用租户应用预置的大模型 </p>
   */
  private LlmClient getLlmClient(ChatContext context) {
    ChatRequestDTO request = context.getRequest();
    if (BooleanUtils.isTrue(request.getCommonFlag())) {
      String modelIdStr = SystemParameter.COMMON_CHAT_MODEL_ID.getValueFromDb();
      Assert.hasLength(modelIdStr, "未配置平台通用对话功能使用的大模型 ID");
      return modelClientCache.getLlmClient(BaseConsts.DEFAULT_TENANT_ID, Long.parseLong(modelIdStr));
    }
    else {
      Long modelId = tenantSettingInfoCache.getModelId(context.getTenantId());
      return modelClientCache.getLlmClient(context.getTenantId(), modelId);
    }
  }

  /**
   * 分析是否需要退出大模型对话
   */
  public boolean analyzeExit(ChatContext context, ChatTraceLogBuilder log) {
    if (BooleanUtils.isTrue(context.getRequest().getCommonFlag())) {
      // 首页大模型对话，无需分析
      return false;
    }
    List<SceneIntentDTO> scenes = botQueryService.querySceneListForIntent(context.getTenantId(), context.getBotId());
    if (CollectionUtils.isEmpty(scenes)) {
      return false;
    }
    List<Message> history = querySessionMsgHelper.getHistory(context.getSessionId(), context.getContextId());
    Long modelId = tenantSettingInfoCache.getModelId(context.getTenantId());
    LlmClient client = modelClientCache.getLlmClient(context.getTenantId(), modelId);

    String template = SystemParameter.ANALYZE_EXIT_MODEL_PROMPT.getValueFromDb();
    Map<String, Object> params = new HashMap<>();
    params.put("question", context.getUserMessage().getContent());
    params.put("agents", scenes);
    String prompt = FreemarkerUtil.process(template, params);
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addMessages(history)
      .addUserMessage(prompt)
      .tenantId(context.getTenantId())
      .botId(context.getBotId())
      .sessionId(context.getSessionId())
      .sourceFrom(ModelConsts.SOURCE_LLM)
      .build();
    ChatCompletionResponse response = client.chatCompletion(request);
    String content = response.getMessageContent();
    boolean exit = StringUtils.isNotEmpty(content) && content.contains("1");
    if (exit) {
      log.addLog("根据消息内容，分析出需要退出大模型对话");
    }
    return exit;
  }

  /**
   * 分析是否需要联网处理
   */
  private boolean analyzeWebSearch(ChatContext context, LlmClient client, List<Message> history, String query) {
    String template = SystemParameter.ANALYZE_WEB_SEARCH_PROMPT.getValueFromDb();
    Map<String, Object> params = new HashMap<>(2);
    params.put("question", query);
    String prompt = FreemarkerUtil.process(template, params);
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addMessages(history)
      .addUserMessage(prompt)
      .tenantId(context.getTenantId())
      .botId(context.getBotId())
      .sessionId(context.getSessionId())
      .sourceFrom(ModelConsts.SOURCE_LLM)
      .build();
    ChatCompletionResponse response = client.chatCompletion(request);
    String content = response.getMessageContent();
    return StringUtils.isNotEmpty(content) && content.contains("1");
  }

  /**
   * 大模型基于召回数据，总结内容，流式输出
   */
  private void chat(ChatContext context, LlmClient client, ChatCompletionRequest request, @Nullable WebPageInfoGroup pageInfo,
                    List<RecommendedSceneDTO> scenes, ChatTraceLogBuilder log) {
    Date startTime = new Date();
    Long msgId = context.newMsgId();
    String msgIdStr = msgId.toString();
    Consumer<ChatCompletionResponse> eventHandler = response -> {
      String reasoningContent = response.getDeltaReasoningContent();
      if (StringUtils.isNotEmpty(reasoningContent)) {
        SseUtil.sendJson(context.getEmitter(), ChatMessageType.REASONING, msgIdStr, reasoningContent);
      }
      String content = response.getDeltaContent();
      if (StringUtils.isNotEmpty(content)) {
        SseUtil.sendJson(context.getEmitter(), ChatMessageType.TEXT, msgIdStr, content);
      }
    };

    // 调用大模型
    ChatCompletionResponse response = client.chatCompletionStreamBlockingAndCollect(request, eventHandler, SseUtil.requestListener);
    log.output(response);

    // 记录回复内容
    AnswerDTO answer = new AnswerDTO(msgIdStr, null, null);
    answer.setReasoning(response.getReasoningContent());
    answer.setText(response.getMessageContent());
    context.addStreamMessage(msgId, startTime, answer);

    // 推荐智能体，只发送给前端，不记录到消息表
    if (CollectionUtils.isNotEmpty(scenes)) {
      SseUtil.sendJson(context.getEmitter(), ChatMessageType.SELECT_SCENE, msgIdStr, SseEvent.ofRecommendedScenes(scenes).getMsgContent());
    }
    // 网页
    if (pageInfo != null) {
      context.sendMessage(ChatMessageType.WEB_PAGE_INFO, Collections.singletonList(pageInfo));
    }
    context.sendDoneMessage();
  }
}
