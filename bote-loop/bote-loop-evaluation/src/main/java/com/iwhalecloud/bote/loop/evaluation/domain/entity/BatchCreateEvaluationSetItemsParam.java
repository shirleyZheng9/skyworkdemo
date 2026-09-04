package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建评估集数据项参数
 * 对应Go: BatchCreateEvaluationSetItemsParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateEvaluationSetItemsParam {

  private Long spaceId;
  private Long evaluationSetId;
  private List<EvaluationSetItem> items;

  /**
   * items 中存在无效数据时，默认不会写入任何数据；设置 skipInvalidItems=true 会跳过无效数据，写入有效数据
   */
  private Boolean skipInvalidItems;

  /**
   * 批量写入 items 如果超出数据集容量限制，默认不会写入任何数据；设置 partialAdd=true 会写入不超出容量限制的前 N 条
   */
  private Boolean allowPartialAdd;
}
