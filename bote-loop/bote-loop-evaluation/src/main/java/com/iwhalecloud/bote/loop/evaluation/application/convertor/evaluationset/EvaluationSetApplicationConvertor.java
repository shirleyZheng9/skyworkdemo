package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetFeaturesDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetStatusDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估集应用转换器
 * 对应Go: evaluation_set/evaluation_set.go
 */
public final class EvaluationSetApplicationConvertor {
  private EvaluationSetApplicationConvertor() {

  }
  /**
   * DO列表转DTO列表
   * 对应Go: EvaluationSetDO2DTOs
   */
  public static List<EvaluationSetDTO> convertDO2DTOs(List<EvaluationSet> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetApplicationConvertor::convertDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * DO转DTO
   * 对应Go: EvaluationSetDO2DTO
   */
  public static EvaluationSetDTO convertDO2DTO(EvaluationSet doEntity) {
    if (doEntity == null) {
      return null;
    }

    DatasetSpecDTO spec = null;
    if (doEntity.getSpec() != null) {
      spec = DatasetSpecDTO.builder()
        .maxItemCount(doEntity.getSpec().getMaxItemCount())
        .maxFieldCount(doEntity.getSpec().getMaxFieldCount())
        .maxItemSize(doEntity.getSpec().getMaxItemSize())
        .maxItemDataNestedDepth(doEntity.getSpec().getMaxItemDataNestedDepth())
        .build();
    }

    DatasetFeaturesDTO features = null;
    if (doEntity.getFeatures() != null) {
      features = DatasetFeaturesDTO.builder()
        .editSchema(doEntity.getFeatures().getEditSchema())
        .repeatedData(doEntity.getFeatures().getRepeatedData())
        .multiModal(doEntity.getFeatures().getMultiModal())
        .build();
    }

    return EvaluationSetDTO.builder()
      .id(doEntity.getId())
      .appId(doEntity.getAppId())
      .workspaceId(doEntity.getSpaceId())
      .name(doEntity.getName())
      .description(doEntity.getDescription())
      .status(DatasetStatusDTO.fromValue(doEntity.getStatus().getValue()))
      .spec(spec)
      .features(features)
      .itemCount(doEntity.getItemCount())
      .changeUncommitted(doEntity.getChangeUncommitted())
      .evaluationSetVersion(EvaluationSetVersionApplicationConvertor.convertVersionDO2DTO(doEntity.getEvaluationSetVersion()))
      .latestVersion(doEntity.getLatestVersion())
      .nextVersionNum(doEntity.getNextVersionNum())
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .catalogItemId(doEntity.getCatalogItemId())
      .build();
  }
}
