package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * claw 智能体定义工作区 Entity
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_CLAW_WORKSPACE")
public class ClawWorkspaceEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "SCENE_ID", parent = true)
  @Schema(description = "场景 ID")
  private Long sceneId;

  @DiffField(name = "FILE_NAME")
  @Schema(description = "文件名")
  private String fileName;

  @DiffField(name = "FILE_CONTENT")
  @Schema(description = "文件内容")
  private String fileContent;

  @DiffField(name = "MEMORY_TYPE")
  @Schema(description = "记忆类型")
  private String memoryType;
}
