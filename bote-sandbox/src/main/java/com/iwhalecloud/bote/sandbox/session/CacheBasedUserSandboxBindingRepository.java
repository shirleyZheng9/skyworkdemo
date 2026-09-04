package com.iwhalecloud.bote.sandbox.session;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.sandbox.config.SandboxEngineProperties;
import com.iwhalecloud.bote.sandbox.dto.UserSandboxBinding;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 基于分布式缓存的用户沙箱绑定存储
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
public class CacheBasedUserSandboxBindingRepository implements UserSandboxBindingRepository {
  private final ICacheClient cacheClient;
  /** 缓存失效时间，要略长于沙箱的过期时间，避免沙箱还未到期而缓存已查不到 */
  private final long cacheExpireSeconds;

  public CacheBasedUserSandboxBindingRepository(CacheFactory cacheFactory, SandboxEngineProperties properties) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_SANDBOX_USER_BINDING);
    this.cacheExpireSeconds = (long) (properties.getSession().getIdleTimeout().toSeconds() * 1.2);
  }

  @Override
  @Nullable
  public UserSandboxBinding get(Long userId) {
    String json = cacheClient.opsForValue().get(String.valueOf(userId));
    if (StringUtils.isEmpty(json)) {
      return null;
    }
    return JsonUtil.parseJson(json, UserSandboxBinding.class);
  }

  @Override
  public void put(Long userId, UserSandboxBinding binding) {
    String json = JsonUtil.toJsonString(binding);
    cacheClient.opsForValue().set(String.valueOf(userId), json, cacheExpireSeconds, TimeUnit.SECONDS);
  }

  @Override
  public void remove(Long userId) {
    cacheClient.delete(String.valueOf(userId));
  }
}
