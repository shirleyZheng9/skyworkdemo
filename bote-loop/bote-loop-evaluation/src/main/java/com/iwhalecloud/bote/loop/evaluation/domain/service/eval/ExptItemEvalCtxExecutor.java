package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.loop.evaluation.domain.component.IConfiger;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorResults;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemResultState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultRunLog;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnRunResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.event.ExptItemEvalEvent;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptItemResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorRecordService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 实验项目评估上下文执行器
 */
@Component
@RequiredArgsConstructor
public class ExptItemEvalCtxExecutor implements ExptItemEvaluation {
  private static final Logger logger = LoggerFactory.getLogger(ExptItemEvalCtxExecutor.class);

  private final IExptTurnResultRepo turnResultRepo;
  private final IExptItemResultRepo itemResultRepo;
  private final IConfiger configer;
  private final IEvalTargetService evalTargetService;
  private final EvaluatorRecordService evaluatorRecordService;
  private final ExptItemTurnEvaluation exptItemTurnEvaluation;

  @Override
  public void eval(ExptItemEvalCtx eiec) throws Exception {
    ExptItemEvalEvent event = eiec.getEvent();
    // 执行轮次评估
    Exception evalErr = evalTurns(eiec);
    // 完成项目运行
    completeItemRun(event, evalErr);
  }

  private Exception evalTurns(ExptItemEvalCtx eiec) {
    if (eiec.getEvalSetItem() == null) {
      throw new BssException("EvalTurns with invalid empty eval_set_item");
    }
    for (Turn turn : eiec.getEvalSetItem().getTurns()) {
      ExptTurnEvalCtx etec = buildExptTurnEvalCtx(turn, eiec);
      ExptTurnRunResult turnRunRes = exptItemTurnEvaluation.eval(etec);
      storeTurnRunResult(etec, turnRunRes);
    }
    return null;
  }

  private void storeTurnRunResult(ExptTurnEvalCtx etec, ExptTurnRunResult result) {
    validateStoreTurnRunResultInput(etec, result);

    ExptTurnResultRunLog clone = createTurnResultLogClone(etec);
    Exception evalErr = determineEvaluationError(result);
    if (evalErr != null) {
      clone.setStatus(TurnRunState.FAIL);
    }
    updateTurnResultLog(clone, etec, result, evalErr);
    result.setEvalErr(evalErr);
    turnResultRepo.saveTurnRunLogs(List.of(clone));

    // 保存 ExptTurnResult
    ExptTurnResult turnResult = createTurnResultFromRunLog(clone, etec);
    turnResultRepo.saveTurnResults(List.of(turnResult));
  }

  private ExptTurnResult createTurnResultFromRunLog(ExptTurnResultRunLog runLog, ExptTurnEvalCtx etec) {
    Turn turn = etec.getTurn();
    Integer turnIdx = getTurnIdx(etec, turn);
    return ExptTurnResult.builder()
      .spaceId(runLog.getSpaceId())
      .exptId(runLog.getExptId())
      .exptRunId(runLog.getExptRunId())
      .itemId(runLog.getItemId())
      .turnId(runLog.getTurnId())
      .status(runLog.getStatus() != null ? runLog.getStatus().getValue() : null)
      .traceId(runLog.getTraceId())
      .logId(runLog.getLogId())
      .targetResultId(runLog.getTargetResultId())
      .evaluatorResults(runLog.getEvaluatorResultIds())
      .errMsg(runLog.getErrMsg())
      .turnIdx(turnIdx)
      .build();
  }

  private Integer getTurnIdx(ExptTurnEvalCtx etec, Turn turn) {
    if (turn == null || etec.getEvalSetItem() == null || etec.getEvalSetItem().getTurns() == null) {
      return null;
    }
    List<Turn> turns = etec.getEvalSetItem().getTurns();
    for (int i = 0; i < turns.size(); i++) {
      if (turns.get(i).getId().equals(turn.getId())) {
        return i;
      }
    }
    return null;
  }

  private void validateStoreTurnRunResultInput(ExptTurnEvalCtx etec, ExptTurnRunResult result) {
    if (result == null) {
      throw new BssException("StoreTurnRunResult with nil result");
    }

    Turn turn = etec.getTurn();
    ExptTurnResultRunLog turnResultLog = etec.getExistItemEvalResult().getTurnResultRunLogs().get(turn.getId());

    if (turnResultLog == null) {
      throw new BssException(String.format("storeTurnRunResult with invalid turn result log, expt_id: %s, item_id: %s, turn_id: %s",
        etec.getExpt().getId(), etec.getEvalSetItem().getItemId(), turn.getId()));
    }
  }

