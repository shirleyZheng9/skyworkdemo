package com.iwhalecloud.bote.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.util.ModelClientUtil;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.llm.client.EmbeddingClient;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.ModelClient;
import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 大模型客户端缓存
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Component
@RequiredArgsConstructor
public class ModelClientCache implements TenantCacheMarker {
  private static final Logger logger = LoggerFactory.getLogger(ModelClientCache.class);
  /** 缓存实例 */
  private final LoadingCache<@NonNull Pair<Long, Long>, @NonNull ModelClient> cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterAccess(Duration.ofHours(1))
    .build(CacheLoader.from(this::buildModelClient));

  private final LargeModelManageMapper modelMapper;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_MODEL_CLIENT;
  }

  /**
   * 获取大语言模型客户端
   *
   * @param tenantId 租户 ID
   * @param modelId 模型 ID
   * @return 大语言模型客户端
   */
  public LlmClient getLlmClient(Long tenantId, Long modelId) {
    ModelClient client = getModelClient(tenantId, modelId);
    Assert.isTrue(client instanceof LlmClient, () -> "模型 " + modelId + " 不是大语言模型");
    return (LlmClient) client;
  }

  /**
   * 获取嵌入模型客户端
   *
   * @param tenantId 租户 ID
   * @param modelId 模型 ID
   * @return 嵌入模型客户端
   */
  public EmbeddingClient getEmbeddingClient(Long tenantId, Long modelId) {
    ModelClient client = getModelClient(tenantId, modelId);
    Assert.isTrue(client instanceof EmbeddingClient, () -> "模型 " + modelId + " 不是文本嵌入模型");
    return (EmbeddingClient) client;
  }

  /**
   * 获取模型客户端
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private ModelClient getModelClient(Long tenantId, Long modelId) {
    Assert.notNull(modelId, "modelId 不能为空");
    try {
      return cache.get(Pair.of(tenantId, modelId));
    }
    catch (ExecutionException e) {
      if (e.getCause() instanceof BssException) {
        throw (BssException) e.getCause();
      }
      throw new BssException("获取大模型客户端失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构造大模型客户端
   */
  private ModelClient buildModelClient(Pair<Long, Long> key) {
    Long tenantId = key.getLeft();
    Long modelId = key.getRight();
    SimpleLargeModelDTO model = modelMapper.selectLargeModelById(tenantId, modelId);
    Assert.notNull(model, "大模型不存在");
    if (ModelConsts.MODEL_TYPE_LLM.equals(model.getModelType())) {
      LlmProperties properties = ModelConfigUtil.buildLlmProperties(model);
      return ModelClientUtil.createLlmClient(model.getProtocolType(), properties);
    }
    else if (ModelConsts.MODEL_TYPE_EMBEDDING.equals(model.getModelType())) {
      EmbeddingProperties properties = ModelConfigUtil.buildEmbeddingProperties(model);
      return ModelClientUtil.createEmbeddingClient(properties);
    }
    else {
      throw new BssException("未知的模型分类: " + model.getModelType());
    }
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache() {
    logger.debug("Refresh local: cacheName={}", getCacheName());
    cache.invalidateAll();
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void refreshLocalCache(List<String> keys) {
    logger.debug("Refresh local by keys: cacheName={}, keys={}", getCacheName(), keys);
    if (CollectionUtils.isEmpty(keys)) {
      return;
    }
    // 删除单个租户的缓存
    if (keys.size() == 1 && StringUtils.isNumeric(keys.get(0))) {
      invalidateLocalCache(cache, keys);
      return;
    }
    for (String key : keys) {
      String[] pieces = StringUtils.split(key, ':');
      if (pieces == null || pieces.length != 2 || !NumberUtils.isCreatable(pieces[0]) || !StringUtils.isNumeric(pieces[1])) {
        continue;
      }
      long tenantId = Long.parseLong(pieces[0]);
      Long modelId = Long.parseLong(pieces[1]);
      // 目前各个租户可能都会缓存平台级的模型，因此刷新平台级模型时需要清除所有租户的缓存
      if (tenantId == -1L) {
        cache.asMap().keySet().removeIf(p -> modelId.equals(p.getRight()));
      }
      else {
        cache.invalidate(Pair.of(tenantId, modelId));
      }
    }
  }

  @Override
  public void refresh() {
    refreshLocalCache();
  }

  @Override
  public void refresh(List<String> keys) {
    refreshLocalCache(keys);
  }

}
