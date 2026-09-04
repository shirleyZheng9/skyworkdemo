package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "模型信息数据传输对象")
public class ModelInfoDTO {

  @Schema(description = "模型ID")
  private Long modelId;

  @Schema(description = "模型名称")
  private String modelName;

  @Schema(description = "显示名称")
  private String showName;

  @Schema(description = "最大Token数")
  private Long maxTokens;

  @Schema(description = "模型家族")
  private Long modelFamily;

  @Schema(description = "平台")
  private ModelPlatformDTO platform;
}
