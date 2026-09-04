package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggrResultDataType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggregateData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggregatorResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggregatorType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Experiment;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggrResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptEvaluatorRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnEvaluatorResultRef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.IEvaluatorVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.PromptEvaluatorVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ScoreDistributionData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ScoreDistributionItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UpdateExptAggrResultParam;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExperimentRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptAggrResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorRecordService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluatorService;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ExptAggrResultService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 实验聚合结果服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/expt_result_aggr_impl.go
 * - 功能: 实验聚合结果业务逻辑实现
 * - 主要方法:
 * * createExptAggrResult - 创建实验聚合结果
 * * updateExptAggrResult - 更新实验聚合结果
 * * batchGetExptAggrResultByExperimentIDs - 批量获取实验聚合结果
 * <p>
 * Java实现说明:
 * - 对应Go的ExptAggrResultServiceImpl结构体
 * - 使用Spring Service注解
 * - 支持聚合器模式
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go map[int64]*entity.EvaluatorRecord -> Java Map<Long, EvaluatorRecord>
 * - Go json.Marshal/Unmarshal -> Java ObjectMapper
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ExptAggrResultServiceImpl implements ExptAggrResultService {
  private static final Logger logger = LoggerFactory.getLogger(ExptAggrResultServiceImpl.class);

  private final IExptTurnResultRepo exptTurnResultRepo;
  private final IExptAggrResultRepo exptAggrResultRepo;
  private final IExperimentRepo experimentRepo;
  private final EvaluatorService evaluatorService;
  private final EvaluatorRecordService evaluatorRecordService;

  @Override
  public void createExptAggrResult(Long spaceId, Long experimentId) {
    try {
      List<ExptTurnEvaluatorResultRef> turnEvaluatorResultRefs = exptTurnResultRepo.getTurnEvaluatorResultRefByExptId(spaceId, experimentId);
      if (turnEvaluatorResultRefs.isEmpty()) {
        logger.info("no evaluator result found, skip create expt aggr result");
        return;
      }
      List<Long> evaluatorResultIDs = new ArrayList<>();
      Map<Long, List<Long>> evaluatorVersionID2ResultIDs = new HashMap<>();
      for (ExptTurnEvaluatorResultRef ref : turnEvaluatorResultRefs) {
        evaluatorResultIDs.add(ref.getEvaluatorResultId());
        evaluatorVersionID2ResultIDs.computeIfAbsent(ref.getEvaluatorVersionId(), k -> new ArrayList<>()).add(ref.getEvaluatorResultId());
      }
      List<EvaluatorRecord> evaluatorRecords = evaluatorRecordService.batchGetEvaluatorRecord(evaluatorResultIDs, false);
      Map<Long, EvaluatorRecord> recordMap = evaluatorRecords.stream().collect(Collectors.toMap(EvaluatorRecord::getId, record -> record));
      Map<Long, AggregatorGroup> evaluatorVersionID2AggregatorGroup = new HashMap<>();
      for (Map.Entry<Long, List<Long>> entry : evaluatorVersionID2ResultIDs.entrySet()) {
        Long evaluatorVersionID = entry.getKey();
        List<Long> resultIDs = entry.getValue();
        AggregatorGroup aggregatorGroup = newAggregatorGroup(withScoreDistributionAggregator());
        evaluatorVersionID2AggregatorGroup.put(evaluatorVersionID, aggregatorGroup);
        for (Long resultID : resultIDs) {
          EvaluatorRecord evalResult = recordMap.get(resultID);
          if (evalResult == null || evalResult.getEvaluatorOutputData() == null || evalResult.getEvaluatorOutputData().getEvaluatorResult() == null || evalResult.getEvaluatorOutputData().getEvaluatorResult().getScore() == null) {
            continue;
          }
          aggregatorGroup.append(evalResult.getEvaluatorOutputData().getEvaluatorResult().getScore());
        }
      }
      createExptAggrResult(spaceId, experimentId, evaluatorVersionID2AggregatorGroup);
    }
    catch (Exception e) {
      throw new BssException("创建实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  private void createExptAggrResult(Long spaceId, Long experimentId, Map<Long, AggregatorGroup> evaluatorVersionID2AggregatorGroup) {
    List<ExptAggrResult> exptAggrResults = new ArrayList<>();
    for (Map.Entry<Long, AggregatorGroup> entry : evaluatorVersionID2AggregatorGroup.entrySet()) {
      Long evaluatorVersionID = entry.getKey();
      AggregatorGroup aggregatorGroup = entry.getValue();
      AggregateResult aggrResult = aggregatorGroup.result();
      double averageScore = 0.0;
      for (AggregatorResult aggregatorResult : aggrResult.getAggregatorResults()) {
        if (aggregatorResult.getAggregatorType() == AggregatorType.AVERAGE) {
          averageScore = aggregatorResult.getScore();
          break;
        }
      }
      exptAggrResults.add(ExptAggrResult.builder().spaceId(spaceId).experimentId(experimentId).fieldType(FieldType.EVALUATOR_SCORE.getValue()).fieldKey(String.valueOf(evaluatorVersionID)).score(averageScore).aggrResult(JsonUtil.toJsonString(aggrResult)).version(0L).status(1).build());
    }
    exptAggrResultRepo.batchCreateExptAggrResult(exptAggrResults);
  }

  @Override
  public void updateExptAggrResult(UpdateExptAggrResultParam param) {
    try {
      if (param.getFieldType() != FieldType.EVALUATOR_SCORE) {
        throw new BssException("INVALID_PARAM", "invalid field type");
      }
      // 如果首次计算尚未完成 返回error mq重试
      try {
        exptAggrResultRepo.getExptAggrResult(param.getExperimentId(), FieldType.EVALUATOR_SCORE.getValue(), param.getFieldKey());
      }
      catch (BssException e) {
        if ("RESOURCE_NOT_FOUND".equals(e.getFailCode())) {
          Experiment experiment = experimentRepo.getById(param.getExperimentId(), param.getSpaceId());
          // 如果实验未结束 不进行MQ重试
          if (!isExptFinished(experiment.getStatus())) {
            return;
          }
        }
        throw e;
      }
      // 计算前先更新版本号
      Long version = exptAggrResultRepo.updateAndGetLatestVersion(param.getExperimentId(), param.getFieldType().getValue(), param.getFieldKey());
      Long evaluatorVersionID = Long.parseLong(param.getFieldKey());
      List<ExptTurnEvaluatorResultRef> turnEvaluatorResultRefs = exptTurnResultRepo.getTurnEvaluatorResultRefByEvaluatorVersionId(param.getSpaceId(), param.getExperimentId(), evaluatorVersionID);
      List<Long> evaluatorResultIDs = turnEvaluatorResultRefs.stream().map(ExptTurnEvaluatorResultRef::getEvaluatorResultId).collect(Collectors.toList());
      List<EvaluatorRecord> evaluatorRecords = evaluatorRecordService.batchGetEvaluatorRecord(evaluatorResultIDs, false);
      Map<Long, EvaluatorRecord> recordMap = evaluatorRecords.stream().collect(Collectors.toMap(EvaluatorRecord::getId, record -> record));
      AggregatorGroup aggregatorGroup = newAggregatorGroup(withScoreDistributionAggregator());
      for (EvaluatorRecord evalResult : recordMap.values()) {
        if (evalResult.getEvaluatorOutputData() == null || evalResult.getEvaluatorOutputData().getEvaluatorResult() == null) {
          continue;
        }
        double score = evalResult.getEvaluatorOutputData().getEvaluatorResult().getScore();
        if (evalResult.getEvaluatorOutputData().getEvaluatorResult().getCorrection() != null) {
          score = evalResult.getEvaluatorOutputData().getEvaluatorResult().getCorrection().getScore();
        }
        aggregatorGroup.append(score);
      }
      updateExptAggrResult(param, evaluatorVersionID, aggregatorGroup, version);
    }
    catch (Exception e) {
      throw new BssException("更新实验聚合结果失败: " + e.getMessage(), e);
    }
  }

  private void updateExptAggrResult(UpdateExptAggrResultParam param, Long evaluatorVersionID, AggregatorGroup aggregatorGroup, Long version) {
    AggregateResult aggrResult = aggregatorGroup.result();
    double averageScore = 0.0;
    for (AggregatorResult aggregatorResult : aggrResult.getAggregatorResults()) {
      if (aggregatorResult.getAggregatorType() == AggregatorType.AVERAGE) {
        averageScore = aggregatorResult.getScore();
        break;
      }
    }
    ExptAggrResult exptAggrResult = ExptAggrResult.builder().spaceId(param.getSpaceId()).experimentId(param.getExperimentId()).fieldType(FieldType.EVALUATOR_SCORE.getValue()).fieldKey(String.valueOf(evaluatorVersionID)).score(averageScore).aggrResult(JsonUtil.toJsonString(aggrResult)).build();
    exptAggrResultRepo.updateExptAggrResultByVersion(exptAggrResult, version);
    logger.info("update expt aggr result success, exptID: {}", param.getExperimentId());
  }

  @Override
  public List<ExptAggregateResult> batchGetExptAggrResultByExperimentIds(Long spaceId, List<Long> exptIds) {
    List<ExptAggrResult> aggrResults = exptAggrResultRepo.batchGetExptAggrResultByExperimentIds(exptIds);
    Map<Long, List<ExptAggrResult>> expt2AggrResults = groupAggrResultsByExperiment(aggrResults);
    Map<Long, Evaluator> versionID2Evaluator = buildEvaluatorVersionMap(exptIds, spaceId);

    return buildExptAggregateResults(expt2AggrResults, versionID2Evaluator);
  }

  private Map<Long, List<ExptAggrResult>> groupAggrResultsByExperiment(List<ExptAggrResult> aggrResults) {
    return aggrResults.stream().collect(Collectors.groupingBy(ExptAggrResult::getExperimentId));
  }

  private Map<Long, Evaluator> buildEvaluatorVersionMap(List<Long> exptIds, Long spaceId) {
    List<ExptEvaluatorRef> evaluatorRef = experimentRepo.getEvaluatorRefByExptIds(exptIds, spaceId);
    List<Long> evaluatorVersionIDs = extractUniqueEvaluatorVersionIds(evaluatorRef);
    List<Evaluator> evaluatorVersionList = evaluatorService.batchGetEvaluatorVersion(spaceId, evaluatorVersionIDs, true);
    return buildVersionIdToEvaluatorMap(evaluatorVersionList, evaluatorVersionIDs);
  }

  private List<Long> extractUniqueEvaluatorVersionIds(List<ExptEvaluatorRef> evaluatorRef) {
    Set<Long> evaluatorVersionIDSet = new HashSet<>();
    for (ExptEvaluatorRef ref : evaluatorRef) {
      evaluatorVersionIDSet.add(ref.getEvaluatorVersionId());
    }
    return new ArrayList<>(evaluatorVersionIDSet);
  }

  private Map<Long, Evaluator> buildVersionIdToEvaluatorMap(List<Evaluator> evaluatorVersionList, List<Long> evaluatorVersionIDs) {
    Map<Long, Evaluator> versionID2Evaluator = new HashMap<>();
    for (Evaluator evaluator : evaluatorVersionList) {
      IEvaluatorVersion evaluatorVersion = evaluator.getEvaluatorVersion();
      if (evaluatorVersion != null && evaluatorVersionIDs.contains(evaluatorVersion.getId())) {
        versionID2Evaluator.put(evaluatorVersion.getId(), evaluator);
      }
    }
    return versionID2Evaluator;
  }

  private List<ExptAggregateResult> buildExptAggregateResults(Map<Long, List<ExptAggrResult>> expt2AggrResults,
                                                              Map<Long, Evaluator> versionID2Evaluator) {
    List<ExptAggregateResult> results = new ArrayList<>();
    for (Map.Entry<Long, List<ExptAggrResult>> entry : expt2AggrResults.entrySet()) {
      Long exptID = entry.getKey();
      List<ExptAggrResult> exptResult = entry.getValue();
      Map<Long, EvaluatorAggregateResult> evaluatorResults = buildEvaluatorResults(exptResult, versionID2Evaluator);
      results.add(ExptAggregateResult.builder().experimentId(exptID).evaluatorResults(evaluatorResults).status(1).build());
    }
    return results;
  }

  private Map<Long, EvaluatorAggregateResult> buildEvaluatorResults(List<ExptAggrResult> exptResult,
                                                                    Map<Long, Evaluator> versionID2Evaluator) {
    Map<Long, EvaluatorAggregateResult> evaluatorResults = new HashMap<>();
    for (ExptAggrResult fieldResult : exptResult) {
      if (fieldResult.getFieldType() == FieldType.EVALUATOR_SCORE.getValue()) {
        processEvaluatorScoreResult(fieldResult, versionID2Evaluator, evaluatorResults);
      }
    }
    return evaluatorResults;
  }

  private void processEvaluatorScoreResult(ExptAggrResult fieldResult, Map<Long, Evaluator> versionID2Evaluator,
                                           Map<Long, EvaluatorAggregateResult> evaluatorResults) {
    Long evaluatorVersionID = Long.parseLong(fieldResult.getFieldKey());
    AggregateResult aggregateResultDO = JsonUtil.parseJsonRequired(fieldResult.getAggrResult(), AggregateResult.class);
    Evaluator evaluator = getEvaluatorByVersionId(versionID2Evaluator, evaluatorVersionID);
    EvaluatorAggregateResult evaluatorAggrResult = createEvaluatorAggregateResult(evaluatorVersionID, aggregateResultDO, evaluator);
    evaluatorResults.put(evaluatorVersionID, evaluatorAggrResult);
  }

  private Evaluator getEvaluatorByVersionId(Map<Long, Evaluator> versionID2Evaluator, Long evaluatorVersionID) {
    Evaluator evaluator = versionID2Evaluator.get(evaluatorVersionID);
    if (evaluator == null) {
      throw new BssException("failed to get evaluator by version_id " + evaluatorVersionID);
    }
    return evaluator;
  }

  private EvaluatorAggregateResult createEvaluatorAggregateResult(Long evaluatorVersionID, AggregateResult aggregateResultDO, Evaluator evaluator) {
    PromptEvaluatorVersion evaluatorVersion = evaluator.getPromptEvaluatorVersion();
    if (evaluatorVersion == null) {
      throw new BssException("failed to get evaluator version by version_id " + evaluatorVersionID);
    }
    return EvaluatorAggregateResult.builder()
      .evaluatorVersionId(evaluatorVersionID)
      .aggregatorResults(aggregateResultDO.getAggregatorResults())
      .name(evaluator.getName())
      .version(evaluatorVersion.getVersion())
      .build();
  }

  private boolean isExptFinished(ExptStatus status) {
    return ExptStatus.isExptFinished(status);
  }


  public static AggregatorGroup newAggregatorGroup(NewAggregatorGroupOption... opts) {
    AggregatorGroup group = new AggregatorGroup();
    group.aggregators.add(new BasicAggregator());
    for (NewAggregatorGroupOption opt : opts) {
      opt.apply(group);
    }
    return group;
  }

  public static NewAggregatorGroupOption withScoreDistributionAggregator() {
    return group -> group.aggregators.add(new ScoreDistributionAggregator());
  }


  // 获取出现次数最高的前 N 个分数
  public static List<ScoreCount> getTopNScores(Map<Double, Long> score2Count, int n) {
    List<ScoreCount> scoreCounts = score2Count.entrySet().stream().map(entry -> new ScoreCount(String.format("%.2f", entry.getKey()), entry.getValue())).sorted((a, b) -> Long.compare(b.getCount(), a.getCount())).collect(Collectors.toList());
    if (scoreCounts.size() > n) {
      long aggregatedCount = scoreCounts.subList(n, scoreCounts.size()).stream().mapToLong(ScoreCount::getCount).sum();
      scoreCounts = new ArrayList<>(scoreCounts.subList(0, n));
      scoreCounts.add(new ScoreCount("其他", aggregatedCount));
    }
    return scoreCounts;
  }

  // 基础聚合器
  public static class BasicAggregator implements Aggregator {
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;
    private double sum = 0.0;
    private int count = 0;

    @Override
    public void append(double score) {
      count++;
      if (count == 1) {
        min = score;
        max = score;
        sum = score;
        return;
      }
      if (score < min) {
        min = score;
      }
      if (score > max) {
        max = score;
      }
      sum += score;
    }

    @Override
    public Map<AggregatorType, AggregateData> result() {
      Map<AggregatorType, AggregateData> res = new HashMap<>();
      double avg = count != 0 ? sum / count : 0.0;
      res.put(AggregatorType.AVERAGE, AggregateData.builder().value(avg).dataType(AggrResultDataType.DOUBLE).build());
      res.put(AggregatorType.SUM, AggregateData.builder().value(sum).dataType(AggrResultDataType.DOUBLE).build());
      res.put(AggregatorType.MAX, AggregateData.builder().value(max).dataType(AggrResultDataType.DOUBLE).build());
      res.put(AggregatorType.MIN, AggregateData.builder().value(min).dataType(AggrResultDataType.DOUBLE).build());
      return res;
    }
  }

  // 分数分布聚合器
  public static class ScoreDistributionAggregator implements Aggregator {
    private final Map<Double, Long> score2Count = new HashMap<>();
    private long total = 0;

    @Override
    public void append(double score) {
      score2Count.put(score, score2Count.getOrDefault(score, 0L) + 1);
      total++;
    }

    @Override
    public Map<AggregatorType, AggregateData> result() {
      final int topN = 5;
      List<ScoreCount> scoreCounts = getTopNScores(score2Count, topN);
      List<ScoreDistributionItem> items = scoreCounts.stream().map(sc -> ScoreDistributionItem.builder().score(sc.getScore()).count(sc.getCount()).percentage((double) sc.getCount() / total).build()).collect(Collectors.toList());
      AggregateData data = AggregateData.builder().dataType(AggrResultDataType.SCORE_DISTRIBUTION).scoreDistribution(ScoreDistributionData.builder().scoreDistributionItems(items).build()).build();
      return Map.of(AggregatorType.DISTRIBUTION, data);
    }
  }

  @Getter
  public static class ScoreCount {
    private final String score;
    private final long count;

    public ScoreCount(String score, long count) {
      this.score = score;
      this.count = count;
    }
  }

  // 聚合器组
  public static class AggregatorGroup {
    private final List<Aggregator> aggregators;

    public AggregatorGroup() {
      this.aggregators = new ArrayList<>();
    }

    public void append(double score) {
      for (Aggregator aggregator : aggregators) {
        aggregator.append(score);
      }
    }

    public AggregateResult result() {
      List<AggregatorResult> aggregatorResults = new ArrayList<>();
      for (Aggregator aggregator : aggregators) {
        for (Map.Entry<AggregatorType, AggregateData> entry : aggregator.result().entrySet()) {
          AggregatorResult aggregatorResult = AggregatorResult.builder().aggregatorType(entry.getKey()).data(entry.getValue()).build();
          aggregatorResults.add(aggregatorResult);
        }
      }
      return AggregateResult.builder().aggregatorResults(aggregatorResults).build();
    }
  }

  @FunctionalInterface
  public interface NewAggregatorGroupOption {
    void apply(AggregatorGroup aggregatorGroup);
  }

  // 聚合器接口
  public interface Aggregator {
    void append(double score);

    Map<AggregatorType, AggregateData> result();
  }
}
