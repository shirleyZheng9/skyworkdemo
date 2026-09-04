package com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WeKnora 登录请求
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Getter
@Setter
@ToString
public class WeKnoraLoginRequest {

  @JsonProperty("email")
  private String email;

  @JsonProperty("password")
  private String password;
}
