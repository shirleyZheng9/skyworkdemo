package com.iwhalecloud.bote.dto.workspace;

import com.iwhalecloud.bote.entity.workspace.WorkspaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作空间 DTO
 *
 * @author chen.linfa
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString(callSuper = true)
public class WorkspaceDTO extends WorkspaceEntity {

  @Schema(description = "修改人名称")
  private String updatorName;

  @Schema(description = "用户数量")
  private Integer userCount;

  @Schema(description = "项目数量")
  private Integer tenantCount;
}
