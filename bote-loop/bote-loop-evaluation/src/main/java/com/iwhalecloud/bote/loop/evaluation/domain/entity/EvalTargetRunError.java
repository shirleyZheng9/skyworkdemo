package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标运行错误实体
 * 迁移对应关系: Go语言EvalTargetRunError
 * - 功能: 评估目标运行错误数据结构
 * - 字段: code, message
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetRunError结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetRunError {
  @JsonProperty("code")
  private Integer code;

  @JsonProperty("message")
  private String message;
}
