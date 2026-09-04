package com.iwhalecloud.bote.loop.evaluation.infra.pdf.handle;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.FieldDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.BotDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalPromptDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetContentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetOutputDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetRunErrorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.WorkflowDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorOutputDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRecordDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorRunErrorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.AggregateDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.AggregatorResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.AggregatorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentTurnPayloadDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ItemResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnEvalSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnEvaluatorOutputDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TurnTargetOutputDTO;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentAggrResultResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentResultResponse;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.pdf.ExptResultData;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.AggregateEvaluatorInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.AggregateSummaryInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.EvaluatorRecordInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ExperimentBaseInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ItemDetailInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ItemInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.OpenPdfData;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ReportInfo;
import com.iwhalecloud.bote.loop.evaluation.infra.pdf.module.ScoreDistributionInfo;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenPdfDataConvertor {

  public static final Double DEFAULT_PASS_SCORE = 0.7;

  public OpenPdfData convert(ExptResultData exptResultData) {
    OpenPdfData openPdfData = new OpenPdfData();

    ReportInfo reportInfo = convertReportInfo(exptResultData);
    openPdfData.setReportInfo(reportInfo);

    ExperimentBaseInfo experimentBaseInfo = convertExperimentBaseInfo(exptResultData);
    openPdfData.setBaseInfo(experimentBaseInfo);

    AggregateSummaryInfo aggregateSummaryInfo = new AggregateSummaryInfo();
    List<AggregateEvaluatorInfo> aggregateEvaluatorInfos = new ArrayList<>();
    Map<String, List<ScoreDistributionInfo>> evaluatorScoreDistributionInfos = new HashMap<>();
    openPdfData.setAggregateSummaryInfo(aggregateSummaryInfo);
    openPdfData.setAggregateEvaluatorInfos(aggregateEvaluatorInfos);
    openPdfData.setEvaluatorScoreDistributionInfos(evaluatorScoreDistributionInfos);

    convertAggregate(exptResultData, openPdfData);

    List<ItemInfo> itemInfos = new ArrayList<>();
    List<ItemDetailInfo> itemDetailInfos = new ArrayList<>();
    openPdfData.setItemInfos(itemInfos);
    openPdfData.setItemDetailInfos(itemDetailInfos);

    convertItem(exptResultData, openPdfData);

    return openPdfData;
  }

  private void convertItem(ExptResultData exptResultData, OpenPdfData openPdfData) {
    AggregateSummaryInfo aggregateSummaryInfo = openPdfData.getAggregateSummaryInfo();
    List<ItemInfo> itemInfos = openPdfData.getItemInfos();
    List<ItemDetailInfo> itemDetailInfos = openPdfData.getItemDetailInfos();

    BatchGetExperimentResultResponse experimentResultResponse = exptResultData.getExperimentResultResponse();
    List<ItemResultDTO> itemResults = experimentResultResponse.getItemResults();

    Map<Long, Map<Double, Long>> scoreDistributionMaps = Maps.newHashMap();
    Map<Long, String> evaluatorInfos = exptResultData.getEvaluatorInfos();
    for (Map.Entry<Long, String> entry : evaluatorInfos.entrySet()) {
      scoreDistributionMaps.put(entry.getKey(), Maps.newHashMap());
    }

    long passCount = 0L;
    for (ItemResultDTO itemResult : itemResults) {
      ItemDetailInfo itemDetailInfo = new ItemDetailInfo();
      ItemInfo itemInfo = new ItemInfo();

      // 最新一轮实验运行结果数据
      Long itemId = itemResult.getItemId();
      List<TurnResultDTO> turnResults = itemResult.getTurnResults();
      if (CollectionUtils.isEmpty(turnResults)) {
        continue;
      }
      TurnResultDTO turnResultDTO = turnResults.get(0);
      List<ExperimentResultDTO> experimentResults = turnResultDTO.getExperimentResults();
      if (CollectionUtils.isEmpty(experimentResults)) {
        continue;
      }
      ExperimentResultDTO experimentResultDTO = experimentResults.get(0);
      ExperimentTurnPayloadDTO payload = experimentResultDTO.getPayload();
      if (payload == null) {
        continue;
      }

      // 评测对象输出数据转换
      TurnTargetOutputDTO targetOutput = payload.getTargetOutput();
      convertTargetOutput(targetOutput, itemDetailInfo, itemInfo);

      // 评估器记录数据转换
      Map<String, Double> evaluatorScores = new HashMap<>();
      List<EvaluatorRecordInfo> evaluatorRecordInfos = new ArrayList<>();
      TurnEvaluatorOutputDTO evaluatorOutput = payload.getEvaluatorOutput();
      if (evaluatorOutput != null) {
        Map<Long, EvaluatorRecordDTO> evaluatorRecords = evaluatorOutput.getEvaluatorRecords();
        convertEvaluatorRecord(exptResultData, evaluatorRecords, evaluatorScores, evaluatorRecordInfos);
        if (evaluatorRecordInfos.stream().allMatch(EvaluatorRecordInfo::isPassFlag)) {
          passCount++;
        }
      }
      computeItemScoreDistribution(evaluatorOutput, scoreDistributionMaps);

      // 评测集数据转换
      Map<String, String> evalSetMap = new HashMap<>();
      TurnEvalSetDTO evalSet = payload.getEvalSet();
      convertEvalSet(evalSet, evalSetMap);

      // 数据明细详情
      itemDetailInfo.setItemId(itemId);
      itemDetailInfo.setEvalSetMap(evalSetMap);
      itemDetailInfo.setEvaluatorRecordInfos(evaluatorRecordInfos);
      itemDetailInfos.add(itemDetailInfo);

      // 数据明细
      itemInfo.setItemId(itemId);
      itemInfo.setEvaluatorScores(evaluatorScores);
      itemInfos.add(itemInfo);
    }

    Long total = experimentResultResponse.getTotal();

    convertEvaluatorPassRate(exptResultData, openPdfData, total, scoreDistributionMaps);

    aggregateSummaryInfo.setEvalSetSize(total);
    double passRate = computePercentage(passCount, total);
    aggregateSummaryInfo.setPassScore(DEFAULT_PASS_SCORE);
    aggregateSummaryInfo.setPassRate(passRate);
    aggregateSummaryInfo.setEvalSetSize(total);
  }

  private void convertEvaluatorPassRate(ExptResultData exptResultData, OpenPdfData openPdfData, Long total, Map<Long, Map<Double, Long>> scoreDistributionMaps) {
    List<AggregateEvaluatorInfo> aggregateEvaluatorInfos = openPdfData.getAggregateEvaluatorInfos();
    Map<String, List<ScoreDistributionInfo>> evaluatorScoreDistributionInfos = openPdfData.getEvaluatorScoreDistributionInfos();
    Map<Long, String> evaluatorInfos = exptResultData.getEvaluatorInfos();
    for (AggregateEvaluatorInfo aggregateEvaluatorInfo : aggregateEvaluatorInfos) {
      Double passScore = aggregateEvaluatorInfo.getPassScore();
      Long evaluatorVersionId = aggregateEvaluatorInfo.getEvaluatorVersionId();
      long evaluatorPassCount = 0L;

      Map<Double, Long> scoreDistributionMap = scoreDistributionMaps.get(aggregateEvaluatorInfo.getEvaluatorVersionId());
      List<ScoreDistributionInfo> scoreDistributionInfos = Lists.newArrayList();
      for (Map.Entry<Double, Long> scoreEntry : scoreDistributionMap.entrySet()) {
        Double score = scoreEntry.getKey();
        Long count = scoreEntry.getValue();
        if (score != null && score >= passScore) {
          evaluatorPassCount += count;
        }
        ScoreDistributionInfo scoreDistributionInfo = new ScoreDistributionInfo();
        scoreDistributionInfo.setScore(score == null ? "无" : String.valueOf(score));
        scoreDistributionInfo.setCount(count);
        scoreDistributionInfo.setPercentage(count * 100.0 / total);
        scoreDistributionInfos.add(scoreDistributionInfo);
      }
      scoreDistributionInfos.sort(Comparator.comparingLong(ScoreDistributionInfo::getCount));
      evaluatorScoreDistributionInfos.put(evaluatorInfos.get(evaluatorVersionId), scoreDistributionInfos);
      aggregateEvaluatorInfo.setPassRate(computePercentage(evaluatorPassCount, total));
    }
  }

  private void computeItemScoreDistribution(TurnEvaluatorOutputDTO evaluatorOutput, Map<Long, Map<Double, Long>> scoreDistributionMaps) {
    if (evaluatorOutput == null) {
      return;
    }
    Map<Long, EvaluatorRecordDTO> evaluatorRecords = evaluatorOutput.getEvaluatorRecords();
    if (MapUtils.isEmpty(evaluatorRecords)) {
      return;
    }
    for (Map.Entry<Long, EvaluatorRecordDTO> entry : evaluatorRecords.entrySet()) {
      Long key = entry.getKey();
      Map<Double, Long> scoreDistributionMap = scoreDistributionMaps.get(key);

      EvaluatorRecordDTO evaluatorRecordDTO = entry.getValue();
      if (evaluatorRecordDTO == null) {
        scoreDistributionMap.putIfAbsent(null, 0L);
        scoreDistributionMap.put(null, scoreDistributionMap.get(null) + 1);
        continue;
      }
      EvaluatorOutputDataDTO evaluatorOutputData = evaluatorRecordDTO.getEvaluatorOutputData();
      if (evaluatorOutputData == null || evaluatorOutputData.getEvaluatorResult() == null) {
        scoreDistributionMap.putIfAbsent(null, 0L);
        scoreDistributionMap.put(null, scoreDistributionMap.get(null) + 1);
        continue;
      }
      EvaluatorResultDTO evaluatorResult = evaluatorOutputData.getEvaluatorResult();
      Double score = evaluatorResult.getScore();
      if (score == null) {
        scoreDistributionMap.putIfAbsent(null, 0L);
        scoreDistributionMap.put(null, scoreDistributionMap.get(null) + 1);
        continue;
      }
      scoreDistributionMap.putIfAbsent(score, 0L);
      scoreDistributionMap.put(score, scoreDistributionMap.get(score) + 1);
    }
  }

  private void convertEvalSet(TurnEvalSetDTO evalSet, Map<String, String> evalSetMap) {
    if (evalSet.getTurn() != null) {
      List<FieldDataDTO> fieldDataList = evalSet.getTurn().getFieldDataList();
      for (FieldDataDTO fieldDataDTO : fieldDataList) {
        String name = fieldDataDTO.getName();
        ContentDTO content = fieldDataDTO.getContent();
        String text = content != null ? content.getText() : "-";
        evalSetMap.put(name, text);
      }
    }
  }

  private void convertTargetOutput(TurnTargetOutputDTO targetOutput, ItemDetailInfo itemDetailInfo, ItemInfo itemInfo) {
    itemDetailInfo.setEvalTargetOutput("-");
    itemInfo.setTotalCost("-");
    if (targetOutput == null) {
      return;
    }
    EvalTargetRecordDTO evalTargetRecord = targetOutput.getEvalTargetRecord();
    if (evalTargetRecord == null) {
      return;
    }
    EvalTargetOutputDataDTO evalTargetOutputData = evalTargetRecord.getEvalTargetOutputData();
    if (evalTargetOutputData == null) {
      return;
    }
    EvalTargetRunErrorDTO evalTargetRunError = evalTargetOutputData.getEvalTargetRunError();
    if (evalTargetRunError == null) {
      String actualOutput = getOutput(evalTargetOutputData);
      itemDetailInfo.setEvalTargetOutput(actualOutput);
    }
    else {
      itemDetailInfo.setEvalTargetOutput("-");
    }
    Long timeConsumingMs = evalTargetOutputData.getTimeConsumingMs();
    itemInfo.setTotalCost(timeConsumingMs + "ms");
  }

  private String getOutput(EvalTargetOutputDataDTO evalTargetOutputData) {
    Map<String, ContentDTO> outputFields = evalTargetOutputData.getOutputFields();
    if (outputFields == null) {
      return "-";
    }
    ContentDTO output = outputFields.get("actual_output");
    return output == null ? "-" : output.getText();
  }

  private void convertEvaluatorRecord(ExptResultData exptResultData, Map<Long, EvaluatorRecordDTO> evaluatorRecords, Map<String, Double> evaluatorScores, List<EvaluatorRecordInfo> evaluatorRecordInfos) {
    Map<Long, String> evaluatorInfos = exptResultData.getEvaluatorInfos();
    for (Map.Entry<Long, String> entry : evaluatorInfos.entrySet()) {
      Long key = entry.getKey();
      String evaluatorInfo = entry.getValue();
      if (evaluatorRecords == null || evaluatorRecords.get(key) == null) {
        evaluatorScores.put(evaluatorInfo, null);
        EvaluatorRecordInfo evaluatorRecordInfo = new EvaluatorRecordInfo();
        evaluatorRecordInfo.setEvaluator(evaluatorInfo);
        evaluatorRecordInfos.add(evaluatorRecordInfo);
      }
      else {
        EvaluatorOutputDataDTO evaluatorOutputData = getEvaluatorOutputData(evaluatorRecords, key);
        if (evaluatorOutputData == null || evaluatorOutputData.getEvaluatorResult() == null) {
          evaluatorScores.put(evaluatorInfo, null);
          EvaluatorRecordInfo evaluatorRecordInfo = new EvaluatorRecordInfo();
          evaluatorRecordInfo.setEvaluator(evaluatorInfo);
          evaluatorRecordInfos.add(evaluatorRecordInfo);
        }
        else {
          EvaluatorRunErrorDTO evaluatorRunError = evaluatorOutputData.getEvaluatorRunError();
          EvaluatorResultDTO evaluatorResult = evaluatorOutputData.getEvaluatorResult();
          Double score = evaluatorResult.getScore();
          String reasoning = evaluatorResult.getReasoning();
          Double passScore = getEvaluatorPassScore(exptResultData, key);
          evaluatorScores.put(evaluatorInfo, score);
          EvaluatorRecordInfo evaluatorRecordInfo = new EvaluatorRecordInfo();
          evaluatorRecordInfo.setEvaluator(evaluatorInfo);
          evaluatorRecordInfo.setReason(reasoning);
          evaluatorRecordInfo.setScore(score);
          evaluatorRecordInfo.setPassFlag(score != null && score >= passScore);
          evaluatorRecordInfo.setStatus(evaluatorRunError == null ? "成功" : "失败");
          evaluatorRecordInfos.add(evaluatorRecordInfo);
        }
      }
    }
  }

  private EvaluatorOutputDataDTO getEvaluatorOutputData(Map<Long, EvaluatorRecordDTO> evaluatorRecords, Long key) {
    EvaluatorRecordDTO evaluatorRecordDTO = evaluatorRecords.get(key);
    if (evaluatorRecordDTO == null) {
      return null;
    }
    return evaluatorRecordDTO.getEvaluatorOutputData();
  }

  private Double getEvaluatorPassScore(ExptResultData exptResultData, Long evaluatorKey) {
    Map<Long, Double> evaluatorPassScoreMap = exptResultData.getEvaluatorPassScoreMap();
    if (MapUtils.isEmpty(evaluatorPassScoreMap)) {
      return DEFAULT_PASS_SCORE;
    }
    return evaluatorPassScoreMap.get(evaluatorKey) == null ? DEFAULT_PASS_SCORE : evaluatorPassScoreMap.get(evaluatorKey);
  }


  private void convertAggregate(ExptResultData exptResultData, OpenPdfData openPdfData) {
    BatchGetExperimentAggrResultResponse response = exptResultData.getExperimentAggrResultResponse();
    List<ExptAggregateResultDTO> exptAggregateResults = response.getExptAggregateResults();
    if (CollectionUtils.isEmpty(exptAggregateResults)) {
      return;
    }
    ExptAggregateResultDTO exptAggregateResultDTO = exptAggregateResults.get(0);
    if (exptAggregateResultDTO == null) {
      return;
    }
    Map<Long, EvaluatorAggregateResultDTO> evaluatorResults = exptAggregateResultDTO.getEvaluatorResults();
    if (MapUtils.isEmpty(evaluatorResults)) {
      return;
    }
    for (Map.Entry<Long, EvaluatorAggregateResultDTO> entry : evaluatorResults.entrySet()) {
      EvaluatorAggregateResultDTO aggregateResultDTO = entry.getValue();
      convertAggregate(exptResultData, openPdfData, aggregateResultDTO);
    }
  }

  private void convertAggregate(ExptResultData exptResultData, OpenPdfData openPdfData, EvaluatorAggregateResultDTO aggregateResultDTO) {
    String name = aggregateResultDTO.getName();
    String version = aggregateResultDTO.getVersion();
    Long evaluatorVersionId = aggregateResultDTO.getEvaluatorVersionId();
    List<AggregatorResultDTO> aggregatorResults = aggregateResultDTO.getAggregatorResults();
    Map<AggregatorTypeDTO, AggregateDataDTO> map = new HashMap<>();
    for (AggregatorResultDTO aggregatorResult : aggregatorResults) {
      map.put(aggregatorResult.getAggregatorType(), aggregatorResult.getData());
    }
    Double average = getAggregatorValue(map, AggregatorTypeDTO.AVERAGE);
    Double max = getAggregatorValue(map, AggregatorTypeDTO.MAX);
    Double min = getAggregatorValue(map, AggregatorTypeDTO.MIN);
    Double sum = getAggregatorValue(map, AggregatorTypeDTO.SUM);

    Double passScore = getEvaluatorPassScore(exptResultData, evaluatorVersionId);

    AggregateEvaluatorInfo aggregateEvaluatorInfo = new AggregateEvaluatorInfo();
    aggregateEvaluatorInfo.setEvaluatorVersionId(evaluatorVersionId);
    aggregateEvaluatorInfo.setEvaluatorName(name);
    aggregateEvaluatorInfo.setEvaluatorVersion(version);
    aggregateEvaluatorInfo.setAverageScore(average);
    aggregateEvaluatorInfo.setMaxScore(max);
    aggregateEvaluatorInfo.setMinScore(min);
    aggregateEvaluatorInfo.setSumScore(sum);
    aggregateEvaluatorInfo.setPassScore(passScore);
    openPdfData.getAggregateEvaluatorInfos().add(aggregateEvaluatorInfo);
  }

  private double getAggregatorValue(Map<AggregatorTypeDTO, AggregateDataDTO> map, AggregatorTypeDTO aggregatorTypeDTO) {
    AggregateDataDTO aggregateDataDTO = map.get(aggregatorTypeDTO);
    if (aggregateDataDTO == null) {
      return 0;
    }
    return aggregateDataDTO.getValue();
  }

  private double computePercentage(long passCount, Long total) {
    if (total == null || total == 0) {
      return 0;
    }
    return passCount * 100.0 / total;
  }

  private ExperimentBaseInfo convertExperimentBaseInfo(ExptResultData exptResultData) {
    ExperimentDTO experiment = exptResultData.getExperiment();
    EvaluationSetDTO evalSet = experiment.getEvalSet();
    String evalSetName = getEvalSet(evalSet);
    EvalTargetDTO evalTarget = experiment.getEvalTarget();
    List<EvaluatorDTO> evaluators = experiment.getEvaluators();
    List<String> evaluatorNameVersions = Collections.emptyList();
    if (CollectionUtils.isNotEmpty(evaluators)) {
      evaluatorNameVersions = evaluators.stream().map(o -> String.join(":", o.getName(), o.getCurrentVersion().getVersion())).toList();
    }

    ExperimentBaseInfo experimentBaseInfo = new ExperimentBaseInfo();
    experimentBaseInfo.setEvalSet(evalSetName);
    handleEvalTargetContent(evalTarget, experimentBaseInfo);
    experimentBaseInfo.setEvaluatorNameVersions(evaluatorNameVersions);
    experimentBaseInfo.setCreatedBy(experiment.getCreatorByName());
    experimentBaseInfo.setCreatedAt(DateUtil.format(new Date(experiment.getStartTime()), DateUtil.DATETIME));
    experimentBaseInfo.setEndDate(DateUtil.format(new Date(experiment.getEndTime()), DateUtil.DATETIME));
    experimentBaseInfo.setDescription(experiment.getDesc());
    return experimentBaseInfo;
  }

  private String getEvalSet(EvaluationSetDTO evalSet) {
    String defaultValue = "-";
    if (evalSet == null) {
      return defaultValue;
    }
    EvaluationSetVersionDTO evaluationSetVersion = evalSet.getEvaluationSetVersion();
    if (evaluationSetVersion == null) {
      return defaultValue;
    }
    String name = evalSet.getName();
    String version = evaluationSetVersion.getVersion();
    return String.join(":", name, version);
  }

  private void handleEvalTargetContent(EvalTargetDTO evalTarget, ExperimentBaseInfo experimentBaseInfo) {
    experimentBaseInfo.setEvalTargetType("-");
    experimentBaseInfo.setEvalTarget("-");
    if (evalTarget == null) {
      return;
    }
    EvalTargetVersionDTO evalTargetVersion = evalTarget.getEvalTargetVersion();
    if (evalTargetVersion == null) {
      return;
    }
    EvalTargetTypeDTO evalTargetType = evalTarget.getEvalTargetType();
    EvalTargetContentDTO evalTargetContent = evalTarget.getEvalTargetVersion().getEvalTargetContent();
    if (evalTargetContent == null) {
      return;
    }
    if (EvalTargetTypeDTO.BOT.equals(evalTargetType)) {
      BotDTO bot = evalTargetContent.getBot();
      experimentBaseInfo.setEvalTargetType("智能体");
      if (bot == null) {
        return;
      }
      experimentBaseInfo.setEvalTarget(bot.getBotName());
    }
    else if (EvalTargetTypeDTO.WORKFLOW.equals(evalTargetType)) {
      WorkflowDTO workflow = evalTargetContent.getWorkflow();
      experimentBaseInfo.setEvalTargetType("工作流");
      if (workflow == null) {
        return;
      }
      experimentBaseInfo.setEvalTarget(workflow.getName() + ":" + workflow.getVersion());
    }
    else if (EvalTargetTypeDTO.LOOP_PROMPT.equals(evalTargetType)) {
      EvalPromptDTO prompt = evalTargetContent.getPrompt();
      experimentBaseInfo.setEvalTargetType("提示词");
      if (prompt == null) {
        return;
      }
      experimentBaseInfo.setEvalTarget(prompt.getName() + ":" + prompt.getVersion());
    }
  }

  private ReportInfo convertReportInfo(ExptResultData exptResultData) {
    String name = exptResultData.getExperiment().getName();
    ReportInfo reportInfo = new ReportInfo();
    reportInfo.setName(name);
    reportInfo.setDateTime(DateUtil.format());
    return reportInfo;
  }

}
