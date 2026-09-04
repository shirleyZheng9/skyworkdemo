package com.iwhalecloud.bote.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.service.reply.service.WordToAudioServiceImpl;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * TTS 音色 ID 缓存
 *
 * <p>缓存音色文件上传结果，避免重复上传</p>
 *
 * @author bianjp
 * @since 2025-11-18
 */
@Component
@SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
public class TtsVoiceIdCache implements Refreshable, InitializingBean {
  /** 分布式缓存的缓存时长，不使用永久以避免缓存残留 */
  private static final Duration EXPIRE_TIME = Duration.ofDays(180);

  /** 分布式缓存客户端 */
  private final ICacheClient cacheClient;
  /** 本地缓存, key 为 (TTS 服务器地址, 音色名称), 确保任一因子变化后都能自动失效缓存 */
  private final Cache<@NonNull Pair<String, String>, @NonNull Long> localCache = CacheBuilder.newBuilder()
    .maximumSize(10)
    .expireAfterAccess(Duration.ofDays(1))
    .build();
  private final IRefreshCacheService refreshCacheService;
  /** 音色文件的 hash 映射, key 为音色名称, value 为文件内容的 hash */
  private Map<String, String> audioHashMap;

  public TtsVoiceIdCache(CacheFactory cacheFactory, IRefreshCacheService refreshCacheService) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_TTS_VOICE_ID);
    this.refreshCacheService = refreshCacheService;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_TTS_VOICE_ID;
  }

  @Override
  public void afterPropertiesSet() throws IOException {
    // 初始化音色名称与文件内容 hash 的映射，只在应用启动时计算一次，避免重复计算
    Map<String, String> map = new HashMap<>();
    for (String audioName : WordToAudioServiceImpl.SUPPORTED_AUDIO_NAMES) {
      try (InputStream inputStream = new ClassPathResource("/wav/" + audioName + ".wav").getInputStream()) {
        String hash = DigestUtils.md5Hex(inputStream);
        map.put(audioName, hash);
      }
    }
    this.audioHashMap = Map.copyOf(map);
  }

  /**
   * 获取缓存的音色 ID
   */
  @Nullable
  public Long get(String baseUrl, String audioName) {
    Pair<String, String> localKey = buildLocalKey(baseUrl, audioName);
    Long value = localCache.getIfPresent(localKey);
    if (value == null) {
      String key = buildRedisKey(baseUrl, audioName);
      String str = cacheClient.opsForValue().get(key);
      if (StringUtils.isNumeric(str)) {
        value = Long.parseLong(str);
        localCache.put(localKey, value);
      }
    }
    return value;
  }

  /**
   * 缓存音色 ID
   */
  public void put(String baseUrl, String audioName, Long value) {
    localCache.put(buildLocalKey(baseUrl, audioName), value);
    cacheClient.opsForValue().set(buildRedisKey(baseUrl, audioName), value.toString(), EXPIRE_TIME);
  }

  /**
   * 删除缓存的音色 ID
   */
  public void delete(String baseUrl, String audioName) {
    cacheClient.delete(buildRedisKey(baseUrl, audioName));
    localCache.invalidate(buildLocalKey(baseUrl, audioName));
    // 广播刷新其它实例的本地缓存。本地缓存 key 比较复杂，干脆全量刷新，以避免转换 key 的麻烦
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_TTS_VOICE_ID);
  }

  /**
   * 清空缓存
   */
  public void clearCache() {
    cacheClient.deleteAll(null);
    localCache.invalidateAll();
    // 广播刷新其它实例的本地缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_TTS_VOICE_ID);
  }

  /**
   * 构造本地缓存 key
   */
  private Pair<String, String> buildLocalKey(String baseUrl, String audioName) {
    // 本地缓存使用音色名称即可，不需要使用文件内容，因为只有在应用重启时音色文件才会变化（不考虑本地开发）
    return Pair.of(baseUrl, audioName);
  }

  /**
   * 构造分布式缓存的 key
   */
  private String buildRedisKey(String baseUrl, String audioName) {
    // 使用音色文件内容的 hash 构造缓存 key, 以确保音色文件变化时自动失效缓存
    String audioHash = audioHashMap.get(audioName);
    Assert.notNull(audioHash, () -> "未知的音色: " + audioName);
    // 使用 hash 以避免缓存 key 太长
    return DigestUtils.md5Hex(baseUrl + ":" + audioHash);
  }

  @Override
  public void refreshLocalCache() {
    localCache.invalidateAll();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    // 本地缓存 key 比较复杂，干脆全量刷新，以避免转换 key 的麻烦
    localCache.invalidateAll();
  }

  @Override
  public void refresh() {
    // 常规刷新时不刷新分布式缓存（用户经常会点缓存刷新页面的一键刷新按钮），避免缓存过早失效。需要清除缓存时应调用 clearCache 方法
    refreshLocalCache();
  }

  @Override
  public void refresh(List<String> keys) {
    // 常规刷新时不刷新分布式缓存
    refreshLocalCache(keys);
  }
}
