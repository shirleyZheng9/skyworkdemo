package com.iwhalecloud.bote.portal.support.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
public class OapiUserGetbyunionidResponse {

  @JsonProperty("contact_type")
  private Long contactType;
  /**
   * 用户id
   */
  @JsonProperty("userid")
  private String userid;
}
