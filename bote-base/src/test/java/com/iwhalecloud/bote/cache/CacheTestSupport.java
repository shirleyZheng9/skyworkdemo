package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Field;

/**
 * 缓存测试公共工具：反射注入/关闭 BaseSecondaryCache 后端、构造与生产一致的 ObjectMapper。
 *
 * <p>BaseSecondaryCache 的 cacheClient/localCache 等字段由 Spring 在 afterPropertiesSet 中注入，
 * 纯单测中需以反射设置；其 useDistributionCache/useLocalCache/useKeySetManager 标志默认为 true，
 * 关闭后 get/put 路径仅经子类 load() 加载，避开 Redis/本地缓存/keySetManager。</p>
 */
final class CacheTestSupport {

  private CacheTestSupport() {
  }

  /** 构造与 Spring Boot 默认一致的 ObjectMapper（JavaTimeModule + ISO 日期 + 忽略未知字段 + 允许空 Bean）。 */
  static ObjectMapper jsonObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    return om;
  }

  /** 关闭分布式缓存/本地缓存/keySetManager，使 get/put 路径仅经 load() 加载。 */
  static void disableCacheBackends(Object target) throws Exception {
    setField(target, "useDistributionCache", false);
    setField(target, "useLocalCache", false);
    setField(target, "useKeySetManager", false);
  }

  /** 沿继承链查找并设置字段（BaseSecondaryCache 字段位于父类）。 */
  static void setField(Object target, String name, Object value) throws Exception {
    Class<?> cls = target.getClass();
    Field f = null;
    while (cls != null) {
      try {
        f = cls.getDeclaredField(name);
        break;
      }
      catch (NoSuchFieldException e) {
        cls = cls.getSuperclass();
      }
    }
    if (f == null) {
      throw new NoSuchFieldException(name);
    }
    f.setAccessible(true);
    f.set(target, value);
  }
}
