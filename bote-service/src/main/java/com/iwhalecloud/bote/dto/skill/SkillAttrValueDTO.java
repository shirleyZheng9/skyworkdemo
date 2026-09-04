package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillAttrValueEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：属性值 DTO
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillAttrValueDTO extends SkillAttrValueEntity {
  @Schema(description = "操作类型")
  private String actionType;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "更新人名称")
  private String updatorName;
  @DiffField(childNode = true)
  @Schema(description = "属性值关联列表-对应数据库")
  private List<SkillAttrValueRelDTO> attrValueRelList;
}
