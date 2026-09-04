package com.iwhalecloud.bote.dto.tenant.setting;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户侧 knowledgeGraph SSO 登录载荷（仅存 project 与对接用户标识，不含服务地址与密钥）
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KnowledgeGraphLoginDTO {

  /** 租户侧项目ID */
  @JsonAlias({ "projectId", "project_id" })
  private String projectId;
  /** 租户侧项目用户ID */
  @JsonAlias({ "projectUserId", "project_user_id" })
  private String projectUserId;
  /** 项目用户名 */
  @JsonAlias({ "projectUserName", "project_user_name" })
  private String projectUserName;
  /** 过期时间 */
  private long exp;
}
