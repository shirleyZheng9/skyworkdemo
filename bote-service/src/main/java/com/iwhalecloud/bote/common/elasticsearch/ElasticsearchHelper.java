package com.iwhalecloud.bote.common.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.analysis.Analyzer;
import co.elastic.clients.elasticsearch._types.analysis.CustomAnalyzer;
import co.elastic.clients.elasticsearch._types.analysis.TokenChar;
import co.elastic.clients.elasticsearch._types.mapping.CompletionProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.SuggestContext;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.CompletionContext;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Context;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.config.properties.ElasticSearchProperties;
import com.iwhalecloud.bote.dto.base.BoteEsDocument;
import com.iwhalecloud.bote.dto.base.BoteEsRequest;
import com.iwhalecloud.bote.dto.base.BoteSuggestionResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Elasticsearch 辅助类，封装索引管理、文档操作及全文搜索功能
 *
 * @author lizuyin
 * @since 2025-06-10
 */
@Component
public final class ElasticsearchHelper {
  /** Map 类型 */
  public static final Type MAP_TYPE = new TypeReference<Map<String, Object>>() {
  }.getType();

  private final ElasticsearchClient client;
  private final ElasticSearchProperties properties;

  public ElasticsearchHelper(ObjectProvider<ElasticsearchClient> clientProvider, ObjectProvider<ElasticSearchProperties> propertiesProvider) {
    this.client = clientProvider.getIfAvailable();
    this.properties = propertiesProvider.getIfAvailable();
  }

  /**
   * 检查Elasticsearch客户端是否可用
   *
   * @return true 如果客户端可用，false 否则
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean isElasticsearchAvailable() {
    return client != null;
  }

  /**
   * 删除指定名称的ElasticSearch索引
   *
   * @param indexName 要删除的索引名称
   */
  public void deleteIndex(String indexName) {
    if (!isElasticsearchAvailable()) {
      return; // ES不可用时静默返回
    }

    try {
      DeleteIndexRequest request = DeleteIndexRequest.of(b -> b.index(indexName));
      client.indices().delete(request);
    } catch (IOException | ElasticsearchException e) {
      throw new BssException("ElasticSearch删除索引失败!" + e.getMessage(), e);
    }
  }

  /**
   * 判断指定名称的索引是否存在
   *
   * @param indexName 要检查的索引名称
   * @return true 表示索引存在，ES不可用时返回false
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean hasIndex(String indexName) {
    if (!isElasticsearchAvailable()) {
      return false;
    }
    try {
      ExistsRequest request = ExistsRequest.of(b -> b.index(indexName));
      return client.indices().exists(request).value();
    }
    catch (ElasticsearchException e) {
      if ("index_not_found_exception".equals(e.error().type())) {
        return false;
      }
      throw new BssException("ElasticSearch查询索引是否存在失败!" + e.getMessage(), e);
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch查询索引是否存在失败!" + e.getMessage(), e);
    }
  }

  /**
   * 构建租户隔离的索引名称
   *
   * @param tenantId 租户ID
   * @return 租户隔离的索引名称
   */
  private String buildIndexName(Long tenantId) {
    String indexBaseName = properties.getNamespace();
    Assert.notNull(indexBaseName, "命名空间不能为空");
    return indexBaseName + "_" + tenantId;
  }

  /**
   * 创建索引别名
   *
   * @param aliasName 别名
   * @param indexName 索引名称
   */
  private void createAlias(String aliasName, String indexName) {
    if (!isElasticsearchAvailable()) {
      return; // ES不可用时静默返回
    }

    try {
      client.indices().updateAliases(b -> b
        .actions(a -> a
          .add(add -> add
            .index(indexName)
            .alias(aliasName)
          )
        )
      );
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch新建别名失败!" + e.getMessage(), e);
    }
  }

  /**
   * 获取别名下的所有索引
   *
   * @param aliasName 别名
   * @return 索引名称列表，ES不可用时返回空列表
   */
  private List<String> getIndicesByAlias(String aliasName) {
    if (!isElasticsearchAvailable()) {
      return Collections.emptyList();
    }

    try {
      return new ArrayList<>(client.indices().getAlias(b -> b.name(aliasName)).result().keySet());
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch获取别名index失败!" + e.getMessage(), e);
    }
  }

