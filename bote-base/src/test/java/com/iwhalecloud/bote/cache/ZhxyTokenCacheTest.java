package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeTokenDTO;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.ValueOperations;

/**
 * {@link ZhxyTokenCache} 单元测试。
 *
 * <p>ZhxyTokenCache 继承 BaseSecondaryCache，cacheClient 字段在构造后为 null（由 Spring 注入），
 * 故以反射注入 mock ICacheClient。构造时已 disableLocalCache，故 useLocalCache=false。
 * query/remove 直接使用 cacheClient；save 经 this.put，为避开分布式序列化与 keySetManager，
 * 反射置 useDistributionCache=false、useKeySetManager=false。JsonUtil 静态初始化依赖 SpringUtil
 * 取 ObjectMapper，@BeforeAll 以 mockStatic(SpringUtil) 注入真实实例。</p>
 */
class ZhxyTokenCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private ICacheClient cacheClient;
  private ValueOperations<String, String> valueOps;
  private ZhxyTokenCache zhxyCache;

  @BeforeAll
  static void setUpSpring() {
    spring = mockStatic(SpringUtil.class);
    spring.when(() -> SpringUtil.getBean(eq(ObjectMapper.class), any()))
      .thenReturn(CacheTestSupport.jsonObjectMapper());
    // 在 mockStatic 生效且无未完成 stubbing 时触发 JsonUtil 静态初始化，
    // 避免后续在 when().thenReturn() 表达式内首次加载导致 UnfinishedStubbing
    assertThat(JsonUtil.toJsonString("init")).isNotNull();
  }

  @AfterAll
  static void tearDownSpring() {
    spring.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    cacheClient = mock(ICacheClient.class);
    valueOps = mock(ValueOperations.class);
    when(cacheClient.opsForValue()).thenReturn(valueOps);
    zhxyCache = new ZhxyTokenCache();
    CacheTestSupport.setField(zhxyCache, "cacheClient", cacheClient);
    // 关闭分布式缓存与 keySetManager，使 save->put 不触碰 Redis/序列化（useLocalCache 构造时已关闭）
    CacheTestSupport.setField(zhxyCache, "useDistributionCache", false);
    CacheTestSupport.setField(zhxyCache, "useKeySetManager", false);
  }

  // ==================== query ====================

  @Test
  void query_emptyInCache_returnsNull() {
    when(valueOps.get("u1")).thenReturn(null);
    assertThat(zhxyCache.query("u1")).isNull();
  }

  @Test
  void query_blankStringInCache_returnsNull() {
    when(valueOps.get("u1")).thenReturn("");
    assertThat(zhxyCache.query("u1")).isNull();
  }

  @Test
  void query_validJson_returnsToken() {
    KnowledgeTokenDTO token = new KnowledgeTokenDTO();
    when(valueOps.get("u1")).thenReturn(JsonUtil.toJsonString(token));
    assertThat(zhxyCache.query("u1")).isNotNull();
  }

  // ==================== remove ====================

  @Test
  void remove_delegatesToCacheClient() {
    when(cacheClient.delete("u1")).thenReturn(true);
    zhxyCache.remove("u1");
    verify(cacheClient).delete("u1");
  }

  // ==================== save ====================

  @Test
  void save_invokesPutWithoutException() {
    KnowledgeTokenDTO token = new KnowledgeTokenDTO();
    zhxyCache.save("u1", token, 60);
    // useDistributionCache=false 时 put 不调用 cacheClient
    verify(cacheClient, org.mockito.Mockito.never()).opsForValue();
  }
}
