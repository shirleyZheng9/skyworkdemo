package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据项快照映射查询结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSnapshotMappingResult {

  private List<ItemSnapshotFieldMapping> fieldMappings;
  private String syncCkDate;
}
