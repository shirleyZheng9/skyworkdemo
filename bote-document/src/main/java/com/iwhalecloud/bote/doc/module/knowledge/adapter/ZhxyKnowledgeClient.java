package com.iwhalecloud.bote.doc.module.knowledge.adapter;

import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.DocChainAdapter.ChatStreamPartialHandler;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.doc.module.knowledge.helper.KnowledgeChatResponseCollector;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgePlatformApiService;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallImageItem;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse.KnowledgeItem;
import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse.KnowledgeScoreItem;
import com.iwhalecloud.bote.dto.knowledge.access.platform.DocResourceDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeCompleteRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeCompleteRequest.Content;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeCompleteRequest.Message;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeSearchRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeCompleteResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeCompleteResponse.StreamChoice;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeCompleteResponse.StreamChoiceDelta;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeCompleteResponse.ToolCalls;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeSearchResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeSearchResponse.DataBlock;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeSearchResponse.ResultItem;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.ResultResponse;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionChoice;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.FunctionCall;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 知识中台客户端
 *
 * @author lxs
 * @since 2025/07/21
 */
@Component
@ConditionalOnBooleanProperty("zhxy.enabled")
@RequiredArgsConstructor
public class ZhxyKnowledgeClient implements KnowledgeClient {

  @Value("${zhxy.apiUrl:}")
  private String apiUrl;

  /** 文档引用前缀 */
  private static final String REFERENCES_PREFIX = "```docchain-references\n";
  /** 公网 DocChain 返回的文档引用前缀 */
  private static final String REFERENCES_PREFIX2 = "```llmdoc-references\n";

  private final DocChainAdapter docChainAdapter;
  private final IKnowledgePlatformApiService knowledgePlatformApiService;

  @Override
  public int getOrder() {
    return 3000;
  }

  @Override
  public String getKnowledgeType() {
    return KnowledgeConsts.KNOWLEDGE_TYPE_PLATFORM;
  }

  @Override
  public KnowledgeRecallResponse recall(KnowledgeRecallParamDTO params) {
    String query = params.getQuery();
    List<DocResourceDTO> docResourceList = transferDocResourceList(params.getKnowledgeIds(), params.getResourceItems());
    // 必填校验
    Assert.hasLength(query, "问句不能为空");
    Assert.notEmpty(docResourceList, "文档资源列表不能为空");
    // 组装参数
    KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
    searchRequest.setQuery(query);
    searchRequest.setDocResourceList(docResourceList);
    searchRequest.setScore(params.getMinScore());
    searchRequest.setSize(params.getMaxNum());
    ResultResponse<KnowledgeSearchResponse> searchResult = knowledgePlatformApiService.knowledgeSearch(searchRequest);
    if (!searchResult.isSuccess()) {
      return new KnowledgeRecallResponse();
    }
    KnowledgeSearchResponse response = searchResult.getData();
    KnowledgeRecallResponse finalResponse = new KnowledgeRecallResponse();
    if (CollectionUtils.isNotEmpty(response.getText())) {
      finalResponse.setText(response.getText().stream()
        // 忽略非法数据
        .filter(scoreItem -> scoreItem.getData() != null && scoreItem.getData().getDocId() != null)
        .map(scoreItem -> new KnowledgeRecallTextItem(convertKnowledgeScoreItem(scoreItem)))
        .collect(Collectors.toList()));
    }
    if (CollectionUtils.isNotEmpty(response.getImage())) {
      finalResponse.setImage(response.getImage().stream()
        // 忽略非法数据
        .filter(scoreItem -> scoreItem.getData() != null && scoreItem.getData().getDocId() != null)
        .map(scoreItem -> new KnowledgeRecallImageItem(convertKnowledgeScoreItem(scoreItem)))
        .collect(Collectors.toList()));
    }
    return finalResponse;
  }

