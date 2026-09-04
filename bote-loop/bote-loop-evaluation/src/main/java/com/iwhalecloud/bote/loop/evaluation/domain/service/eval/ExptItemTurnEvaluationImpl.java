package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorsConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteTargetCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvalCtx;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnRunResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RunEvaluatorParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TargetConf;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptItemTurnEvaluationImpl implements ExptItemTurnEvaluation {

  private static final Logger logger = LoggerFactory.getLogger(ExptItemTurnEvaluationImpl.class);

  private final IEvalTargetService evalTargetService;
  private final EvaluatorService evaluatorService;
  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IIDGenerator idGenerator;

  @Override
  public ExptTurnRunResult eval(ExptTurnEvalCtx etec) {

    ExptTurnRunResult trr = new ExptTurnRunResult();
    EvalTargetRecord targetResult;

    try {
      targetResult = callTarget(etec);
    }
    catch (JsonProcessingException e) {
      logger.error("ExptTurnEval call target fail, err: {}", e.getMessage());
      trr.setEvalErr(e);
      return trr;
    }
    trr.setTargetResult(targetResult);

    Map<Long, EvaluatorRecord> evaluatorResults;
    try {
      evaluatorResults = callEvaluators(etec, targetResult);
    }
    catch (JsonProcessingException e) {
      logger.error("[ExptTurnEval] call evaluators fail, err: ", e);
      trr.setEvalErr(e);
      return trr;
    }
    trr.setEvaluatorResults(evaluatorResults);

    return trr;
  }

  private Map<Long, EvaluatorRecord> callEvaluators(ExptTurnEvalCtx etec, EvalTargetRecord targetResult) throws JsonProcessingException {
    Experiment expt = etec.getExpt();
    Map<Long, EvaluatorRecord> evaluatorResults = Maps.newHashMap();
    List<Long> pendingEvaluatorVersionIDs = Lists.newArrayList();
    for (Evaluator evaluatorVersion : expt.getEvaluators()) {
      Long id = evaluatorVersion.getEvaluatorVersion().getId();
      if (etec.getExptTurnRunResult() == null || etec.getExptTurnRunResult().getEvaluatorResults() == null) {
        pendingEvaluatorVersionIDs.add(id);
        continue;
      }
      EvaluatorRecord existResult = etec.getExptTurnRunResult().getEvaluatorResults().get(id);
      if (existResult != null && existResult.getStatus() == EvaluatorRunStatus.SUCCESS) {
        evaluatorResults.put(existResult.getId(), existResult);
      }
      pendingEvaluatorVersionIDs.add(id);
    }
    if (pendingEvaluatorVersionIDs.isEmpty()) {
      return evaluatorResults;
    }
    Map<Long, EvaluatorRecord> runEvalRes = callEvaluators(pendingEvaluatorVersionIDs, etec, targetResult);
    for (Map.Entry<Long, EvaluatorRecord> entry : runEvalRes.entrySet()) {
      Long evID = entry.getKey();
      EvaluatorRecord result = entry.getValue();
      evaluatorResults.put(evID, result);
    }
    return evaluatorResults;
  }

  private Map<Long, EvaluatorRecord> callEvaluators(List<Long> execEvaluatorVersionIDs, ExptTurnEvalCtx etec, EvalTargetRecord targetResult) throws JsonProcessingException {
    Experiment expt = etec.getExpt();
    EvaluatorsConf evaluatorsConf = expt.getEvalConf().getConnectorConf().getEvaluatorsConf();

    if (!validateEvaluatorConfiguration(evaluatorsConf)) {
      return Map.of();
    }

    Map<Long, Boolean> execEvalVerIDMap = buildExecEvalVerIDMap(execEvaluatorVersionIDs);
    Map<String, Content> turnFields = buildTurnFieldsMap(etec.getTurn());
    Map<String, Content> targetFields = buildTargetFieldsMap(targetResult);
    Map<Long, EvaluatorRecord> recordMap = new ConcurrentHashMap<>();
    List<Runnable> tasks = buildEvaluatorTasks(expt, evaluatorsConf, execEvalVerIDMap, turnFields, targetFields, etec, recordMap);

    ThreadPools.invokeTasks(ThreadPools.getEval(), tasks);
    return recordMap;
  }

  private boolean validateEvaluatorConfiguration(EvaluatorsConf evaluatorsConf) {
    try {
      evaluatorsConf.valid();
      return true;
    }
    catch (Exception e) {
      logger.error("Evaluator configuration validation failed", e);
      return false;
    }
  }

  private Map<Long, Boolean> buildExecEvalVerIDMap(List<Long> execEvaluatorVersionIDs) {
    return execEvaluatorVersionIDs.stream()
      .collect(Collectors.toMap(id -> id, id -> true));
  }

  private Map<String, Content> buildTurnFieldsMap(Turn turn) {
    return turn.getFieldDataList().stream()
      .collect(Collectors.toMap(FieldData::getName, FieldData::getContent));
  }

  private Map<String, Content> buildTargetFieldsMap(EvalTargetRecord targetResult) {
    return targetResult.getEvalTargetOutputData().getOutputFields();
  }

  private List<Runnable> buildEvaluatorTasks(Experiment expt, EvaluatorsConf evaluatorsConf, Map<Long, Boolean> execEvalVerIDMap,
                                             Map<String, Content> turnFields, Map<String, Content> targetFields,
                                             ExptTurnEvalCtx etec, Map<Long, EvaluatorRecord> recordMap) {
    List<Runnable> tasks = new ArrayList<>();
    Turn turn = etec.getTurn();
    Long spaceId = expt.getSpaceId();

    for (Evaluator ev : expt.getEvaluators()) {
      Long versionId = ev.getEvaluatorVersion().getId();
      if (!execEvalVerIDMap.containsKey(versionId)) {
        continue;
      }

      EvaluatorConf ec = evaluatorsConf.getEvaluatorConf(versionId);
      if (ec == null) {
        logger.error("Expt's evaluator conf not found, evaluator_version_id: {}", versionId);
        continue;
      }

      Map<String, Content> curFields = buildEvaluatorFields(ec, turnFields, targetFields);
      tasks.add(createEvaluatorTask(etec, turn, spaceId, versionId, curFields, recordMap));
    }

    return tasks;
  }

  private Map<String, Content> buildEvaluatorFields(EvaluatorConf ec, Map<String, Content> turnFields, Map<String, Content> targetFields) {
    Map<String, Content> curFields = new HashMap<>();
    processTargetAdapterFields(ec, targetFields, curFields);
    processEvalSetAdapterFields(ec, turnFields, curFields);
    return curFields;
  }

  private void processTargetAdapterFields(EvaluatorConf ec, Map<String, Content> targetFields, Map<String, Content> curFields) {
    if (targetFields == null) {
      return;
    }
    for (FieldConf fc : ec.getIngressConf().getTargetAdapter().getFieldConfs()) {
      processFieldConfiguration(fc, targetFields, curFields);
    }
  }

  private void processEvalSetAdapterFields(EvaluatorConf ec, Map<String, Content> turnFields, Map<String, Content> curFields) {
    for (FieldConf fc : ec.getIngressConf().getEvalSetAdapter().getFieldConfs()) {
      processFieldConfiguration(fc, turnFields, curFields);
    }
  }

  private void processFieldConfiguration(FieldConf fc, Map<String, Content> sourceFields, Map<String, Content> curFields) {
    try {
      String firstField = JsonPathUtils.getFirstPathField(fc.getFromField());
      if (firstField.equals(fc.getFromField())) {
        curFields.put(fc.getFieldName(), sourceFields.get(fc.getFromField()));
      }
      else {
        Content content = getContentByJsonPath(sourceFields.get(firstField), fc.getFromField());
        curFields.put(fc.getFieldName(), content);
      }
    }
    catch (JsonProcessingException e) {
      logger.error("Error processing field configuration: {}", fc.getFromField(), e);
    }
  }

  private Runnable createEvaluatorTask(ExptTurnEvalCtx etec, Turn turn, Long spaceId, Long evaluatorVersionId,
                                      Map<String, Content> curFields, Map<Long, EvaluatorRecord> recordMap) {
    return () -> {
      try {
        Map<String, Content> inputFields = new HashMap<>(curFields);
        EvaluatorRecord evaluatorRecord = evaluatorService.runEvaluator(RunEvaluatorParam.builder()
          .spaceId(spaceId)
          .name("")
          .evaluatorVersionId(evaluatorVersionId)
          .inputData(EvaluatorInputData.builder()
            .historyMessages(new ArrayList<>())
            .inputFields(inputFields)
            .build())
          .experimentId(etec.getEvent().getExptId())
          .experimentRunId(etec.getEvent().getExptRunId())
          .itemId(etec.getEvalSetItem().getItemId())
          .turnId(turn.getId())
          .ext(etec.getExt())
          .build());

        saveTurnEvaluatorResultRef(etec, turn, evaluatorVersionId, evaluatorRecord);
        recordMap.put(evaluatorVersionId, evaluatorRecord);
      }
      catch (Exception e) {
        logger.error("Error executing evaluator {}: {}", evaluatorVersionId, e.getMessage(), e);
      }
    };
  }

  /**
   * 保存单个轮次评估器结果关联记录
   * 注意：此时 ExptTurnResult 可能还未创建，需要通过 exptId、itemId、turnId 查询
   */
  private void saveTurnEvaluatorResultRef(ExptTurnEvalCtx etec, Turn turn, Long evaluatorVersionId, EvaluatorRecord evaluatorRecord) {
    if (evaluatorRecord == null || evaluatorRecord.getId() == null) {
      return;
    }

    try {
      Experiment expt = etec.getExpt();
      Long spaceId = expt.getSpaceId();
      Long exptId = etec.getEvent().getExptId();
      Long itemId = etec.getEvalSetItem().getItemId();
      Long turnId = turn.getId();

      // 查询 ExptTurnResult（可能还未创建，需要先创建或等待创建）
      List<ExptTurnResult> turnResults = exptTurnResultRepo.getItemTurnResults(exptId, itemId, spaceId);
      ExptTurnResult turnResult = turnResults.stream()
        .filter(tr -> tr.getTurnId() != null && tr.getTurnId().equals(turnId))
        .findFirst()
        .orElse(null);

      // 如果 ExptTurnResult 不存在，尝试通过 batchGet 查询
      if (turnResult == null) {
        List<ExptTurnResult> batchResults = exptTurnResultRepo.batchGet(spaceId, exptId, List.of(itemId));
        turnResult = batchResults.stream()
          .filter(tr -> tr.getTurnId() != null && tr.getTurnId().equals(turnId))
          .findFirst()
          .orElse(null);
      }

      // 如果仍然不存在，记录调试日志但不抛出异常（ExptTurnResult 可能在后续步骤中创建）
      if (turnResult == null || turnResult.getId() == null) {
        logger.debug("ExptTurnResult not found yet, will be saved later. exptId={}, itemId={}, turnId={}",
          exptId, itemId, turnId);
        return;
      }

      // 创建关联记录
      ExptTurnEvaluatorResultRef ref = ExptTurnEvaluatorResultRef.builder()
        .id(idGenerator.genId())
        .spaceId(spaceId)
        .exptId(exptId)
        .exptTurnResultId(turnResult.getId())
        .evaluatorVersionId(evaluatorVersionId)
        .evaluatorResultId(evaluatorRecord.getId())
        .build();

      // 保存关联记录
      exptTurnResultRepo.createTurnEvaluatorRefs(List.of(ref));
      logger.debug("Saved evaluator result ref for exptId={}, itemId={}, turnId={}, evaluatorVersionId={}, turnResultId={}",
        exptId, itemId, turnId, evaluatorVersionId, turnResult.getId());
    }
    catch (Exception e) {
      logger.error("Failed to save evaluator result ref: {}", e.getMessage(), e);
      // 不抛出异常，避免影响主流程
    }
  }

  private EvalTargetRecord callTarget(ExptTurnEvalCtx etec) throws JsonProcessingException {
    EvalTargetRecord existResult = etec.getExptTurnRunResult().getTargetResult();
    if (existResult != null && existResult.getStatus() != null && existResult.getStatus() == EvalTargetRunStatus.SUCCESS) {
      return existResult;
    }
    return callTarget(etec, etec.getHistory(), etec.getEvent().getSpaceId());
  }

  private EvalTargetRecord callTarget(ExptTurnEvalCtx etec, List<Message> history, Long spaceId) throws JsonProcessingException {

    Turn turn = etec.getTurn();
    TargetConf targetConf = etec.getExpt().getEvalConf().getConnectorConf().getTargetConf();

    Map<String, Content> turnFields = turn.getFieldDataList().stream()
      .collect(Collectors.toMap(FieldData::getName, FieldData::getContent));

    List<FieldConf> fieldConfs = targetConf.getIngressConf().getEvalSetAdapter().getFieldConfs();

    Map<String, Content> fields = Maps.newHashMap();
    for (FieldConf fc : fieldConfs) {
      String firstField = JsonPathUtils.getFirstPathField(fc.getFromField());
      if (firstField.equals(fc.getFromField())) {
        fields.put(fc.getFieldName(), turnFields.get(fc.getFromField()));
        continue;
      }
      Content content = getContentByJsonPath(turnFields.get(firstField), fc.getFromField());
      fields.put(fc.getFieldName(), content);
    }
    Long targetId = etec.getExpt().getTarget().getId();
    Long targetVersionId = etec.getExpt().getTarget().getEvalTargetVersion().getId();
    ExecuteTargetCtx param = new ExecuteTargetCtx();
    param.setExperimentRunId(etec.getEvent().getExptRunId());
    param.setItemId(etec.getEvalSetItem().getItemId());
    param.setTurnId(etec.getTurn().getId());

    EvalTargetInputData inputData = new EvalTargetInputData();
    inputData.setHistoryMessages(history);
    inputData.setInputFields(fields);
    inputData.setExt(etec.getExt());

    return evalTargetService.executeTarget(spaceId, targetId, targetVersionId, param, inputData);
  }

  private Content getContentByJsonPath(Content content, String jsonPath) throws JsonProcessingException {
    if (content == null) {
      return null;
    }
    if (content.getContentType() == null || content.getContentType() != ContentType.TEXT) {
      return null;
    }
    jsonPath = JsonPathUtils.removeFirstPathLevel(jsonPath);
    String text = JsonPathUtils.getStringByPath(content.getText(), jsonPath);
    Content result = new Content();
    result.setContentType(ContentType.TEXT);
    result.setText(text);
    return result;
  }

}
