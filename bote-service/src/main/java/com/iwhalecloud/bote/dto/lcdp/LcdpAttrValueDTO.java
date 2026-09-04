package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 灵犀平台属性值信息
 *
 * @author qian.sisheng
 * @since 2025-06-09
 */

@Setter
@Getter
@ToString
@Schema(description = "灵犀平台属性值信息")
public class LcdpAttrValueDTO {
  @Schema(description = "属性值ID")
  private Long attrValueId;
  @Schema(description = "属性值")
  private String attrValue;
  @Schema(description = "属性值名称")
  private String attrValueName;
  @Schema(description = "属性值描述")
  private String attrValueDesc;
  @Schema(description = "属性ID")
  private Long attrId;
}