  /**
   * 删除别名
   *
   * @param aliasName 别名
   * @param indexName 索引名称
   */
  public void removeAlias(String aliasName, String indexName) {
    if (!isElasticsearchAvailable()) {
      return; // ES不可用时静默返回
    }

    try {
      client.indices().updateAliases(b -> b
        .actions(a -> a
          .remove(remove -> remove
            .index(indexName)
            .alias(aliasName)
          )
        )
      );
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch删除别名失败!" + e.getMessage(), e);
    }
  }

  /**
   * 执行批量请求
   *
   * @param bulkRequest 批量请求对象
   * @param expectedCount 期望的文档数量
   * @return 实际添加的文档数量
   */
  private int executeBulkRequest(BulkRequest bulkRequest, int expectedCount) {
    try {
      BulkResponse bulkResponse = client.bulk(bulkRequest);

      if (bulkResponse.errors()) {
        throw new BssException("批量添加文档时发生错误，部分文档可能未成功添加");
      }

      return expectedCount;
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch批量添加文档失败", e.getMessage(), e);
    }
  }

  /**
   * 添加支持建议功能的文档到ElasticSearch
   *
   * @param document 要添加的文档对象
   * @return 成功时返回生成的文档ID，ES不可用时返回提示信息
   */
  public String addSuggestionDocument(BoteEsDocument document) {
    if (!isElasticsearchAvailable()) {
      return null;
    }

    String indexName = buildIndexName(document.getTenantId());

    if (!hasIndex(indexName)) {
      String index = createNgramIndex(document.getTenantId());
      createAlias(indexName, index);
    }

    IndexRequest<BoteEsDocument> request = IndexRequest.of(b -> b
      .index(indexName)
      .id(document.getId())
      .document(document)
      .refresh(Refresh.True)
    );

    try {
      return client.index(request).id();
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch新增文档失败!" + e.getMessage(), e);
    }
  }

  /**
   * 更新支持建议功能的文档
   *
   * @param document 包含新数据的文档对象
   * @return 成功时返回更新后的文档ID，ES不可用时返回null
   */
  public String updateSuggestionDocument(BoteEsDocument document) {
    if (!isElasticsearchAvailable()) {
      return null;
    }

    String indexName = buildIndexName(document.getTenantId());

    // 使用UpdateRequest进行更新
    UpdateRequest<BoteEsDocument, BoteEsDocument> request = UpdateRequest.of(b -> b
      .index(indexName)
      .id(document.getId())
      .doc(document)
      .refresh(Refresh.True)
    );

    try {
      return client.update(request, BoteEsDocument.class).id();
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch更新文档失败!" + e.getMessage(), e);
    }
  }

  /**
   * 删除指定ID的文档（支持自定义索引名和刷新策略）
   *
   * @param id 文档唯一标识符
   * @param tenantId 租户ID
   * @return true 表示文档删除成功，ES不可用时返回false
   */
  public boolean deleteSuggestionDocument(String id, Long tenantId) {
    if (!isElasticsearchAvailable()) {
      return false;
    }

    String indexName = buildIndexName(tenantId);
    DeleteRequest request = new DeleteRequest.Builder()
      .index(indexName)
      .id(id)
      .refresh(Refresh.True)
      .build();

    try {
      DeleteResponse response = client.delete(request);
      return response.result() == Result.Deleted;
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch删除文档失败!" + e.getMessage(), e);
    }
  }

  /**
   * 构建建议查询的上下文
   *
   * @param req 搜索请求参数
   * @return 构建好的上下文Map
   */
  private Map<String, List<CompletionContext>> buildSuggestionContext(BoteEsRequest req) {
    Map<String, Object> contextMap = new HashMap<>();
    contextMap.put("tenantId", String.valueOf(req.getTenantId()));

    if (StringUtils.isNotBlank(req.getOwnerType())) {
      contextMap.put("ownerType", req.getOwnerType());
    }

    if (StringUtils.isNotBlank(req.getOwnerId())) {
      contextMap.put("ownerId", req.getOwnerId());
    }

    String combinedContextJson = JsonUtil.toJsonString(contextMap);

    Map<String, List<CompletionContext>> contexts = new HashMap<>();
    contexts.put("combined_context", Collections.singletonList(
      CompletionContext.of(b -> b
        .context(Context.of(cb -> cb.category(combinedContextJson)))
      )
    ));

    return contexts;
  }

