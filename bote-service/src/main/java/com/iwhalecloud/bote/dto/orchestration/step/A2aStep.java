package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A2A 服务步骤
 *
 * @author bianjp
 * @since 2025-10-21
 */
@Getter
@Setter
public class A2aStep extends AbstractStep {
  /** A2A 服务 ID */
  private Long agentId;
  /** 是否使用流式输出(即使用 A2A 的 message/stream 方法，默认为 true, 关闭时使用 A2A 的 message/send) */
  private Boolean stream;
  /** 非流式输出时接收 A2A 服务器通知的工作流 ID */
  private Long notificationFlowId;
  /** A2A 任务配置 */
  private A2aTaskConfig taskConfig;
  /** 消息文本（模板字符串） */
  private String messageText;
  /** 消息数据（取值表达式） */
  private String messageData;
  /** 消息文件（文件地址或文件 ID(取值表达式), 常量值只能是一个文件，引用变量时可以是多个文件） */
  private String messageFile;
  /** 消息元数据（取值表达式） */
  private String metadata;

  public A2aStep() {
    super(StepType.A2A);
  }

  /**
   * A2A 任务配置
   */
  @Getter
  @Setter
  @ToString
  public static class A2aTaskConfig {
    /** 是否自定义上下文 ID */
    private Boolean useCustomContextId;
    /** 是否自定义任务 ID */
    private Boolean useCustomTaskId;
    /** 上下文 ID(取值表达式), 仅在关闭自动管理时使用 */
    private String contextId;
    /** 任务 ID(取值表达式), 仅在关闭自动管理时使用 */
    private String taskId;
  }
}
