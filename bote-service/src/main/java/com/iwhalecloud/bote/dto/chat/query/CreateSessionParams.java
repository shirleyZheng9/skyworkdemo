package com.iwhalecloud.bote.dto.chat.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增会话参数
 *
 * @author chen.linfa
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString
@Schema(description = "新增会话参数")
public class CreateSessionParams {
  /** 空间 ID */
  private Long spaceId;
  /** 租户 ID */
  private Long tenantId;
  /** 应用 ID */
  private Long botId;
  /** 应用归属的租户 ID */
  private Long botTenantId;
  /** 外系统 ID */
  private Long extSystemId;
  /** 平台应用 ID */
  private Long platBotId;
  /** 是否调试 */
  private Boolean isTest;
}
