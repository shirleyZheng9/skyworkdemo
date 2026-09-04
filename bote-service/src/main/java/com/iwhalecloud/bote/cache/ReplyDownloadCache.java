package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.chat.ReplyDownloadInfoDTO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 回复下载缓存
 *
 * <p>用于在调试工作流时缓存回复内容（调试时不记录消息到数据库，无法从数据库查询）</p>
 *
 * @author bianjp
 * @since 2025-04-25
 */
@Component
public class ReplyDownloadCache {
  /** 缓存时间 */
  private static final Duration EXPIRE_TIME = Duration.ofHours(1);

  private final ICacheClient cacheClient;

  public ReplyDownloadCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_REPLY_DOWNLOAD);
  }

  /**
   * 缓存下载信息
   */
  public void set(Long tenantId, String msgId, ReplyDownloadInfoDTO info) {
    String key = tenantId + ":" + msgId;
    cacheClient.opsForValue().set(key, JsonUtil.toJsonString(info), EXPIRE_TIME);
  }

  /**
   * 获取下载信息
   *
   * @return 下载信息，不存在时返回 null
   */
  @Nullable
  public ReplyDownloadInfoDTO get(Long tenantId, String msgId) {
    String key = tenantId + ":" + msgId;
    String value = cacheClient.opsForValue().get(key);
    return StringUtils.isNotEmpty(value) ? JsonUtil.parseJsonRequired(value, ReplyDownloadInfoDTO.class) : null;
  }
}
