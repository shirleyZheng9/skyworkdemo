package com.iwhalecloud.bote.adapter.dify.client;

import com.iwhalecloud.bote.adapter.dify.dto.ChatFlowBlockingResponse;
import com.iwhalecloud.bote.adapter.dify.dto.ChatFlowRequest;
import com.iwhalecloud.bote.adapter.dify.dto.KnowledgeDetailDTO;
import com.iwhalecloud.bote.adapter.dify.dto.KnowledgeSearchRequest;
import com.iwhalecloud.bote.adapter.dify.dto.KnowledgeSearchResponse;
import com.iwhalecloud.bote.adapter.dify.dto.RetrievalModelDictDTO;
import com.iwhalecloud.bote.adapter.dify.listener.DifyEventListener;
import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.doc.module.knowledge.helper.KnowledgeChatResponseCollector;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Dify API
 *
 * @author qian.sisheng
 * @since 2025-10-13
 */
@Component
@ConditionalOnBooleanProperty("dify.enabled")
public final class DifyApiHelper {

  private static final DcParamCache dcParamCache = SpringUtil.getBean(DcParamCache.class);
  /** 知识库检索接口地址 */
  private static final String KNOWLEDGE_SEARCH_URL = "/datasets/{dataset_id}/retrieve";
  /** 知识库详情接口地址 */
  private static final String KNOWLEDGE_DETAIL_URL = "/datasets/{dataset_id}";
  /** chatFlow接口地址 */
  private static final String CHAT_FLOW_URL = "/chat-messages";

  private DifyApiHelper() {

  }

