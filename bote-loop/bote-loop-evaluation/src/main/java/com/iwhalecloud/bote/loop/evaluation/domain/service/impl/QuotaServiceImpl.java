package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptExecConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.QuotaSpaceExpt;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.QuotaRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.QuotaUpdateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.service.QuotaService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 配额服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_run_impl.go
 * - 功能: 实验运行配额管理实现
 * - 主要方法:
 * * allowExptRun - 允许实验运行
 * * releaseExptRun - 释放实验运行
 * <p>
 * Java实现说明:
 * - 对应Go的QuotaServiceImpl结构体
 * - 使用Spring Service注解
 * - 支持实验运行配额控制
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.Session -> Java Session
 * - Go map[int64]int64 -> Java Map<Long, Long>
 */
@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {
  private final QuotaRepo quotaRepo;
  private final IConfiger configer;

  /**
   * 释放实验运行
   * 迁移对应关系: Go语言ReleaseExptRun
   * - 功能: 释放实验运行配额
   * - 参数: exptId - 实验ID, spaceId - 空间ID, session - 会话信息
   * - 返回: 无
   * - 异常: BssException - 释放失败时抛出
   */
  @Override
  public void releaseExptRun(Long exptId, Long spaceId, Session session) {
    try {
      quotaRepo.createOrUpdate(spaceId, (cur) -> {
        if (cur == null || cur.getExptId2RunTime() == null) {
          return new QuotaUpdateResult(cur, false);
        }

        Map<Long, Long> exptID2RunTime = cur.getExptId2RunTime();
        if (exptID2RunTime.containsKey(exptId)) {
          exptID2RunTime.remove(exptId);
          return new QuotaUpdateResult(cur, true);
        }

        return new QuotaUpdateResult(cur, false);
      }, session);
    }
    catch (Exception e) {
      throw new BssException("释放实验运行配额失败: " + e.getMessage(), e);
    }
  }

  /**
   * 允许实验运行
   */
  @Override
  public void allowExptRun(Long exptId, Long spaceId, Session session) {
    long now = System.currentTimeMillis() / 1000;
    ExptExecConf exptExecConf = configer.getExptExecConf(spaceId);
    int zombieInterval = exptExecConf.getZombieIntervalSecond();
    int concurLimit = exptExecConf.getSpaceExptConcurLimit();
    quotaRepo.createOrUpdate(spaceId, (cur) -> {
      if (cur == null) {
        Map<Long, Long> exptID2RunTime = new HashMap<>();
        exptID2RunTime.put(exptId, now);
        QuotaSpaceExpt newQuota = QuotaSpaceExpt.builder()
          .exptId2RunTime(exptID2RunTime)
          .build();
        return new QuotaUpdateResult(newQuota, true);
      }
      Map<Long, Long> exptID2RunTime = cur.getExptId2RunTime();
      if (exptID2RunTime == null) {
        exptID2RunTime = new HashMap<>();
      }
      if (exptID2RunTime.size() >= concurLimit) {
        return new QuotaUpdateResult(null, false);
      }
      exptID2RunTime.put(exptId, now);
      // 清理僵尸实验
      exptID2RunTime.entrySet().removeIf(entry -> {
        long runTime = entry.getValue();
        return (now - runTime) > zombieInterval;
      });
      cur.setExptId2RunTime(exptID2RunTime);
      return new QuotaUpdateResult(cur, true);
    }, session);
  }
}
