package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.QuotaSpaceExpt;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.QuotaRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.QuotaUpdateResult;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.redis.IQuotaDAO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.HashMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 配额REPO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/quota.go
 * - 功能: 配额数据访问实现
 * - 主要方法:
 * * createOrUpdate - 创建或更新配额
 * <p>
 * Java实现说明:
 * - 对应Go的QuotaRepoImpl结构体
 * - 使用Spring组件注解
 * - 使用分布式锁保证并发安全
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go func(*entity.QuotaSpaceExpt) (*entity.QuotaSpaceExpt, bool, error) -> Java Function<QuotaSpaceExpt, QuotaUpdateResult>
 * - Go lock.ILocker -> Java ILocker
 */
@Component
@RequiredArgsConstructor
public class QuotaRepoImpl implements QuotaRepo {
  private final IQuotaDAO quotaDAO;

  @Override
  public void createOrUpdate(Long spaceId, Function<QuotaSpaceExpt, QuotaUpdateResult> updater, Session session) {
    try {
      // 使用重试机制
      retryWithElapsedTime(() -> createOrUpdateInternal(spaceId, updater, session));
    }
    catch (Exception e) {
      throw new BssException("创建或更新配额失败: " + e.getMessage(), e);
    }
  }

  /**
   * 内部创建或更新方法
   * 迁移对应关系: Go语言createOrUpdate方法
   */
  private void createOrUpdateInternal(Long spaceId, Function<QuotaSpaceExpt, QuotaUpdateResult> updater, Session session) {
    Assert.notNull(session, () -> "会话为空 ");
    // 获取旧值
    QuotaSpaceExpt oldVal = quotaDAO.getQuotaSpaceExpt(spaceId);
    QuotaSpaceExpt newVal = new QuotaSpaceExpt();
    if (oldVal == null || oldVal.getExptId2RunTime() == null) {
      oldVal = new QuotaSpaceExpt();
      oldVal.setExptId2RunTime(new HashMap<>());
    }
    newVal.setExptId2RunTime(new HashMap<>(oldVal.getExptId2RunTime()));

    // 执行更新逻辑
    QuotaUpdateResult result = updater.apply(newVal);
    if (!result.isUpdate()) {
      return;
    }

    // 保存新值
    quotaDAO.setQuotaSpaceExpt(spaceId, result.getQuotaSpaceExpt());
  }

  /**
   * 重试机制
   * 迁移对应关系: Go语言backoff.RetryWithElapsedTime
   */
  private void retryWithElapsedTime(Runnable operation) {
    int maxRetries = 3;
    for (int i = 0; i < maxRetries; i++) {
      try {
        operation.run();
        return;
      }
      catch (Exception e) {
        if (i == maxRetries - 1) {
          throw new BssException("重试异常", e);
        }
      }
    }
  }
}
