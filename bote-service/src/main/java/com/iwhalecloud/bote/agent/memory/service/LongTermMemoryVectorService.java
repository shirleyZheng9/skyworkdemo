package com.iwhalecloud.bote.agent.memory.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.PutAliasRequest;
import co.elastic.clients.json.JsonData;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.elasticsearch.ElasticsearchHelper;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.config.properties.ElasticSearchProperties;
import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 长期记忆向量存储服务
 *
 * <p>负责将长期记忆文件（MEMORY.md / MEMORY-YYYY-MM-DD.md）的内容按 markdown 切片后向量化，
 * 并存储到 Elasticsearch 中，支持语义检索（向量检索 + BM25 混合检索）。</p>
 *
 * <p>当 Elasticsearch 未配置或不可用时，降级为纯关键字搜索（通过数据库 LIKE 实现）。</p>
 *
 * @author wangtingyun
 * @since 2026-04-18
 */
@Service
public class LongTermMemoryVectorService {
  private static final Logger logger = LoggerFactory.getLogger(LongTermMemoryVectorService.class);

  /** 向量索引名称前缀 */
  private static final String INDEX_PREFIX = "memory_vector";
  /** 向量字段名称 */
  private static final String VECTOR_FIELD = "memory_vector";
  /** 默认向量维度（获取不到真实维度时使用） */
  private static final int DEFAULT_VECTOR_DIMENSION = 1024;
  /** 单次批量写入大小 */
  private static final int BATCH_SIZE = 20;
  /** 单个分片的最大字符数 */
  private static final int MAX_CHUNK_CHARS = 500;
  /** 默认返回的检索结果数量上限 */
  public static final int DEFAULT_TOP_N = 5;
  /** 余弦相似度脚本 */
  private static final String COSINE_SCRIPT = "cosineSimilarity(params.query_vector, 'memory_vector') + 1.0";
  /** Markdown 标题分割正则（## 或 ### 开头的行） */
  private static final Pattern HEADER_PATTERN = Pattern.compile("(?m)^#{1,3}\\s+.+$");

  private final ModelClientCache modelClientCache;
  private final ObjectProvider<ElasticsearchClient> clientProvider;
  private final ObjectProvider<ElasticSearchProperties> propertiesProvider;

  /** Elasticsearch 是否可用（初始化时计算，避免重复检查） */
  @Getter
  private boolean available;

  public LongTermMemoryVectorService(ModelClientCache modelClientCache, ObjectProvider<ElasticsearchClient> clientProvider,
      ObjectProvider<ElasticSearchProperties> propertiesProvider) {
    this.modelClientCache = modelClientCache;
    this.clientProvider = clientProvider;
    this.propertiesProvider = propertiesProvider;
  }

  /**
   * 检查 Elasticsearch 是否可用
   */
  @PostConstruct
  public void setEsAvailable() {
    // 检查并缓存ES搜索引擎的是否可用状态
    ElasticSearchProperties props = propertiesProvider.getIfAvailable();
    this.available = props != null && Boolean.TRUE.equals(props.getEnabled())
      && clientProvider.getIfAvailable() != null;
  }

