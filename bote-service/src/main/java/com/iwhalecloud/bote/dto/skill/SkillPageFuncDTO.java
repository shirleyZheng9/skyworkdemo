package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillPageFuncEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：页面函数 DTO
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillPageFuncDTO extends SkillPageFuncEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "复制的页面函数ID")
  private Long copyPageFuncId;
}
