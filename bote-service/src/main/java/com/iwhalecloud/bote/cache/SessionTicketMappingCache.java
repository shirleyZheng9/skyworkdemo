package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.portal.ExternalSystemSession;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 会话与票据映射缓存
 *
 * @author lxs
 * @since 2025-08-02
 */
@Component
public class SessionTicketMappingCache {

  private final ICacheClient cacheClient;

  public SessionTicketMappingCache(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_SESSION_TICKET);
  }

  /**
   * 保存映射
   *
   * @param key  键
   * @param sessionId  会话 ID
   * @param systemCode 系统编码
   */
  public void saveMapping(String key, String sessionId, String systemCode) {
    ExternalSystemSession value = ExternalSystemSession.builder().systemCode(systemCode).sessionId(sessionId).build();
    cacheClient.opsForValue().set(key, JsonUtil.toJsonString(value));
  }

  /**
   * 获取映射
   *
   * @param key 键
   * @return 值
   */
  @Nullable
  public ExternalSystemSession getMapping(String key) {
    String json = cacheClient.opsForValue().get(key);
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    return JsonUtil.parseJson(json, ExternalSystemSession.class);
  }

  /**
   * 删除映射
   *
   * @param key 键
   */
  public void deleteMapping(String key) {
    cacheClient.delete(key);
  }

}