  /**
   * 更新记忆文件的向量存储
   *
   * <p>使用原子更新策略：创建新索引 → 向量化 → 批量插入 → 删除旧索引 → 重命名新索引，
   * 最大限度减少更新过程对向量检索的影响。</p>
   *
   * @param tenantId  租户 ID（用于获取向量模型配置）
   * @param userId    用户 ID
   * @param botId     应用 ID
   * @param spaceId   空间 ID
   * @param fileName  记忆文件名
   * @param content   记忆文件完整内容
   */
  public void updateMemoryVector(Long tenantId, Long userId, Long botId, Long spaceId,
      String fileName, String content) {
    if (!isAvailable()) {
      return;
    }
    try {
      // 通用智能体使用 spaceId，自主规划智能体使用 tenantId
      Long finalTenantId = tenantId == null ? spaceId : tenantId;

      String indexName = buildIndexName(finalTenantId, userId, botId, fileName);
      String tempIndexName = indexName + "_temp_" + System.currentTimeMillis();

      if (StringUtils.isBlank(content)) {
        // 内容为空时，直接删除旧索引
        deleteIndexIfExists(indexName);
        return;
      }

      // 1. 创建临时索引
      createIndex(tempIndexName);

      EmbeddingClient embeddingClient = getEmbeddingClient();
      List<String> chunks = splitMarkdown(content);
      if (chunks.isEmpty()) {
        // 没有内容块时，删除临时索引
        deleteIndexIfExists(tempIndexName);
        return;
      }

      // 2. 向量化
      List<float[]> vectors = embeddingClient.embedding(chunks);

      // 3. 批量插入到临时索引（使用内容 hash 作为 chunkId）
      List<BulkOperation> operations = new ArrayList<>(chunks.size());
      for (int i = 0; i < chunks.size(); i++) {
        String chunkId = computeChunkHash(fileName, chunks.get(i), i);
        Map<String, Object> doc = buildDocument(chunkId, fileName, chunks.get(i), vectors.get(i));
        // lambda 中引用的变量必须是 effectively final，所以将 chunkId 赋给局部 final 变量
        final String finalChunkId = chunkId;
        operations.add(BulkOperation.of(b -> b.index(
            IndexOperation.of(io -> io.index(tempIndexName).id(finalChunkId).document(doc)))));
      }

      // 分批写入
      for (int from = 0; from < operations.size(); from += BATCH_SIZE) {
        List<BulkOperation> batch = operations.subList(from,
            Math.min(from + BATCH_SIZE, operations.size()));
        BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(batch));
        BulkResponse response = getClient().bulk(bulkRequest);
        if (response.errors()) {
          logger.error("批量写入记忆向量时出现错误，fileName={}", fileName);
        }
      }

      // 4. 删除旧索引
      deleteIndexIfExists(indexName);

      // 5. 为临时索引创建目标名称的 alias
      getClient().indices().putAlias(PutAliasRequest.of(b ->
          b.index(tempIndexName).name(indexName)));

      if (logger.isDebugEnabled()) {
        logger.debug("记忆向量更新完成: fileName={}, chunks={}", fileName, chunks.size());
      }
    }
    catch (Exception e) {
      logger.error("更新记忆向量失败: fileName={}", fileName, e);
    }
  }

  /**
   * 混合检索：向量检索 + BM25 关键字检索，结果去重后返回
   *
   * @param tenantId 租户 ID
   * @param userId   用户 ID
   * @param botId    应用 ID
   * @param spaceId  空间 ID
   * @param query    查询文本
   * @param topN     返回数量上限
   * @return 匹配的记忆文本分片列表（按相关度降序）
   */
  public List<String> hybridSearch(Long tenantId, Long userId, Long botId, Long spaceId, String fileName,
      String query, int topN) {
    if (!isAvailable()) {
      return List.of();
    }
    try {
      // 通用智能体使用 spaceId，自主规划智能体使用 tenantId
      Long finalTenantId = tenantId == null ? spaceId : tenantId;

      String indexName = buildIndexName(finalTenantId, userId, botId, fileName);
      if (!hasIndex(indexName)) {
        return List.of();
      }

      EmbeddingClient embeddingClient = getEmbeddingClient();
      float[] queryVector = embeddingClient.embedding(query);

      // @formatter:off
      // 向量检索 + BM25 关键字 bool 联合查询
      SearchRequest request = SearchRequest.of(b -> b
          .index(indexName)
          .size(topN)
          .query(q -> q.bool(bq -> bq
              // 向量相似度（主要排序依据）
              .should(s -> s.scriptScore(ss -> ss
                  .query(sq -> sq.matchAll(ma -> ma))
                  .script(sc -> sc
                      .source(COSINE_SCRIPT)
                      .params("query_vector", JsonData.of(queryVector)))))
              // BM25 关键字检索（辅助排序）
              .should(s -> s.match(m -> m.field("chunkText").query(query)))
          ))
      );
      // @formatter:on
      SearchResponse<Map<String, Object>> response = getClient().search(request,
          ElasticsearchHelper.MAP_TYPE);

      return response.hits().hits().stream()
          .map(hit -> {
            Map<String, Object> source = hit.source();
            return source != null ? (String) source.get("chunkText") : null;
          })
          .filter(StringUtils::isNotBlank)
          .toList();
    }
    catch (Exception e) {
      logger.error("记忆向量混合检索失败: query={}", query, e);
      return List.of();
    }
  }

  /**
   * 构建索引名称（按租户+用户+应用隔离）
   */
  private String buildIndexName(Long tenantId, Long userId, Long botId, String fileName) {
    ElasticSearchProperties props = getProperties();
    return props.getNamespace() + "_" + INDEX_PREFIX + "_" + tenantId + "_" + userId + "_" + botId + "_" + fileName.toLowerCase();
  }

  /**
   * 检查索引是否存在
   */
  private boolean hasIndex(String indexName) {
    try {
      //noinspection resource
      return getClient().indices().exists(ExistsRequest.of(b -> b.index(indexName))).value();
    }
    catch (IOException e) {
      logger.error("检查记忆索引是否存在失败: {}", indexName, e);
      return false;
    }
  }

  /**
   * 创建向量索引，包含 dense_vector 和 text 字段映射
   */
  private void createIndex(String indexName) {
    int dimension = getVectorDimension();
    try {
      // @formatter:off
      TypeMapping mapping = TypeMapping.of(m -> m
          .properties("chunkId",   Property.of(p -> p.keyword(k -> k)))
          .properties("fileName",  Property.of(p -> p.keyword(k -> k)))
          .properties("chunkText", Property.of(p -> p.text(t -> t)))
          .properties(VECTOR_FIELD, Property.of(p -> p.denseVector(dv -> dv
              .dims(dimension)
              .index(true)
              .similarity(DenseVectorSimilarity.Cosine))))
      );
      // @formatter:on
      CreateIndexRequest req = CreateIndexRequest.of(b -> b.index(indexName).mappings(mapping));
      //noinspection resource
      getClient().indices().create(req);
      logger.info("创建记忆向量索引成功: {}, 维度: {}", indexName, dimension);
    }
    catch (IOException e) {
      throw new BssException("创建记忆向量索引失败: " + indexName, e);
    }
  }

  /**
   * 计算 chunk 的 hash 值（基于文件名、内容和索引位置）
   *
   * @param fileName 文件名
   * @param content  chunk 内容
   * @param index    chunk 在文件中的索引位置
   * @return SHA-256 hash 值（16进制字符串）
   */
  private String computeChunkHash(String fileName, String content, int index) {
    String sourceStr = fileName + "::" + index + "::" + content;
    return DigestUtils.sha256Hex(sourceStr);
  }

  /**
   * 删除索引（如果存在）
   */
  private void deleteIndexIfExists(String indexName) {
    try {
      if (hasIndex(indexName)) {
        getClient().indices().delete(co.elastic.clients.elasticsearch.indices.DeleteIndexRequest.of(b -> b.index(indexName)));
        if (logger.isDebugEnabled()) {
          logger.debug("删除记忆向量索引: {}", indexName);
        }
      }
    }
    catch (IOException e) {
      logger.error("删除记忆向量索引失败，忽略继续: {}", indexName, e);
    }
  }

  /**
   * 将 Markdown 文本按标题和段落切片
   *
   * <p>切片策略：
   * <ol>
   *   <li>先尝试按 ##/### 标题分割</li>
   *   <li>若分片过长（> MAX_CHUNK_CHARS）则再按空行分割段落</li>
   * </ol>
   * </p>
   */
  public static List<String> splitMarkdown(String content) {
    if (StringUtils.isBlank(content)) {
      return List.of();
    }
    // 提取所有标题
    List<String> headers = extractHeaders(content);
    // 按标题分割内容
    String[] sections = splitByHeaders(content);
    // 处理每个分片
    List<String> chunks = processSections(sections, headers);
    // 兜底：内容没有标题结构时直接按段落切
    if (chunks.isEmpty()) {
      return splitByParagraphs(content);
    }
    return chunks;
  }

  /**
   * 提取所有 Markdown 标题
   */
  private static List<String> extractHeaders(String content) {
    return HEADER_PATTERN.matcher(content).results()
        .map(mr -> mr.group().trim())
        .toList();
  }

  /**
   * 按标题分割内容
   */
  private static String[] splitByHeaders(String content) {
    return HEADER_PATTERN.split(content);
  }

  /**
   * 处理分割后的各个分片
   */
  private static List<String> processSections(String[] sections, List<String> headers) {
    List<String> chunks = new ArrayList<>();
    for (int i = 0; i < sections.length; i++) {
      String header = getHeaderForSection(i, headers);
      String body = sections[i].trim();
      if (StringUtils.isEmpty(body)) {
        continue;
      }
      processSectionContent(chunks, header, body);
    }
    return chunks;
  }

  /**
   * 获取分片对应的标题
   */
  private static String getHeaderForSection(int index, List<String> headers) {
    return (index > 0 && index - 1 < headers.size()) ? headers.get(index - 1) + "\n" : "";
  }

  /**
   * 处理单个分片内容
   */
  private static void processSectionContent(List<String> chunks, String header, String body) {
    String section = header + body;
    if (section.length() <= MAX_CHUNK_CHARS) {
      chunks.add(section);
    }
    else {
      splitLongSection(chunks, header, body);
    }
  }

  /**
   * 分割过长的分片
   */
  private static void splitLongSection(List<String> chunks, String header, String body) {
    for (String para : body.split("\\n{2,}")) {
      String trimmed = (header + para.trim()).trim();
      if (StringUtils.isNotBlank(trimmed)) {
        chunks.add(trimmed);
      }
    }
  }

  /**
   * 按段落分割内容（兜底策略）
   */
  private static List<String> splitByParagraphs(String content) {
    List<String> chunks = new ArrayList<>();
    for (String para : content.split("\\n{2,}")) {
      if (StringUtils.isNotBlank(para)) {
        chunks.add(para.trim());
      }
    }
    return chunks;
  }

  /**
   * 构建向量文档
   */
  private Map<String, Object> buildDocument(String chunkId, String fileName, String chunkText,
      float[] vector) {
    Map<String, Object> doc = new HashMap<>(6);
    doc.put("chunkId", chunkId);
    doc.put("fileName", fileName);
    doc.put("chunkText", chunkText);
    // ES 要求 dense_vector 为 List<Float>
    List<Float> vectorList = new ArrayList<>(vector.length);
    for (float v : vector) {
      vectorList.add(v);
    }
    doc.put(VECTOR_FIELD, vectorList);
    return doc;
  }

  /**
   * 获取平台预置的向量模型的向量维度
   */
  private int getVectorDimension() {
    try {
      EmbeddingClient client = getEmbeddingClient();
      return client.embedding("test").length;
    }
    catch (Exception e) {
      logger.error("获取平台预置的向量模型的向量维度失败，使用默认维度 {}", DEFAULT_VECTOR_DIMENSION, e);
      return DEFAULT_VECTOR_DIMENSION;
    }
  }

  /**
   * 使用平台预置的向量模型客户端
   */
  private EmbeddingClient getEmbeddingClient() {
    Long platformEmbedModelId = Long.valueOf(SystemParameter.BOTECLAW_EMBEDDING_MODEL_ID.getValueFromDb());
    return modelClientCache.getEmbeddingClient(BaseConsts.PLATFORM_TENANT_ID, platformEmbedModelId);
  }

  /**
   * 获取 Elasticsearch 客户端
   */
  private ElasticsearchClient getClient() {
    ElasticsearchClient client = clientProvider.getIfAvailable();
    if (client == null) {
      throw new BssException("Elasticsearch 客户端不可用，请检查配置");
    }
    return client;
  }

  /**
   * 获取 Elasticsearch 配置
   */
  private ElasticSearchProperties getProperties() {
    ElasticSearchProperties props = propertiesProvider.getIfAvailable();
    if (props == null) {
      throw new BssException("Elasticsearch 配置不可用");
    }
    return props;
  }
}
