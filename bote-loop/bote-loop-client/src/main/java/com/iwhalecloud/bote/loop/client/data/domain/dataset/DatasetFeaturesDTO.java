package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集特性DTO
 * 对应Go: dataset.DatasetFeatures
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "数据集特性DTO")
public class DatasetFeaturesDTO {

  /**
   * 是否可编辑模式
   * 对应Go: EditSchema bool
   */
  @Schema(description = "是否可编辑模式")
  private Boolean editSchema;

  /**
   * 是否支持重复数据
   * 对应Go: RepeatedData bool
   */
  @Schema(description = "是否支持重复数据")
  private Boolean repeatedData;

  /**
   * 是否支持多模态
   * 对应Go: MultiModal bool
   */
  @Schema(description = "是否支持多模态")
  private Boolean multiModal;
}
