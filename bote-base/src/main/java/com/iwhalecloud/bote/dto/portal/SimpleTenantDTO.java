package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单租户信息
 *
 * <p>用作前端切换租户功能的下拉框数据，只包含必要的几个字段</p>
 *
 * @author bianjp
 * @since 2025-03-06
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "简单租户信息")
public class SimpleTenantDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "租户编码")
  private String tenantCode;
  @Schema(description = "租户名称")
  private String tenantName;
  @Schema(description = "用户角色")
  private String userRole;
  @Schema(description = "系统类型: lcdp 灵犀")
  private String systemType;
  @Schema(description = "创建人")
  private Long creatorId;
  @Schema(description = "企业空间 ID")
  private Long spaceId;
  @Schema(description = "企业空间名称")
  private String spaceName;
}
