package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据项系统信息实体
 * 对应Go: entity.ItemSystemInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSystemInfo {

  /**
   * 运行状态
   * 对应Go: RunState ItemRunState
   */
  private ItemRunState runState;

  /**
   * 日志ID
   * 对应Go: LogID *string
   */
  private String logId;

  /**
   * 错误信息
   * 对应Go: Error *RunError
   */
  private RunError error;
}
