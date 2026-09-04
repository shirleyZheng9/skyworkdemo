package com.iwhalecloud.bote.dto.workspace.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作空间查询条件
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "工作空间查询条件")
public class WorkspaceQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "在线环境 ID")
  private Long gatewayId;
}
