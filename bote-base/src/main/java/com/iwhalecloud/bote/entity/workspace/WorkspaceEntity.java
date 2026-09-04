package com.iwhalecloud.bote.entity.workspace;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作空间 Entity
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workspace")
public class WorkspaceEntity extends BaseEntity {

  @Schema(description = "主键")
  private Long spaceId;

  @Schema(description = "名称")
  private String spaceName;

  @Schema(description = "图标")
  private String spaceIcon;

  @Schema(description = "空间租户id")
  private Long spaceTenantId;

  @Schema(description = "外系统空间id")
  private String extSpaceId;
}
