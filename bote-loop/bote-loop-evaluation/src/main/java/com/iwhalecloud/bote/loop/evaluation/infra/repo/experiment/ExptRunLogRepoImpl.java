package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptRunLogRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptRunLogDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptRunLogConvertor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验运行日志仓储实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/expt_run_log.go
 * - 功能: 实验运行日志数据访问实现
 * - 主要方法:
 * * get - 获取实验运行日志
 * * create - 创建实验运行日志
 * * save - 保存实验运行日志
 * * update - 更新实验运行日志
 * <p>
 * Java实现说明:
 * - 对应Go的ExptRunLogImpl结构体
 * - 使用转换器进行DO和PO转换
 * - 使用异常处理机制
 * - 支持时间戳设置
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.ExptRunLog -> Java ExptRunLog
 * - Go time.Now() -> Java LocalDateTime.now()
 * - Go map[string]any -> Java Map<String, Object>
 * - Go int64 -> Java Long
 */
@Component
@RequiredArgsConstructor
public class ExptRunLogRepoImpl implements IExptRunLogRepo {
  private final IExptRunLogDAO exptRunLogDAO;

  @Override
  public ExptRunLog get(Long exptId, Long exptRunId) {
    try {
      ExptRunLogEntity po = exptRunLogDAO.get(exptId, exptRunId);
      return ExptRunLogConvertor.convertToDO(po);
    }
    catch (Exception e) {
      throw new BssException("获取实验运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void create(ExptRunLog exptRunLog) {
    try {
      ExptRunLogEntity po = ExptRunLogConvertor.convertToPO(exptRunLog);
      po.setCreatedAt(new Date());
      po.setDeletedAt(0L);
      exptRunLogDAO.create(po);
    }
    catch (Exception e) {
      throw new BssException("创建实验运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void save(ExptRunLog exptRunLog) {
    try {
      ExptRunLogEntity po = ExptRunLogConvertor.convertToPO(exptRunLog);
      po.setUpdatedAt(new Date());
      po.setDeletedAt(0L);
      exptRunLogDAO.save(po);
    }
    catch (Exception e) {
      throw new BssException("保存实验运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void update(Long exptId, Long exptRunId, Map<String, Object> ufields) {
    try {
      ufields.put("updated_at", LocalDateTime.now());
      exptRunLogDAO.update(exptId, exptRunId, ufields);
    }
    catch (Exception e) {
      throw new BssException("更新实验运行日志失败: " + e.getMessage(), e);
    }
  }
}
