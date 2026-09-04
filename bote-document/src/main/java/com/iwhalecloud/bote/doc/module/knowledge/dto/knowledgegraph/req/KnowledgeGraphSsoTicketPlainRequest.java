package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SSO ticket 明文 JSON（AES 加密前），字段名与第三方约定一致
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class KnowledgeGraphSsoTicketPlainRequest {

  /** 项目 ID*/
  @JsonProperty("project_id")
  private String projectId;

  /** 项目名称*/
  @JsonProperty("project_name")
  private String projectName;

  /** 项目用户 ID*/
  @JsonProperty("project_user_id")
  private String projectUserId;

  /** 项目用户名称*/
  @JsonProperty("project_user_name")
  private String projectUserName;

  /** 用户名 */
  private String username;

  /** 过期时间 */
  private Long exp;

  /** 随机数 */
  private String nonce;
}
