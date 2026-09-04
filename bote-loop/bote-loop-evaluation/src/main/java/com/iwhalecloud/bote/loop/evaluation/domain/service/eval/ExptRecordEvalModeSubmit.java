package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验记录评估提交模式实现
 * 迁移对应关系: Go语言ExptRecordEvalModeSubmit
 * - 功能: 提交/追加模式的评估处理
 * - 主要方法:
 * * preEval - 预评估处理
 * * postEval - 后评估处理
 */
@Component
@RequiredArgsConstructor
public class ExptRecordEvalModeSubmit implements RecordEvalMode {
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
      ExptItemEvalEvent event = eiec.getEvent();
      List<Turn> turns = eiec.getEvalSetItem().getTurns();

      // 查找缺失的轮次运行日志
      List<Long> absentRunLogTurnIDs = new ArrayList<>();
      for (Turn turn : turns) {
        if (turn == null) {
          continue;
        }

        if (eiec.getExistTurnResultRunLog(turn.getId()) == null) {
          absentRunLogTurnIDs.add(turn.getId());
        }
      }

      if (!absentRunLogTurnIDs.isEmpty()) {
        // 生成ID
        List<Long> ids = idgen.genMultiIds(absentRunLogTurnIDs.size());

        // 创建轮次运行日志
        List<ExptTurnResultRunLog> turnRunResults = new ArrayList<>();
        for (int i = 0; i < absentRunLogTurnIDs.size(); i++) {
          Long turnID = absentRunLogTurnIDs.get(i);
          Long id = ids.get(i);

          ExptTurnResultRunLog runLog = ExptTurnResultRunLog.builder()
            .id(id)
            .spaceId(event.getSpaceId())
            .exptId(event.getExptId())
            .exptRunId(event.getExptRunId())
            .itemId(event.getEvalSetItemId())
            .turnId(turnID)
            .status(TurnRunState.PROCESSING)
            .logId("") // TODO: 获取日志ID
            .build();

          turnRunResults.add(runLog);
        }

        // 批量创建运行日志
        exptTurnResultRepo.batchCreateNxRunLog(turnRunResults);

        // 更新上下文
        Map<Long, ExptTurnResultRunLog> turnRunResultMap = turnRunResults.stream()
          .collect(Collectors.toMap(ExptTurnResultRunLog::getTurnId, v -> v));

        eiec.getExptItemEvalResult().setTurnResultRunLogs(turnRunResultMap);
      }
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
    // 提交模式不需要后处理
  }
}
