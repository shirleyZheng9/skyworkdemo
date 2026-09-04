package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计计数算术操作实体
 * 迁移对应关系: Go语言StatsCntArithOp
 * - 功能: 统计计数算术操作数据结构
 * - 字段: opStatusCnt
 * <p>
 * Java实现说明:
 * - 对应Go的StatsCntArithOp结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go map[ItemRunState]int -> Java Map<ItemRunState, Integer>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsCntArithOp {
  @JsonProperty("op_status_cnt")
  private Map<ItemRunState, Integer> opStatusCnt;
}
