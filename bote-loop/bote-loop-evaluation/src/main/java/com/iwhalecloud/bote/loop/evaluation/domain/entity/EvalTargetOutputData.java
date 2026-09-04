package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标输出数据实体
 * 迁移对应关系: Go语言EvalTargetOutputData
 * - 功能: 评估目标输出数据结构
 * - 字段: outputFields, evalTargetUsage, evalTargetRunError, timeConsumingMS
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetOutputData结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go map[string]*Content -> Java Map<String, Content>
 * - Go *EvalTargetUsage -> Java EvalTargetUsage
 * - Go *EvalTargetRunError -> Java EvalTargetRunError
 * - Go *int64 -> Java Long
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetOutputData {
  @JsonProperty("output_fields")
  private Map<String, Content> outputFields;

  @JsonProperty("eval_target_usage")
  private EvalTargetUsage evalTargetUsage;

  @JsonProperty("eval_target_run_error")
  private EvalTargetRunError evalTargetRunError;

  @JsonProperty("time_consuming_ms")
  private Long timeConsumingMs;
}