  private ExptTurnResultRunLog createTurnResultLogClone(ExptTurnEvalCtx etec) {
    Turn turn = etec.getTurn();
    ExptTurnResultRunLog turnResultLog = etec.getExistItemEvalResult().getTurnResultRunLogs().get(turn.getId());
    ExptTurnResultRunLog clone = new ExptTurnResultRunLog();
    BeanUtils.copyProperties(turnResultLog, clone);
    return clone;
  }

  private Exception determineEvaluationError(ExptTurnRunResult result) {
    Exception evalErr = checkTargetResultError(result);
    if (evalErr == null) {
      evalErr = checkEvaluatorResultsError(result);
    }
    if (evalErr == null) {
      evalErr = result.getEvalErr();
    }
    return evalErr;
  }

  private Exception checkTargetResultError(ExptTurnRunResult result) {
    if (result.getTargetResult() != null
      && result.getTargetResult().getEvalTargetOutputData() != null
      && result.getTargetResult().getEvalTargetOutputData().getEvalTargetRunError() != null
      && result.getTargetResult().getEvalTargetOutputData().getEvalTargetRunError().getCode() != null
      && result.getTargetResult().getEvalTargetOutputData().getEvalTargetRunError().getCode() > 0) {
      return new BssException("Target result error: " +
        result.getTargetResult().getEvalTargetOutputData().getEvalTargetRunError().getMessage());
    }
    return null;
  }

  private Exception checkEvaluatorResultsError(ExptTurnRunResult result) {
    if (result.getEvaluatorResults() == null) {
      return null;
    }

    for (Map.Entry<Long, EvaluatorRecord> entry : result.getEvaluatorResults().entrySet()) {
      if (entry.getValue().getEvaluatorOutputData() != null
        && entry.getValue().getEvaluatorOutputData().getEvaluatorRunError() != null
        && entry.getValue().getEvaluatorOutputData().getEvaluatorRunError().getCode() > 0) {
        return new BssException("Evaluator result error: " +
          entry.getValue().getEvaluatorOutputData().getEvaluatorRunError().getMessage());
      }
    }
    return null;
  }

  private void updateTurnResultLog(ExptTurnResultRunLog clone, ExptTurnEvalCtx etec,
                                   ExptTurnRunResult result, Exception evalErr) {
    clone.setExptRunId(etec.getEvent().getExptRunId());

    if (result.getTargetResult() != null) {
      clone.setTargetResultId(result.getTargetResult().getId());
    }

    clone.setEvaluatorResultIds(new EvaluatorResults(new HashMap<>()));
    if (result.getEvaluatorResults() != null) {
      for (Map.Entry<Long, EvaluatorRecord> entry : result.getEvaluatorResults().entrySet()) {
        clone.getEvaluatorResultIds().getEvalVerIdToResId().put(entry.getKey(), entry.getValue().getId());
      }
    }

    if (evalErr != null) {
      String errMsg = configer.getErrCtrl().convertErrMsg(evalErr.getMessage());
      logger.warn("[ExptTurnEval] store turn run err, before: {}, after: {}", evalErr, errMsg);
      clone.setStatus(TurnRunState.FAIL);
      clone.setErrMsg(ExpUtil.getMsg(evalErr));
    }
    else {
      clone.setStatus(TurnRunState.SUCCESS);
    }
  }

  private ExptTurnEvalCtx buildExptTurnEvalCtx(Turn turn, ExptItemEvalCtx eiec) {
    Long spaceId = eiec.getEvent().getSpaceId();
    ExptTurnResultRunLog existTurnRunResult = eiec.getExistTurnResultRunLog(turn.getId());

    ExptTurnEvalCtx etec = createBaseExptTurnEvalCtx(turn, eiec);
    setExtensionInfo(etec, eiec);

    if (existTurnRunResult != null) {
      loadExistingResults(etec, existTurnRunResult, spaceId);
    }

    return etec;
  }

  private ExptTurnEvalCtx createBaseExptTurnEvalCtx(Turn turn, ExptItemEvalCtx eiec) {
    ExptTurnEvalCtx etec = new ExptTurnEvalCtx();
    etec.setEvent(eiec.getEvent());
    etec.setExpt(eiec.getExpt());
    etec.setEvalSetItem(eiec.getEvalSetItem());
    etec.setExistItemEvalResult(eiec.getExptItemEvalResult());
    etec.setTurn(turn);
    etec.setExptTurnRunResult(new ExptTurnRunResult());
    return etec;
  }

