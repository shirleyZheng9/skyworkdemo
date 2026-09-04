package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.ExptTurnResultDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptTurnEvaluatorResultRefDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptTurnEvaluatorResultRefConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptTurnResultConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptTurnResultRunLogConvertor;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

/**
 * 实验轮次结果REPO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/expt_turn_result.go
 * - 功能: 实验轮次结果数据访问实现
 * - 主要方法:
 * * updateTurnResultsWithItemIDs - 根据项目ID更新轮次结果
 * * updateTurnResults - 更新轮次结果
 * * scanTurnResults - 扫描轮次结果
 * * scanTurnRunLogs - 扫描轮次运行日志
 * * batchCreateNx - 批量创建轮次结果
 * * createTurnEvaluatorRefs - 创建轮次评估器引用
 * * batchGet - 批量获取轮次结果
 * * saveTurnResults - 保存轮次结果
 * * saveTurnRunLogs - 保存轮次运行日志
 * * updateTurnRunLogWithItemIDs - 根据项目ID更新轮次运行日志
 * * createOrUpdateItemsTurnRunLogStatus - 创建或更新项目轮次运行日志状态
 * * getItemTurnResults - 获取项目轮次结果
 * * getItemTurnRunLogs - 获取项目轮次运行日志
 * * mGetItemTurnRunLogs - 批量获取项目轮次运行日志
 * * batchCreateNxRunLog - 批量创建轮次运行日志
 * * listTurnResult - 列出轮次结果
 * * listTurnResultByItemIDs - 根据项目ID列出轮次结果
 * * batchGetTurnEvaluatorResultRef - 批量获取轮次评估器结果引用
 * * getTurnEvaluatorResultRefByExptId - 根据实验ID获取轮次评估器结果引用
 * * getTurnEvaluatorResultRefByEvaluatorVersionId - 根据评估器版本ID获取轮次评估器结果引用
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultRepoImpl结构体
 * - 使用Spring组件注解
 * - 使用转换器进行DO和PO转换
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go []*entity.ExptTurnResult -> Java List<ExptTurnResult>
 * - Go convert.NewExptTurnResultConvertor() -> Java ExptTurnResultConvertor
 */
@Component
@RequiredArgsConstructor
public class ExptTurnResultRepoImpl implements IExptTurnResultRepo {
  private final IIDGenerator iidGenerator;
  private final ExptTurnResultDAO exptTurnResultDAO;
  private final IExptTurnEvaluatorResultRefDAO exptTurnEvaluatorResultRefDAO;

