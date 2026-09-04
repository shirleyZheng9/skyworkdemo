package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能发布申请 Entity
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_publish_apply")
public class SkillPublishApplyEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键 ID")
  private Long applyId;
  @DiffField(name = "SKILL_ID")
  @Schema(description = "技能 ID")
  private Long skillId;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间 ID")
  private Long spaceId;
  @DiffField(name = "APPLY_CONTENT")
  @Schema(description = "申请描述内容")
  private String applyContent;
  @DiffField(name = "AUDIT_STATUS")
  @Schema(description = "审核状态（0:待审核，1:已通过，2:未通过，-1:失效）")
  private Integer auditStatus;
  @DiffField(name = "AUDIT_USER_ID")
  @Schema(description = "审核人 ID")
  private Long auditUserId;
  @DiffField(name = "AUDIT_CONTENT")
  @Schema(description = "审核内容")
  private String auditContent;
}
