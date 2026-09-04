package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptResultService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验记录评估失败重试模式实现
 * 迁移对应关系: Go语言ExptRecordEvalModeFailRetry
 * - 功能: 失败重试模式的评估处理
 * - 主要方法:
 * * preEval - 预评估处理
 * * postEval - 后评估处理
 */
@Component
@RequiredArgsConstructor
public class ExptRecordEvalModeFailRetry implements RecordEvalMode {
  private final ExptResultService resultSvc;
  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IIDGenerator idgen;

  /**
   * 预评估处理
   * 迁移对应关系: Go语言PreEval
   * - 功能: 评估前的准备工作
   * - 参数: eiec - 评估上下文
   * - 返回: 无
   * - 异常: BssException - 处理失败时抛出
   */
  @Override
  public void preEval(ExptItemEvalCtx eiec) {
    try {
      // 获取实验项目轮次结果（带exptRunId过滤）
      List<ExptTurnResult> itemTurnResults = resultSvc.getExptItemTurnResults(
        eiec.getEvent().getExptId(), eiec.getEvent().getEvalSetItemId(),
        eiec.getEvent().getSpaceId(), eiec.getEvent().getExptRunId(), eiec.getEvent().getSession());

      // 生成ID
      List<Long> ids = idgen.genMultiIds(itemTurnResults.size());

      // 创建轮次运行日志
      List<ExptTurnResultRunLog> turnRunLogDOs = new ArrayList<>();
      for (int i = 0; i < itemTurnResults.size(); i++) {
        ExptTurnResult tr = itemTurnResults.get(i);
        ExptTurnResultRunLog runLog = tr.toRunLogDO();
        runLog.setId(ids.get(i));
        runLog.setStatus(TurnRunState.PROCESSING);
        runLog.setExptRunId(eiec.getEvent().getExptRunId());
        turnRunLogDOs.add(runLog);
      }

      // 批量创建运行日志
      exptTurnResultRepo.batchCreateNxRunLog(turnRunLogDOs);

      // 更新上下文
      Map<Long, ExptTurnResultRunLog> turnRunResultMap = turnRunLogDOs.stream()
        .collect(Collectors.toMap(ExptTurnResultRunLog::getTurnId, v -> v));

      eiec.getExptItemEvalResult().setTurnResultRunLogs(turnRunResultMap);
    }
    catch (Exception e) {
      throw new BssException("预评估处理失败: " + e.getMessage(), e);
    }
  }

  /**
   * 后评估处理
   * 迁移对应关系: Go语言PostEval
   * - 功能: 评估后的清理工作
   * - 参数: eiec - 评估上下文
   * - 返回: 无
   * - 异常: BssException - 处理失败时抛出
   */
  @Override
  public void postEval(ExptItemEvalCtx eiec) {
    // 失败重试模式不需要后处理
  }
}