  /**
   * 构建建议查询的搜索请求
   *
   * @param req 搜索请求参数
   * @param indexName 索引名称
   * @param contexts 建议上下文
   * @return 构建好的搜索请求
   */
  private SearchRequest buildSuggestionSearchRequest(BoteEsRequest req, String indexName,
                                                   Map<String, List<CompletionContext>> contexts) {
    return SearchRequest.of(b -> b
      .index(indexName)
      .suggest(Suggester.of(s -> s
        .suggesters("term_suggest", sg -> sg
          .prefix(StringUtils.isNotBlank(req.getKeyword()) ? req.getKeyword() : "")
          .completion(c -> c
            .field("suggest")
            .size(Optional.ofNullable(req.getLimit()).orElse(10))
            .skipDuplicates(true)
            .contexts(contexts)
          )
        )
      ))
    );
  }

  /**
   * 处理建议查询的响应结果
   *
   * @param response Elasticsearch响应结果
   * @param keyword 搜索关键字，用于计算补全内容
   * @return 处理后的建议响应列表
   */
  private List<BoteSuggestionResponse> processSuggestionResponse(SearchResponse<Map<String, Object>> response, String keyword) {
    List<Suggestion<Map<String, Object>>> termSuggestions = response.suggest().get("term_suggest");
    if (CollectionUtils.isEmpty(termSuggestions)) {
      return Collections.emptyList();
    }
    List<BoteSuggestionResponse> responses = new ArrayList<>();
    for (Suggestion<Map<String, Object>> suggestion : termSuggestions) {
      if (suggestion.isCompletion()) {
        for (CompletionSuggestOption<Map<String, Object>> option : suggestion.completion().options()) {
          responses.add(extractSuggestionFromOption(option, keyword));
        }
      }
    }
    return responses;
  }

    /**
   * 从单个建议选项中提取建议响应对象
   *
   * @param option 建议选项
   * @param keyword 搜索关键字，用于计算补全内容
   * @return 建议响应对象
   */
  private BoteSuggestionResponse extractSuggestionFromOption(CompletionSuggestOption<Map<String, Object>> option, String keyword) {
    BoteSuggestionResponse result = new BoteSuggestionResponse();
    result.setText(option.text());

    Optional.ofNullable(option.source())
      .ifPresent(source -> {
        String termContent = MapUtils.getString(source, "content");
        result.setTermId(MapUtils.getString(source, "id"));
        result.setTermContent(termContent);
        result.setOwnerType(MapUtils.getString(source, "ownerType"));
        result.setOwnerId(MapUtils.getString(source, "ownerId"));
        result.setType(MapUtils.getString(source, "type"));

        // 计算补全内容
        result.setTermCompletion(calculateTermCompletion(termContent, keyword));

        // Suggestion 搜索不提供高亮功能，设置为空字符串
        result.setHighlight("");
      });

    return result;
  }

  /**
   * 计算补全内容
   *
   * @param termContent 术语完整内容
   * @param keyword 搜索关键字
   * @return 补全内容，如果术语内容以关键字开头则返回剩余部分，否则返回空字符串
   */
  private String calculateTermCompletion(String termContent, String keyword) {
    if (StringUtils.isBlank(termContent) || StringUtils.isBlank(keyword)) {
      return "";
    }

    return termContent.toLowerCase().startsWith(keyword.toLowerCase()) ?
            termContent.substring(keyword.length()).trim() : "";
  }

  /**
   * 准备建议文档的批量添加数据
   *
   * @param documents 原始文档列表
   * @return 处理后的文档列表
   */
  private List<BoteEsDocument> prepareDocumentsForSuggestionAdd(List<BoteEsDocument> documents) {
    return documents.stream()
      .peek(document -> {
        if (StringUtils.isBlank(document.getId())) {
          document.setId(String.valueOf(IDUtils.nextId()));
        }
      })
      .collect(Collectors.toList());
  }

