package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 灵犀平台属性规格信息
 *
 * @author qian.sisheng
 * @since 2025-06-09
 */

@Setter
@Getter
@ToString
@Schema(description = "灵犀平台属性规格信息")
public class LcdpAttrSpecDTO {
  @Schema(description = "属性规格ID")
  private Long attrId;
  @Schema(description = "属性规格名称")
  private String attrName;
  @Schema(description = "属性规格描述")
  private String attrDesc;
  @Schema(description = "属性规格编码")
  private String attrNbr;
  @Schema(description = "应用ID")
  private Long appId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "属性规格值列表")
  private List<LcdpAttrValueDTO> attrValueDTOList;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "属性ID列表")
  private List<Long> attrIds;
}
