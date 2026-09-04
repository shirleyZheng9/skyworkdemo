package com.iwhalecloud.bote.service.publish.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 连接健康检查服务
 * 定时检查长连接状态，并在连接断开时自动恢复
 *
 * @author system
 * @since 2025-01-09
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBooleanProperty("bote.connection-health-check.enabled")
@SuppressWarnings("PMD.GuardLogStatement")
public class ConnectionHealthCheckService {
  private static final Logger logger = LoggerFactory.getLogger(ConnectionHealthCheckService.class);
  /** 首次重试延迟 */
  private static final long INITIAL_RETRY_DELAY_MS = Duration.ofMinutes(1).toMillis();
  /** 退避重试延迟上限 */
  private static final long MAX_RETRY_DELAY_MS = Duration.ofHours(2).toMillis();

  private final SdkConnectionManager sdkConnectionManager;
  private final ResourcePublishRecordMapper resourcePublishRecordMapper;
  /** 重试状态缓存，key 为 callBackCode */
  private final LoadingCache<String, RetryState> retryStateCache = CacheBuilder.newBuilder()
    .expireAfterAccess(Duration.ofHours(4))
    .build(CacheLoader.from(callbackCode -> new RetryState()));

  /**
   * 定时检查连接健康状态
   */
  @Scheduled(fixedDelayString = "${bote.connection-health-check.checkInterval:30000}", initialDelayString = "${bote.connection-health-check.initialDelay:60000}")
  public void checkConnectionHealth() {
    // 获取所有活跃的发布记录
    List<ResourcePublishRecordDTO> activeRecords = resourcePublishRecordMapper.getActivePublishRecords();
    if (activeRecords.isEmpty()) {
      return;
    }
    // 检查每个长连接平台的连接状态，按发布记录创建人模拟登录以保证恢复连接流程有正确用户上下文
    for (ResourcePublishRecordDTO record : activeRecords) {
      LoginInfo simulatedLogin = buildSimulatedLoginInfo(record);
      SessionUtil.setLoginInfo(simulatedLogin);
      try {
        checkAndRecoverConnection(record);
      }
      finally {
        SessionUtil.clearThreadLocal();
      }
    }
  }

  /**
   * 检查并恢复单个连接
   */
  private void checkAndRecoverConnection(ResourcePublishRecordDTO record) {
    String callbackCode = record.getCallbackCode();
    String connectionStatus = sdkConnectionManager.getConnectionStatus(callbackCode);
    // 连接正常，清除重试状态
    if (!"DISCONNECTED".equals(connectionStatus)) {
      retryStateCache.invalidate(callbackCode);
      return;
    }

    RetryState state = retryStateCache.getUnchecked(callbackCode);
    // 未到重试窗口时直接跳过，避免无效频繁重连
    if (!state.canRetryNow(record)) {
      return;
    }
    logger.warn("Try to recover connection, channel={}, callbackCode={}, retryCount={}", record.getPublishChannel(), callbackCode, state.failureCount);
    try {
      sdkConnectionManager.startConnection(record);
    }
    catch (Exception e) {
      logger.error("Failed to recover connection: channel={}, callbackCode={}", record.getPublishChannel(), callbackCode, e);
    }
    finally {
      // startConnection 是异步的，不能根据是否抛出异常来判断是否成功，先固定标记为失败，下次检查到成功时再清除重试状态
      state.markFailure();
    }
  }

  /**
   * 构建渠道场景下的模拟登录信息：以发布记录创建人身份作为当前用户。
   */
  private LoginInfo buildSimulatedLoginInfo(ResourcePublishRecordDTO record) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setUserId(record.getCreatorId());
    loginInfo.setDefaultTenantId(record.getTenantId());
    loginInfo.setUserName(record.getCreatorName());
    return loginInfo;
  }

  /**
   * 重试状态
   */
  private static final class RetryState {
    /** 当前连续失败次数 */
    private int failureCount;
    /** 下次允许重试的时间戳（毫秒） */
    private long nextRetryAt;
    /** 用于判断配置是否变更的指纹 */
    private String configFingerprint;

    /**
     * 检查是否可以重试
     */
    public boolean canRetryNow(ResourcePublishRecordDTO record) {
      // 配置变化时重置状态，允许立刻重试
      String newConfigFingerprint = record.getPublishChannel() + "|" + record.getPublishParams();
      if (!newConfigFingerprint.equals(configFingerprint)) {
        configFingerprint = newConfigFingerprint;
        failureCount = 0;
        nextRetryAt = 0L;
        return true;
      }
      return System.currentTimeMillis() >= nextRetryAt;
    }

    /**
     * 标记重试失败
     */
    public void markFailure() {
      failureCount++;
      nextRetryAt = System.currentTimeMillis() + calculateRetryDelay(failureCount);
    }

    /**
     * 计算下次重试间隔，采用指数退避算法：1m, 2m, 4m ...，直到上限
     */
    private static long calculateRetryDelay(int count) {
      long delay = INITIAL_RETRY_DELAY_MS;
      for (int i = 1; i < count; i++) {
        delay = delay * 2;
        if (delay >= MAX_RETRY_DELAY_MS) {
          return MAX_RETRY_DELAY_MS;
        }
      }
      return delay;
    }
  }
}
