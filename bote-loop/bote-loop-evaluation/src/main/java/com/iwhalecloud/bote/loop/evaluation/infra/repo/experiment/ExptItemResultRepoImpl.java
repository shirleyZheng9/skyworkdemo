package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultRunLogEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemRunLogFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptItemResultDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptItemResultConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptItemResultRunLogConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptTurnResultConvertor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验数据项结果仓储实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/expt_item_result.go
 * - 功能: 实验数据项结果数据访问实现
 * - 主要方法:
 * * batchGet - 批量获取数据项结果
 * * updateItemsResult - 更新数据项结果
 * * getItemTurnResults - 获取数据项轮次结果
 * * saveItemResults - 保存数据项结果
 * * getItemRunLog - 获取数据项运行日志
 * * mGetItemRunLog - 批量获取数据项运行日志
 * * updateItemRunLog - 更新数据项运行日志
 * * scanItemResults - 扫描数据项结果
 * * getItemIdListByExptId - 获取数据项ID列表
 * * listItemResultsByExptId - 分页获取数据项结果
 * * scanItemRunLogs - 扫描数据项运行日志
 * * batchCreateNx - 批量创建（如果不存在）
 * * batchCreateNxRunLogs - 批量创建运行日志（如果不存在）
 * * getMaxItemIdxByExptId - 获取最大数据项序号
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemResultRepoImpl结构体
 * - 使用转换器进行DO和PO转换
 * - 使用异常处理机制
 * - 支持批量操作
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.ExptItemResult -> Java ExptItemResult
 * - Go []*entity.ExptItemResult -> Java List<ExptItemResult>
 * - Go map[string]any -> Java Map<String, Object>
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 */
@Component
@RequiredArgsConstructor
public class ExptItemResultRepoImpl implements IExptItemResultRepo {
  private final IExptItemResultDAO exptItemResultDAO;

