package com.iwhalecloud.bote.dto.a2a;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A2A 通知的上下文信息
 *
 * @author bianjp
 * @since 2025-11-27
 */
@Getter
@Setter
@ToString
public class A2aNotificationContext {
  /** 登录信息 */
  private LoginInfo loginInfo;
  /** 租户 ID */
  private Long tenantId;
  /** 工作流 ID */
  private Long flowId;

  // 暂时没用到，仅作记录，可能有助于排查问题
  /** A2A 服务 ID */
  private Long agentId;
  /** A2A 任务 ID */
  private String taskId;
}
