package com.iwhalecloud.bote.dto.base;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 编辑锁信息
 *
 * @author qian.sisheng
 * @since 2025-05-08
 */
@Setter
@Getter
@Builder
public class EditLockInfoDTO {
  /** 编辑的对象 ID */
  private Long id;
  /** 用户 ID */
  private Long userId;
  /** 用户名称 */
  private String realName;
  /** 加锁时间(ms) */
  private Long lockTime;
  /** 最近加锁时间(ms) */
  private Long lastUpdateTime;
  /** 实体名称 */
  private String entityName;
}
