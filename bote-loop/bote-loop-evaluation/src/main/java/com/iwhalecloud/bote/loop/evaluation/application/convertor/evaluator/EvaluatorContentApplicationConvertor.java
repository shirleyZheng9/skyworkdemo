package com.iwhalecloud.bote.loop.evaluation.application.convertor.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.FunctionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.ToolDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.ToolTypeDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Function;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolType;

/**
 * 评估器内容转换器
 * 对应Go: evaluator/evaluator_content.go
 */
public final class EvaluatorContentApplicationConvertor {
  private EvaluatorContentApplicationConvertor() {
  }

  /**
   * 工具DTO转DO
   * 对应Go: ConvertToolDTO2DO
   */
  public static Tool convertToolDTO2DO(ToolDTO dto) {
    if (dto == null) {
      return null;
    }
    return Tool.builder()
      .type(ToolType.fromValue(dto.getType().getValue()))
      .function(convertFunctionDTO2DO(dto.getFunction()))
      .build();
  }

  /**
   * 工具DO转DTO
   * 对应Go: ConvertToolDO2DTO
   */
  public static ToolDTO convertToolDO2DTO(Tool doEntity) {
    if (doEntity == null) {
      return null;
    }
    return ToolDTO.builder()
      .type(ToolTypeDTO.fromValue(doEntity.getType().getValue()))
      .function(convertFunctionDO2DTO(doEntity.getFunction()))
      .build();
  }

  /**
   * 函数DTO转DO
   * 对应Go: ConvertFunctionDTO2DO
   */
  public static Function convertFunctionDTO2DO(FunctionDTO dto) {
    if (dto == null) {
      return null;
    }

    String description = "";
    if (dto.getDescription() != null) {
      description = dto.getDescription();
    }

    String parameters = "";
    if (dto.getParameters() != null) {
      parameters = dto.getParameters();
    }

    return Function.builder()
      .name(dto.getName())
      .description(description)
      .parameters(parameters)
      .build();
  }

  /**
   * 函数DO转DTO
   * 对应Go: ConvertFunctionDO2DTO
   */
  public static FunctionDTO convertFunctionDO2DTO(Function doEntity) {
    if (doEntity == null) {
      return null;
    }
    return FunctionDTO.builder()
      .name(doEntity.getName())
      .description(doEntity.getDescription())
      .parameters(doEntity.getParameters())
      .build();
  }
}
