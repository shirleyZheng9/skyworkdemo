package com.iwhalecloud.bote.dto.portal;

import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UserDTO extends UserEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "默认租户名称")
  private String defaultTenantName;
  @Schema(description = "租户成员列表")
  @DiffField(childNode = true)
  private List<TenantUserDTO> tenantUserList;
  @Schema(description = "企业空间ID")
  private Long spaceId;
}
