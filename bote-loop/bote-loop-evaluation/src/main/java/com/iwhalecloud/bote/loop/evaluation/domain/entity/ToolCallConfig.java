package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用配置实体
 * 迁移对应关系: Go语言ToolCallConfig
 * - 功能: 工具调用配置数据结构
 * - 字段: toolChoice
 * <p>
 * Java实现说明:
 * - 对应Go的ToolCallConfig结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go ToolChoiceType -> Java ToolChoiceType
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCallConfig {
  @JsonProperty("tool_choice")
  private ToolChoiceType toolChoice;
}
