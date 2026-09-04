package com.iwhalecloud.bote.entity.agent;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户级的提示词 Entity
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_AI_WORKSPACE")
public class AiWorkspaceEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用ID")
  private Long botId;
  @DiffField(name = "FILE_NAME")
  @Schema(description = "文件名")
  private String fileName;
  @DiffField(name = "FILE_CONTENT")
  @Schema(description = "提示词内容")
  private String fileContent;
  @DiffField(name = "MEMORY_TYPE")
  @Schema(description = "记忆类型（LONG_TERM: 长期记忆, DAILY: 每日记忆）")
  private String memoryType;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景ID")
  private Long sceneId;
}
