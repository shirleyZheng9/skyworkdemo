package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.MultiModalSpecDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetSchemaDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldDisplayFormat;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.MultiModalSpec;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估集模式应用转换器
 * 对应Go: evaluation_set/evaluation_set_schema.go
 */
public final class EvaluationSetSchemaApplicationConvertor {

  private EvaluationSetSchemaApplicationConvertor() {
  }

  /**
   * 模式DTO转DO
   * 对应Go: SchemaDTO2DO
   */
  public static EvaluationSetSchema convertSchemaDTO2DO(EvaluationSetSchemaDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluationSetSchema.builder()
      .id(dto.getId())
      .appId(dto.getAppId())
      .spaceId(dto.getWorkspaceId())
      .evaluationSetId(dto.getEvaluationSetId())
      .fieldSchemas(convertFieldSchemaDTO2DOs(dto.getFieldSchemas()))
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(dto.getBaseInfo()))
      .build();
  }

  /**
   * 字段模式DTO列表转DO列表
   * 对应Go: FieldSchemaDTO2DOs
   */
  public static List<FieldSchema> convertFieldSchemaDTO2DOs(List<FieldSchemaDTO> dtos) {
    if (dtos == null) {
      return null;
    }
    return dtos.stream()
      .map(EvaluationSetSchemaApplicationConvertor::convertFieldSchemaDTO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 字段模式DTO转DO
   * 对应Go: FieldSchemaDTO2DO
   */
  public static FieldSchema convertFieldSchemaDTO2DO(FieldSchemaDTO dto) {
    if (dto == null) {
      return null;
    }

    MultiModalSpec multiModelSpec = null;
    if (dto.getMultiModelSpec() != null) {
      multiModelSpec = MultiModalSpec.builder()
        .maxFileCount(dto.getMultiModelSpec().getMaxFileCount())
        .maxFileSize(dto.getMultiModelSpec().getMaxFileSize())
        .supportedFormats(dto.getMultiModelSpec().getSupportedFormats())
        .build();
    }

    return FieldSchema.builder()
      .key(dto.getKey())
      .name(dto.getName())
      .description(dto.getDescription())
      .contentType(CommonConvertor.convertContentTypeDTO2DO(dto.getContentType().getValue()))
      .defaultDisplayFormat(FieldDisplayFormat.fromValue(dto.getDefaultDisplayFormat().getDescription()))
      .status(FieldStatus.fromString(dto.getStatus().getDescription()))
      .textSchema(dto.getTextSchema())
      .multiModelSpec(multiModelSpec)
      .hidden(dto.getHidden())
      .isRequired(dto.getIsRequired())
      .defaultTransformations(dto.getDefaultTransformations())
      .build();
  }

  /**
   * 模式DO转DTO
   * 对应Go: SchemaDO2DTO
   */
  public static EvaluationSetSchemaDTO convertSchemaDO2DTO(EvaluationSetSchema doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluationSetSchemaDTO.builder()
      .id(doEntity.getId())
      .appId(doEntity.getAppId())
      .workspaceId(doEntity.getSpaceId())
      .evaluationSetId(doEntity.getEvaluationSetId())
      .fieldSchemas(convertFieldSchemaDO2DTOs(doEntity.getFieldSchemas()))
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .build();
  }

  /**
   * 字段模式DO列表转DTO列表
   * 对应Go: FieldSchemaDO2DTOs
   */
  public static List<FieldSchemaDTO> convertFieldSchemaDO2DTOs(List<FieldSchema> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetSchemaApplicationConvertor::convertFieldSchemaDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 字段模式DO转DTO
   * 对应Go: FieldSchemaDO2DTO
   */
  public static FieldSchemaDTO convertFieldSchemaDO2DTO(FieldSchema doEntity) {
    if (doEntity == null) {
      return null;
    }

    MultiModalSpecDTO multiModelSpec = null;
    if (doEntity.getMultiModelSpec() != null) {
      multiModelSpec = MultiModalSpecDTO.builder()
        .maxFileCount(doEntity.getMultiModelSpec().getMaxFileCount())
        .maxFileSize(doEntity.getMultiModelSpec().getMaxFileSize())
        .supportedFormats(doEntity.getMultiModelSpec().getSupportedFormats())
        .build();
    }

    return FieldSchemaDTO.builder()
      .key(doEntity.getKey())
      .name(doEntity.getName())
      .description(doEntity.getDescription())
      .contentType(ContentTypeDTO.fromValue(CommonConvertor.convertContentTypeDO2DTO(doEntity.getContentType())))
      .defaultDisplayFormat(FieldDisplayFormatDTO.fromString(doEntity.getDefaultDisplayFormat().getValue()))
      .status(FieldStatusDTO.fromString(doEntity.getStatus().getDescription()))
      .textSchema(doEntity.getTextSchema())
      .multiModelSpec(multiModelSpec)
      .hidden(doEntity.getHidden())
      .isRequired(doEntity.getIsRequired())
      .defaultTransformations(doEntity.getDefaultTransformations())
      .build();
  }
}
