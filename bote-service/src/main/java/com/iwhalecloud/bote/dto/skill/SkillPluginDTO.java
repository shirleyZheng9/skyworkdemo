package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillPluginEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：插件 DTO
 *
 * @author auto
 * @since 2024-09-21
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillPluginDTO extends SkillPluginEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "复制的插件ID")
  private Long copyApiId;
}
