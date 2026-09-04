package com.iwhalecloud.bote.doc.module.knowledge.semantic.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.IKnowledgeQuestionEmbeddingService;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.dto.IndexDocDTO;
import com.iwhalecloud.bote.doc.module.knowledge.semantic.dto.KnowledgeQuestionDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 知识库提问向量
 *
 * @author qian.sisheng
 * @since 2026/04/02
 */
@Component
@RequiredArgsConstructor
public class KnowledgeQuestionVectorHelper {

  private static final Logger logger = LoggerFactory.getLogger(KnowledgeQuestionVectorHelper.class);

  private static final Type MAP_TYPE = new TypeReference<Map<String, Object>>() {
  }.getType();

  /** 探测 embedding 维度失败时的默认 dense_vector 维度 */
  private static final int DEFAULT_VECTOR_DIMENSION = 1024;
  /** 索引前缀 */
  private static final String INDEX_PREFIX = "knowledge_question_vector";
  /** 向量字段名 */
  private static final String VECTOR_FIELD = "question_vector";
  /** 余弦相似度脚本模板 */
  private static final String SCRIPT_COSINE = "cosineSimilarity(params.query_vector, '" + VECTOR_FIELD + "')";

  private final IKnowledgeQuestionEmbeddingService embeddingService;
  private final ObjectProvider<ElasticsearchClient> clientProvider;

  private final Map<Long, Integer> tenantVectorDimensionCache = new ConcurrentHashMap<>();

  /**
   * 知识库提问 embedding 是否可用
   */
  public boolean isQuestionEmbeddingAvailable() {
    return clientProvider.getIfAvailable() != null && StringUtils.isNotBlank(
      BaseSystemParameter.KNOWLEDGE_QUESTION_EMBEDDING_MODEL_ID.getValueFromDb());
  }

  /**
   * 获取 Elasticsearch 客户端
   */
  public ElasticsearchClient client() {
    ElasticsearchClient cilent = clientProvider.getIfAvailable();
    if (cilent == null) {
      throw new BssException("Elasticsearch 客户端不可用");
    }
    return cilent;
  }

  /**
   * 获取索引名称
   */
  public String indexName(Long tenantId) {
    Environment environment = SpringUtil.getEnvironment();
    String elasticsearchNamespace = environment.getProperty("bote.elasticsearch.namespace");
    if (StringUtils.isBlank(elasticsearchNamespace)) {
      throw new BssException("Elasticsearch 命名空间配置无效");
    }
    return elasticsearchNamespace + "_" + INDEX_PREFIX + "_" + tenantId;
  }

  /**
   * 检查索引是否存在
   */
  public boolean indexExists(String indexName) {
    try {
      ExistsRequest request = ExistsRequest.of(b -> b.index(indexName));
      // noinspection resource
      return client().indices().exists(request).value();
    }
    catch (Exception e) {
      logger.error("检查索引是否存在失败: {}", indexName, e);
      return false;
    }
  }

  /**
   * 确保索引存在
   */
  public void ensureIndex(String indexName, Long tenantId) {
    if (!indexExists(indexName)) {
      createIndex(indexName, resolveVectorDimension(tenantId));
    }
  }

  /**
   * 获取向量
   */
  public float[] embed(Long tenantId, String text) {
    try {
      return embeddingService.embed(tenantId, text);
    }
    catch (Exception e) {
      logger.error("提问 embedding 不可用 tenantId={}", tenantId, e);
      return null;
    }
  }

  /**
   * 检索最相似的会话提问
   */
  public List<KnowledgeQuestionDTO> topMatches(Long tenantId, float[] queryVector, float scoreThreshold, int topNum) {
    String indexName = indexName(tenantId);
    if (!indexExists(indexName)) {
      return List.of();
    }
    double minCosine = scoreThreshold * 2.0 - 1.0;
    try {
      SearchRequest request = buildVectorSimilaritySearch(indexName, tenantId, queryVector, minCosine, topNum);
      // noinspection resource
      SearchResponse<Map<String, Object>> response = client().search(request, MAP_TYPE);
      List<KnowledgeQuestionDTO> out = new ArrayList<>();
      for (Hit<Map<String, Object>> hit : response.hits().hits()) {
        KnowledgeQuestionDTO dto = mapHitToDto(hit);
        if (dto != null) {
          out.add(dto);
        }
      }
      return out;
    }
    catch (Exception e) {
      logger.error("提问向量检索失败 tenantId={}, index={}", tenantId, indexName, e);
      return List.of();
    }
  }

