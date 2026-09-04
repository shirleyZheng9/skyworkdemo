package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qian.sisheng
 * @since 2024/10/14
 */
@Getter
@Setter
@ToString
public class FunctionTestParams {
  @Schema(description = "函数参数")
  private Map<String, Object> params;
  @Schema(description = "请求参数")
  private String reqJson;
  @Schema(description = "响应参数")
  private String repJson;
  @Schema(description = "函数类型：Groovy,Python3")
  private String funcType;
  @Schema(description = "脚本JSON")
  private String scriptJson;
  @Schema(description = "python包列表")
  private List<String> pyPackageList;
}
