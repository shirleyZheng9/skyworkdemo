package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据项运行状态过滤器实体
 * 迁移对应关系: Go语言ItemRunStateFilter
 * - 功能: 数据项运行状态过滤条件
 * - 字段: status, operator
 * <p>
 * Java实现说明:
 * - 对应Go的ItemRunStateFilter结构体
 * - 使用Lombok注解简化代码
 * - JSON序列化使用camelCase命名（研发规范2）
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []ItemRunState -> Java List<ItemRunState>
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRunStateFilter {
  private List<ItemRunState> status;

  private String operator;
}

