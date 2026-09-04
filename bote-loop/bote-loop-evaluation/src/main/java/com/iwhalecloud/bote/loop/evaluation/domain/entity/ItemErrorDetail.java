package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项错误详情实体
 * 迁移对应关系: Go语言ItemErrorDetail
 * - 功能: 项错误详情数据结构
 * - 字段: message, index, startIndex, endIndex
 * <p>
 * Java实现说明:
 * - 对应Go的ItemErrorDetail结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go *int32 -> Java Integer
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemErrorDetail {
  @JsonProperty("message")
  private String message;

  @JsonProperty("index")
  private Integer index;

  @JsonProperty("start_index")
  private Integer startIndex;

  @JsonProperty("end_index")
  private Integer endIndex;
}
