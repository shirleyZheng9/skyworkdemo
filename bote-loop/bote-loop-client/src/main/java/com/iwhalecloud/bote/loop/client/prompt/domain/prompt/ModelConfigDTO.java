package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置DTO
 * 迁移对应关系: Thrift struct ModelConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "模型配置DTO")
public class ModelConfigDTO {

  @Schema(description = "模型ID")
  private Long modelId;

  @Schema(description = "最大令牌数")
  private Integer maxTokens;

  @Schema(description = "温度参数")
  private Double temperature;

  @Schema(description = "Top-K参数")
  private Integer topK;

  @Schema(description = "Top-P参数")
  private Double topP;

  @Schema(description = "存在惩罚")
  private Double presencePenalty;

  @Schema(description = "频率惩罚")
  private Double frequencyPenalty;

  @Schema(description = "JSON模式")
  private Boolean jsonMode;
}
