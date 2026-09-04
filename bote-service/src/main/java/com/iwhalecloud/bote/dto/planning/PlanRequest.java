package com.iwhalecloud.bote.dto.planning;

import com.iwhalecloud.bote.service.chat.context.ChatContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 执行计划入参
 *
 * @author chen.linfa
 * @since 2025-05-19
 */
@Getter
@Setter
@ToString
public class PlanRequest {
  /** 租户 ID */
  private Long tenantId;
  /** 计划 ID */
  private Long planId;

  /** 是否中断 */
  private Boolean interrupted;
  /** 标记已完成的步骤 ID */
  private Long completedStepId;
  /** 标记已完成的上下文 ID */
  private String completedContextId;
  /** 标记已完成的步骤序号 */
  private Integer completedStepIndex;

  /** 会话上下文 */
  private ChatContext chatContext;
  /** 计划定义 */
  private PlanRecordDTO record;
}
