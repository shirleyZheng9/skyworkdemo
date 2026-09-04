package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次系统信息实体
 * 对应Go: entity.TurnSystemInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnSystemInfo {

  /**
   * 轮次运行状态
   * 对应Go: TurnRunState TurnRunState
   */
  private TurnRunState turnRunState;

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
