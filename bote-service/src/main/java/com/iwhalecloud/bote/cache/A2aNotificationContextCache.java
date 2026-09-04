package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.a2a.A2aNotificationContext;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Duration;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * A2A 通知上下文缓存
 *
 * <p>key 为 A2A 通知的 token, value 为相关的上下文信息</p>
 *
 * @author bianjp
 * @since 2025-11-27
 */
@Component
public class A2aNotificationContextCache {
  /** 缓存失效时间 */
  private static final Duration EXPIRE_TIME = Duration.ofHours(2);

  private final ICacheClient cacheClient;

  public A2aNotificationContextCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, "a2aNotificationContext");
  }

  /**
   * 获取通知上下文
   */
  @Nullable
  public A2aNotificationContext getContext(String token) {
    String value = cacheClient.opsForValue().get(token);
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    return JsonUtil.parseJsonRequired(value, A2aNotificationContext.class);
  }

  /**
   * 保存通知上下文
   */
  public void saveContext(String token, A2aNotificationContext context) {
    cacheClient.opsForValue().set(token, JsonUtil.toJsonString(context), EXPIRE_TIME);
  }

  /**
   * 删除通知上下文
   */
  public void deleteContext(String token) {
    cacheClient.delete(token);
  }

  /**
   * 生成新 token
   */
  public String newToken() {
    return UUID.randomUUID().toString();
  }

}
