package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页实体
 * 迁移对应关系: Go语言Page
 * - 功能: 分页参数
 * - 字段: offset, limit
 * <p>
 * Java实现说明:
 * - 对应Go的Page结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int -> Java Integer
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {
  @JsonProperty("offset")
  private Integer offset;

  @JsonProperty("limit")
  private Integer limit;
}