  /**
   * 构建建议功能的批量请求
   *
   * @param documents 文档列表
   * @param indexName 索引名称
   * @return 批量请求对象
   */
  private BulkRequest buildSuggestionBulkRequest(List<BoteEsDocument> documents, String indexName) {
    BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

    documents.forEach(document -> {
      IndexRequest<BoteEsDocument> indexReq = IndexRequest.of(b -> b
        .index(indexName)
        .id(document.getId())
        .document(document)
      );

      BulkOperation operation = BulkOperation.of(b -> b
        .index(IndexOperation.of(io -> io
          .document(indexReq.document())
          .index(indexName)
          .id(indexReq.id())
        ))
      );

      bulkBuilder.operations(operation);
    });

    return bulkBuilder.build();
  }

  /**
   * 添加match查询的过滤条件
   *
   * @param bq bool查询构建器
   * @param req 搜索请求参数
   */
  private void addMatchFilters(co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder bq, BoteEsRequest req) {
    // 添加所有者ID过滤
    if (StringUtils.isNotBlank(req.getOwnerId())) {
      bq.filter(f -> f
          .term(t -> t
              .field("ownerId")
              .value(req.getOwnerId())
          )
      );
    }

    // 添加所有者类型过滤
    if (StringUtils.isNotBlank(req.getOwnerType())) {
      bq.filter(f -> f
          .term(t -> t
              .field("ownerType")
              .value(req.getOwnerType())
          )
      );
    }

    // 添加租户ID过滤
    bq.filter(f -> f
        .term(t -> t
            .field("tenantId")
            .value(req.getTenantId())
        )
    );

    // 添加类型过滤
    if (StringUtils.isNotBlank(req.getType())) {
      bq.filter(f -> f
          .term(t -> t
              .field("type")
              .value(req.getValidType())
          )
      );
    }
  }

  /**
   * 处理 match 搜索的响应结果
   *
   * @param response Elasticsearch响应结果
   * @return 处理后的搜索结果列表
   */
  private List<BoteSuggestionResponse> processMatchResponse(SearchResponse<Map<String, Object>> response) {
    return Optional.ofNullable(response.hits())
        .map(HitsMetadata::hits)
        .orElse(Collections.emptyList())
        .stream()
        .filter(hit -> hit.source() != null)
        .map(this::convertHitToSuggestionResponse)
        .collect(Collectors.toList());
  }

    /**
   * 将搜索命中结果转换为建议响应对象
   *
   * @param hit 搜索命中结果
   * @return 建议响应对象
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private BoteSuggestionResponse convertHitToSuggestionResponse(Hit<Map<String, Object>> hit) {
    BoteSuggestionResponse response = new BoteSuggestionResponse();
    Map<String, Object> source = hit.source();

    String termContent = MapUtils.getString(source, "content");
    response.setTermId(MapUtils.getString(source, "id"));
    // 将score限制为两位小数点
    response.setScore(hit.score() != null ? Math.round(hit.score() * 100.0) / 100.0 : null);
    response.setTermContent(termContent);
    response.setText(termContent);
    response.setOwnerType(MapUtils.getString(source, "ownerType"));
    response.setOwnerId(MapUtils.getString(source, "ownerId"));
    response.setType(MapUtils.getString(source, "type"));
    response.setHighlight(extractHighlightContent(hit));
    return response;
  }

  /**
   * 提取高亮内容，优先获取content.ngram字段的高亮数据
   *
   * @param hit 搜索命中结果
   * @return 高亮内容，优先返回content.ngram的高亮，如果没有则尝试content字段，都没有则返回空字符串
   */
  private String extractHighlightContent(Hit<Map<String, Object>> hit) {
    return Optional.ofNullable(hit.highlight())
        .map(highlightMap -> {
            List<String> ngramHighlight = highlightMap.get("content.ngram");
            if (ngramHighlight != null && !ngramHighlight.isEmpty()) {
                return ngramHighlight.get(0);
            }
            return "";
        })
        .orElse("");
  }

