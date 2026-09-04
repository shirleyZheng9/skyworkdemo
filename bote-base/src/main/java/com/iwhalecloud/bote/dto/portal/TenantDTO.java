package com.iwhalecloud.bote.dto.portal;

import com.iwhalecloud.bote.entity.portal.TenantEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class TenantDTO extends TenantEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "用户角色")
  private String userRole;
  @Schema(description = "租户成员ID")
  private Long tenantUserId;
  @Schema(description = "是否开启联网搜索")
  private Boolean webSearchEnabled;
  @Schema(description = "是否开启对话窗口语音")
  private Boolean chatVoiceEnabled;
  @Schema(description = "空间编码（用于外部平台，通过spaceCode获取spaceId）")
  private String spaceCode;
}
