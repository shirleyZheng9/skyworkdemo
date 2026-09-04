package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bote.entity.skill.SkillAttrSpecEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：属性 DTO
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillAttrSpecDTO extends SkillAttrSpecEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "属性值列表")
  @DiffField(childNode = true)
  private List<SkillAttrValueDTO> attrValues;
  @Schema(description = "复制的属性ID")
  private Long copyAttrId;
}
