package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码评测器数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "代码评测器数据传输对象")
public class CodeEvaluatorDTO {

  @Schema(description = "语言类型")
  private LanguageTypeDTO languageType;

  @Schema(description = "代码")
  private String code;
}
