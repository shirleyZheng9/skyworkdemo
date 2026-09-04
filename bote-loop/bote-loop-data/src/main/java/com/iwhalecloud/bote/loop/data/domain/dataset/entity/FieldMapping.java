package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段映射实体
 * 迁移对应关系: Go语言entity.FieldMapping
 * - 功能: 存储字段映射信息
 * - 字段定义:
 * * Source: string - 源字段
 * * Target: string - 目标字段
 * <p>
 * Java实现说明:
 * - 对应Go的entity.FieldMapping结构体
 * - 使用Java类定义，包含字段映射字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldMapping {

  @JsonProperty("source")
  private String source;

  @JsonProperty("target")
  private String target;
}
