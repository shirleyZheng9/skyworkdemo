package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WeKnora 注册请求
 *
 * @author huangyunming
 * @since 2026-04-01
 */
@Getter
@Setter
@ToString
public class WeKnoraRegisterRequest {

  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;

  @JsonProperty("password")
  private String password;
}
