package com.iwhalecloud.bote.dto.tenant.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 安全围栏工作流设置
 *
 * @author linemgnfan
 * @since 2026-03-02
 */
@Getter
@Setter
@ToString
public class TenantSecurityFlowSettingDTO {
  /** 用户输入工作流 ID */
  private Long userInputFlowId;
  /** 模型输入工作流 ID */
  private Long llmInputFlowId;
  /** 模型输出工作流 ID */
  private Long llmOutputFlowId;
}
