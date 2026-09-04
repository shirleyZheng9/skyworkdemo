package com.iwhalecloud.bote.portal.support.model;

import lombok.Data;

/**
 * 免登code换取用户信息请求实体
 *
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
public class OapiV2UserGetuserinfoRequest {

  /**
   * 免登授权码
   */
  private String code;
}
