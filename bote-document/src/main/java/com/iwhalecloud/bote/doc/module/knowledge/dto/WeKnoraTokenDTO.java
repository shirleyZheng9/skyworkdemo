package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WeKnora Token DTO
 *
 * @author qian.sisheng
 * @since 2026-04-10
 */
@Getter
@Setter
@ToString
public class WeKnoraTokenDTO {
  /** WeKnora Token */
  private String weKnoraToken;
  /** WeKnora Refresh Token */
  private String weKnoraRefreshToken;
  /** WeKnora 用户信息 */
  private String weKnoraUser;
}
