package com.iwhalecloud.bote.dto.lcdp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网关信息
 *
 * @author qian.sisheng
 * @since 2025-06-04
 */
@Setter
@Getter
@ToString
public class LcdpGatewayDTO {
  @Schema(description = "环境编码: dev,test,prod")
  private String envCode;
  @Schema(description = "环境实例列表")
  private List<EnvironmentInst> environmentInsts;

  /**
   * 环境实例
   */
  @Setter
  @Getter
  @ToString
  public static class EnvironmentInst {
    @Schema(description = "网关编码")
    private String attrCode;
    @Schema(description = "网关值")
    private String attrValue;
    @Schema(description = "环境类型")
    private String environmentType;
  }
}
