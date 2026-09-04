package com.iwhalecloud.bote.intent.vector.adapter;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.json.JsonData;
import com.iwhalecloud.bote.common.elasticsearch.ElasticsearchHelper;
import com.iwhalecloud.bote.dto.intent.IntentionEmbeddingDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemDTO;
import com.iwhalecloud.bote.intent.vector.IntentionVectorStore;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 意图向量存储实现
 * <p>使用 Elasticsearch 的向量搜索功能</p>
 *
 * @author chen.linfa
 * @since 2025-07-31
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ElasticsearchIntentionVectorStore implements IntentionVectorStore {

  private static final Logger logger = LoggerFactory.getLogger(ElasticsearchIntentionVectorStore.class);

  private final ElasticsearchVector vector;
  /** 批量处理大小 */
  private static final int BATCH_SIZE = 50;
  /** 余弦相似度脚本模板 */
  private static final String COSINE_SIMILARITY_SCRIPT = "cosineSimilarity(params.query_vector, 'question_vector')";

  @Override
  public void add(IntentionEmbeddingDTO embedding) {
    String indexName = vector.buildIndexName(embedding.getTenantId());
    vector.ensureIndexExists(indexName, embedding.getTenantId());
    Map<String, Object> document = vector.buildVectorDocument(embedding);
    try {
      // @formatter:off
      IndexRequest<Map<String, Object>> request = IndexRequest.of(b -> b
        .index(indexName)
        .id(embedding.getTenantId() + "_" + embedding.getQuestionId())
        .document(document)
        .refresh(Refresh.True));
      // @formatter:on
      vector.getClient().index(request);
    }
    catch (IOException e) {
      logger.error("Failed to store document, questionId={}", embedding.getQuestionId(), e);
      throw new BssException("添加向量文档失败", e);
    }
  }

  @Override
  public void batchAdd(List<IntentionEmbeddingDTO> embeddings) {
    Long tenantId = embeddings.get(0).getTenantId();
    String indexName = vector.buildIndexName(tenantId);
    vector.ensureIndexExists(indexName, tenantId);
    // 分批处理
    for (List<IntentionEmbeddingDTO> batch : ListUtils.partition(embeddings, BATCH_SIZE)) {
      List<BulkOperation> operations = batch.stream().map(embedding -> {
        Map<String, Object> document = vector.buildVectorDocument(embedding);
        return BulkOperation.of(
          b -> b.index(IndexOperation.of(i -> i.index(indexName).id(embedding.getTenantId() + "_" + embedding.getQuestionId()).document(document))));
      }).collect(Collectors.toList());

      try {
        BulkRequest request = BulkRequest.of(b -> b.operations(operations).refresh(Refresh.True));
        BulkResponse response = vector.getClient().bulk(request);
        if (response.errors()) {
          logger.error("批量添加向量文档失败: {}", response.items());
          throw new BssException("批量添加向量文档失败");
        }
      }
      catch (IOException e) {
        logger.error("批量添加向量文档失败", e);
        throw new BssException("批量添加向量文档失败", e);
      }
    }
  }

  @Override
  public void save(IntentionEmbeddingDTO embedding) {
    add(embedding);
  }

  @Override
  public void batchSave(List<IntentionEmbeddingDTO> embeddings) {
    batchAdd(embeddings);
  }

  @Override
  public void delete(Long tenantId, Long questionId) {
    String indexName = vector.buildIndexName(tenantId);
    try {
      vector.getClient().delete(b -> b.index(indexName).id(tenantId + "_" + questionId));
    }
    catch (IOException e) {
      logger.error("Failed to delete document, questionId={}", questionId, e);
      // 删除失败时不抛出异常，因为可能文档不存在
    }
    catch (ElasticsearchException e) {
      // 索引或文档不存在时忽略错误
      if (e.getMessage() != null && (e.getMessage().contains("index_not_found_exception") || e.getMessage().contains("not_found"))) {
        logger.debug("Document or index not found, questionId={}, index={}", questionId, indexName);
      }
      else {
        logger.error("Failed to delete document, questionId={}", questionId, e);
      }
    }
  }

  @Override
  public void deleteByTenant(Long tenantId) {
    String indexName = vector.buildIndexName(tenantId);
    try {
      //noinspection resource
      vector.getClient().indices().delete(b -> b.index(indexName));
      logger.info("删除租户索引成功: {}", indexName);
    }
    catch (IOException e) {
      logger.error("Failed to delete es index: {}", indexName, e);
      // 索引不存在时不抛出异常
    }
    catch (ElasticsearchException e) {
      // 索引不存在时忽略错误（index_not_found_exception）
      if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
        logger.info("索引不存在，无需删除: {}", indexName);
      }
      else {
        logger.error("Failed to delete es index: {}", indexName, e);
      }
    }
  }

  @Override
  @Nullable
  public Long bestMatch(Long tenantId, @Nullable List<Long> sceneIds, float[] question, float scoreThreshold) {
    List<IntentionMatchItemDTO> items = topMatches(tenantId, sceneIds, question, scoreThreshold, 1);
    return items.isEmpty() ? null : items.get(0).getQuestionId();
  }

  @Override
  public List<IntentionMatchItemDTO> topMatches(Long tenantId, @Nullable List<Long> sceneIds, float[] question, float scoreThreshold, int topNum) {
    String indexName = vector.buildIndexName(tenantId);
    if (!vector.hasIndex(indexName)) {
      // 索引不存在，返回空结果
      return new ArrayList<>();
    }

    try {
      // @formatter:off
      // 构建向量搜索查询
      // 将0-1范围的阈值转换为-1到1范围的余弦相似度阈值
      double cosineThreshold = (scoreThreshold * 2.0) - 1.0;
      SearchRequest request = SearchRequest.of(b -> b
        .index(indexName)
        .size(topNum)
        .minScore(cosineThreshold)
        .query(q -> q.bool(bq -> {
          // 租户过滤
          bq.filter(f -> f.term(t -> t.field("tenantId").value(tenantId)));
          // 智能体过滤
          if (CollectionUtils.isNotEmpty(sceneIds)) {
            bq.filter(f -> f.bool(p -> {
              for (Long sceneId : sceneIds) {
                p.should(s -> s.term(t -> t.field("sceneId").value(sceneId)));
              }
              return p;
            }));
          }
          // 向量相似度搜索
          bq.must(m -> m.scriptScore(ss -> ss
            .query(sq -> sq.matchAll(ma -> ma))
            .script(s -> s
              .source(COSINE_SIMILARITY_SCRIPT)
              .params("query_vector", JsonData.of(question))
            )
          ));
          return bq;
        }))
      );
      // @formatter:on
      SearchResponse<Map<String, Object>> response = vector.getClient().search(request, ElasticsearchHelper.MAP_TYPE);
      return response.hits().hits().stream().map(hit -> {
        Map<String, Object> source = hit.source();
        IntentionMatchItemDTO item = new IntentionMatchItemDTO();
        item.setQuestionId(MapUtils.getLong(source, "questionId"));
        // 将余弦相似度从[-1,1]范围归一化到[0,1]范围
        float rawScore = hit.score() != null ? hit.score().floatValue() : 0.0f;
        item.setScore((rawScore + 1.0f) / 2.0f);
        return item;
      }).collect(Collectors.toList());
    }
    catch (IOException e) {
      logger.error("向量搜索失败，租户ID: {}, 索引: {}", tenantId, indexName, e);
      return new ArrayList<>();
    }
    catch (Exception e) {
      logger.error("向量搜索发生未知错误，租户ID: {}, 索引: {}", tenantId, indexName, e);
      return new ArrayList<>();
    }
  }
}
