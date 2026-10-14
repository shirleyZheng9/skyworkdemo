package com.iwhalecloud.bote.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 模型累计调用分布项。
 *
 */
@Schema(description = "模型累计调用分布项")
public class ModelUsageDistributionItemVO {
  @Schema(description = "模型产品系列编码；无法识别产品系列的模型固定返回 other", example = "1100")
  private final String code;

  @Schema(description = "模型产品系列展示值；暂不翻译字典，默认与 code 一致，other 返回自建/其他", example = "1100")
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
