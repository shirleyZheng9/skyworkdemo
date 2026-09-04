package com.iwhalecloud.bote.portal.dto;

import jakarta.validation.constraints.NotNull;

import org.springframework.lang.Nullable;

import lombok.Data;
import lombok.ToString;

/**
 * 对钉钉用户信息进行转换
 *
 * @author Aiqing
 * @since 2025/6/5
 */
@Data
@ToString
public class DingTalkUserDTO {

  /**
   * 钉钉的用户ID，单个企业内唯一，数值类型
   */
  @NotNull
  private String userId;
  private String avatarUrl;
  @Nullable
  private String email;
  @NotNull
  private String mobile;
  private String nick;
  private String openId;
  private String unionId;
  private String jobNumber;
  private String stateCode;
}
