package com.iwhalecloud.bote.portal.support.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
public class UserGetByCodeResponse {

  /**
   * 用户统一id
   */
  @JsonProperty("associated_unionid")
  private String associatedUnionid;
  /**
   * 设备id
   */
  @JsonProperty("device_id")
  private String deviceId;
  /**
   * 用户名字
   */
  @JsonProperty("name")
  private String name;
  /**
   * 是否为管理员
   */
  @JsonProperty("sys")
  private Boolean sys;
  /**
   * 员工级别。 1：主管理员 2：子管理员 100：老板 0：其他（如普通员工）
   */
  @JsonProperty("sys_level")
  private Long sysLevel;
  /**
   * 用户unionId
   */
  @JsonProperty("unionid")
  private String unionid;
  /**
   * 用户id
   */
  @JsonProperty("userid")
  private String userid;
}
