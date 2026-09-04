package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BoteModelDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.WeknoraModelRelDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.WeKnoraModelRelMapper;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.WeKnoraProperties;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraCreateModelRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraHybridSearchRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraSearchRequest;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraChatChunk;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraDataResponse;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraKnowledgeBaseRespDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraModelDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraSearchResultDTO;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallTextItem;
import com.iwhalecloud.bote.dto.knowledge.ReferenceChunkDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriUtils;

import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;

/**
 * WeKnoraKnowledgeClient 的内部编排辅助工具。
 */
@Component
@ConditionalOnBooleanProperty(name = "knowledge.weknora.enabled")
@RequiredArgsConstructor
public final class WeKnoraKnowledgeClientHelper {

  private static final Logger logger = LoggerFactory.getLogger(WeKnoraKnowledgeClientHelper.class);

  private final WeKnoraLoginHelper loginHelper;
  private final WeKnoraProperties properties;
  private final WeKnoraModelRelMapper weKnoraModelRelMapper;

  /**
   * 查询 WeKnora 模型列表。
   *
   * <p>对应接口：GET /v1/models。</p>
   */
  public List<WeKnoraModelDTO> listModels(Long tenantId) {
    String url = properties.getListModelsApiUrl(tenantId);
    WeKnoraDataResponse<List<WeKnoraModelDTO>> result = exchange(tenantId, url, HttpMethod.GET, null,
      new ParameterizedTypeReference<>() {
      });
    if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
      logger.warn("WeKnora listModels failed or returned empty: tenantId={}", tenantId);
      return Collections.emptyList();
    }
    return result.getData();
  }

  /**
   * 创建 WeKnora 模型。
   *
   * <p>对应接口：POST /v1/models。</p>
   */
  public WeKnoraModelDTO createModel(Long tenantId, WeKnoraCreateModelRequest request) {
    Assert.notNull(request, "创建模型请求不能为空");
    String url = properties.getListModelsApiUrl(tenantId);
    WeKnoraDataResponse<WeKnoraModelDTO> result = exchange(tenantId, url, HttpMethod.POST, request,
      new ParameterizedTypeReference<>() {
      });
    if (result == null || !Boolean.TRUE.equals(result.getSuccess())) {
      String detail = result == null ? "" : StringUtils.defaultString(result.getMessage());
      throw new BssException("创建 WeKnora 模型失败" + (detail.isEmpty() ? "" : ": " + detail));
    }
    return result.getData();
  }

  /**
   * 查询知识库列表。
   *
   * <p>对应接口：GET /knowledge-bases。</p>
   */
  public List<WeKnoraKnowledgeBaseRespDTO> listKnowledgeBases(Long tenantId) {
    String url = properties.getListKnowledgeBasesApiUrl(tenantId);
    WeKnoraDataResponse<List<WeKnoraKnowledgeBaseRespDTO>> result = exchange(tenantId, url, HttpMethod.GET, null,
      new ParameterizedTypeReference<>() {
      });
    if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
      logger.warn("WeKnora listKnowledgeBases failed or returned empty: tenantId={}", tenantId);
      return Collections.emptyList();
    }
    return result.getData();
  }

  /**
   * 执行知识检索（混合检索）。
   *
   * <p>单库直接检索；多库逐库检索后按 chunkId 去重并按分数降序合并。</p>
   */
  public List<WeKnoraSearchResultDTO> knowledgeSearch(Long tenantId, WeKnoraSearchRequest request) {
    Assert.hasText(request.getQuery(), "query 不能为空");
    if (CollectionUtils.isNotEmpty(request.getKnowledgeIds())) {
      throw new BssException("仅支持按知识库混合检索，请指定 knowledge_base_id 或 knowledge_base_ids");
    }
    List<String> kbIds = resolveKnowledgeBaseIds(request);
    if (kbIds.isEmpty()) {
      throw new BssException("须指定 knowledge_base_id 或 knowledge_base_ids");
    }
    WeKnoraHybridSearchRequest hybridBody = buildDefaultHybridBody(request);
    if (kbIds.size() == 1) {
      return hybridSearch(tenantId, kbIds.get(0), hybridBody);
    }
    List<WeKnoraSearchResultDTO> merged = new ArrayList<>();
    for (String kbId : kbIds) {
      merged.addAll(hybridSearch(tenantId, kbId, hybridBody));
    }
    return mergeHybridResults(merged);
  }

  /**
   * 发起知识问答 SSE 请求并将数据分发给 chunk 处理器。
   *
   * <p>对应接口：POST /knowledge-chat/{sessionId}。</p>
   */
  public void knowledgeChatStream(Long tenantId, String weKnoraSessionId, String query,
                                  List<String> knowledgeBaseIds,
                                  @Nullable Consumer<? super Call> requestListener,
                                  Consumer<WeKnoraChatChunk> chunkHandler,
                                  Long modelId) {
    String urlStr = properties.buildUrl("/knowledge-chat/" + weKnoraSessionId, tenantId);
    HttpUrl url = HttpUrl.get(urlStr);
    HttpHeaders springHeaders = loginHelper.buildOkHttpHeaders(tenantId);
    Headers headers = toOkHttpHeaders(springHeaders);
    Map<String, Object> body = new HashMap<>(12);
    body.put("query", query);
    body.put("agent_enabled", false);
    body.put("agent_id", "builtin-quick-answer");
    body.put("web_search_enabled", false);
    body.put("summary_model_id", getWeKnoraModelId(modelId, tenantId));
    body.put("knowledge_base_ids", knowledgeBaseIds);
    ModelHttpClient.sseBlocking("POST", url, headers, body, requestListener, event -> handleSseEvent(event, chunkHandler));
  }

  /**
   * 获取weknora模型
   */
  private String getWeKnoraModelId(Long modelId, Long tenantId) {
    // 转换默认模型
    if (modelId == -1L) {
      String tenantDefaultModel = weKnoraModelRelMapper.getTenantDefaultModel(tenantId);
      Map<String, Object> defaultModelInfo = JsonUtil.parseJson(tenantDefaultModel, new TypeReference<>() {
      });
      if (defaultModelInfo == null) {
        throw new BssException("默认模型不存在，请检查默认模型");
      }
      modelId = MapUtils.getLong(defaultModelInfo, "largeModelId");
    }
    WeknoraModelRelDTO weKnoraModelRel = weKnoraModelRelMapper.getWeKnoraModelRelByBoteModelId(modelId, tenantId);
    // 如果模型关系存在，直接返回weknora模型
    if (weKnoraModelRel != null) {
      return weKnoraModelRel.getWeknoraModelId();
    }
    // 如果不存在，则新增模型
    BoteModelDTO modelInfo = weKnoraModelRelMapper.getModelById(modelId, tenantId);
    if (modelInfo == null) {
      throw new BssException("模型不存在");
    }
    WeKnoraModelDTO weknoraModel = createWeKnoraModel(tenantId, modelInfo);
    if (weknoraModel == null) {
      throw new BssException("WeKnora模型同步失败");
    }
    creteWeKnoraModelRel(modelId, tenantId, weknoraModel.getId());
    return weknoraModel.getId();
  }

  /**
   * 创建 weknora 模型
   */
  private WeKnoraModelDTO createWeKnoraModel(Long tenantId, BoteModelDTO modelInfo) {
    WeKnoraCreateModelRequest request = new WeKnoraCreateModelRequest();
    request.setType(modelInfo.getModelType());
    request.setName(modelInfo.getModelCode());
    request.setDescription(modelInfo.getModelDesc());
    request.setType("KnowledgeQA");
    request.setSource("remote");
    Map<String, Object> parameter = new HashMap<>();
    // 去除v1/chat/completions openApi端点
    if (StringUtils.isNotEmpty(modelInfo.getAccessUrl())) {
      parameter.put("base_url", StringUtils.substringBefore(modelInfo.getAccessUrl(), "v1/chat/completions"));
    }
    parameter.put("api_key", modelInfo.getAccessKey());
    parameter.put("provider", "generic");
    request.setParameters(parameter);
    return createModel(tenantId, request);
  }

  /**
   * 创建模型关系
   */
  private void creteWeKnoraModelRel(Long modelId, Long tenantId, String weknoraModelId) {
    WeknoraModelRelDTO rel = new WeknoraModelRelDTO();
    rel.setRelId(IDUtils.nextId());
    rel.setBoteModelId(modelId);
    rel.setWeknoraModelId(weknoraModelId);
    rel.setTenantId(tenantId);
    rel.setStatusCd(BaseConsts.STATUS_CD_VALID);
    rel.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    weKnoraModelRelMapper.insertWeKnoraModelRel(rel);
  }

  /**
   * 查询单个知识库详情。
   */
  @Nullable
  public WeKnoraKnowledgeBaseRespDTO getKnowledge(Long tenantId, String knowledgeId) {
    String url = properties.getKnowledgeBaseApiUrl(tenantId) + "/" + knowledgeId;
    WeKnoraDataResponse<WeKnoraKnowledgeBaseRespDTO> result = exchange(tenantId, url, HttpMethod.GET, null,
      new ParameterizedTypeReference<>() {
      });
    if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
      logger.warn("WeKnora listKnowledgeBases failed or returned empty: tenantId={}", tenantId);
      return null;
    }
    return result.getData();
  }

  /**
   * 从检索请求中解析知识库 ID（优先单值，再取列表去重）。
   */
  public static List<String> resolveKnowledgeBaseIds(WeKnoraSearchRequest request) {
    if (StringUtils.isNotBlank(request.getKnowledgeBaseId())) {
      return List.of(request.getKnowledgeBaseId().trim());
    }
    if (CollectionUtils.isNotEmpty(request.getKnowledgeBaseIds())) {
      return request.getKnowledgeBaseIds().stream()
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .distinct()
        .sorted()
        .toList();
    }
    return List.of();
  }

  /**
   * 构建默认混合检索请求体。
   */
  public static WeKnoraHybridSearchRequest buildDefaultHybridBody(WeKnoraSearchRequest request) {
    return WeKnoraHybridSearchRequest.builder()
      .queryText(request.getQuery().trim())
      .vectorThreshold(0.5d)
      .matchCount(10)
      .build();
  }

  /**
   * 合并多知识库检索结果：按 chunkId 仅保留最高分，并按分数降序返回。
   */
  public static List<WeKnoraSearchResultDTO> mergeHybridResults(List<WeKnoraSearchResultDTO> items) {
    Map<String, WeKnoraSearchResultDTO> bestById = new LinkedHashMap<>();
    for (WeKnoraSearchResultDTO item : items) {
      if (item == null || item.getId() == null) {
        continue;
      }
      WeKnoraSearchResultDTO existing = bestById.get(item.getId());
      if (existing == null || score(item) > score(existing)) {
        bestById.put(item.getId(), item);
      }
    }
    return bestById.values().stream()
      .sorted((a, b) -> Double.compare(score(b), score(a)))
      .collect(Collectors.toList());
  }

  /**
   * 解析召回场景中的 WeKnora 知识库 ID 列表。
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public static List<String> resolveWeKnoraKbIdsForRecall(KnowledgeRecallParamDTO params) {
    if (CollectionUtils.isNotEmpty(params.getWeKnoraKnowledgeBaseIds())) {
      return params.getWeKnoraKnowledgeBaseIds().stream()
        .filter(StringUtils::isNotBlank)
        .map(String::trim)
        .distinct()
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  /**
   * 将 WeKnora 检索结果转换为平台召回文本结构。
   */
  public static KnowledgeRecallTextItem convertToRecallTextItem(WeKnoraSearchResultDTO src) {
    KnowledgeRecallTextItem item = new KnowledgeRecallTextItem();
    item.setScore(src.getScore());
    item.setContent(src.getContent());
    item.setDocId(src.getKnowledgeId());
    item.setDocName(StringUtils.defaultIfEmpty(src.getKnowledgeFilename(), src.getKnowledgeTitle()));
    item.setChunkId(src.getId());
    return item;
  }

  /**
   * 将 WeKnora 引用信息转换为平台标准引用结构（按文档分组）。
   */
  public static List<ReferenceDocumentDTO> convertReferences(List<WeKnoraChatChunk.KnowledgeReference> refs) {
    Map<String, List<WeKnoraChatChunk.KnowledgeReference>> groupedByDoc = refs.stream()
      .collect(Collectors.groupingBy(r -> r.getKnowledgeId() != null ? r.getKnowledgeId() : "unknown"));
    List<ReferenceDocumentDTO> result = new ArrayList<>();
    for (Map.Entry<String, List<WeKnoraChatChunk.KnowledgeReference>> entry : groupedByDoc.entrySet()) {
      ReferenceDocumentDTO doc = new ReferenceDocumentDTO();
      doc.setOpenEnabled(false);
      doc.setDownloadEnabled(false);
      doc.setId(entry.getKey());
      doc.setType(KnowledgeConsts.REFERENCE_DOC);
      List<WeKnoraChatChunk.KnowledgeReference> docRefs = entry.getValue();
      if (!docRefs.isEmpty()) {
        WeKnoraChatChunk.KnowledgeReference first = docRefs.get(0);
        doc.setName(StringUtils.defaultIfEmpty(first.getKnowledgeFilename(), first.getKnowledgeTitle()));
      }
      List<ReferenceChunkDTO> chunks = docRefs.stream().map(ref -> {
        ReferenceChunkDTO chunk = new ReferenceChunkDTO();
        chunk.setChunkId(ref.getId());
        chunk.setId(entry.getKey());
        chunk.setName(ref.getContent());
        chunk.setScore(ref.getScore() != null ? ref.getScore().toString() : null);
        return chunk;
      }).collect(Collectors.toList());
      doc.setChunks(chunks);
      result.add(doc);
    }
    return result;
  }

  /**
   * 解析创建人 ID：优先当前登录用户，其次参数中的 userId。
   */
  public static Long resolveCreatorId(KnowledgeChatParamsDTO params) {
    LoginInfo login = SessionUtil.getOptionalLoginInfo();
    if (login != null && login.getUserId() != null) {
      return login.getUserId();
    }
    if (params.getUserId() != null) {
      return params.getUserId();
    }
    return -1L;
  }

  /**
   * 解析问答场景中的 WeKnora 知识库 ID。
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public static List<String> resolveWeKnoraKbIds(KnowledgeChatParamsDTO params) {
    if (CollectionUtils.isNotEmpty(params.getExtKnowledgeIds())) {
      return params.getExtKnowledgeIds().stream()
        .filter(StringUtils::isNotBlank)
        .map(String::trim)
        .distinct()
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private static double score(WeKnoraSearchResultDTO x) {
    return x.getScore() != null ? x.getScore() : 0d;
  }

  /**
   * 单知识库混合检索调用。
   *
   * <p>对应接口：GET /knowledge-bases/{id}/hybrid-search。</p>
   */
  private List<WeKnoraSearchResultDTO> hybridSearch(Long tenantId, String knowledgeBaseId, WeKnoraHybridSearchRequest body) {
    String path = "/knowledge-bases/" + UriUtils.encodePathSegment(knowledgeBaseId, StandardCharsets.UTF_8) + "/hybrid-search";
    String url = properties.buildUrl(path, tenantId);
    WeKnoraDataResponse<List<WeKnoraSearchResultDTO>> result = exchange(tenantId, url, HttpMethod.GET, body,
      new ParameterizedTypeReference<>() {
      });
    if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
      logger.error("WeKnora hybrid search failed: tenantId={}, knowledgeBaseId={}", tenantId, knowledgeBaseId);
      return Collections.emptyList();
    }
    return result.getData();
  }

  /**
   * 统一 HTTP 调用入口，发生 401 时刷新 token 后重试一次。
   */
  private <T> T exchange(Long tenantId, String url, HttpMethod method, @Nullable Object body,
                         ParameterizedTypeReference<T> responseType) {
    try {
      return doExchange(tenantId, url, method, body, responseType);
    }
    catch (BssException e) {
      if (e.getMessage() != null && e.getMessage().contains("401")) {
        logger.info("WeKnora Bearer API returned 401, refreshing token and retrying: tenantId={}", tenantId);
        loginHelper.refreshApiKey(tenantId);
        return doExchange(tenantId, url, method, body, responseType);
      }
      throw e;
    }
  }

  /**
   * 执行一次 HTTP 调用，不包含重试逻辑。
   */
  private <T> T doExchange(Long tenantId, String url, HttpMethod method, @Nullable Object body,
                           ParameterizedTypeReference<T> responseType) {
    HttpHeaders headers = loginHelper.buildOkHttpHeaders(tenantId);
    headers.set("Content-Type", "application/json");
    HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    try {
      ResponseEntity<T> response = HttpUtil.getRestTemplate().exchange(url, method, entity, responseType);
      return response.getBody();
    }
    catch (HttpClientErrorException.Unauthorized e) {
      throw new BssException("WeKnora Bearer API 401 鉴权失败", e);
    }
    catch (Exception e) {
      throw new BssException("WeKnora Bearer API 调用失败 [" + method + " " + url + "]: " + e.getMessage(), e);
    }
  }

  /**
   * 将 Spring HttpHeaders 转为 OkHttp Headers。
   */
  private Headers toOkHttpHeaders(HttpHeaders springHeaders) {
    Headers.Builder builder = new Headers.Builder();
    springHeaders.forEach((name, values) -> {
      for (String value : values) {
        builder.add(name, value);
      }
    });
    return builder.build();
  }

  /**
   * 解析 SSE 事件并回调 chunkHandler。
   */
  private void handleSseEvent(ServerSentEvent event, Consumer<WeKnoraChatChunk> chunkHandler) {
    if (event.data() == null || event.data().isBlank()) {
      return;
    }
    try {
      WeKnoraChatChunk chunk = JsonUtil.parseJson(event.data(), WeKnoraChatChunk.class);
      if (chunk != null) {
        chunkHandler.accept(chunk);
      }
    }
    catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to parse WeKnora SSE event: data={}, error={}", event.data(), e.getMessage());
      }
    }
  }
}
