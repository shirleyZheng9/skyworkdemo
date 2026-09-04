package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集规格DTO
 * 对应Go: dataset.DatasetSpec
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "数据集规格DTO")
public class DatasetSpecDTO {

  /**
   * 最大数据项数量
   * 对应Go: MaxItemCount int64
   */
  @Schema(description = "最大数据项数量")
  private Long maxItemCount;

  /**
   * 最大字段数量
   * 对应Go: MaxFieldCount int32
   */
  @Schema(description = "最大字段数量")
  private Integer maxFieldCount;

  /**
   * 最大数据项大小
   * 对应Go: MaxItemSize int64
   */
  @Schema(description = "最大数据项大小")
  private Long maxItemSize;

  /**
   * 最大数据项数据嵌套深度
   * 对应Go: MaxItemDataNestedDepth int32
   */
  @Schema(description = "最大数据项数据嵌套深度")
  private Integer maxItemDataNestedDepth;
}
