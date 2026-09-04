package com.iwhalecloud.bote.portal.support.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * userId查询用户信息请求参数
 *
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
public class OapiV2UserGetRequest {

  /**
   * 语言
   */
  private String language;

  /**
   * 用户id
   */
  @JsonProperty("userid")
  private String userid;
}