  /**
   * 构建ngram索引的映射配置 - 使用IK分词器+ngram组合
   *
   * @return ngram索引的TypeMapping
   */
  private TypeMapping buildNgramIndexMapping() {
    return TypeMapping.of(m -> m
      .properties("id", Property.of(p -> p.keyword(k -> k)))
      .properties("content", Property.of(p -> p.text(t -> t
        .analyzer("ik_max_word")
        .searchAnalyzer("ik_max_word")
        .fields("ngram", Property.of(nf -> nf.text(nt -> nt
          .analyzer("trigram_analyzer")
          .searchAnalyzer("trigram_analyzer")
        )))
      )))
      .properties("ownerType", Property.of(p -> p.keyword(k -> k)))
      .properties("ownerId", Property.of(p -> p.keyword(k -> k)))
      .properties("tenantId", Property.of(p -> p.long_(l -> l)))
      .properties("type", Property.of(p -> p.keyword(k -> k)))
      .properties("suggest", Property.of(p -> p
        .completion(CompletionProperty.of(c -> c
          .contexts(Collections.singletonList(SuggestContext.of(d -> d
            .name("combined_context")
            .type("category")
          )))
          .analyzer("ik_max_word")
          .searchAnalyzer("ik_max_word")
        ))
      ))
    );
  }

  /**
   * 构建IK+ngram组合索引的设置配置
   *
   * @return 组合索引的IndexSettings
   */
  private IndexSettings buildNgramIndexSettings() {
    return IndexSettings.of(s -> s
      .maxNgramDiff(10)
      .analysis(IndexSettingsAnalysis.of(a -> a
        .analyzer("trigram_analyzer", Analyzer.of(an -> an
          .custom(CustomAnalyzer.of(ca -> ca
            .tokenizer("trigram_tokenizer")
            .filter("lowercase")
          ))
        ))
        .tokenizer("trigram_tokenizer", co.elastic.clients.elasticsearch._types.analysis.Tokenizer.of(t -> t
          .definition(co.elastic.clients.elasticsearch._types.analysis.TokenizerDefinition.of(td -> td
            .ngram(co.elastic.clients.elasticsearch._types.analysis.NGramTokenizer.of(ngt -> ngt
              .minGram(1)
              .maxGram(2)
              .tokenChars(TokenChar.Letter, TokenChar.Digit, TokenChar.Punctuation, TokenChar.Symbol)
            ))
          ))
        ))
      ))
    );
  }

  /**
   * 创建支持IK+ngram组合分词的统一索引
   *
   * @param tenantId  租户ID
   * @return 成功时返回索引名称
   */
  public String createNgramIndex(Long tenantId) {
    try {
      String newIndexName = buildIndexName(tenantId) + "_" + IDUtils.nextId();

      // 使用统一的映射构建方法
      TypeMapping mapping = buildNgramIndexMapping();

      // 构建索引设置和分析器配置
      IndexSettings settings = buildNgramIndexSettings();

      CreateIndexRequest request = CreateIndexRequest.of(b -> b
        .index(newIndexName)
        .mappings(mapping)
        .settings(settings)
      );

      client.indices().create(request);
      return newIndexName;
    }
    catch (IOException | ElasticsearchException e) {
      throw new BssException("ElasticSearch新建IK+ngram组合索引失败!" + e.getMessage(), e);
    }
  }

  /**
   * 添加支持ngram分词的文档到ElasticSearch
   *
   * @param document 要添加的文档对象
   * @return 成功时返回生成的文档ID，ES不可用时返回提示信息
   */
  public String addNgramDocument(BoteEsDocument document) {
    if (!isElasticsearchAvailable()) {
      return null;
    }

    String indexName = buildIndexName(document.getTenantId());

    if (!hasIndex(indexName)) {
      String index = createNgramIndex(document.getTenantId());
      createAlias(indexName, index);
    }

    IndexRequest<BoteEsDocument> request = IndexRequest.of(b -> b
      .index(indexName)
      .id(document.getId())
      .document(document)
      .refresh(Refresh.True)
    );

    try {
      return client.index(request).id();
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch新增ngram文档失败!" + e.getMessage(), e);
    }
  }

