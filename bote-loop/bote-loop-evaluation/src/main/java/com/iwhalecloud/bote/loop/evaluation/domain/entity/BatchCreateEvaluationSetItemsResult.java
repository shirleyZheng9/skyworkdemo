package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建评估集数据项结果
 * 对应Go: BatchCreateEvaluationSetItemsResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateEvaluationSetItemsResult {

  /**
   * ID映射关系：原始ID -> 新生成的ID
   */
  private Map<Long, Long> idMap;

  /**
   * 错误信息列表
   */
  private List<ItemErrorGroup> errors;
}
