package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.MockTool;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Scenario;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableVal;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式执行参数
 * 迁移对应关系: Go语言service.ExecuteStreamingParam
 * - 功能: Prompt流式执行时的参数封装
 * - 字段定义:
 * * Prompt: *entity.Prompt - Prompt对象
 * * Messages: []*entity.Message - 消息列表
 * * VariableVals: []*entity.VariableVal - 变量值列表
 * * MockTools: []*entity.MockTool - 模拟工具列表
 * * SingleStep: bool - 是否单步调试
 * * DebugTraceKey: string - 调试追踪键
 * * Scenario: entity.Scenario - 执行场景
 * * ResultStream: chan<- *entity.Reply - 结果流通道
 * <p>
 * Java实现说明:
 * - 对应Go的service.ExecuteStreamingParam结构体
 * - 直接包含ExecuteParam的所有字段
 * - 使用Java的Consumer替代Go的channel
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体嵌入 -> Java直接包含字段
 * - Go channel -> Java Consumer
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteStreamingParam {

  /**
   * Prompt对象
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.Prompt (*entity.Prompt)
   * - 功能: 要执行的Prompt对象
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 包含Prompt的详细信息和配置
   */
  @JsonProperty("prompt")
  private Prompt prompt;

  /**
   * 消息列表
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.Messages ([]*entity.Message)
   * - 功能: 执行时的消息列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 存储对话历史和当前消息
   */
  @JsonProperty("messages")
  private List<Message> messages;

  /**
   * 变量值列表
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.VariableVals ([]*entity.VariableVal)
   * - 功能: 模板变量的值列表
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 模板变量替换
   */
  @JsonProperty("variable_vals")
  private List<VariableVal> variableVals;

  /**
   * 模拟工具列表
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.MockTools ([]*entity.MockTool)
   * - 功能: 调试时使用的模拟工具
   * - 类型: Go的切片类型对应Java的List
   * - 用途: 调试环境下的工具模拟
   */
  @JsonProperty("mock_tools")
  private List<MockTool> mockTools;

  /**
   * 是否单步调试
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.SingleStep (bool)
   * - 功能: 标识是否为单步调试模式
   * - 类型: Go的bool对应Java的Boolean
   * - 用途: 控制调试执行模式
   */
  @JsonProperty("single_step")
  private Boolean singleStep;

  /**
   * 调试追踪键
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.DebugTraceKey (string)
   * - 功能: 调试会话的追踪标识
   * - 类型: Go的string对应Java的String
   * - 用途: 调试会话管理和追踪
   */
  @JsonProperty("debug_trace_key")
  private String debugTraceKey;

  /**
   * 执行场景
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.Scenario (entity.Scenario)
   * - 功能: 执行场景标识
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 区分不同的执行场景
   */
  @JsonProperty("scenario")
  private Scenario scenario;

  /**
   * 结果流处理器
   * 迁移对应关系: Go语言service.ExecuteStreamingParam.ResultStream (chan<- *entity.Reply)
   * - 功能: 流式结果的处理器
   * - 类型: Go的channel对应Java的Consumer
   * - 用途: 处理流式返回的结果
   */
  @JsonProperty("result_stream")
  private Consumer<Reply> resultStream;
}
