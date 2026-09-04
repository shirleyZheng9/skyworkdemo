package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标版本实体
 * 迁移对应关系: Go语言EvalTargetVersion
 * - 功能: 评估目标版本数据结构
 * - 字段: id, spaceId, targetId, sourceTargetVersion, evalTargetType, cozeBot, prompt, cozeWorkflow, inputSchema, outputSchema, baseInfo
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetVersion结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *CozeBot -> Java Bot
 * - Go *LoopPrompt -> Java LoopPrompt
 * - Go *CozeWorkflow -> Java Workflow
 * - Go []*ArgsSchema -> Java List<ArgsSchema>
 * - Go *BaseInfo -> Java BaseInfo
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetVersion {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("target_id")
  private Long targetId;

  @JsonProperty("source_target_version")
  private String sourceTargetVersion;

  @JsonProperty("eval_target_type")
  private EvalTargetType evalTargetType;

  @JsonProperty("coze_bot")
  private Bot bot;

  @JsonProperty("prompt")
  private LoopPromptDO prompt;

  @JsonProperty("coze_workflow")
  private Workflow workflow;

  @JsonProperty("input_schema")
  private List<ArgsSchema> inputSchema;

  @JsonProperty("output_schema")
  private List<ArgsSchema> outputSchema;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
