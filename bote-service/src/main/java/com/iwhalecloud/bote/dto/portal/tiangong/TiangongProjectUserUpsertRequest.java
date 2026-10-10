package com.iwhalecloud.bote.dto.portal.tiangong;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 天工项目成员新增请求
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * externalProjectId 定位 AADP 项目，externalSpaceId 校验项目归属；creatorExtUserId 必须属于 users。
 */
@Getter
@Setter
public class TiangongProjectUserUpsertRequest {
  @Schema(description = "消息唯一标识")
  private String requestId;
  @Schema(description = "天工空间 ID，用于校验项目归属")
  private String externalSpaceId;
  @Schema(description = "天工项目 ID")
  private Long externalProjectId;
  @Schema(description = "AADP 租户 ID，用于二次校验")
  private Long boteTenantId;
  @Schema(description = "项目创建人的天工外部用户 ID")
  private String creatorExtUserId;
  @Schema(description = "待写入成员")
  private List<TiangongProjectUserItem> users;
}
