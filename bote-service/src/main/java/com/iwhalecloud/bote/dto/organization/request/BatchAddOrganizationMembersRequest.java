package com.iwhalecloud.bote.dto.organization.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 批量添加组织成员请求
 *
 * @author zhao.xu104
 * @since 2025-09-09
 */
@Getter
@Setter
@ToString
@Schema(description = "批量添加组织成员请求")
public class BatchAddOrganizationMembersRequest {
  @NotNull(message = "组织ID不能为空")
  @Schema(description = "组织ID")
  private Long orgId;
  @NotNull(message = "企业空间ID不能为空")
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @NotEmpty(message = "用户ID列表不能为空")
  @Schema(description = "用户ID列表")
  private List<Long> userIds;
  @Schema(description = "默认成员角色", example = "member")
  private String defaultMemberRole = "member";
  @Schema(description = "默认成员类型", example = "regular")
  private String defaultMemberType = "regular";
  @Schema(description = "默认直接上级用户ID")
  private Long defaultDirectManagerId;
  @Schema(description = "备注")
  private String remark;
}
