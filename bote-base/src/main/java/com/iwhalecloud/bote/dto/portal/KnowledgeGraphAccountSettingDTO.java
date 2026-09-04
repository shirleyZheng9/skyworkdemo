package com.iwhalecloud.bote.dto.portal;

import lombok.Getter;
import lombok.Setter;

/**
 * knowledgeGraph 账号测试参数
 *
 * @author qian.sisheng
 * @since 2026-04-14
 */
@Getter
@Setter
public class KnowledgeGraphAccountSettingDTO {
  /** 租户ID */
  private Long tenantId;
  /** 租户侧项目ID */
  private String projectId;
  /** 租户侧项目用户ID */
  private String projectUserId;
  /** 项目用户名 */
  private String projectUserName;
  /** 过期时间 */
  private Long exp;
}