  /**
   * 更新支持ngram分词的文档
   *
   * @param document 包含新数据的文档对象
   * @return 成功时返回更新后的文档ID，ES不可用时返回null
   */
  public String updateNgramDocument(BoteEsDocument document) {
    if (!isElasticsearchAvailable()) {
      return null;
    }

    String indexName = buildIndexName(document.getTenantId());

    // 使用UpdateRequest进行更新
    UpdateRequest<BoteEsDocument, BoteEsDocument> request = UpdateRequest.of(b -> b
      .index(indexName)
      .id(document.getId())
      .doc(document)
      .refresh(Refresh.True)
    );

    try {
      return client.update(request, BoteEsDocument.class).id();
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch更新ngram文档失败!" + e.getMessage(), e);
    }
  }

  /**
   * 删除指定ID的文档（支持自定义索引名和刷新策略）
   *
   * @param id 文档唯一标识符
   * @param tenantId 租户ID
   * @return true 表示文档删除成功，ES不可用时返回false
   */
  public boolean deleteNgramDocument(String id, Long tenantId) {
    if (!isElasticsearchAvailable()) {
      return false;
    }

    String indexName = buildIndexName(tenantId);
    DeleteRequest request = new DeleteRequest.Builder()
      .index(indexName)
      .id(id)
      .refresh(Refresh.True)
      .build();

    try {
      DeleteResponse response = client.delete(request);
      return response.result() == Result.Deleted;
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch删除ngram文档失败!" + e.getMessage(), e);
    }
  }

  /**
   * 执行ngram建议查询
   *
   * @param req 搜索请求参数
   * @return 返回匹配的ngram建议结果列表
   */
  public List<BoteSuggestionResponse> searchNgramSuggestions(BoteEsRequest req) {
    if (!isElasticsearchAvailable()) {
      return Collections.emptyList();
    }

    String indexName = buildIndexName(req.getTenantId());
    if (!hasIndex(indexName)) {
      return Collections.emptyList();
    }

    try {
      Map<String, List<CompletionContext>> contexts = buildSuggestionContext(req);
      SearchRequest searchRequest = buildSuggestionSearchRequest(req, indexName, contexts);
      SearchResponse<Map<String, Object>> response = client.search(searchRequest, MAP_TYPE);
      return processSuggestionResponse(response, req.getKeyword());
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch ngram 建议查询失败!" + e.getMessage(), e);
    }
    catch (Exception e) {
      throw new BssException("构建 ngram 查询 context 失败!" + e.getMessage(), e);
    }
  }

  /**
   * 批量重建ngram索引
   *
   * @param documents 要添加的文档列表
   * @param tenantId  租户ID
   */
  public void rebuildNgramIndex(List<BoteEsDocument> documents, Long tenantId) {
    if (!isElasticsearchAvailable() || documents.isEmpty()) {
      return;
    }

    String baseIndexName = buildIndexName(tenantId);
    String newIndexName = createNgramIndex(tenantId);

    try {
      // 使用批量添加方法
      addNgramDocuments(documents, newIndexName);

      // 创建别名和清理旧索引
      createAlias(baseIndexName, newIndexName);
      List<String> oldIndices = getIndicesByAlias(baseIndexName);

      oldIndices.stream()
        .filter(oldIndex -> !oldIndex.equals(newIndexName))
        .forEach(this::deleteIndex);
    }
    catch (Exception e) {
      deleteIndex(newIndexName);
      throw new BssException("ElasticSearch ngram 重建索引失败!" + e.getMessage(), e);
    }
  }

  /**
   * 批量添加支持ngram分词的文档到ElasticSearch（直接添加到现有索引）
   *
   * @param documents 要添加的文档对象列表
   * @param indexName 目标索引名称
   * @return 返回成功添加的文档数量
   */
  public int addNgramDocuments(List<BoteEsDocument> documents, String indexName) {
    if (!isElasticsearchAvailable() || documents.isEmpty()) {
      return 0;
    }

    List<BoteEsDocument> processedDocuments = prepareDocumentsForSuggestionAdd(documents);
    BulkRequest bulkRequest = buildSuggestionBulkRequest(processedDocuments, indexName);

    return executeBulkRequest(bulkRequest, documents.size());
  }