  /**
   * 构建向量相似度搜索请求
   */
  private static SearchRequest buildVectorSimilaritySearch(String indexName, Long tenantId, float[] queryVector,
    double minCosineScore, int topNum) {
    // @formatter:off
    return SearchRequest.of(b -> b
      .index(indexName)
      .size(topNum)
      .minScore(minCosineScore)
      .query(q -> q.bool(bq -> {
        bq.filter(f -> f.term(t -> t.field("tenantId").value(tenantId)));
        bq.must(m -> m.scriptScore(ss -> ss
          .query(sq -> sq.matchAll(ma -> ma))
          .script(s -> s.source(SCRIPT_COSINE).params("query_vector", JsonData.of(queryVector)))));
        return bq;
      })));
    // @formatter:on
  }

  /**
   * 将搜索结果中的文档映射为 KnowledgeQuestionDTO
   */
  private static KnowledgeQuestionDTO mapHitToDto(Hit<Map<String, Object>> hit) {
    Map<String, Object> source = hit.source();
    if (source == null) {
      return null;
    }
    KnowledgeQuestionDTO question = new KnowledgeQuestionDTO();
    question.setMsgId(MapUtils.getString(source, "msgId"));
    question.setQuestion(MapUtils.getString(source, "question"));
    question.setStandardQuestion(MapUtils.getString(source, "standardQuestion"));
    question.setScore(hit.score() == null ? 0.0f : (hit.score().floatValue() + 1.0f) / 2.0f);
    return question;
  }

  /**
   * 索引文档
   */
  public void indexDocument(IndexDocDTO doc) {
    String idx = indexName(doc.getTenantId());
    ensureIndex(idx, doc.getTenantId());
    Map<String, Object> body = doc.toSource();
    try {
      IndexRequest<Map<String, Object>> request = IndexRequest.of(b -> b
        .index(idx)
        .id(doc.getTenantId() + "_" + doc.getMsgId())
        .document(body)
        .refresh(Refresh.True));
      // noinspection resource
      client().index(request);
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("写入提问向量失败 msgId={}", doc.getMsgId(), e);
      }
    }
  }

  /**
   * 解析向量维度
   */
  private int resolveVectorDimension(Long tenantId) {
    try {
      Integer cached = tenantVectorDimensionCache.get(tenantId);
      if (cached != null) {
        return cached;
      }
      float[] probe = embeddingService.embed(tenantId, "test");
      int dim = (probe == null || probe.length == 0) ? DEFAULT_VECTOR_DIMENSION : probe.length;
      tenantVectorDimensionCache.put(tenantId, dim);
      return dim;
    }
    catch (Exception e) {
      logger.error("获取租户 {} 的向量维度失败，使用默认维度 {}", tenantId, DEFAULT_VECTOR_DIMENSION, e);
      return DEFAULT_VECTOR_DIMENSION;
    }
  }

  /**
   * 创建索引
   */
  private void createIndex(String indexName, int dimension) {
    try {
      TypeMapping mapping = TypeMapping.of(m -> m
        .properties("tenantId", Property.of(p -> p.long_(l -> l)))
        .properties("msgId", Property.of(p -> p.keyword(l -> l)))
        .properties("question", Property.of(p -> p.text(t -> t)))
        .properties("standardQuestion", Property.of(p -> p.text(t -> t)))
        .properties("matchScore", Property.of(p -> p.float_(f -> f)))
        .properties(VECTOR_FIELD, Property.of(p -> p.denseVector(dv -> dv
          .dims(dimension)
          .index(true)
          .similarity(DenseVectorSimilarity.Cosine)))));
      CreateIndexRequest request = CreateIndexRequest.of(b -> b.index(indexName).mappings(mapping));
      // noinspection resource
      client().indices().create(request);
      if (logger.isInfoEnabled()) {
        logger.info("创建会话提问向量索引成功: {}, 维度 {}", indexName, dimension);
      }
    }
    catch (Exception e) {
      logger.error("创建会话提问向量索引失败: {}", indexName, e);
    }
  }
}
