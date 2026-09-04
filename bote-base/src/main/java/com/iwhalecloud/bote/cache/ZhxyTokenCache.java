package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeTokenDTO;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 知识中台接口令牌缓存
 *
 * @author lxs
 * @since 2025/07/18
 */
@Component
public final class ZhxyTokenCache extends BaseSecondaryCache<KnowledgeTokenDTO> {

  public ZhxyTokenCache() {
    super(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_ZHXY_TOKEN);
    // 禁用本地缓存
    disableLocalCache();
  }

  /**
   * 保存知识中台令牌信息
   *
   * @param userId 用户标识
   * @param tokenInfo 令牌信息
   */
  public void save(String userId, KnowledgeTokenDTO tokenInfo, int expireTime) {
    this.put(userId, tokenInfo, expireTime);
  }


  /**
   * 查询知识中台令牌信息
   *
   * @param userId 用户标识
   * @return 知识中台令牌信息
   */
  @Nullable
  public KnowledgeTokenDTO query(String userId) {
    String infoStr = cacheClient.opsForValue().get(userId);
    if (StringUtils.isEmpty(infoStr)) {
      return null;
    }
    return JsonUtil.parseJsonRequired(infoStr, KnowledgeTokenDTO.class);
  }

  /**
   * 删除
   *
   * @param key 键
   */
  public void remove(String key) {
    cacheClient.delete(key);
  }
}