  /**
   * 执行 IK+ngram 组合搜索
   *
   * @param req 搜索请求参数
   * @return 返回匹配的组合搜索结果列表，使用IK分词器（中文）+ngram分词器（英文数字）
   */
  public List<BoteSuggestionResponse> searchWithNgramMatch(BoteEsRequest req) {
    return !isElasticsearchAvailable() ? Collections.emptyList() :
        Optional.ofNullable(req)
            .filter(r -> StringUtils.isNotBlank(r.getKeyword()))
            .filter(r -> r.getTenantId() != null)
            .map(this::executeNgramMatchSearch)
            .orElse(Collections.emptyList());
  }

  /**
   * 执行基于 IK+ngram 组合查询的搜索
   *
   * @param req 搜索请求参数
   * @return 返回匹配的组合搜索结果列表
   */
  private List<BoteSuggestionResponse> executeNgramMatchSearch(BoteEsRequest req) {
    String indexName = buildIndexName(req.getTenantId());
    return !hasIndex(indexName) ? Collections.emptyList() :
        executeNgramMatchSearchRequest(req, indexName);
  }

  /**
   * 执行 IK+ngram 组合搜索请求
   *
   * @param req 搜索请求参数
   * @param indexName 索引名称
   * @return 返回匹配的组合搜索结果列表
   */
  private List<BoteSuggestionResponse> executeNgramMatchSearchRequest(BoteEsRequest req, String indexName) {
    try {
      SearchRequest searchRequest = buildNgramMatchSearchRequest(req, indexName);
      SearchResponse<Map<String, Object>> response = client.search(searchRequest, MAP_TYPE);
      return processMatchResponse(response);
    }
    catch (IOException e) {
      throw new BssException("ElasticSearch IK+ngram 组合搜索失败!" + e.getMessage(), e);
    }
  }

  /**
   * 构建基于 IK+ngram 组合查询的搜索请求
   *
   * @param req 搜索请求参数
   * @param indexName 索引名称
   * @return 返回构建完成的组合搜索请求对象
   */
  private SearchRequest buildNgramMatchSearchRequest(BoteEsRequest req, String indexName) {
    double minScore = Optional.ofNullable(req.getScore()).orElse(5.0);
    int size = Optional.ofNullable(req.getLimit()).orElse(10);
    String field = StringUtils.isNotBlank(req.getField()) ? req.getField() : "content";

    return SearchRequest.of(b -> b
        .index(indexName)
        .minScore(minScore)
        .size(size)
        .query(q -> q
            .bool(bq -> {
              // 使用 should 查询，结合IK分词器和ngram分词器
              bq.should(s -> s
                  .match(match -> match
                      .field(field)
                      .query(req.getKeyword())
                      .boost(2.0f)
                  )
              )
              .should(s -> s
                  .match(match -> match
                      .field(field + ".ngram")
                      .query(req.getKeyword())
                      .boost(1.0f)
                  )
              )
              .minimumShouldMatch("1");

              // 添加过滤条件
              addMatchFilters(bq, req);

              return bq;
            })
        )
        .highlight(h -> h
            .fields(field, hf -> hf
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(150)
                .numberOfFragments(1)
            )
            .fields(field + ".ngram", hf -> hf
                .preTags("<em>")
                .postTags("</em>")
                .fragmentSize(150)
                .numberOfFragments(1)
            )
        )
    );
  }

  /**
   * 调用Elasticsearch的_analyze API获取分词结果
   *
   * @param text 待分词的文本
   * @return 分词结果列表，ES不可用时返回空列表
   */
  public List<String> analyzeText(String text) {
    if (!isElasticsearchAvailable()) {
      return Collections.emptyList();
    }

    if (StringUtils.isBlank(text)) {
      return Collections.emptyList();
    }

    try {
      AnalyzeRequest request = AnalyzeRequest.of(b -> b
        .text(text)
        .analyzer("ik_smart")
      );
      AnalyzeResponse response = client.indices().analyze(request);
      return response.tokens().stream()
          .map(AnalyzeToken::token)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new BssException("ElasticSearch调用_analyze API失败!" + e.getMessage(), e);
    } catch (ElasticsearchException e) {
      throw new BssException("ElasticSearch分词分析失败!" + e.getMessage(), e);
    }
  }
}
