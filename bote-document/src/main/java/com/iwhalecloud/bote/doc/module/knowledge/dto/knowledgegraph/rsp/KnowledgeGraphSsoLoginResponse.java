package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SSO 登录成功时响应 payload 中的凭证
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KnowledgeGraphSsoLoginResponse {
  /** 凭证 */
  private String token;
  /** 用户信息 */
  private UserInfo userInfo;

  @Getter
  @Setter
  @ToString
  public static class UserInfo {
    /** 用户信息 */
    private User user;
  }

  @Getter
  @Setter
  @ToString
  public static class User {
    /** 用户ID */
    private Long uid;
    /** 用户名 */
    private String account;
  }
}
