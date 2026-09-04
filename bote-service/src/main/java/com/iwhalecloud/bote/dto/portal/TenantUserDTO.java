package com.iwhalecloud.bote.dto.portal;

import com.iwhalecloud.bote.entity.portal.TenantUserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户成员 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class TenantUserDTO extends TenantUserEntity {
  @Schema(description = "用户名")
  private String userName;
  @Schema(description = "用户姓名")
  private String realName;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "是否归属当前租户")
  private Boolean isBelongCurrentTenant;
  @Schema(description = "系统编码")
  private String systemCode;
  @Schema(description = "邮箱")
  private String email;
  @Schema(description = "手机号码")
  private String phoneNo;
  @Schema(description = "用户编码（userCode，用于Beyond系统）")
  private String userCode;
  @Schema(description = "对端系统的用户ID（用于Beyond系统，赋值给extUserId）")
  private String beyondUserId;
}
