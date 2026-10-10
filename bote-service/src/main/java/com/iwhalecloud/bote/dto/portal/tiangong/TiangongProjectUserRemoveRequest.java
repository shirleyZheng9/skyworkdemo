package com.iwhalecloud.bote.dto.portal.tiangong;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 天工项目成员移除请求
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 使用外部用户 ID 删除成员，服务端不会根据调用方提交的内部用户 ID 执行删除。
 */
@Getter
@Setter
public class TiangongProjectUserRemoveRequest {
  @Schema(description = "消息唯一标识")
  private String requestId;
  @Schema(description = "天工空间 ID，用于校验项目归属")
  private String externalSpaceId;
  @Schema(description = "天工项目 ID")
  private Long externalProjectId;
  @Schema(description = "AADP 租户 ID，用于二次校验")
  private Long boteTenantId;
  @Schema(description = "待移除成员的天工外部用户 ID")
  private List<String> extUserIds;
}
