package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 灵犀参数
 *
 * @author qian.sisheng
 * @since 2025-06-06
 */

@Getter
@Setter
@ToString
public class LcdpParams {
  @Schema(description = "属性ID")
  private String serviceAttrId;
  @Schema(description = "属性编码")
  private String code;
  @Schema(description = "属性名称")
  private String name;
  @Schema(description = "属性类型: 参数类型 object对象；array数组；field属性")
  private String attrType;
  @Schema(description = "是否必填")
  private String mustFlag;
  @Schema(description = "默认值")
  private String defaultValue;
  @Schema(description = "类型: 日期型 1000, 日期时间型 1100, 字符型 1200, 浮点型 1300, 整数型 1400, 布尔型 1500, 对象型 1600, 数组型 1700")
  private String type;
  @Schema(description = "子属性")
  private List<LcdpParams> children;
}
