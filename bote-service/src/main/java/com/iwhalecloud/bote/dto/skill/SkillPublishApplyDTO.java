package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.skill.SkillPublishApplyEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能发布申请 DTO
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillPublishApplyDTO extends SkillPublishApplyEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "技能名称")
  private String skillName;
  @Schema(description = "审核人名称")
  private String auditUserName;
  @Schema(description = "空间名称")
  private String spaceName;
  @Schema(description = "文件信息 ID")
  private Long fileInfoId;
  @Schema(description = "技能描述")
  private String skillDesc;
}
