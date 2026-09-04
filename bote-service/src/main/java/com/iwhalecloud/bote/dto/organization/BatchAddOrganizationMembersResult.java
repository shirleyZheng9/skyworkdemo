package com.iwhalecloud.bote.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

/**
 * 批量添加组织成员结果
 *
 * @author zhao.xu104
 * @since 2025-09-09
 */
@Getter
@Setter
@ToString
@Schema(description = "批量添加组织成员结果")
public class BatchAddOrganizationMembersResult {
  @Schema(description = "成功添加的成员列表")
  private List<OrganizationMemberDTO> successMembers;
  @Schema(description = "成功添加的数量")
  private int successCount;
  @Schema(description = "失败的用户ID列表")
  private List<Long> failedUserIds;
  @Schema(description = "失败的数量")
  private int failCount;
  @Schema(description = "总处理数量")
  private int totalCount;
  @Schema(description = "失败原因详情")
  private List<FailedMemberInfo> failedDetails;
  @Getter
  @Setter
  @ToString
  @Schema(description = "失败成员信息")
  public static class FailedMemberInfo {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "失败原因")
    private String reason;
  }
}
