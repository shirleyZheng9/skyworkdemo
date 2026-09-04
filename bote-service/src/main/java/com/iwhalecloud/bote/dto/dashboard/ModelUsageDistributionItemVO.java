package com.iwhalecloud.bote.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模型累计调用分布项。
 *
 */
@Schema(description = "模型累计调用分布项")
public class ModelUsageDistributionItemVO {
  @Schema(description = "模型产品系列编码；为空表示未配置产品系列", example = "1100")
  private final String code;

  @Schema(description = "模型产品系列展示值；后端暂不翻译字典，由前端按 code 匹配 MODEL_PRODUCT_TYPE", example = "通义千问")
  private final String name;

  @Schema(description = "自统计功能上线以来的累计调用次数", example = "500")
  private final Long count;

  public ModelUsageDistributionItemVO(String code, String name, Long count) {
    this.code = code;
    this.name = name;
    this.count = count;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public Long getCount() {
    return count;
  }
}
