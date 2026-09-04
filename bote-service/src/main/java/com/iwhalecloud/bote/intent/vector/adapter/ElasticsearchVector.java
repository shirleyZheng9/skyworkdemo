package com.iwhalecloud.bote.intent.vector.adapter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.config.properties.ElasticSearchProperties;
import com.iwhalecloud.bote.dto.intent.IntentStrategyDTO;
import com.iwhalecloud.bote.dto.intent.IntentionEmbeddingDTO;
import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchVector {
  private static final Logger logger = LoggerFactory.getLogger(ElasticsearchVector.class);

  private final ModelClientCache modelClientCache;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final ObjectProvider<ElasticsearchClient> clientProvider;
  private final ObjectProvider<ElasticSearchProperties> properties;

  /** 默认向量维度，当无法获取实际维度时使用 */
  private static final int DEFAULT_VECTOR_DIMENSION = 1024;
  /** 向量索引名称前缀 */
  private static final String VECTOR_INDEX_PREFIX = "intention_vector";
  /** 向量字段名称 */
  private static final String VECTOR_FIELD = "question_vector";

  /** 缓存租户的向量维度，避免重复计算 */
  private final Map<Long, Integer> tenantVectorDimensionCache = new ConcurrentHashMap<>();

  /**
   * 获取Elasticsearch客户端
   */
  public ElasticsearchClient getClient() {
    ElasticsearchClient client = clientProvider.getIfAvailable();
    if (client == null) {
      throw new BssException("Elasticsearch客户端不可用");
    }
    return client;
  }

  /**
   * 构建租户隔离的索引名称
   */
  public String buildIndexName(Long tenantId) {
    ElasticSearchProperties props = getProperties();
    String namespace = props.getNamespace();
    if (namespace == null || namespace.trim().isEmpty()) {
      throw new BssException("Elasticsearch命名空间配置无效");
    }
    return namespace + "_" + VECTOR_INDEX_PREFIX + "_" + tenantId;
  }


  /**
   * 检查索引是否存在
   */
  public boolean hasIndex(String indexName) {
    try {
      ExistsRequest request = ExistsRequest.of(b -> b.index(indexName));
      //noinspection resource
      return getClient().indices().exists(request).value();
    }
    catch (IOException e) {
      logger.error("检查索引是否存在失败: {}", indexName, e);
      return false;
    }
  }

  /**
   * 确保索引存在
   */
  public void ensureIndexExists(String indexName, Long tenantId) {
    if (!hasIndex(indexName)) {
      int dimension = getVectorDimension(tenantId);
      createVectorIndex(indexName, dimension);
    }
  }

  /**
   * 构建向量文档
   */
  public Map<String, Object> buildVectorDocument(IntentionEmbeddingDTO embedding) {
    Map<String, Object> document = new HashMap<>();
    document.put("tenantId", embedding.getTenantId());
    document.put("sceneId", embedding.getSceneId());
    document.put("questionId", embedding.getQuestionId());
    // 将 float 数组转换为 List<Float>，Elasticsearch 需要这种格式
    document.put(VECTOR_FIELD, convertToFloatList(embedding.getQuestion()));
    return document;
  }

  /**
   * 获取Elasticsearch配置
   */
  private ElasticSearchProperties getProperties() {
    ElasticSearchProperties props = properties.getIfAvailable();
    if (props == null) {
      throw new BssException("Elasticsearch配置不可用");
    }
    return props;
  }

  /**
   * 获取租户的向量维度
   */
  private int getVectorDimension(Long tenantId) {
    try {
      if (tenantVectorDimensionCache.containsKey(tenantId)) {
        return tenantVectorDimensionCache.get(tenantId);
      }
      IntentStrategyDTO setting = tenantSettingInfoCache.getIntentStrategy(tenantId);
      EmbeddingClient client = modelClientCache.getEmbeddingClient(tenantId, setting.getEmbeddingModelId());
      float[] testEmbedding = client.embedding("test");
      int dimension = testEmbedding.length;
      tenantVectorDimensionCache.put(tenantId, dimension);
      return dimension;
    }
    catch (Exception e) {
      logger.error("获取租户 {} 的向量维度失败，使用默认维度 {}", tenantId, DEFAULT_VECTOR_DIMENSION, e);
      return DEFAULT_VECTOR_DIMENSION;
    }
  }

  /**
   * 创建向量索引
   */
  private void createVectorIndex(String indexName, int dimension) {
    try {
      // @formatter:off
      // 构建向量索引映射
      TypeMapping mapping = TypeMapping.of(m -> m
        .properties("tenantId", Property.of(p -> p.long_(l -> l)))
        .properties("sceneId", Property.of(p -> p.long_(l -> l)))
        .properties("questionId", Property.of(p -> p.long_(l -> l)))
        .properties(VECTOR_FIELD, Property.of(p -> p.denseVector(dv -> dv
          .dims(dimension)
          .index(true)
          .similarity(DenseVectorSimilarity.Cosine))))
      );
      // @formatter:on
      CreateIndexRequest request = CreateIndexRequest.of(b -> b.index(indexName).mappings(mapping));
      //noinspection resource
      getClient().indices().create(request);
      logger.info("创建向量索引成功: {}, 维度: {}", indexName, dimension);
    }
    catch (IOException e) {
      throw new BssException("创建向量索引失败: " + indexName, e);
    }
  }

  /**
   * 将 float 数组转换为 List<Float>
   */
  private List<Float> convertToFloatList(float[] array) {
    List<Float> list = new ArrayList<>();
    for (float value : array) {
      list.add(value);
    }
    return list;
  }
}
