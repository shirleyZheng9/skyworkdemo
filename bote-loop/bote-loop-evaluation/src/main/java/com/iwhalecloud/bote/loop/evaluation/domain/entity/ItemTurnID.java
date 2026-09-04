package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项轮次ID实体
 * 迁移对应关系: Go语言ItemTurnID
 * - 功能: 项轮次ID数据结构
 * - 字段: itemId, turnId
 * <p>
 * Java实现说明:
 * - 对应Go的ItemTurnID结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemTurnID {
  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("turn_id")
  private Long turnId;
}
