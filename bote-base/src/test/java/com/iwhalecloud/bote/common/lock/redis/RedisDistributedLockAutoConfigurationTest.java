package com.iwhalecloud.bote.common.lock.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import org.junit.jupiter.api.Test;

/**
 * {@link RedisDistributedLockAutoConfiguration} 单元测试
 *
 * <p>使用 Mockito 模拟 {@link CacheFactory}，以真实 {@link RedisDistributedLockProperties}
 * 作为配置夹具，直接调用 @Bean 方法 {@code redisDistributedLockFactory} 覆盖
 * createRedisDistributedLockCacheClient 私有方法（含 validate 与 getCacheClient 调用）
 * 以及工厂创建逻辑。enabled=false 时 validate 为空操作；enabled=true 时使用默认配置通过校验。</p>
 */
class RedisDistributedLockAutoConfigurationTest {

  @Test
  void redisDistributedLockFactory_enabledFalse_createsFactory() {
    CacheFactory cacheFactory = mock(CacheFactory.class);
    ICacheClient cacheClient = mock(ICacheClient.class);
    when(cacheFactory.getCacheClient(anyString(), anyString())).thenReturn(cacheClient);

    RedisDistributedLockProperties props = new RedisDistributedLockProperties();
    // enabled=false（默认），validate() 为空操作

    RedisDistributedLockAutoConfiguration autoConfig = new RedisDistributedLockAutoConfiguration();
    DistributedLockFactory factory = autoConfig.redisDistributedLockFactory(cacheFactory, props);

    assertThat(factory).isNotNull();
    assertThat(factory.getFactoryType()).isEqualTo("redis");
    assertThat(factory.isAvailable()).isTrue();
  }

  @Test
  void redisDistributedLockFactory_enabledTrue_createsFactory() {
    CacheFactory cacheFactory = mock(CacheFactory.class);
    ICacheClient cacheClient = mock(ICacheClient.class);
    when(cacheFactory.getCacheClient(anyString(), anyString())).thenReturn(cacheClient);

    RedisDistributedLockProperties props = new RedisDistributedLockProperties();
    props.setEnabled(true);
    // 默认配置通过 validate 校验（connectionString 非空、各数值合法、cacheGroup/keyPrefix 非空）

    RedisDistributedLockAutoConfiguration autoConfig = new RedisDistributedLockAutoConfiguration();
    DistributedLockFactory factory = autoConfig.redisDistributedLockFactory(cacheFactory, props);

    assertThat(factory).isNotNull();
    assertThat(factory.getFactoryType()).isEqualTo("redis");
  }
}