  private List<DocResourceDTO> transferDocResourceList(List<Long> knowledgeIds, List<ResourceExtItem> resourceItems) {
    List<DocResourceDTO> docResourceList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(knowledgeIds)) {
      // 转为 List<DocResourceDTO>
      for (Long knowledgeId : knowledgeIds) {
        DocResourceDTO docResourceDTO = new DocResourceDTO();
        docResourceDTO.setResourceWid(knowledgeId.toString());
        docResourceDTO.setResourceType(KnowledgeConsts.KNOW_BASE);
        docResourceList.add(docResourceDTO);
      }
    }
    if (CollectionUtils.isNotEmpty(resourceItems)) {
      // 转为 List<DocResourceDTO>
      for (ResourceExtItem item : resourceItems) {
        DocResourceDTO docResourceDTO = new DocResourceDTO();
        docResourceDTO.setResourceWid(item.getResourceWid());
        docResourceDTO.setResourceType(item.getResourceType());
        docResourceList.add(docResourceDTO);
      }
    }
    return docResourceList;
  }

  private KnowledgeScoreItem convertKnowledgeScoreItem(ResultItem scoreItem) {
    KnowledgeScoreItem knowledgeScoreItem = new KnowledgeScoreItem();
    // 分数转换
    knowledgeScoreItem.setScore(scoreItem.getScore() != null ? scoreItem.getScore().doubleValue() : null);
    // DataBlock -> KnowledgeItem
    if (scoreItem.getData() != null) {
      DataBlock data = scoreItem.getData();
      KnowledgeItem knowledgeItem = new KnowledgeItem();
      knowledgeItem.setDocId(data.getDocId() != null ? scoreItem.getData().getDocId().longValue() : null);
      knowledgeItem.setHeading(data.getHeadingChain());
      knowledgeItem.setContent(data.getContent());
      knowledgeItem.setSummary(data.getSummary());
      knowledgeItem.setType(data.getType());
      knowledgeItem.setUrl(data.getUrl());
      knowledgeScoreItem.setData(knowledgeItem);
    }
    return knowledgeScoreItem;
  }

  @Override
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params) {
    KnowledgeCompleteRequest completeRequest = getKnowledgeCompleteRequest(params);
    completeRequest.setStream(false);
    ResultResponse<List<KnowledgeCompleteResponse>> completeResult = knowledgePlatformApiService.knowledgeComplete(completeRequest);
    if (!completeResult.isSuccess()) {
      KnowledgeChatResponse chatResponse = new KnowledgeChatResponse();
      chatResponse.setAnswer(completeResult.getMessage());
      return chatResponse;
    }
    ChatCompletionResponse chatReturn = convertToChatCompletion(completeResult.getData().get(0));
    Assert.notEmpty(chatReturn.getChoices(), "调用 DocChain 失败, choices 为空");
    chatReturn.assertSuccess();
    String messageContent = chatReturn.getMessageContent();
    Pair<String, List<ReferenceDocumentDTO>> pair = docChainAdapter.splitAnswerAndReferences(params.getTenantId(), messageContent, null);
    KnowledgeChatResponse response = new KnowledgeChatResponse();
    response.setAnswer(pair.getLeft());
    response.setReferences(pair.getRight());
    return response;
  }

  private KnowledgeCompleteRequest getKnowledgeCompleteRequest(KnowledgeChatParamsDTO params) {
    String question = params.getQuestion();
    List<DocResourceDTO> docResourceList = transferDocResourceList(params.getKnowledgeIds(), params.getResourceItems());
    // 必填校验
    Assert.hasLength(question, "问句不能为空");
    Assert.notEmpty(docResourceList, "文档资源列表不能为空");
    // 组装参数
    KnowledgeCompleteRequest completeRequest = new KnowledgeCompleteRequest();
    completeRequest.setDocResourceList(docResourceList);
    Content content = Content.builder().type("text").text(question).build();
    Message message = Message.builder().content(Collections.singletonList(content)).role("user").build();
    completeRequest.setMessages(Collections.singletonList(message));
    return completeRequest;
  }

  private ChatCompletionResponse convertToChatCompletion(KnowledgeCompleteResponse data) {
    if (data == null) {
      return null;
    }
    ChatCompletionResponse resp = new ChatCompletionResponse();
    resp.setId(data.getId());
    resp.setObject(data.getObject());
    resp.setCreated(data.getCreated());
    resp.setModel(data.getModel());
    // usage 映射
    if (data.getUsage() != null) {
      Usage usage = new Usage();
      usage.setPromptTokens(data.getUsage().getPrompt_tokens());
      usage.setCompletionTokens(data.getUsage().getCompletion_tokens());
      usage.setTotalTokens(data.getUsage().getTotal_tokens());
      resp.setUsage(usage);
    }
    // choices 映射
    if (data.getChoices() != null) {
      List<ChatCompletionChoice> choices = new ArrayList<>();
      for (StreamChoice c : data.getChoices()) {
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(c.getIndex());
        choice.setFinishReason(c.getFinish_reason());
        // message
        if (c.getMessage() != null) {
          choice.setMessage(convertToAssistantMessage(c.getMessage()));
        }
        // delta
        if (c.getDelta() != null) {
          choice.setDelta(convertToAssistantMessage(c.getDelta()));
        }
        choices.add(choice);
      }
      resp.setChoices(choices);
    }
    return resp;
  }

  private AssistantMessage convertToAssistantMessage(StreamChoiceDelta src) {
    if (src == null) {
      return null;
    }
    AssistantMessage msg = new AssistantMessage();
    msg.setContent(src.getContent());
    // tool_calls 映射
    if (src.getTool_calls() != null && !src.getTool_calls().isEmpty()) {
      List<ToolCall> toolCalls = new ArrayList<>();
      for (ToolCalls tc : src.getTool_calls()) {
        ToolCall toolCall = new ToolCall();
        toolCall.setId(tc.getId());
        if (tc.getFunction() != null) {
          FunctionCall func = new FunctionCall();
          func.setName(tc.getFunction().getName());
          func.setArguments(tc.getFunction().getArguments());
          toolCall.setFunction(func);
        }
        toolCalls.add(toolCall);
      }
      msg.setToolCalls(toolCalls);
    }
    return msg;
  }

  @Override
  public EventSource chatStream(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(params, partialHandler, completionHandler, this::chatStreamBlocking);
  }

  @Override
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params, @Nullable Consumer<SseEvent> eventHandler, @Nullable Consumer<Object> requestListener) {
    KnowledgeCompleteRequest request = getKnowledgeCompleteRequest(params);
    request.setStream(true);
    Headers headers = knowledgePlatformApiService.getChatStreamHeaders(request);
    HttpUrl chatApiUrl = HttpUrl.parse(StringUtils.stripEnd(apiUrl, "/") + "/open-api/chat/completionsWithStream");
    Assert.notNull(chatApiUrl, "知识中台地址配置错误");

    KnowledgeChatResponseCollector responseCollector = new KnowledgeChatResponseCollector(eventHandler);
    ChatStreamPartialHandler partialHandler = new ChatStreamPartialHandler(responseCollector, params);
    // 调用接口
    ModelHttpClient.chatCompletionsStream(chatApiUrl, headers, request, partialHandler, requestListener);
    partialHandler.onFinish();
    return responseCollector.getResponse();
  }

}
