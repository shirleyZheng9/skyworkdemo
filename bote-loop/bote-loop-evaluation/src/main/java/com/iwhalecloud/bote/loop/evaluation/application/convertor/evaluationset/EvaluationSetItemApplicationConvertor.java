package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluationset;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorDetailDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.FieldDataDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.TurnDTO;
import com.iwhalecloud.bote.loop.evaluation.application.convertor.common.CommonConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorDetail;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemErrorGroup;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估集数据项应用转换器
 * 对应Go: evaluation_set/evaluation_set_item.go
 */
public final class EvaluationSetItemApplicationConvertor {
  private EvaluationSetItemApplicationConvertor() {
  }

  /**
   * 数据项DTO列表转DO列表
   * 对应Go: ItemDTO2DOs
   */
  public static List<EvaluationSetItem> convertItemDTO2DOs(List<EvaluationSetItemDTO> dtos) {
    if (dtos == null) {
      return null;
    }
    return dtos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertItemDTO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 数据项DTO转DO
   * 对应Go: ItemDTO2DO
   */
  public static EvaluationSetItem convertItemDTO2DO(EvaluationSetItemDTO dto) {
    if (dto == null) {
      return null;
    }
    return EvaluationSetItem.builder()
      .id(dto.getId())
      .appId(dto.getAppId())
      .spaceId(dto.getWorkspaceId())
      .evaluationSetId(dto.getEvaluationSetId())
      .schemaId(dto.getSchemaId())
      .itemId(dto.getItemId())
      .itemKey(dto.getItemKey())
      .turns(convertTurnDTO2DOs(dto.getTurns()))
      .baseInfo(CommonConvertor.convertBaseInfoDTO2DO(dto.getBaseInfo()))
      .build();
  }

  /**
   * 轮次DTO列表转DO列表
   * 对应Go: TurnDTO2DOs
   */
  public static List<Turn> convertTurnDTO2DOs(List<TurnDTO> dtos) {
    if (dtos == null) {
      return null;
    }
    return dtos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertTurnDTO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 轮次DTO转DO
   * 对应Go: TurnDTO2DO
   */
  public static Turn convertTurnDTO2DO(TurnDTO dto) {
    if (dto == null) {
      return null;
    }
    return Turn.builder()
      .id(dto.getId())
      .fieldDataList(convertFieldDataDTO2DOs(dto.getFieldDataList()))
      .build();
  }

  /**
   * 字段数据DTO列表转DO列表
   * 对应Go: FieldDataDTO2DOs
   */
  public static List<FieldData> convertFieldDataDTO2DOs(List<FieldDataDTO> dtos) {
    if (dtos == null) {
      return null;
    }
    return dtos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertFieldDataDTO2DO)
      .collect(Collectors.toList());
  }

  /**
   * 字段数据DTO转DO
   * 对应Go: FieldDataDTO2DO
   */
  public static FieldData convertFieldDataDTO2DO(FieldDataDTO dto) {
    if (dto == null) {
      return null;
    }
    return FieldData.builder()
      .key(dto.getKey())
      .name(dto.getName())
      .content(CommonConvertor.convertContentDTO2DO(dto.getContent()))
      .build();
  }

  /**
   * 数据项DO列表转DTO列表
   * 对应Go: ItemDO2DTOs
   */
  public static List<EvaluationSetItemDTO> convertItemDO2DTOs(List<EvaluationSetItem> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertItemDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 数据项DO转DTO
   * 对应Go: ItemDO2DTO
   */
  public static EvaluationSetItemDTO convertItemDO2DTO(EvaluationSetItem doEntity) {
    if (doEntity == null) {
      return null;
    }
    return EvaluationSetItemDTO.builder()
      .id(doEntity.getId())
      .appId(doEntity.getAppId())
      .workspaceId(doEntity.getSpaceId())
      .evaluationSetId(doEntity.getEvaluationSetId())
      .schemaId(doEntity.getSchemaId())
      .itemId(doEntity.getItemId())
      .itemKey(doEntity.getItemKey())
      .turns(convertTurnDO2DTOs(doEntity.getTurns()))
      .baseInfo(CommonConvertor.convertBaseInfoDO2DTO(doEntity.getBaseInfo()))
      .build();
  }

  /**
   * 轮次DO列表转DTO列表
   * 对应Go: TurnDO2DTOs
   */
  public static List<TurnDTO> convertTurnDO2DTOs(List<Turn> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertTurnDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 轮次DO转DTO
   * 对应Go: TurnDO2DTO
   */
  public static TurnDTO convertTurnDO2DTO(Turn doEntity) {
    if (doEntity == null) {
      return null;
    }
    return TurnDTO.builder()
      .id(doEntity.getId())
      .fieldDataList(convertFieldDataDO2DTOs(doEntity.getFieldDataList()))
      .build();
  }

  /**
   * 字段数据DO列表转DTO列表
   * 对应Go: FieldDataDO2DTOs
   */
  public static List<FieldDataDTO> convertFieldDataDO2DTOs(List<FieldData> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertFieldDataDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 字段数据DO转DTO
   * 对应Go: FieldDataDO2DTO
   */
  public static FieldDataDTO convertFieldDataDO2DTO(FieldData doEntity) {
    if (doEntity == null) {
      return null;
    }
    return FieldDataDTO.builder()
      .key(doEntity.getKey())
      .name(doEntity.getName())
      .content(CommonConvertor.convertContentDO2DTO(doEntity.getContent()))
      .build();
  }

  /**
   * 数据项错误组DO列表转DTO列表
   * 对应Go: ItemErrorGroupDO2DTOs
   */
  public static List<ItemErrorGroupDTO> convertItemErrorGroupDO2DTOs(List<ItemErrorGroup> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertItemErrorGroupDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 数据项错误组DO转DTO
   * 对应Go: ItemErrorGroupDO2DTO
   */
  public static ItemErrorGroupDTO convertItemErrorGroupDO2DTO(ItemErrorGroup doEntity) {
    if (doEntity == null) {
      return null;
    }
    return ItemErrorGroupDTO.builder()
      .type(ItemErrorTypeDTO.fromValue(doEntity.getType().getValue()))
      .summary(doEntity.getSummary())
      .errorCount(doEntity.getErrorCount())
      .details(convertItemErrorDetailDO2DTOs(doEntity.getDetails()))
      .build();
  }

  /**
   * 数据项错误详情DO列表转DTO列表
   * 对应Go: ItemErrorDetailDO2DTOs
   */
  public static List<ItemErrorDetailDTO> convertItemErrorDetailDO2DTOs(List<ItemErrorDetail> dos) {
    if (dos == null) {
      return null;
    }
    return dos.stream()
      .map(EvaluationSetItemApplicationConvertor::convertItemErrorDetailDO2DTO)
      .collect(Collectors.toList());
  }

  /**
   * 数据项错误详情DO转DTO
   * 对应Go: ItemErrorDetailDO2DTO
   */
  public static ItemErrorDetailDTO convertItemErrorDetailDO2DTO(ItemErrorDetail doEntity) {
    if (doEntity == null) {
      return null;
    }
    return ItemErrorDetailDTO.builder()
      .message(doEntity.getMessage())
      .index(doEntity.getIndex())
      .startIndex(doEntity.getStartIndex())
      .endIndex(doEntity.getEndIndex())
      .build();
  }
}