  @Override
  public List<ExptItemResult> batchGet(Long spaceId, Long exptId, List<Long> itemIds) {
    try {
      List<ExptItemResultEntity> pos = exptItemResultDAO.batchGet(spaceId, exptId, itemIds);
      return pos.stream()
        .map(ExptItemResultConvertor::convertToDO)
        .toList();
    }
    catch (Exception e) {
      throw new BssException("批量获取数据项结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void updateItemsResult(Long spaceId, Long exptId, List<Long> itemIds, Map<String, Object> ufields) {
    try {
      exptItemResultDAO.updateItemsResult(spaceId, exptId, itemIds, ufields);
    }
    catch (Exception e) {
      throw new BssException("更新数据项结果失败, expt_id: " + exptId + ", item_id: " + itemIds + ", ufields: " + ufields + ", " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResult> getItemTurnResults(Long spaceId, Long exptId, Long itemId) {
    try {
      List<ExptTurnResultEntity> pos = exptItemResultDAO.getItemTurnResults(spaceId, exptId, itemId);
      return pos.stream()
        .map(po -> ExptTurnResultConvertor.convertToDO(po, null))
        .toList();
    }
    catch (Exception e) {
      throw new BssException("获取数据项轮次结果失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void saveItemResults(List<ExptItemResult> itemResults) {
    try {
      List<ExptItemResultEntity> pos = itemResults.stream()
        .map(ExptItemResultConvertor::convertToPO)
        .toList();
      exptItemResultDAO.saveItemResults(pos);
    }
    catch (Exception e) {
      throw new BssException("保存数据项结果失败, cnt: " + itemResults.size() + ", " + e.getMessage(), e);
    }
  }

  @Override
  public ExptItemResultRunLog getItemRunLog(Long exptId, Long exptRunId, Long itemId, Long spaceId) {
    try {
      ExptItemResultRunLogEntity po = exptItemResultDAO.getItemRunLog(exptId, exptRunId, itemId, spaceId);
      return ExptItemResultRunLogConvertor.convertToDO(po);
    }
    catch (Exception e) {
      throw new BssException("获取数据项运行日志失败, expt_id: " + exptId + ", expt_run_id: " + exptRunId + ", item_id: " + itemId + ", " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptItemResultRunLog> mGetItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId) {
    try {
      List<ExptItemResultRunLogEntity> pos = exptItemResultDAO.mGetItemRunLog(exptId, exptRunId, itemIds, spaceId);
      return pos.stream()
        .map(ExptItemResultRunLogConvertor::convertToDO)
        .toList();
    }
    catch (Exception e) {
      throw new BssException("批量获取数据项运行日志失败, expt_id: " + exptId + ", expt_run_id: " + exptRunId + ", item_ids: " + itemIds + ", " + e.getMessage(), e);
    }
  }

  @Override
  public void updateItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields, Long spaceId) {
    try {
      exptItemResultDAO.updateItemRunLog(exptId, exptRunId, itemIds, ufields, spaceId);
    }
    catch (Exception e) {
      throw new BssException("更新数据项运行日志失败, expt_id: " + exptId + ", expt_run_id: " + exptRunId + ", item_ids: " + itemIds + ", ufields: " + ufields + ", " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptItemResult> scanItemResults(Long exptId, Long cursor, Long limit, List<Integer> status, Long spaceId) {
    try {
      List<ExptItemResultEntity> pos = exptItemResultDAO.scanItemResults(exptId, cursor, limit, status, spaceId);
      return pos.stream()
        .map(ExptItemResultConvertor::convertToDO)
        .toList();
    }
    catch (Exception e) {
      throw new BssException("扫描数据项结果失败, exptID=" + exptId + ", cursor=" + cursor + ", " + e.getMessage(), e);
    }
  }

  @Override
  public List<Long> getItemIdListByExptId(Long exptId, Long spaceId) {
    try {
      return exptItemResultDAO.getItemIdListByExptId(exptId, spaceId);
    }
    catch (Exception e) {
      throw new BssException("获取数据项ID列表失败: " + e.getMessage(), e);
    }
  }

  @Override
  public PageInfo<ExptItemResult> listItemResultsByExptId(Long exptId, Long spaceId, Page page, Boolean desc) {
    try {
      PageInfo<ExptItemResultEntity> pos = exptItemResultDAO.listItemResultsByExptId(exptId, spaceId, page, desc);
      return pos.convert(ExptItemResultConvertor::convertToDO);
    }
    catch (Exception e) {
      throw new BssException("分页获取数据项结果失败, exptID=" + exptId + ", spaceID=" + spaceId + ", page=" + page + ", desc=" + desc + ", " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptItemResultRunLog> scanItemRunLogs(Long exptId, Long exptRunId, ExptItemRunLogFilter filter, Long cursor, Long limit, Long spaceId) {
    try {
      List<ExptItemResultRunLogEntity> pos = exptItemResultDAO.scanItemRunLogs(exptId, exptRunId, filter, cursor, limit, spaceId);
      return pos.stream()
        .map(ExptItemResultRunLogConvertor::convertToDO)
        .toList();
    }
    catch (Exception e) {
      throw new BssException("扫描数据项运行日志失败, exptID=" + exptId + ", exptRunID=" + exptRunId + ", cursor=" + cursor + ", " + e.getMessage(), e);
    }
  }

  @Override
  public void batchCreateNx(List<ExptItemResult> itemResults) {
    try {
      List<ExptItemResultEntity> pos = itemResults.stream()
        .map(ExptItemResultConvertor::convertToPO)
        .toList();
      exptItemResultDAO.batchCreateNx(pos);
    }
    catch (Exception e) {
      throw new BssException("批量创建数据项结果失败, cnt: " + itemResults.size() + ", " + e.getMessage(), e);
    }
  }

  @Override
  public void batchCreateNxRunLogs(List<ExptItemResultRunLog> itemResults) {
    try {
      List<ExptItemResultRunLogEntity> pos = itemResults.stream()
        .map(ExptItemResultRunLogConvertor::convertToPO)
        .toList();
      exptItemResultDAO.batchCreateNxRunLogs(pos);
    }
    catch (Exception e) {
      throw new BssException("批量创建数据项运行日志失败, cnt: " + itemResults.size() + ", " + e.getMessage(), e);
    }
  }

  @Override
  public Integer getMaxItemIdxByExptId(Long exptId, Long spaceId) {
    try {
      return exptItemResultDAO.getMaxItemIdxByExptId(exptId, spaceId);
    }
    catch (Exception e) {
      throw new BssException("获取最大数据项序号失败: " + e.getMessage(), e);
    }
  }
}
