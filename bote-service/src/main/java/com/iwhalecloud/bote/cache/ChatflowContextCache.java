package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 对话流上下文缓存
 *
 * <p>用作持久化存储，不要做缓存刷新。</p>
 *
 * @author bianjp
 * @since 2024-09-01
 */
@Component
public class ChatflowContextCache {
  /** 缓存失效时间 */
  private static final Duration EXPIRE_TIME = Duration.ofDays(30);
  /** 调试对话流的缓存失效时间，不需要太长 */
  private static final Duration DEBUG_EXPIRE_TIME = Duration.ofHours(2);

  private final ICacheClient cacheClient;

  public ChatflowContextCache(CacheFactory cacheFactory) {
    cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_CHATFLOW_CONTEXT);
  }

  /**
   * 写入上下文变量
   */
  public void put(boolean isDebug, @Nullable Long sceneId, @Nullable Long flowId, String contextId, Map<String, Object> variables) {
    String key;
    if (sceneId != null) {
      key = "scene:" + sceneId + ":" + contextId;
    }
    else {
      key = "flow:" + flowId + ":" + contextId;
    }
    cacheClient.opsForValue().set(key, JsonUtil.toJsonString(variables), isDebug ? DEBUG_EXPIRE_TIME : EXPIRE_TIME);
  }

  /**
   * 读取上下文变量
   */
  @Nullable
  public Map<String, Object> get(@Nullable Long sceneId, @Nullable Long flowId, String contextId) {
    Assert.isTrue(sceneId != null || flowId != null, "sceneId 和 flowId 不能同时为空");
    Assert.hasLength(contextId, "contextId 不能为空");
    String key = sceneId != null ? "scene:" + sceneId + ":" + contextId : "flow:" + flowId + ":" + contextId;
    String value = cacheClient.opsForValue().get(key);
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    if ("{}".equals(value)) {
      return Collections.emptyMap();
    }
    return JsonUtil.parseJsonRequired(value, new TypeReference<Map<String, Object>>() {
    });
  }
}
