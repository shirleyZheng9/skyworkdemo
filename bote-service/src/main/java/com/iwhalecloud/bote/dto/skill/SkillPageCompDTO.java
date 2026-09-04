package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillPageCompEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：页面组件 DTO
 *
 * @author lizuyin
 * @since 2026-01-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillPageCompDTO extends SkillPageCompEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "修改人名称")
  private String updatorName;
}

