package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试核心实体
 * 迁移对应关系: Go语言entity.DebugCore
 * - 功能: 调试核心的数据结构
 * - 字段定义:
 * * MockContexts: []*DebugMessage - 模拟上下文
 * * MockVariables: []*VariableVal - 模拟变量
 * * MockTools: []*MockTool - 模拟工具
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DebugCore结构体
 * - 使用Java类定义，包含调试核心字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
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
public class DebugCore {

  /**
   * 模拟上下文
   * 迁移对应关系: Go语言entity.DebugCore.MockContexts ([]*DebugMessage)
   * - 功能: 模拟的调试消息上下文
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储模拟的调试消息
   */
  @JsonProperty("mock_contexts")
  private List<DebugMessage> mockContexts;

  /**
   * 模拟变量
   * 迁移对应关系: Go语言entity.DebugCore.MockVariables ([]*VariableVal)
   * - 功能: 模拟的变量值
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储模拟的变量值
   */
  @JsonProperty("mock_variables")
  private List<VariableVal> mockVariables;

  /**
   * 模拟工具
   * 迁移对应关系: Go语言entity.DebugCore.MockTools ([]*MockTool)
   * - 功能: 模拟的工具配置
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储模拟的工具配置
   */
  @JsonProperty("mock_tools")
  private List<MockTool> mockTools;
}
