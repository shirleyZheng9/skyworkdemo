package com.iwhalecloud.bote.dto.plugin.params;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 数据单值转换插件参数
 *
 * @author zyt
 * @since 2025-06-30
 */
@Getter
@Setter
@ToString
public class AttrDataTransformParams {

  @Schema(description = "数据编码")
  private String attrCode;

  @Schema(description = "要转换的值【数组】（要转换的值，支持单个或多个值）")
  private List<String> inputValue;

  @Schema(description = "转换方向（name_to_value-属性值名称转属性值，value_to_name-属性值转属性值名称）")
  private String transformDirection;
}
