package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 渠道长连接缓存
 *
 * <p>虚拟缓存，借助缓存刷新机制，实现禁用渠道时广播通知各个服务器节点关闭长连接</p>
 *
 * <p>缓存 key 为 </p>
 *
 * @author bianjp
 * @since 2026-04-22
 */
@Component
@RequiredArgsConstructor
public class ChannelConnectionCache implements Refreshable {
  private static final Logger logger = LoggerFactory.getLogger(ChannelConnectionCache.class);

  private final SdkConnectionManager sdkConnectionManager;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_CHANNEL_CONNECTION;
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    // 不支持全量刷新
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    for (String key : keys) {
      try {
        sdkConnectionManager.stopConnection(key);
      }
      catch (Exception e) {
        logger.warn("Failed to stop channel connection: callbackCode={}", key, e);
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
