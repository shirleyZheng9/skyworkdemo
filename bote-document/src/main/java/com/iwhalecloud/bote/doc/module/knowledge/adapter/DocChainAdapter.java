package com.iwhalecloud.bote.doc.module.knowledge.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.ThinkingStrategy;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.DocChainApiUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.doc.module.knowledge.helper.KnowledgeChatResponseCollector;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainLoginHelper;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.ReferenceChunkDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainChatLogDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainDocDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainLinkDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainReferenceDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.ZcmDocDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.KnowledgeChatLogResponse;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.sse.EventSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * DocChain 适配器
 *
 * @author bianjp
 * @since 2024-10-08
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocChainAdapter {
  private static final Logger logger = LoggerFactory.getLogger(DocChainAdapter.class);
  /** 文档引用前缀 */
  private static final String REFERENCES_PREFIX = "```docchain-references\n";
  /** 公网 DocChain 返回的文档引用前缀 */
  private static final String REFERENCES_PREFIX2 = "```llmdoc-references\n";
  private static final String REFERENCES_SUFFIX = "\n```";
  /** 引用到的文档 ID列表 正则匹配 */
  private static final Pattern DOC_ID_PATTERN = Pattern.compile("<!--docChainDocId:\\s*(.*?)-->");
  private static final String DOC_ID_PREFIX = "<!--docChainDocId:";

  private final DocChainProperties docChainProperties;
  private final DocChainLoginHelper docChainLoginHelper;
  /** 研发云文档中心的文档路径缓存。key 为租户 ID, value 为文档 ID -> 文档路径的映射。注意文档 ID 是指文档中心的文档 ID, 不是 DocChain 的文档 ID */
  private final LoadingCache<@NonNull Long, @NonNull Map<Integer, String>> tenantDocPathCache = CacheBuilder.newBuilder()
    .maximumSize(50)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build(CacheLoader.from(this::buildZcmDocPathMap));

  private final DocChainDocumentHelper docChainDocumentHelper;

  /**
   * 知识问答
   *
   * @param params 问答参数
   * @param topicIds 主题列表
   * @param docIds 文档列表
   * @return 回复内容
   */
  public KnowledgeChatResponse chat(KnowledgeChatParamsDTO params, List<String> topicIds, List<String> docIds) {
    Long tenantId = params.getTenantId();
    Map<String, Object> request = buildChatParams(params, topicIds, docIds, false);
    HttpUrl chatApiUrl = docChainProperties.getChatApiUrl(tenantId);
    Headers headers = docChainLoginHelper.buildOkHttpHeaders(tenantId);
    ChatCompletionResponse response = ModelHttpClient.post(chatApiUrl, headers, request, SseUtil.requestListener);
    Assert.notEmpty(response.getChoices(), "调用 DocChain 失败, choices 为空");
    response.assertSuccess();

    Pair<String, List<ReferenceDocumentDTO>> pair = splitAnswerAndReferences(tenantId, response.getMessageContent(), params.getPromptTemplate());
    return KnowledgeChatResponse.builder()
      .chatLogId(response.getId())
      .reasoning(response.getMessage().getReasoningContent())
      .answer(pair.getLeft())
      .references(pair.getRight())
      .build();
  }

  /**
   * 知识问答，流式响应
   *
   * @param params 问答参数
   * @param topicIds 主题列表
   * @param docIds 文档列表
   * @param partialHandler 片段处理器
   * @param completionHandler 完成回调，参数为异常对象（成功时为 null）
   */
  public EventSource chatStream(KnowledgeChatParamsDTO params, List<String> topicIds, List<String> docIds, Consumer<SseEvent> partialHandler,
                                Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(params, partialHandler, completionHandler,
      (request, eventHandler, requestListener) -> chatStreamBlocking(request, topicIds, docIds, eventHandler, requestListener));
  }

  /**
   * 知识问答，流式响应 - 同步阻塞模式
   *
   * @param params 问答参数
   * @param topicIds 主题列表
   * @param docIds 文档列表
   * @param eventHandler 事件处理器
   */
  public KnowledgeChatResponse chatStreamBlocking(KnowledgeChatParamsDTO params, List<String> topicIds, List<String> docIds,
                                                  @Nullable Consumer<SseEvent> eventHandler,
                                                  @Nullable Consumer<Object> requestListener) {
    Long tenantId = params.getTenantId();
    Map<String, Object> request = buildChatParams(params, topicIds, docIds, true);
    HttpUrl chatApiUrl = docChainProperties.getChatApiUrl(tenantId);

    KnowledgeChatResponseCollector responseCollector = new KnowledgeChatResponseCollector(eventHandler);
    ChatStreamPartialHandler partialHandler = new ChatStreamPartialHandler(responseCollector, params);
    // 调用接口
    ModelHttpClient.chatCompletionsStream(chatApiUrl, docChainLoginHelper.buildOkHttpHeaders(tenantId), request, partialHandler, requestListener);
    partialHandler.onFinish();
    return responseCollector.getResponse();
  }

  /**
   * 构造会话请求参数
   */
  private Map<String, Object> buildChatParams(KnowledgeChatParamsDTO params, List<String> topicIds, List<String> docIds, boolean stream) {
    List<Message> messages = new ArrayList<>();
    List<Message> history = params.getHistory();
    if (CollectionUtils.isNotEmpty(history)) {
      messages.addAll(filterHistoryMessages(history));
    }
    messages.add(new UserMessage(params.getQuestion()));
    // https://docs.iwhalecloud.com/doi/cjtgmr/docchain-doc-space/interface-manual/dialogue-recall-api
    Map<String, Object> request = new HashMap<>();
    // 主题 ID，逗号分隔
    request.put("model", String.join(",", topicIds));
    // 消息列表
    request.put("messages", messages);
    // 提示词模板
    if (StringUtils.isNotEmpty(params.getPromptTemplate())) {
      request.put("prompt_template", params.getPromptTemplate());
    }
    // 是否流式返回
    request.put("stream", stream);
    // 是否返回参考文档
    request.put("source", params.isWithReferences());
    if (MapUtils.isNotEmpty(params.getModelInfo())) {
      // 自定义大模型信息
      request.put("llm_info", params.getModelInfo());
    }
    if (CollectionUtils.isNotEmpty(docIds)) {
      // 文档 ID，逗号分隔
      request.put("doc_id_list", docIds);
    }
    return request;
  }

  /**
   * 过滤历史消息
   */
  private List<Message> filterHistoryMessages(List<Message> historyMessages) {
    List<Message> filteredMessages = new ArrayList<>(historyMessages.size());
    // 忽略空消息
    for (Message message : historyMessages) {
      if (isNonEmptyMessage(message)) {
        filteredMessages.add(message);
      }
    }

    // 忽略末尾的所有用户消息（如果末尾有多条用户消息会影响问答结果）
    if (!filteredMessages.isEmpty() && filteredMessages.get(filteredMessages.size() - 1) instanceof UserMessage) {
      for (int i = filteredMessages.size() - 2; i >= 0; i--) {
        if (!(filteredMessages.get(i) instanceof UserMessage)) {
          return filteredMessages.subList(0, i + 1);
        }
      }
      return Collections.emptyList();
    }
    return filteredMessages;
  }

  /**
   * 判断消息是否不为空
   */
  private boolean isNonEmptyMessage(Message message) {
    if (message instanceof UserMessage) {
      return ObjectUtils.isNotEmpty(((UserMessage) message).getContent());
    }
    else if (message instanceof AssistantMessage) {
      return StringUtils.isNotEmpty(((AssistantMessage) message).getContent());
    }
    return false;
  }

  /**
   * 分离回复和参考文档
   *
   * @param tenantId 租户 ID
   * @param content DocChain 的回复内容
   * @param prompt 问答的提示词
   * @return 回复和参考文档
   */
  public Pair<String, List<ReferenceDocumentDTO>> splitAnswerAndReferences(Long tenantId, String content, @Nullable String prompt) {
    if (StringUtils.isEmpty(content)) {
      return Pair.of("", Collections.emptyList());
    }
    String answer = StringUtils.stripEnd(StringUtils.substringBefore(StringUtils.substringBefore(content, REFERENCES_PREFIX), REFERENCES_PREFIX2), null);
    List<ReferenceDocumentDTO> references = parseReferences(tenantId, content, prompt);
    if (answer.contains(DOC_ID_PREFIX)) {
      // 正文剔除 <!--docChainDocId:-->
      answer = DOC_ID_PATTERN.matcher(answer).replaceAll("").trim();
    }
    return Pair.of(answer, references);
  }

  /**
   * 解析参考文档
   */
  public List<ReferenceDocumentDTO> parseReferences(Long tenantId, String content, @Nullable String prompt) {
    // 提取引用
    DocChainReferenceDTO docChainReferenceInfo = extractDocChainReference(content);
    if (docChainReferenceInfo == null) {
      return Collections.emptyList();
    }

    List<ReferenceDocumentDTO> references = new ArrayList<>();
    // 提取参考文档
    if (CollectionUtils.isNotEmpty(docChainReferenceInfo.getReferences())) {
      parseReferenceDocuments(tenantId, references, docChainReferenceInfo.getReferences());
    }
    // 提取参考图片
    if (CollectionUtils.isNotEmpty(docChainReferenceInfo.getReferenceImages())) {
      parseReferenceImages(references, docChainReferenceInfo.getReferenceImages());
    }
    // 兼容存量数据，只有提示词明确要求完善参考文档，才需处理
    if (StringUtils.isEmpty(prompt) || !content.contains(DOC_ID_PREFIX) || CollectionUtils.isEmpty(references)) {
      return references;
    }

    // 大模型最后会总结用到的"文档ID_块ID"列表，以<!--docChainId:d1,d2-->形式隐藏展示
    Matcher matcher = DOC_ID_PATTERN.matcher(content);
    Set<String> docIds = new HashSet<>();
    while (matcher.find()) {
      String docIdAndChunkIdStr = matcher.group(1).trim();
      String[] split = StringUtils.split(docIdAndChunkIdStr, ",");
      for (String docIdAndChunkId : split) {
        if (!docIdAndChunkId.contains("_")) {
          continue;
        }
        docIds.add(StringUtils.split(docIdAndChunkId, "_")[0]);
      }
    }
    // 按需剔除不相关的参考文档
    return references.stream().filter(reference -> docIds.contains(reference.getId())).toList();
  }

  /**
   * 从 markdown 的代码块中提取引用
   */
  @Nullable
  private DocChainReferenceDTO extractDocChainReference(String content) {
    // DocChain 会在第一个片段、最后一个片段都返回 docchain-references 代码块，我们只处理最后一个片段中的代码块
    // 第一个片段中的代码块内容是个列表，最后一个是对象
    String json;
    if (content.contains(REFERENCES_PREFIX)) {
      json = StringUtils.trimToNull(StringUtils.substringBetween(content, REFERENCES_PREFIX, REFERENCES_SUFFIX));
    }
    else {
      json = StringUtils.trimToNull(StringUtils.substringBetween(content, REFERENCES_PREFIX2, REFERENCES_SUFFIX));
    }
    if (json == null || !json.startsWith("{")) {
      return null;
    }
    // 转换引用对象
    return JsonUtil.parseJsonRequired(json, DocChainReferenceDTO.class);
  }

  /**
   * 提取参考文档
   */
  private void parseReferenceDocuments(Long tenantId, List<ReferenceDocumentDTO> references, List<DocChainDocDTO> docReferences) {
    // 研发云文档中心阅读视图的基础地址
    String readerViewBaseUrl = StringUtils.trimToNull(docChainProperties.getReaderViewBaseUrl(tenantId));
    // 研发云文档中心的文档 ID -> 文档路径映射
    Map<Integer, String> docIdToPathMap = readerViewBaseUrl != null ? tenantDocPathCache.getUnchecked(tenantId) : null;

    for (DocChainDocDTO docChainReference : docReferences) {
      if (CollectionUtils.isEmpty(docChainReference.getLinks())) {
        continue;
      }
      // 从文档切片的链接中提取文档 ID 和文档链接
      String linkUrl = docChainReference.getLinks().stream().map(DocChainLinkDTO::getUrl).filter(StringUtils::isNotEmpty).findFirst().orElse(null);
      // 忽略不合法的地址，避免报错
      if (StringUtils.isEmpty(linkUrl)) {
        continue;
      }
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(linkUrl).build().getQueryParams();
      String docId = queryParams.getFirst("doc_id");
      String docName = queryParams.getFirst("doc_name");
      // 忽略不合法的地址，避免报错
      if (StringUtils.isEmpty(docId) || StringUtils.isEmpty(docName)) {
        continue;
      }

      // 如果配置了文档中心阅读视图地址，则使用文档中心地址
      String url = null;
      if (readerViewBaseUrl != null) {
        url = buildZcmDocUrl(readerViewBaseUrl, docIdToPathMap, docName);
        // 找不到地址时忽略文档
        if (url == null) {
          logger.warn("ZCM doc not found: tenantId={}, docId={}, docName={}", tenantId, docId, docName);
          continue;
        }
      }

      ReferenceDocumentDTO reference = new ReferenceDocumentDTO();
      reference.setId(docId);
      reference.setName(docChainReference.getTitle());
      reference.setType(KnowledgeConsts.REFERENCE_DOC);
      reference.setUrl(url);
      reference.setChunks(parseReferenceChunks(docChainReference.getLinks(), docId));
      references.add(reference);
    }
  }

  private List<ReferenceChunkDTO> parseReferenceChunks(List<DocChainLinkDTO> links, String docId) {
    List<ReferenceChunkDTO> chunks = new ArrayList<>();
    for (DocChainLinkDTO link : links) {
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(link.getUrl()).build().getQueryParams();
      String chunkId = queryParams.getFirst("chunk_id");
      String rank = queryParams.getFirst("rank");
      String score = queryParams.getFirst("score");
      if (StringUtils.isEmpty(docId) || StringUtils.isEmpty(chunkId)) {
        continue;
      }
      ReferenceChunkDTO chunk = new ReferenceChunkDTO();
      chunk.setId(docId);
      chunk.setChunkId(chunkId);
      chunk.setName(link.getDescription());
      chunk.setScore(score);
      chunk.setRank(rank);
      chunks.add(chunk);
    }
    return chunks;
  }

  /**
   * 解析引用图片数据
   */
  private void parseReferenceImages(List<ReferenceDocumentDTO> references, List<String> imageUrlList) {
    for (String url : imageUrlList) {
      MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
      String docId = queryParams.getFirst("doc_id");
      String path = queryParams.getFirst("path");
      // 忽略非法的图片链接
      if (StringUtils.isEmpty(docId) || StringUtils.isEmpty(path)) {
        continue;
      }

      ReferenceDocumentDTO reference = new ReferenceDocumentDTO();
      reference.setId(docId);
      reference.setName(StringUtils.substringAfterLast(path, "/"));
      reference.setPath(path);
      reference.setType(KnowledgeConsts.REFERENCE_IMAGE);
      references.add(reference);
    }
  }

  /**
   * 构造研发云文档中心阅读视图的文档地址
   */
  @Nullable
  private String buildZcmDocUrl(String readerViewBaseUrl, Map<Integer, String> docIdToPathMap, @Nullable String docName) {
    // 提取文档中心的文档 ID. docName 是 DocChain 的文档名称，生成规则是文档中心的 docId_docName
    String docIdStr = StringUtils.substringBefore(docName, "_");
    if (StringUtils.isNumeric(docIdStr)) {
      Integer docId = Integer.parseInt(docIdStr);
      String path = docIdToPathMap.get(docId);
      if (StringUtils.isNotEmpty(path)) {
        return readerViewBaseUrl + path;
      }
    }
    return null;
  }

  /**
   * 构造研发云文档中心的文档 ID -> 文档路径映射
   */
  private Map<Integer, String> buildZcmDocPathMap(Long tenantId) {
    String readerViewDirApiUrl = docChainProperties.getReaderViewDirApiUrl(tenantId);
    Assert.hasLength(readerViewDirApiUrl, () -> "未配置阅读模式的目录 API 地址: tenantId=" + tenantId);
    // 未返回 Content-Type 响应头，只能用 String 接收，否则会报错
    String json = HttpUtil.get(readerViewDirApiUrl, ParameterizedTypeReference.forType(String.class));
    Assert.notNull(json, () -> "获取文档中心的目录失败: tenantId=" + tenantId);
    List<ZcmDocDTO> zcmDocuments = JsonUtil.parseJsonRequired(json, new TypeReference<List<ZcmDocDTO>>() {
    });
    Assert.notEmpty(zcmDocuments, () -> "获取文档中心的目录失败: tenantId=" + tenantId);
    Map<Integer, String> docIdToPathMap = new HashMap<>();
    buildZocDocPathRecursively(docIdToPathMap, zcmDocuments, "");
    return docIdToPathMap;
  }

  /**
   * 递归构造研发云文档中心的文档路径
   */
  private void buildZocDocPathRecursively(Map<Integer, String> docIdToPathMap, List<ZcmDocDTO> documents, String parentPath) {
    for (ZcmDocDTO doc : documents) {
      String path = parentPath.isEmpty() ? doc.getAliasOrName() : (parentPath + "/" + doc.getAliasOrName());
      if (doc.isDoc()) {
        docIdToPathMap.put(doc.getDocId(), path);
      }
      if (CollectionUtils.isNotEmpty(doc.getChildren())) {
        buildZocDocPathRecursively(docIdToPathMap, doc.getChildren(), path);
      }
    }
  }

  /**
   * 知识召回
   *
   * @param tenantId 租户 ID
   * @param topicIds 主题 ID 列表
   * @param docIds 文档 ID 列表
   * @param query 问句
   * @param size 查询数量
   * @param minScore 分数阈值
   * @param isChatExcel 是否 chatExcel 类型主题
   * @return 召回响应
   */
  public SearchKnowledgeResponse recall(Long tenantId, List<String> topicIds, List<String> docIds, String query, @Nullable Integer size,
                                        @Nullable Float minScore, boolean isChatExcel) {
    Map<String, Object> args = new HashMap<>();
    checkRecallAndParams(args, topicIds, docIds, query, size, minScore);
    String recallApiUrl = isChatExcel ? docChainProperties.getRecallChatExcelApiUrl(tenantId) : docChainProperties.getRecallApiUrl(tenantId);
    HttpHeaders headers = docChainLoginHelper.buildHeader(tenantId);
    HttpEntity<?> requestEntity = new HttpEntity<>(args, headers);
    JsonNode result;
    try {
      result = HttpUtil.getRestTemplate().exchange(recallApiUrl, HttpMethod.POST, requestEntity, JsonNode.class).getBody();
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      logger.error("Failed to recall knowledge: params={}, status={}, response={}", JsonUtil.toJsonString(args), e.getStatusCode().value(), responseBody);
      throw new BssException("知识检索失败: " + DocChainApiUtil.extractErrorMsg(e, responseBody), e);
    }
    catch (Exception e) {
      logger.error("Failed to recall knowledge: params={}", JsonUtil.toJsonString(args), e);
      throw new BssException("知识检索失败: " + ExpUtil.getMsg(e), e);
    }
    // 不大可能出现
    if (result == null) {
      logger.error("Failed to recall knowledge, empty response: params={}", JsonUtil.toJsonString(args));
      throw new BssException("知识检索失败，响应为空");
    }
    // 有些报错会通过 200 状态码返回，比如 topic_id 不是整数
    if (!result.isObject() || !result.path("success").asBoolean(true)) {
      logger.error("Failed to recall knowledge: params={}, response={}", JsonUtil.toJsonString(args), result);
      throw new BssException("知识检索失败: " + StringUtils.defaultIfEmpty(result.path("err").asText(""), "未知错误"));
    }
    if (isChatExcel) {
      JsonNode data = result.path("data");
      SearchKnowledgeResponse response = new SearchKnowledgeResponse();
      if (data != null && data.isArray()) {
        response.setData(JsonUtil.convert(data, new TypeReference<>() {
        }));
      }
      return response;
    }
    else {
      return JsonUtil.convert(result, SearchKnowledgeResponse.class);
    }
  }

  private void checkRecallAndParams(Map<String, Object> args, List<String> topicIds, List<String> docIds, String query, @Nullable Integer size,
                                    @Nullable Float minScore) {
    args.put("query", query);
    args.put("topic_id", topicIds.getFirst());
    if (topicIds.size() > 1) {
      args.put("topic_id_list", topicIds);
    }
    if (size != null) {
      args.put("size", size);
    }
    if (minScore != null) {
      args.put("score", minScore);
    }
    if (CollectionUtils.isNotEmpty(docIds)) {
      args.put("doc_id", docIds.getFirst());
      if (docIds.size() > 1) {
        args.put("doc_id_list", docIds);
      }
    }
  }

  /**
   * 查询对话记录
   *
   * @param tenantId 租户 ID
   * @param chatLogId 对话记录 ID
   * @return 对话记录详情
   */
  @SuppressWarnings("unchecked")
  public DocChainChatLogDTO queryChatLog(Long tenantId, String chatLogId) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.set("chat_seq_id", chatLogId);
    String url = docChainProperties.getQueryChatLogApiUrl(tenantId);
    HttpHeaders headers = docChainLoginHelper.buildHeader(tenantId);
    KnowledgeChatLogResponse response = HttpUtil.get(url, params, ParameterizedTypeReference.forType(KnowledgeChatLogResponse.class), headers);
    // 日志不存在时会返回 null
    if (response == null) {
      throw new BssException("DocChain 对话记录不存在");
    }
    String reasoningContent = null;
    String answerContent;
    String chatResponse = StringUtils.defaultString(response.getChatResponse());
    // 拆分思考内容和正文
    if (chatResponse.startsWith("<think>")) {
      int pos = chatResponse.indexOf("</think>");
      if (pos > 0) {
        reasoningContent = chatResponse.substring("<think>".length(), pos).trim();
        answerContent = chatResponse.substring(pos + "</think>".length()).trim();
      }
      else {
        answerContent = chatResponse;
      }
    }
    else {
      answerContent = chatResponse;
    }
    // 删除引用内容
    if (answerContent.contains(REFERENCES_PREFIX)) {
      answerContent = StringUtils.substringBefore(answerContent, REFERENCES_PREFIX).trim();
    }
    else if (answerContent.contains(REFERENCES_PREFIX2)) {
      answerContent = StringUtils.substringBefore(answerContent, REFERENCES_PREFIX2).trim();
    }
    DocChainChatLogDTO log = new DocChainChatLogDTO();
    log.setQuestion(response.getQueryText());
    log.setReasoningContent(reasoningContent);
    log.setAnswerContent(answerContent);
    String prompt = docChainDocumentHelper.replaceImageUrlsWithEncodedPath(response.getChatPrompt(), tenantId);
    Map<String, Object> promptMap = JsonUtil.parseJsonRequired(prompt, new TypeReference<>() {
    });
    log.setMessages(
      MapUtils.isEmpty(promptMap) ? null : ((List<Message>) MapUtils.getObject(promptMap, "message")));
    return log;
  }

  /**
   * 识别图片中的文本
   *
   * @param tenantId 租户 ID
   * @param base64Image 图片的 base64 编码形式
   * @return 图片中的文本
   */
  public Object ocr(Long tenantId, String base64Image) {
    Map<String, Object> args = new HashMap<>();
    args.put("image_path", base64Image);
    args.put("lang", "ch");
    Object result = HttpUtil.post(docChainProperties.getOcrApiUrl(null), args, new ParameterizedTypeReference<Object>() {
    }, docChainLoginHelper.buildHeader(tenantId));
    if (result == null) {
      return ResultVO.fail("响应结果为空");
    }
    return result;
  }

  /**
   * 对话接口的片段处理器
   */
  public static final class ChatStreamPartialHandler implements Consumer<ChatCompletionResponse> {
    /** 对话日志 ID 持有器 */
    @Getter
    private String chatLogId;
    /** 参考文档列表 */
    @Getter
    private List<ReferenceDocumentDTO> references;
    /** 片段处理器 */
    private final Consumer<SseEvent> partialHandler;
    /** 知识问答参数 */
    private final KnowledgeChatParamsDTO params;
    /** 是否开启推理 */
    private final boolean thinkingEnabled;
    /** 是否有 ChatExcel 返回的思考内容（实际是 DocChain 构造的处理过程，而非大模型返回的） */
    private boolean hasLlmDocReasoning;
    /** 完整回复内容,不包含思考内容 */
    private final StringBuilder fullContent = new StringBuilder();

    public ChatStreamPartialHandler(Consumer<SseEvent> partialHandler, KnowledgeChatParamsDTO params) {
      this.partialHandler = partialHandler;
      this.params = params;
      this.thinkingEnabled = ThinkingStrategy.shouldShowReasoning(params.getThinkingStrategy());
    }

    @Override
    public void accept(ChatCompletionResponse response) {
      if (chatLogId == null) {
        chatLogId = response.getId();
      }
      // 检查是否需要忽略事件
      if (shouldIgnoreEvent(response)) {
        return;
      }
      // 使用推理模型时会返回思考内容
      if (thinkingEnabled) {
        String reasoningContent = response.getDeltaReasoningContent();
        if (StringUtils.isNotEmpty(reasoningContent)) {
          // ChatExcel 返回的思考内容末尾只有一个换行符，和大模型返回的思考内容之间需要增加一个换行，否则页面上会连在一行显示（Markdown 需要一个空行区分不同段落）
          // 已向 DocChain 反馈，1.10 会增加返回一个空行，等现场都升级后再删除这里的特殊处理逻辑
          if (hasLlmDocReasoning && !"llmdoc.completion.chunk".equals(response.getObject())) {
            partialHandler.accept(SseEvent.ofReasoning("\n"));
            hasLlmDocReasoning = false;
          }
          partialHandler.accept(SseEvent.ofReasoning(reasoningContent));
          return;
        }
      }
      String content = response.getDeltaContent();
      if (StringUtils.isNotEmpty(content)) {
        fullContent.append(content);
        // 引用和回答内容不会同时返回
        if (content.contains(REFERENCES_PREFIX) || content.contains(REFERENCES_PREFIX2)) {
          references = SpringUtil.getBean(DocChainAdapter.class).parseReferences(params.getTenantId(), fullContent.toString(), params.getPromptTemplate());
        }
        else {
          partialHandler.accept(SseEvent.ofText(content));
        }
      }
    }

    /**
     * 检查是否需要忽略事件
     */
    private boolean shouldIgnoreEvent(ChatCompletionResponse response) {
      if ("llmdoc.completion.chunk".equals(response.getObject())) {
        // ChatExcel 会以思考内容的方式返回处理过程，不能忽略
        if (StringUtils.isNotEmpty(response.getDeltaReasoningContent())) {
          hasLlmDocReasoning = true;
          return false;
        }
        // 忽略开头返回的参考文档信息
        return Strings.CS.contains(response.getDeltaContent(), "```");
      }
      return false;
    }

    /**
     * 结束回调，发送参考文档、对话日志 ID 事件
     */
    public void onFinish() {
      if (CollectionUtils.isNotEmpty(references)) {
        partialHandler.accept(SseEvent.ofReferences(references));
      }
      if (params.isWithLog() && StringUtils.isNotEmpty(chatLogId)) {
        partialHandler.accept(SseEvent.ofKnowledgeChatLog(chatLogId));
      }
    }
  }
}
