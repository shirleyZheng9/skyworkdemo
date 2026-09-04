package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 计划上下文缓存
 *
 * <p>不要做缓存刷新。</p>
 *
 * @author chen.linfa
 * @since 2025-07-11
 */
@Component
public class PlanContextCache {
  /** 缓存失效时间 */
  private static final Duration EXPIRE_TIME = Duration.ofHours(1);

  private final ICacheClient cacheClient;

  public PlanContextCache(CacheFactory cacheFactory) {
    cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_PLAN_CONTEXT);
  }

  public void put(Long planId, PlanRecordDTO record) {
    cacheClient.opsForValue().set(planId.toString(), JsonUtil.toJsonString(record), EXPIRE_TIME);
  }

  @Nullable
  public PlanRecordDTO get(Long planId) {
    String value = cacheClient.opsForValue().get(planId.toString());
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    return JsonUtil.parseJsonRequired(value, PlanRecordDTO.class);
  }
}
