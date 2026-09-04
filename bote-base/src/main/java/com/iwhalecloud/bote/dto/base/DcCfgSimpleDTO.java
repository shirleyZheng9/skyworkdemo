package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统参数基本信息
 *
 * @author qian.sisheng
 * @since 2025-07-10
 */
@Data
@Schema(description = "系统参数基本信息")
@JsonInclude(Include.NON_NULL)
public class DcCfgSimpleDTO {
  @Schema(description = "静态数据编码")
  private String paramCode;
  @Schema(description = "属性名称")
  private String paramName;
  @Schema(description = "属性描述")
  private String paramDesc;
  @Schema(description = "当前值")
  private String paramVal;
  @Schema(description = "取值来源(特殊标记或 JSON 字符串)")
  private String valueSource;
  @Schema(description = "是否需要更新")
  private String needUpdate;
  @Schema(description = "子类型, model 大模型;select:下拉框;image:图片base64")
  private String subType;
}
