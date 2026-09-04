package com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment;

import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.AggregateDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.AggregatorResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.AggregatorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.DataTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptAggregateCalculateStatusDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptAggregateResultDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ScoreDistributionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ScoreDistributionItemDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggregateData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.AggregatorResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptAggregateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ScoreDistributionItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实验聚合结果转换器
 * 对应Go: experiment/aggr_result.go
 */
public final class ExperimentAggrResultApplicationConvertor {
  private ExperimentAggrResultApplicationConvertor() {
  }

  /**
   * 实验聚合结果DO转DTO
   * 对应Go: ExptAggregateResultDOToDTO
   */
  public static ExptAggregateResultDTO convertDOToDTO(ExptAggregateResult data) {
    if (data == null) {
      return null;
    }

    Map<Long, EvaluatorAggregateResultDTO> evaluatorResults = data.getEvaluatorResults().entrySet().stream()
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        entry -> convertEvaluatorResultsDOToDTO(entry.getValue())
      ));

    return ExptAggregateResultDTO.builder()
      .experimentId(data.getExperimentId())
      .evaluatorResults(evaluatorResults)
      .status(ExptAggregateCalculateStatusDTO.fromValue(data.getStatus()))
      .build();
  }

  /**
   * 评估器聚合结果DO转DTO
   * 对应Go: EvaluatorResultsDOToDTO
   */
  public static EvaluatorAggregateResultDTO convertEvaluatorResultsDOToDTO(EvaluatorAggregateResult result) {
    if (result == null) {
      return null;
    }

    return EvaluatorAggregateResultDTO.builder()
      .evaluatorVersionId(result.getEvaluatorVersionId())
      .aggregatorResults(convertAggregatorResultDOsToDTOs(result.getAggregatorResults()))
      .name(result.getName())
      .version(result.getVersion())
      .build();
  }

  /**
   * 聚合器结果DO列表转DTO列表
   * 对应Go: AggregatorResultDOsToDTOs
   */
  public static List<AggregatorResultDTO> convertAggregatorResultDOsToDTOs(List<AggregatorResult> result) {
    if (result == null || result.isEmpty()) {
      return null;
    }
    return result.stream()
      .map(ExperimentAggrResultApplicationConvertor::convertAggregatorResultDOToDTO)
      .collect(Collectors.toList());
  }

  /**
   * 聚合器结果DO转DTO
   * 对应Go: AggregatorResultDOToDTO
   */
  public static AggregatorResultDTO convertAggregatorResultDOToDTO(AggregatorResult result) {
    if (result == null) {
      return null;
    }

    return AggregatorResultDTO.builder()
      .aggregatorType(AggregatorTypeDTO.fromValue(result.getAggregatorType().getValue()))
      .data(convertAggregateDataDOToDTO(result.getData()))
      .build();
  }

  /**
   * 聚合数据DO转DTO
   * 对应Go: AggregateDataDOToDTO
   */
  public static AggregateDataDTO convertAggregateDataDOToDTO(AggregateData data) {
    if (data == null) {
      return null;
    }

    AggregateDataDTO.AggregateDataDTOBuilder builder = AggregateDataDTO.builder()
      .dataType(DataTypeDTO.fromValue(data.getDataType().getValue()));

    // 值四舍五入到小数点后两位
    if (data.getValue() != null) {
      BigDecimal roundedValue = new BigDecimal(data.getValue())
        .setScale(2, RoundingMode.HALF_UP);
      builder.value(roundedValue.doubleValue());
    }

    if (data.getScoreDistribution() != null) {
      builder.scoreDistribution(ScoreDistributionDTO.builder()
        .scoreDistributionItems(convertScoreDistributionItemsDOToDTO(data.getScoreDistribution().getScoreDistributionItems()))
        .build());
    }

    return builder.build();
  }

  /**
   * 分数分布项DO列表转DTO列表
   * 对应Go: ScoreDistributionItemsDOToDTO
   */
  public static List<ScoreDistributionItemDTO> convertScoreDistributionItemsDOToDTO(List<ScoreDistributionItem> data) {
    if (data == null || data.isEmpty()) {
      return null;
    }

    return data.stream()
      .filter(item -> item != null)
      .map(item -> ScoreDistributionItemDTO.builder()
        .score(item.getScore())
        .count(item.getCount())
        .percentage(item.getPercentage())
        .build())
      .collect(Collectors.toList());
  }
}
