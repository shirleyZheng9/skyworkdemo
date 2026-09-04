package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.TokenUsageDTO;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 大模型 token 数量统计缓存
 *
 * @author bianjp
 * @since 2025-11-10
 */
@Component
public class LlmTokenCountCache {
  /** 缓存 key: 总数 */
  private static final String KEY_TOTAL = "total";
  /** 缓存 key: 输入 */
  private static final String KEY_PROMPT = "prompt";
  /** 缓存 key: 输出 */
  private static final String KEY_COMPLETION = "completion";
  /** 缓存 key : 模型前缀 */
  private static final String KEY_MODEL_PREFIX = "model:";
  /** 缓存有效期(小时), 不需要太久 */
  private static final long EXPIRE_HOURS = 2;

  private final ICacheClient cacheClient;

  public LlmTokenCountCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LLM_TOKEN_COUNT);
  }

  /**
   * 增加单次模型调用的 token 数量
   */
  public void increment(@Nullable String traceId, @Nullable Usage usage, @Nullable String modelCode) {
    if (StringUtils.isEmpty(traceId) || usage == null) {
      return;
    }
    Integer totalTokens = usage.getTotalTokens();
    if (totalTokens == null || totalTokens <= 0) {
      return;
    }
    Integer promptTokens = usage.getPromptTokens();
    Integer completionTokens = usage.getCompletionTokens();
    
    // 统计所有模型的总token数量
    cacheClient.opsForHash().increment(traceId, KEY_TOTAL, totalTokens);
    if (promptTokens != null && promptTokens > 0) {
      cacheClient.opsForHash().increment(traceId, KEY_PROMPT, promptTokens);
    }
    if (completionTokens != null && completionTokens > 0) {
      cacheClient.opsForHash().increment(traceId, KEY_COMPLETION, completionTokens);
    }
    
    // 统计单个模型的token数量
    incrementForModel(traceId, usage, modelCode);
    
    cacheClient.expire(traceId, EXPIRE_HOURS, TimeUnit.HOURS);
  }

  /**
   * 统计单个模型的 token 数量
   */
  private void incrementForModel(String traceId, Usage usage, @Nullable String modelCode) {
    Integer totalTokens = usage.getTotalTokens();
    Integer promptTokens = usage.getPromptTokens();
    Integer completionTokens = usage.getCompletionTokens();

    if (StringUtils.isNotEmpty(modelCode)) {
      String modelPrefix = KEY_MODEL_PREFIX + modelCode + CacheConsts.COLON;
      cacheClient.opsForHash().increment(traceId, modelPrefix + KEY_TOTAL, totalTokens);
      if (promptTokens != null && promptTokens > 0) {
        cacheClient.opsForHash().increment(traceId, modelPrefix + KEY_PROMPT, promptTokens);
      }
      if (completionTokens != null && completionTokens > 0) {
        cacheClient.opsForHash().increment(traceId, modelPrefix + KEY_COMPLETION, completionTokens);
      }
    }
  }

  /**
   * 获取 token 数量
   */
  @Nullable
  public TokenUsageDTO get(String traceId) {
    Map<Object, Object> entries = cacheClient.opsForHash().entries(traceId);
    if (MapUtils.isEmpty(entries)) {
      return null;
    }
    TokenUsageDTO result = new TokenUsageDTO();
    result.setTotal(MapUtils.getInteger(entries, KEY_TOTAL));
    result.setPrompt(MapUtils.getInteger(entries, KEY_PROMPT));
    result.setCompletion(MapUtils.getInteger(entries, KEY_COMPLETION));

    // 解析各个模型的token使用量
    parseModelToken(entries, result);

    return result;
  }

  /**
   * 解析各个模型 token 使用量
   */
  private void parseModelToken(Map<Object, Object> entries, TokenUsageDTO result) {
    Map<String, TokenUsageDTO.ModelTokenUsage> modelTokenUsages = new HashMap<>();
    for (Map.Entry<Object, Object> entry : entries.entrySet()) {
      String key = (String) entry.getKey();
      if (key.startsWith(KEY_MODEL_PREFIX)) {
        // 解析模型编码和类型
        int firstColon = key.indexOf(CacheConsts.COLON);
        int lastColon = key.lastIndexOf(CacheConsts.COLON);
        if (firstColon < lastColon) {
          String modelCode = key.substring(firstColon + 1, lastColon);
          String type = key.substring(lastColon + 1);
          // 获取或创建模型的token使用量对象
          TokenUsageDTO.ModelTokenUsage modelUsage = modelTokenUsages.computeIfAbsent(modelCode, k -> new TokenUsageDTO.ModelTokenUsage());
          // 根据类型设置对应的值
          Integer value = MapUtils.getInteger(entries, key);
          switch (type) {
            case KEY_TOTAL:
              modelUsage.setTotal(value);
              break;
            case KEY_PROMPT:
              modelUsage.setPrompt(value);
              break;
            case KEY_COMPLETION:
              modelUsage.setCompletion(value);
              break;
            default: break;
          }
        }
      }
    }
    result.setModelTokenUsages(modelTokenUsages);
  }

}