  /**
   * 知识库检索
   */
  @Nullable
  public static KnowledgeSearchResponse knowledgeSearch(KnowledgeRecallParamDTO params) {
    Map<String, Object> knowledgeTypeExt = params.getKnowledge().getKnowledgeTypeExt();
    String knowledgeId = MapUtils.getString(knowledgeTypeExt, "difyKnowledgeId");
    if (StringUtils.isEmpty(knowledgeId)) {
      throw new RuntimeException("dify知识库ID为空，请检查知识库配置");
    }
    // 知识库API密钥
    String knowledgeSecret = getDifyKnowledgeApiSecret();
    String searchUrl = getDifyApiUrl() + KNOWLEDGE_SEARCH_URL.replace("{dataset_id}", knowledgeId);
    // 添加请求头API密钥
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + knowledgeSecret);
    KnowledgeSearchRequest request = new KnowledgeSearchRequest();
    request.setQuery(params.getQuery());
    // 设置检索模型
    RetrievalModelDictDTO retrievalModel = buildRetrievalModel(knowledgeId, knowledgeSecret, knowledgeTypeExt);
    retrievalModel.setScoreThresholdEnabled(true);
    retrievalModel.setScoreThreshold(params.getMinScore());
    retrievalModel.setTopK(params.getMaxNum());
    request.setRetrievalModel(retrievalModel);
    return HttpUtil.post(searchUrl, request, new ParameterizedTypeReference<>() {
    }, httpHeaders);
  }

  /**
   * 构建检索模型
   */
  private static RetrievalModelDictDTO buildRetrievalModel(String knowledgeId, String knowledgeSecret, Map<String, Object> knowledgeTypeExt) {
    // 旧版本dify不支持调用查找知识库详情接口，通过参数获取
    String difyKnowledgeDetail = MapUtils.getString(knowledgeTypeExt, "difyKnowledgeDetail");
    if (StringUtils.isNotEmpty(difyKnowledgeDetail)) {
      RetrievalModelDictDTO retrievalModelDict = JsonUtil.parseJson(difyKnowledgeDetail, RetrievalModelDictDTO.class);
      if (retrievalModelDict != null) {
        RetrievalModelDictDTO model = new RetrievalModelDictDTO();
        BeanUtils.copyProperties(retrievalModelDict, model);
        return model;
      }
    }
    // 获取知识库详情
    KnowledgeDetailDTO knowledge = findKnowledge(knowledgeId, knowledgeSecret);
    if (knowledge != null && knowledge.getRetrievalModelDict() != null) {
      return knowledge.getRetrievalModelDict();
    }
    return new RetrievalModelDictDTO();
  }

  /**
   * 知识库详情
   */
  @Nullable
  public static KnowledgeDetailDTO findKnowledge(String knowledgeId, String knowledgeSecret) {
    String detailUrl = getDifyApiUrl() + KNOWLEDGE_DETAIL_URL.replace("{dataset_id}", knowledgeId);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + knowledgeSecret);
    return HttpUtil.get(detailUrl, null, new ParameterizedTypeReference<>() {
    }, httpHeaders);
  }

  /**
   * LLM问答（非流式）
   * <p>通过调用dify chatflow工作流</p>
   */
  @Nullable
  public static ChatFlowBlockingResponse llmChat(ChatCompletionRequest request, String chatFlowSecret) {
    // 添加请求头API密钥
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + chatFlowSecret);
    String chatUrl = getDifyApiUrl() + CHAT_FLOW_URL;
    ChatFlowRequest chatFlowRequest = buildChatFlowRequest(buildUserPrompt(request.getMessages()), "blocking");
    return HttpUtil.post(chatUrl, chatFlowRequest, new ParameterizedTypeReference<>() {
    }, httpHeaders);
  }

  /**
   * LLM问答（流式）
   * <p>通过调用dify chatflow工作流</p>
   */
  public static void llmChatStream(ChatCompletionRequest request, String chatFlowSecret, Consumer<ChatCompletionResponse> partialHandler,
                                   @Nullable Consumer<Object> requestListener) {
    // 添加请求头API密钥
    ChatFlowRequest chatFlowRequest = buildChatFlowRequest(buildUserPrompt(request.getMessages()), "streaming");
    Headers headers = Headers.of(HttpHeaders.AUTHORIZATION, "Bearer " + chatFlowSecret);
    HttpUrl chatApiUrl = HttpUrl.parse(getDifyApiUrl() + CHAT_FLOW_URL);
    Assert.notNull(chatApiUrl, "dify API地址配置错误");
    DifyEventListener listener = DifyEventListener.builder()
      .partialHandler(partialHandler)
      .convertReasoning(request.isConvertReasoning())
      .build();
    ModelHttpClient.sseBlocking("POST", chatApiUrl, headers, chatFlowRequest, requestListener, listener);
  }


  /**
   * 知识问答（非流式）
   * <p>通过调用dify chatflow工作流进行知识问答</p>
   */
  @Nullable
  public static ChatFlowBlockingResponse knowledgeChat(KnowledgeChatParamsDTO params) {
    // 提取工作流API密钥
    String chatFlowSecret = extractChatFlowSecret(params);
    // 添加请求头API密钥
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + chatFlowSecret);
    String chatUrl = getDifyApiUrl() + CHAT_FLOW_URL;
    // 构建请求体
    ChatFlowRequest request = buildChatFlowRequest(params.getQuestion(), "blocking");
    request.setInputs(Map.of("history", buildUserPrompt(params.getHistory())));
    return HttpUtil.post(chatUrl, request, new ParameterizedTypeReference<>() {
    }, httpHeaders);
  }

  /**
   * 知识问答（流式）
   * <p>通过调用dify chatFlow工作流进行知识问答</p>
   */
  public static KnowledgeChatResponse knowledgeChatStream(KnowledgeChatParamsDTO params,
                                                          @Nullable Consumer<SseEvent> partialHandler,
                                                          @Nullable Consumer<Object> requestListener) {
    ChatFlowRequest request = buildChatFlowRequest(params.getQuestion(), "streaming");
    request.setInputs(Map.of("history", buildUserPrompt(params.getHistory())));
    String secret = extractChatFlowSecret(params);
    Headers headers = Headers.of(HttpHeaders.AUTHORIZATION, "Bearer " + secret);
    HttpUrl chatApiUrl = HttpUrl.parse(getDifyApiUrl() + CHAT_FLOW_URL);
    Assert.notNull(chatApiUrl, "dify API地址配置错误");

    KnowledgeChatResponseCollector responseCollector = new KnowledgeChatResponseCollector(partialHandler);
    DifyEventListener listener = DifyEventListener.builder()
      .sseEventHandler(responseCollector)
      .convertReasoning(true)
      .thinkingStrategy(params.getThinkingStrategy())
      .build();
    ModelHttpClient.sseBlocking("POST", chatApiUrl, headers, request, requestListener, listener);
    return responseCollector.getResponse();
  }

  /**
   * 构建用户提示
   */
  private static ChatFlowRequest buildChatFlowRequest(String question, String responseMode) {
    ChatFlowRequest request = new ChatFlowRequest();
    request.setQuery(question);
    request.setResponseMode(responseMode);
    request.setUser("bote");
    return request;
  }

  /**
   * 提取dify工作流API密钥
   */
  private static String extractChatFlowSecret(KnowledgeChatParamsDTO params) {
    Map<String, Object> knowledgeTypeExt = params.getKnowledgeList().getFirst().getKnowledgeTypeExt();
    String chatFlowSecret = MapUtils.getString(knowledgeTypeExt, "difyChatFlowSecret");
    if (StringUtils.isEmpty(chatFlowSecret)) {
      throw new BssException("dify工作流API密钥为空，请检查知识库配置");
    }
    return chatFlowSecret;
  }

  /**
   * 获取Dify API地址
   */
  private static String getDifyApiUrl() {
    String difyApiUrl = dcParamCache.getDcParamValByCode("DIFY_API_URL");
    if (StringUtils.isEmpty(difyApiUrl)) {
      throw new RuntimeException("未配置dify API地址，请联系管理员");
    }
    return StringUtils.stripEnd(difyApiUrl, "/");
  }

  /**
   * 获取Dify知识库API密钥
   */
  private static String getDifyKnowledgeApiSecret() {
    String difyKnowledgeApiSecret = dcParamCache.getDcParamValByCode("DIFY_KNOWLEDGE_API_SECRET");
    if (StringUtils.isEmpty(difyKnowledgeApiSecret)) {
      throw new RuntimeException("未配置dify知识库API密钥，请联系管理员");
    }
    return difyKnowledgeApiSecret;
  }

  /**
   * 构造用户提示词
   */
  private static String buildUserPrompt(@Nullable List<Message> historyMessages) {
    if (CollectionUtils.isEmpty(historyMessages)) {
      historyMessages = new ArrayList<>();
    }
    if (historyMessages.size() == 1) {
      Message message = historyMessages.getFirst();
      if (message instanceof SystemMessage) {
        return ((SystemMessage) message).getContent();
      }
      else if (message instanceof UserMessage) {
        Assert.isTrue(((UserMessage) message).getContent() instanceof String, "用户消息只支持文本内容");
        return (String) ((UserMessage) message).getContent();
      }
      else {
        throw new BssException("只有一条消息时类型必须是系统消息或用户消息，实际是 " + message.getRole());
      }
    }

    StringBuilder sb = new StringBuilder("以下是大模型和用户之间的会话记录(system 表示系统提示词，user 表示用户消息，assistant 表示大模型的回复，tool 表示工具调用的结果)，请回答最后一条用户消息:");
    String systemPrompt = "";
    for (Message message : historyMessages) {
      switch (message.getRole()) {
        case MessageRole.SYSTEM:
          systemPrompt = StringUtils.trimToEmpty(((SystemMessage) message).getContent());
          sb.append("<system>").append(systemPrompt).append("</system>\n");
          break;
        case MessageRole.USER:
          Assert.isTrue(((UserMessage) message).getContent() instanceof String, "用户消息只支持文本内容");
          sb.append("<user>").append(StringUtils.trimToEmpty((String) ((UserMessage) message).getContent())).append("</user>\n");
          break;
        case MessageRole.ASSISTANT:
          sb.append("<assistant>").append(StringUtils.trimToEmpty(((AssistantMessage) message).getContent())).append("</assistant>\n");
          break;
        case MessageRole.FUNCTION:
          sb.append("<tool name=\"").append(((FunctionMessage) message).getName()).append("\">")
            .append(StringUtils.trimToEmpty(((FunctionMessage) message).getContent()))
            .append("</tool>\n");
          break;
        default:
          throw new BssException("未知的消息类型: " + message.getRole());
      }
    }
    return systemPrompt + "\n" + sb.toString().trim();
  }


}

