package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统参数信息保存请求对象
 */
@Getter
@Setter
@ToString
@Schema(description = "系统参数信息保存请求对象")
public class DcCfgSaveRequest {
  @Schema(description = "参数编码")
  private String paramCode;
  @Schema(description = "参数值")
  private String paramVal;
}
