package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单属性值关联 DTO
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
@Getter
@Setter
@ToString
public class SimpleAttrValueRelDTO {

  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "Z端属性 ID")
  private Long zAttrId;

  @Schema(description = "A端属性值 ID")
  private Long aAttrValueId;

  @Schema(description = "Z端属性值 ID")
  private Long zAttrValueId;

  @Schema(description = "Z端属性编码")
  private String zAttrNbr;

  @Schema(description = "Z端属性名称")
  private String zAttrName;

  @Schema(description = "Z端属性值名称")
  private String zAttrValueName;

  @Schema(description = "Z端属性值")
  private String zAttrValue;
}
