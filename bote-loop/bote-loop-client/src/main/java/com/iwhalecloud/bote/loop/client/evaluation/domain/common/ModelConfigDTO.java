package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "模型配置数据传输对象")
public class ModelConfigDTO {

  @Schema(description = "模型ID")
  private Long modelId;

  @Schema(description = "模型名称")
  private String modelName;

  @Schema(description = "温度参数")
  private Double temperature;

  @Schema(description = "最大Token数")
  private Integer maxTokens;

  @Schema(description = "TopP参数")
  private Double topP;
}
