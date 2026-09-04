package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptStatsEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStats;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.StatsCntArithOp;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptStatsRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptStatsDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptStatsConvertor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验统计REPO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/expt_stats_repo_impl.go
 * - 功能: 实验统计数据访问实现
 * - 主要方法:
 * * create - 创建实验统计
 * * get - 获取实验统计
 * * mGet - 批量获取实验统计
 * * updateByExptId - 根据实验ID更新统计
 * * arithOperateCount - 算术操作计数
 * * save - 保存实验统计
 * <p>
 * Java实现说明:
 * - 对应Go的exptStatsRepo结构体
 * - 使用Spring组件注解
 * - 使用转换器进行DO和PO转换
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go []*entity.ExptStats -> Java List<ExptStats>
 * - Go convert.NewExptStatsConverter() -> Java ExptStatsConvertor
 */
@Component
@RequiredArgsConstructor
public class ExptStatsRepoImpl implements IExptStatsRepo {
  private final IExptStatsDAO exptStatsDAO;

  @Override
  public void create(ExptStats stats) {
    try {
      ExptStatsEntity statsPO = ExptStatsConvertor.convertToPO(stats);
      exptStatsDAO.create(statsPO);
    }
    catch (Exception e) {
      throw new BssException("创建实验统计失败: " + e.getMessage(), e);
    }
  }

  @Override
  public ExptStats get(Long exptId, Long spaceId) {
    try {
      ExptStatsEntity statsPO = exptStatsDAO.get(exptId, spaceId);
      return ExptStatsConvertor.convertToDO(statsPO);
    }
    catch (Exception e) {
      throw new BssException("获取实验统计失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptStats> mGet(List<Long> exptIds, Long spaceId) {
    try {
      List<ExptStatsEntity> statsPOs = exptStatsDAO.mGet(exptIds, spaceId);
      return statsPOs.stream().map(ExptStatsConvertor::convertToDO).toList();
    }
    catch (Exception e) {
      throw new BssException("批量获取实验统计失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateByExptId(Long exptId, Long spaceId, ExptStats stats) {
    try {
      ExptStatsEntity statsPO = ExptStatsConvertor.convertToPO(stats);
      exptStatsDAO.updateByExptId(exptId, spaceId, statsPO);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID更新统计失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void arithOperateCount(Long exptId, Long spaceId, StatsCntArithOp cntArithOp) {
    try {
      exptStatsDAO.arithOperateCount(exptId, spaceId, cntArithOp);
    }
    catch (Exception e) {
      throw new BssException("算术操作计数失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void save(ExptStats stats) {
    try {
      ExptStatsEntity statsPO = ExptStatsConvertor.convertToPO(stats);
      exptStatsDAO.save(statsPO);
    }
    catch (Exception e) {
      throw new BssException("保存实验统计失败: " + e.getMessage(), e);
    }
  }
}
