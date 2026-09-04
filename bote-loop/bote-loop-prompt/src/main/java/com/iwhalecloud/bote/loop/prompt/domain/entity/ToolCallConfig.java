package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用配置实体
 * 迁移对应关系: Go语言entity.ToolCallConfig
 * - 功能: 配置工具调用的行为
 * - 字段定义:
 * * ToolChoice: ToolChoiceType - 工具选择类型
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ToolCallConfig结构体
 * - 使用Java类定义，包含工具调用配置字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCallConfig {

  /**
   * 工具选择类型
   * 迁移对应关系: Go语言entity.ToolCallConfig.ToolChoice (ToolChoiceType)
   * - 功能: 工具选择的策略
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 控制工具调用行为
   */
  @JsonProperty("tool_choice")
  private ToolChoiceType toolChoice;

}
