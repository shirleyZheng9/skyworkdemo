package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 静态属性
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Getter
@Setter
@ToString
public class SimpleAttrDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "静态属性值 ID")
  private Long attrValueId;
  @Schema(description = "静态属性 ID")
  private Long attrId;
  @Schema(description = "静态属性值")
  private String attrValue;
  @Schema(description = "静态属性值名称")
  private String attrValueName;
  @Schema(description = "静态属性值编码")
  private String attrValueCode;
  @Schema(description = "静态属性值备注")
  private String attrValueDesc;
  @Schema(description = "静态属性排序")
  private Integer sortby;
  @Schema(description = "静态数据编码")
  private String attrCode;
  @Schema(description = "静态数据名称")
  private String attrName;
  @Schema(description = "静态属性是否必填")
  private String isRequired;
  @Schema(description = "属性渲染风格")
  private String compType;
  @Schema(description = "关联静态属性列表")
  private List<SimpleAttrDTO> attrRelList;
}
