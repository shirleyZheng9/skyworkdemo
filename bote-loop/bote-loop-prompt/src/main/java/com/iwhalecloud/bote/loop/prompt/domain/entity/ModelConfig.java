package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置实体
 * 迁移对应关系: Go语言entity.ModelConfig
 * - 功能: 存储模型运行时的配置参数
 * - 字段定义:
 * * ModelID: int64 - 模型ID
 * * MaxTokens: *int32 - 最大token数
 * * Temperature: *float64 - 温度参数
 * * TopK: *int32 - TopK参数
 * * TopP: *float64 - TopP参数
 * * PresencePenalty: *float64 - 存在惩罚
 * * FrequencyPenalty: *float64 - 频率惩罚
 * * JSONMode: *bool - JSON模式
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ModelConfig结构体
 * - 使用Java类定义，包含模型配置字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 数值类型使用Java的包装类型支持可空
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 * - Go *int32 -> Java Integer (可空)
 * - Go *float64 -> Java Double (可空)
 * - Go *bool -> Java Boolean (可空)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelConfig {

  /**
   * 模型ID
   * 迁移对应关系: Go语言entity.ModelConfig.ModelID (int64)
   * - 功能: 模型的唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 用途: 模型选择和识别
   */
  @JsonProperty("model_id")
  private Long modelId;

  /**
   * 最大token数
   * 迁移对应关系: Go语言entity.ModelConfig.MaxTokens (*int32)
   * - 功能: 生成的最大token数量限制
   * - 类型: Go的*int32对应Java的Integer (可空)
   * - 用途: 控制生成长度
   */
  @JsonProperty("max_tokens")
  private Integer maxTokens;

  /**
   * 温度参数
   * 迁移对应关系: Go语言entity.ModelConfig.Temperature (*float64)
   * - 功能: 控制生成的随机性
   * - 类型: Go的*float64对应Java的Double (可空)
   * - 用途: 控制输出多样性
   */
  @JsonProperty("temperature")
  private Double temperature;

  /**
   * TopK参数
   * 迁移对应关系: Go语言entity.ModelConfig.TopK (*int32)
   * - 功能: TopK采样参数
   * - 类型: Go的*int32对应Java的Integer (可空)
   * - 用途: 控制候选词数量
   */
  @JsonProperty("top_k")
  private Integer topK;

  /**
   * TopP参数
   * 迁移对应关系: Go语言entity.ModelConfig.TopP (*float64)
   * - 功能: TopP采样参数
   * - 类型: Go的*float64对应Java的Double (可空)
   * - 用途: 控制累积概率阈值
   */
  @JsonProperty("top_p")
  private Double topP;

  /**
   * 存在惩罚
   * 迁移对应关系: Go语言entity.ModelConfig.PresencePenalty (*float64)
   * - 功能: 对已出现token的惩罚
   * - 类型: Go的*float64对应Java的Double (可空)
   * - 用途: 减少重复内容
   */
  @JsonProperty("presence_penalty")
  private Double presencePenalty;

  /**
   * 频率惩罚
   * 迁移对应关系: Go语言entity.ModelConfig.FrequencyPenalty (*float64)
   * - 功能: 对高频token的惩罚
   * - 类型: Go的*float64对应Java的Double (可空)
   * - 用途: 减少重复内容
   */
  @JsonProperty("frequency_penalty")
  private Double frequencyPenalty;

  /**
   * JSON模式
   * 迁移对应关系: Go语言entity.ModelConfig.JSONMode (*bool)
   * - 功能: 是否启用JSON模式
   * - 类型: Go的*bool对应Java的Boolean (可空)
   * - 用途: 强制JSON格式输出
   */
  @JsonProperty("json_mode")
  private Boolean jsonMode;

}
