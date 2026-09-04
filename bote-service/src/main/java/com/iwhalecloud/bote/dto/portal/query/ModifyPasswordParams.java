package com.iwhalecloud.bote.dto.portal.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 修改用户密码入参
 *
 * @author auto
 * @since 2024-09-23
 */
@Getter
@Setter
@ToString
public class ModifyPasswordParams {
  @Schema(description = "旧密码")
  private String oldPassword;
  @Schema(description = "新密码")
  private String newPassword;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "用户编码")
  private String userName;
}
