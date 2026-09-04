package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillAttrValueRelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 属性值关联 DTO
 *
 * @author qian.sisheng
 * @since 2025-1-16
 */

@Getter
@Setter
@ToString(callSuper = true)
public class SkillAttrValueRelDTO extends SkillAttrValueRelEntity {
  @Schema(description = "Z端属性名称，联动的静态数据")
  private String zAttrName;

  @Schema(description = "Z端属性值名称，联动值名称")
  private String zAttrValueName;

  @Schema(description = "Z端属性值，联动值")
  private String zAttrValue;

  @Schema(description = "Z端属性编码，联动的静态数据编码")
  private String zAttrNbr;
}
