package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptAggrResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggrResult;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptAggrResultRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.ExptAggrResultDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptAggrResultConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验聚合结果仓储实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/expt_aggr_result.go
 * - 功能: 实验聚合结果数据访问实现
 * - 主要方法:
 * * getExptAggrResult - 获取实验聚合结果
 * * getExptAggrResultByExperimentId - 根据实验ID获取聚合结果
 * * batchGetExptAggrResultByExperimentIds - 批量获取聚合结果
 * * createExptAggrResult - 创建聚合结果
 * * batchCreateExptAggrResult - 批量创建聚合结果
 * * updateExptAggrResultByVersion - 根据版本更新聚合结果
 * * updateAndGetLatestVersion - 更新并获取最新版本
 * <p>
 * Java实现说明:
 * - 对应Go的ExptAggrResultRepoImpl结构体
 * - 使用转换器进行DO和PO转换
 * - 使用ID生成器生成主键
 * - 使用异常处理机制
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.ExptAggrResult -> Java ExptAggrResult
 * - Go []*entity.ExptAggrResult -> Java List<ExptAggrResult>
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 */
@Component
@RequiredArgsConstructor
public class ExptAggrResultRepoImpl implements IExptAggrResultRepo {
  private final ExptAggrResultDAO exptAggrResultDAO;
  private final IIDGenerator idGenerator;

  @Override
  public ExptAggrResult getExptAggrResult(Long experimentId, Integer fieldType, String fieldKey) {
    try {
      ExptAggrResultEntity exptAggrResultPO = exptAggrResultDAO.getExptAggrResult(experimentId, fieldType, fieldKey);
      return ExptAggrResultConvertor.convertToDO(exptAggrResultPO);
    }
    catch (Exception e) {
      throw new BssException("获取实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptAggrResult> getExptAggrResultByExperimentId(Long experimentId) {
    try {
      List<ExptAggrResultEntity> exptAggrResultPOs = exptAggrResultDAO.getExptAggrResultByExperimentId(experimentId);
      return exptAggrResultPOs.stream()
        .map(ExptAggrResultConvertor::convertToDO)
        .toList();
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取聚合结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptAggrResult> batchGetExptAggrResultByExperimentIds(List<Long> experimentIds) {
    try {
      List<ExptAggrResultEntity> exptAggrResultPOs = exptAggrResultDAO.batchGetExptAggrResultByExperimentIds(experimentIds);
      return exptAggrResultPOs.stream()
        .map(ExptAggrResultConvertor::convertToDO)
        .toList();
    }
    catch (Exception e) {
      throw new BssException("批量获取聚合结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void createExptAggrResult(ExptAggrResult exptAggrResult) {
    try {
      Long id = idGenerator.genId();
      exptAggrResult.setId(id);
      ExptAggrResultEntity exptAggrResultPO = ExptAggrResultConvertor.convertToPO(exptAggrResult);
      exptAggrResultDAO.createExptAggrResult(exptAggrResultPO);
    }
    catch (Exception e) {
      throw new BssException("创建实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void batchCreateExptAggrResult(List<ExptAggrResult> exptAggrResults) {
    try {
      List<Long> ids = idGenerator.genMultiIds(exptAggrResults.size());
      for (int i = 0; i < exptAggrResults.size(); i++) {
        exptAggrResults.get(i).setId(ids.get(i));
      }

      List<ExptAggrResultEntity> exptAggrResultsPO = exptAggrResults.stream()
        .map(ExptAggrResultConvertor::convertToPO)
        .toList();

      exptAggrResultDAO.batchCreateExptAggrResult(exptAggrResultsPO);
    }
    catch (Exception e) {
      throw new BssException("批量创建实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateExptAggrResultByVersion(ExptAggrResult exptAggrResult, Long taskVersion) {
    try {
      ExptAggrResultEntity exptAggrResultPO = ExptAggrResultConvertor.convertToPO(exptAggrResult);
      exptAggrResultDAO.updateExptAggrResultByVersion(exptAggrResultPO, taskVersion);
    }
    catch (Exception e) {
      throw new BssException("根据版本更新聚合结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Long updateAndGetLatestVersion(Long experimentId, Integer fieldType, String fieldKey) {
    try {
      return exptAggrResultDAO.updateAndGetLatestVersion(experimentId, fieldType, fieldKey);
    }
    catch (Exception e) {
      throw new BssException("更新并获取最新版本失败: " + e.getMessage(), e);
    }
  }
}
