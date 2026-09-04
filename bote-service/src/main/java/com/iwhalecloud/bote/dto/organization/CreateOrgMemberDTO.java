package com.iwhalecloud.bote.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增组织成员DTO
 *
 * @author wangtingyun
 * @since 2025-10-24
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "新增组织成员信息")
public class CreateOrgMemberDTO {

  @Schema(description = "用户信息")
  private UserDTO userInfo;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "组织ID")
  private Long orgId;
  @Schema(description = "默认成员角色", example = "member")
  private String defaultMemberRole = "member";
  @Schema(description = "默认成员类型", example = "regular")
  private String defaultMemberType = "regular";

}
