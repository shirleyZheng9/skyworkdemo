package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt详细信息实体
 * 迁移对应关系: Go语言entity.PromptDetail
 * - 功能: 存储Prompt的详细配置信息
 * - 字段定义:
 * * PromptTemplate: *PromptTemplate - 模板信息
 * * Tools: []*Tool - 工具列表
 * * ToolCallConfig: *ToolCallConfig - 工具调用配置
 * * ModelConfig: *ModelConfig - 模型配置
 * <p>
 * Java实现说明:
 * - 对应Go的entity.PromptDetail结构体
 * - 使用Java类定义，包含所有详细配置字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现深度比较方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片类型 -> Java List
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptDetail {

  /**
   * 模板信息
   * 迁移对应关系: Go语言entity.PromptDetail.PromptTemplate (*PromptTemplate)
   * - 功能: Prompt的模板配置
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储消息模板和变量定义
   */
  @JsonProperty("prompt_template")
  private PromptTemplate promptTemplate;

  /**
   * 工具列表
   * 迁移对应关系: Go语言entity.PromptDetail.Tools ([]*Tool)
   * - 功能: 可用的工具列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 定义可调用的工具
   */
  @JsonProperty("tools")
  private List<Tool> tools;

  /**
   * 工具调用配置
   * 迁移对应关系: Go语言entity.PromptDetail.ToolCallConfig (*ToolCallConfig)
   * - 功能: 工具调用的配置
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 控制工具调用行为
   */
  @JsonProperty("tool_call_config")
  private ToolCallConfig toolCallConfig;

  /**
   * 模型配置
   * 迁移对应关系: Go语言entity.PromptDetail.ModelConfig (*ModelConfig)
   * - 功能: 模型参数配置
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 控制模型行为参数
   */
  @JsonProperty("model_config")
  private ModelConfig modelConfig;
}
