package com.iwhalecloud.bote.dto.workspace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单企业信息
 * <p>用作前端切换企业功能的下拉框数据，只包含必要的几个字段</p>
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "简单企业信息")
public class SimpleWorkspaceDTO {
  @Schema(description = "企业空间 ID")
  private Long spaceId;

  @Schema(description = "企业空间名称")
  private String spaceName;

  @Schema(description = "用户角色")
  private String roleCode;

  @Schema(description = "项目列表")
  private List<SimpleTenantDTO> tenants;
}
