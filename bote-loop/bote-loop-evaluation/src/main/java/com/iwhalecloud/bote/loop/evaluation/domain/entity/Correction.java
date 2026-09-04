package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 纠正实体
 * 迁移对应关系: Go语言Correction
 * - 功能: 纠正数据结构
 * - 字段: score, explain, updatedBy
 * <p>
 * Java实现说明:
 * - 对应Go的Correction结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *float64 -> Java Double
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correction {
  @JsonProperty("score")
  private Double score;

  @JsonProperty("explain")
  private String explain;

  @JsonProperty("updated_by")
  private String updatedBy;
}
