package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置实体
 * 迁移对应关系: Go语言ModelConfig
 * - 功能: 模型配置数据结构
 * - 字段: modelId, modelName, maxTokens, temperature, topP, toolChoice, providerModelId
 * <p>
 * Java实现说明:
 * - 对应Go的ModelConfig结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *int32 -> Java Integer
 * - Go *float64 -> Java Double
 * - Go ToolChoiceType -> Java ToolChoiceType
 * - Go *string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelConfig {
  @JsonProperty("model_id")
  private Long modelId;

  @JsonProperty("model_name")
  private String modelName;

  @JsonProperty("max_tokens")
  private Integer maxTokens;

  @JsonProperty("temperature")
  private Double temperature;

  @JsonProperty("top_p")
  private Double topP;

  @JsonProperty("tool_choice")
  private ToolChoiceType toolChoice;

  @JsonProperty("provider_model_id")
  private String providerModelId;
}
