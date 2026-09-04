package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.redis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.QuotaSpaceExpt;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.redis.IQuotaDAO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class QuotaDAOImpl implements IQuotaDAO {

  private static final String table = "experiment";
  private final Duration expireTime = Duration.ofHours(48); // 48小时过期
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final ICacheClient cacheClient;

  public QuotaDAOImpl(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_OAUTH2TOKEN);
  }

  @Override
  public QuotaSpaceExpt getQuotaSpaceExpt(Long spaceId) {
    final String key = makeQuotaSpaceExptKey(spaceId);
    final String value = this.cacheClient.opsForValue().get(key);
    if (value == null || value.isBlank()) {
      return new QuotaSpaceExpt();
    }
    return JsonUtil.parseJson(value, QuotaSpaceExpt.class);
  }

  @Override
  public void setQuotaSpaceExpt(Long spaceId, QuotaSpaceExpt qse) {
    String key = makeQuotaSpaceExptKey(spaceId);
    String value;
    try {
      value = objectMapper.writeValueAsString(qse);
    }
    catch (JsonProcessingException e) {
      throw new BssException("设置空间实验配额失败: " + e.getMessage(), e);
    }
    this.cacheClient.opsForValue().set(key, value, expireTime);
  }

  /**
   * 生成配额空间实验键
   * 迁移对应关系: Go语言makeQuotaSpaceExptKey
   *
   * @param spaceId 空间ID
   * @return Redis键
   */
  private String makeQuotaSpaceExptKey(Long spaceId) {
    return String.format("[%s]quota_space_expt:%d", table, spaceId);
  }
}
