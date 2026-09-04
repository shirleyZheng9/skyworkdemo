package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.customized.IWorkerIdStrategy;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 博特定制的机器号生成策略
 *
 * <p>荔枝包的 {@link com.iwhalecloud.bss.litchi.util.sequence.IDUtils} 使用系统的 MAC 地址生成 dataCenterId, 然后使用 dataCenterId + 进程 ID 生成 workerId,
 * 一般情况下不同应用实例生成的 dataCenterId, workerId 是不同的（容器部署时 MAC 地址一般不同，传统部署时进程 ID 一般不同）。</p>
 *
 * <p>但某些环境会有问题，比如性能压测时，在几台主机上分别启动一个容器实例，且使用 Docker 默认的容器网络，此时每个容器的 IP 都相同，
 * MAC 地址也相同（Docker 使用 IP 生成 MAC 地址），导致每个实例生成的 dataCenterId 都相同；
 * 不同容器实例中 JVM 进程的 ID 也相同，导致生成的 workerId 也相同，因此在高并发压测时有很大概率出现冲突。</p>
 *
 * <p>此方案通过使用 Redis 自增序列确保每个实例能获取到唯一的 workerId. 理论上不能完全确保唯一，但可以兼容大部分场景了。</p>
 *
 * <p>一般无需开启，只在高频出现冲突时开启。</p>
 *
 * @author bianjp
 * @since 2025-07-23
 */
@Component
@ConditionalOnBooleanProperty("bote.snowflake.extend.enabled")
public class BoteWorkerIdStrategy implements IWorkerIdStrategy {
  /** 缓存 key */
  private static final String WORKER_ID_KEY = "snowflake:workerId";
  /** 缓存过期时间（天） */
  private static final int EXPIRE_DAYS = 365;

  private final ICacheClient cacheClient;

  public BoteWorkerIdStrategy(CacheFactory cacheFactory) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM);
  }

  @Override
  public long getWorkerId(long dataCenterId, long maxWorkerId) {
    try {
      return generateWorkId(maxWorkerId);
    }
    catch (RedisSystemException e) {
      // 值为 Long.MAX_VALUE 时会报 overflow 异常，值不是整数时会报 value is not an integer 异常
      // 删除 key 后重试
      if (Strings.CS.containsAny(e.getMessage(), "overflow", "value is not an integer")) {
        cacheClient.delete(WORKER_ID_KEY);
        return generateWorkId(maxWorkerId);
      }
      throw e;
    }
  }

  /**
   * 生成机器号
   */
  private long generateWorkId(long maxWorkerId) {
    Long value = cacheClient.opsForValue().increment(WORKER_ID_KEY);
    Assert.notNull(value, "获取机器号失败");
    cacheClient.expire(WORKER_ID_KEY, EXPIRE_DAYS, TimeUnit.DAYS);
    return value % maxWorkerId;
  }
}
