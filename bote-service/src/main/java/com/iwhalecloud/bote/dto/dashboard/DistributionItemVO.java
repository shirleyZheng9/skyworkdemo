package com.iwhalecloud.bote.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 看板分布统计项。
 * count 使用 Long，避免大租户 数据量超过 Integer 上限；ratio 使用 BigDecimal，避免浮点数精度误差。
 *
 * @author zhengxueli
 * @since 2026-08-28
 */
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "看板分布统计项")
public class DistributionItemVO {

  @Schema(description = "分类编码", example = "scene")
  private final String code;

  /** 用于页面展示的分类名称。 */
  @Schema(description = "分类名称", example = "自主规划")
  private final String name;

  /** 当前分类的有效数据量。 */
  @Schema(description = "数量", example = "257")
  private final Long count;

  /** 当前分类占总数的百分比，保留两位小数。 */
  @Schema(description = "占比（百分数）", example = "22.46")
  private final BigDecimal ratio;
}
