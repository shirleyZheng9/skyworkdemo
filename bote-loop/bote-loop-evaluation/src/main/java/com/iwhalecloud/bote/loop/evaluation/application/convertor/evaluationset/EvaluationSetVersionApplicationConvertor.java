package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估集版本应用转换器
 * 对应Go: evaluation_set/evaluation_set_version.go
 */
public final class EvaluationSetVersionApplicationConvertor {
  private EvaluationSetVersionApplicationConvertor() {
  }

  /**
   * 版本DO列表转DTO列表
   * 对应Go: VersionDO2DTOs
   */
  public static List<EvaluationSetVersionDTO> convertVersionDO2DTOs(List<EvaluationSetVersion> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetVersionApplicationConvertor::convertVersionDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 版本DO转DTO
   * 对应Go: VersionDO2DTO
   */
  public static EvaluationSetVersionDTO convertVersionDO2DTO(EvaluationSetVersion doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluationSetVersionDTO.builder()
      .id(doEntity.getId())
      .appId(doEntity.getAppId())
      .workspaceId(doEntity.getSpaceId())
      .evaluationSetId(doEntity.getEvaluationSetId())
      .version(doEntity.getVersion())
      .versionNum(doEntity.getVersionNum())
      .description(doEntity.getDescription())
      .evaluationSetSchema(EvaluationSetSchemaApplicationConvertor.convertSchemaDO2DTO(doEntity.getEvaluationSetSchema()))
      .itemCount(doEntity.getItemCount())
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .build();
  }
}