  private void setExtensionInfo(ExptTurnEvalCtx etec, ExptItemEvalCtx eiec) {
    setSpanIdFromFieldData(etec, eiec);
    setBasicExtensionInfo(etec, eiec);
    copyEventExtensionInfo(etec, eiec);
  }

  private void setSpanIdFromFieldData(ExptTurnEvalCtx etec, ExptItemEvalCtx eiec) {
    if (!eiec.getEvalSetItem().getTurns().isEmpty()) {
      for (FieldData fieldData : eiec.getEvalSetItem().getTurns().getFirst().getFieldDataList()) {
        if ("span_id".equals(fieldData.getName())) {
          etec.getExt().put("span_id", fieldData.getContent().getText());
        }
      }
    }
  }

  private void setBasicExtensionInfo(ExptTurnEvalCtx etec, ExptItemEvalCtx eiec) {
    if (etec.getExt() == null) {
      etec.setExt(new HashMap<>());
    }
    etec.getExt().put("task_id", eiec.getExpt().getSourceId());
    etec.getExt().put("workspace_id", String.valueOf(eiec.getExpt().getSpaceId()));
    etec.getExt().put("start_time", String.valueOf(eiec.getEvalSetItem().getBaseInfo().getCreatedAt() * 1000));
  }

  private void copyEventExtensionInfo(ExptTurnEvalCtx etec, ExptItemEvalCtx eiec) {
    if (eiec.getEvent().getExt() == null) {
      return;
    }
    for (Map.Entry<String, String> entry : eiec.getEvent().getExt().entrySet()) {
      etec.getExt().put(entry.getKey(), entry.getValue());
    }
  }

  private void loadExistingResults(ExptTurnEvalCtx etec, ExptTurnResultRunLog existTurnRunResult, Long spaceId) {
    loadTargetResult(etec, existTurnRunResult, spaceId);
    loadEvaluatorResults(etec, existTurnRunResult);
  }

  private void loadTargetResult(ExptTurnEvalCtx etec, ExptTurnResultRunLog existTurnRunResult, Long spaceId) {
    if (existTurnRunResult.getTargetResultId() != null && existTurnRunResult.getTargetResultId() > 0) {
      EvalTargetRecord targetRecord = evalTargetService.getRecordById(spaceId, existTurnRunResult.getTargetResultId());
      etec.getExptTurnRunResult().setTargetResult(targetRecord);
    }
  }

  private void loadEvaluatorResults(ExptTurnEvalCtx etec, ExptTurnResultRunLog existTurnRunResult) {
    if (existTurnRunResult.getEvaluatorResultIds() != null &&
      !existTurnRunResult.getEvaluatorResultIds().getEvalVerIdToResId().isEmpty()) {

      List<Long> recordIds = new ArrayList<>(existTurnRunResult.getEvaluatorResultIds().getEvalVerIdToResId().values());
      List<EvaluatorRecord> evaluatorRecords = evaluatorRecordService.batchGetEvaluatorRecord(recordIds, false);

      Map<Long, EvaluatorRecord> recordMap = new HashMap<>();
      for (EvaluatorRecord record : evaluatorRecords) {
        recordMap.put(record.getId(), record);
      }
      etec.getExptTurnRunResult().setEvaluatorResults(recordMap);
    }
  }

  private void completeItemRun(ExptItemEvalEvent event, Exception evalErr) throws Exception {
    Map<String, Object> ufields = new HashMap<>();
    Map<String, Object> uItemsResultfields = new HashMap<>();
    ufields.put("result_state", ExptItemResultState.LOGGED.getValue());
    if (evalErr != null) {
      ufields.put("status", ItemRunState.FAIL.getValue());
      ufields.put("err_msg", ExpUtil.getMsg(evalErr));
      uItemsResultfields.put("status", ItemRunState.FAIL.getValue());
      uItemsResultfields.put("err_msg", ExpUtil.getMsg(evalErr));
    }
    else {
      ufields.put("status", ItemRunState.SUCCESS.getValue());
      uItemsResultfields.put("status", ItemRunState.SUCCESS.getValue());
    }
    itemResultRepo.updateItemRunLog(event.getExptId(), event.getExptRunId(),
      List.of(event.getEvalSetItemId()), ufields, event.getSpaceId());
    itemResultRepo.updateItemsResult(event.getSpaceId(), event.getExptId(), List.of(event.getEvalSetItemId()), uItemsResultfields);
    if (evalErr != null) {
      throw evalErr;
    }
  }
}