  @Override
  public void updateTurnResultsWithItemIds(Long exptId, List<Long> itemIds, Long spaceId, Map<String, Object> ufields) {
    try {
      exptTurnResultDAO.updateTurnResultsWithItemIds(exptId, itemIds, spaceId, ufields);
    }
    catch (Exception e) {
      throw new BssException("根据项目ID更新轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateTurnResults(Long exptId, List<ItemTurnID> itemTurnIDs, Long spaceId, Map<String, Object> ufields) {
    try {
      exptTurnResultDAO.updateTurnResults(exptId, itemTurnIDs, spaceId, ufields);
    }
    catch (Exception e) {
      throw new BssException("更新轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public PageInfo<ExptTurnResult> scanTurnResults(Long exptId, List<Integer> status, int pageNumber, Long pageSize, Long spaceId) {
    try {
      PageInfo<ExptTurnResultEntity> exptTurnResultPOs = exptTurnResultDAO.scanTurnResults(exptId, status, pageNumber, pageSize, spaceId);
      return exptTurnResultPOs.convert(ExptTurnResultConvertor::convertToDO);
    }
    catch (Exception e) {
      throw new BssException("扫描轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResultRunLog> scanTurnRunLogs(Long exptId, Long cursor, Long limit, Long spaceId) {
    try {
      List<ExptTurnResultRunLogEntity> exptTurnResultRunLogPOs = exptTurnResultDAO.scanTurnRunLogs(exptId, cursor, limit, spaceId);
      return ExptTurnResultRunLogConvertor.convertToDOList(exptTurnResultRunLogPOs);
    }
    catch (Exception e) {
      throw new BssException("扫描轮次运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void batchCreateNx(List<ExptTurnResult> turnResults) {
    try {
      List<ExptTurnResultEntity> turnResultPOs = ExptTurnResultConvertor.convertToPOList(turnResults);
      exptTurnResultDAO.batchCreateNx(turnResultPOs);
    }
    catch (Exception e) {
      throw new BssException("批量创建轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void createTurnEvaluatorRefs(List<ExptTurnEvaluatorResultRef> refs) {
    try {
      List<ExptTurnEvaluatorResultRefEntity> refPOs = ExptTurnEvaluatorResultRefConvertor.convertToPOList(refs);
      exptTurnResultDAO.createTurnEvaluatorRefs(refPOs);
    }
    catch (Exception e) {
      throw new BssException("创建轮次评估器引用失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResult> batchGet(Long spaceId, Long exptId, List<Long> itemIds) {
    try {
      List<ExptTurnResultEntity> exptTurnResultPOs = exptTurnResultDAO.batchGet(spaceId, exptId, itemIds);
      return ExptTurnResultConvertor.convertToDOList(exptTurnResultPOs);
    }
    catch (Exception e) {
      throw new BssException("批量获取轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void saveTurnResults(List<ExptTurnResult> turnResults) {
    try {
      List<ExptTurnResultEntity> turnResultPOs = ExptTurnResultConvertor.convertToPOList(turnResults);
      exptTurnResultDAO.saveTurnResults(turnResultPOs);
    }
    catch (Exception e) {
      throw new BssException("保存轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void saveTurnRunLogs(List<ExptTurnResultRunLog> runLogs) {
    try {
      List<ExptTurnResultRunLogEntity> runLogPOs = ExptTurnResultRunLogConvertor.convertToPOList(runLogs);
      exptTurnResultDAO.saveTurnRunLogs(runLogPOs);
    }
    catch (Exception e) {
      throw new BssException("保存轮次运行日志失败: " + e.getMessage(), e);
    }
  }

  public void updateTurnResultsWithItemIds(Long spaceId, Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields) {
    try {
      exptTurnResultDAO.updateTurnRunLogWithItemIds(spaceId, exptId, exptRunId, itemIds, ufields);
    }
    catch (Exception e) {
      throw new BssException("根据项目ID更新轮次运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void createOrUpdateItemsTurnRunLogStatus(Long spaceId, Long exptId, Long exptRunId, List<Long> itemIds, TurnRunState status) {
    try {
      // 获取轮次结果
      List<ExptTurnResultEntity> turnResults = exptTurnResultDAO.batchGet(spaceId, exptId, itemIds);

      // 生成ID
      List<Long> ids = iidGenerator.genMultiIds(turnResults.size());

      // 创建运行日志
      List<ExptTurnResultRunLogEntity> runLogs = Lists.newArrayList();
      for (int i = 0; i < turnResults.size(); i++) {
        ExptTurnResultRunLogEntity runLog = ExptTurnResultRunLogEntity.builder()
          .id(ids.get(i))
          .spaceId(spaceId)
          .exptId(exptId)
          .exptRunId(exptRunId)
          .itemId(turnResults.get(i).getItemId())
          .turnId(turnResults.get(i).getTurnId())
          .status(status.getValue())
          .errMsg("turn status not updated for long interval")
          .build();
        runLogs.add(runLog);
      }

      // 批量创建运行日志
      exptTurnResultDAO.batchCreateNxRunLog(runLogs);

      // 更新运行日志状态
      updateTurnResultsWithItemIds(spaceId, exptId, exptRunId, itemIds, Map.of("status", status.getValue()));
    }
    catch (Exception e) {
      throw new BssException("创建或更新项目轮次运行日志状态失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResult> getItemTurnResults(Long exptId, Long itemId, Long spaceId) {
    return getItemTurnResults(exptId, itemId, spaceId, null);
  }

  @Override
  public List<ExptTurnResult> getItemTurnResults(Long exptId, Long itemId, Long spaceId, Long exptRunId) {
    try {
      List<ExptTurnResultEntity> exptTurnResultPOs = exptTurnResultDAO.getItemTurnResults(exptId, itemId, spaceId, exptRunId);
      return ExptTurnResultConvertor.convertToDOList(exptTurnResultPOs);
    }
    catch (Exception e) {
      throw new BssException("获取项目轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResultRunLog> getItemTurnRunLogs(Long exptId, Long exptRunId, Long itemId, Long spaceId) {
    try {
      List<ExptTurnResultRunLogEntity> exptTurnResultRunLogPOs = exptTurnResultDAO.getItemTurnRunLogs(exptId, exptRunId, itemId, spaceId);
      return ExptTurnResultRunLogConvertor.convertToDOList(exptTurnResultRunLogPOs);
    }
    catch (Exception e) {
      throw new BssException("获取项目轮次运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResultRunLog> mGetItemTurnRunLogs(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId) {
    try {
      List<ExptTurnResultRunLogEntity> exptTurnResultRunLogPOs = exptTurnResultDAO.mGetItemTurnRunLogs(exptId, exptRunId, itemIds, spaceId);
      return ExptTurnResultRunLogConvertor.convertToDOList(exptTurnResultRunLogPOs);
    }
    catch (Exception e) {
      throw new BssException("批量获取项目轮次运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void batchCreateNxRunLog(List<ExptTurnResultRunLog> exptTurnResultRunLogs) {
    try {
      List<ExptTurnResultRunLogEntity> runLogPOs = ExptTurnResultRunLogConvertor.convertToPOList(exptTurnResultRunLogs);
      exptTurnResultDAO.batchCreateNxRunLog(runLogPOs);
    }
    catch (Exception e) {
      throw new BssException("批量创建轮次运行日志失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResult> listTurnResult(Long spaceId, Long exptId, ExptTurnResultFilter filter, Page page, Boolean desc) {
    try {
      List<ExptTurnResultEntity> exptTurnResultPOs = exptTurnResultDAO.listTurnResult(spaceId, exptId, filter, page, desc);
      return ExptTurnResultConvertor.convertToDOList(exptTurnResultPOs);
    }
    catch (Exception e) {
      throw new BssException("列出轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResult> listTurnResultByItemIds(Long spaceId, Long exptId, List<Long> itemIds, ExptTurnResultFilter filter, Page page, Boolean desc) {
    try {
      List<ExptTurnResultEntity> exptTurnResultPOs = exptTurnResultDAO.listTurnResultByItemIds(spaceId, exptId, itemIds, filter, page, desc);
      return ExptTurnResultConvertor.convertToDOList(exptTurnResultPOs);
    }
    catch (Exception e) {
      throw new BssException("根据项目ID列出轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnEvaluatorResultRef> batchGetTurnEvaluatorResultRef(Long spaceId, List<Long> exptTurnResultIds) {
    try {
      List<ExptTurnEvaluatorResultRefEntity> refPOs = exptTurnEvaluatorResultRefDAO.batchGet(spaceId, exptTurnResultIds);
      return ExptTurnEvaluatorResultRefConvertor.convertToDOList(refPOs);
    }
    catch (Exception e) {
      throw new BssException("批量获取轮次评估器结果引用失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnEvaluatorResultRef> getTurnEvaluatorResultRefByExptId(Long spaceId, Long exptId) {
    try {
      List<ExptTurnEvaluatorResultRefEntity> refPOs = exptTurnEvaluatorResultRefDAO.getByExptId(spaceId, exptId);
      return ExptTurnEvaluatorResultRefConvertor.convertToDOList(refPOs);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取轮次评估器结果引用失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnEvaluatorResultRef> getTurnEvaluatorResultRefByEvaluatorVersionId(Long spaceId, Long exptId, Long evaluatorVersionId) {
    try {
      List<ExptTurnEvaluatorResultRefEntity> refPOs = exptTurnEvaluatorResultRefDAO.getByExptEvaluatorVersionId(spaceId, exptId, evaluatorVersionId);
      return ExptTurnEvaluatorResultRefConvertor.convertToDOList(refPOs);
    }
    catch (Exception e) {
      throw new BssException("根据评估器版本ID获取轮次评估器结果引用失败: " + e.getMessage(), e);
    }
  }
}
