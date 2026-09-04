package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 目标结果
 * 对应Go: TargetResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetResult {

  /**
   * 目标ID
   * 对应Go: TargetID int64
   */
  private Long targetId;

  /**
   * 目标版本ID
   * 对应Go: TargetVersionID int64
   */
  private Long targetVersionId;

  /**
   * 目标名称
   * 对应Go: TargetName string
   */
  private String targetName;

  /**
   * 输出内容
   * 对应Go: OutputContent map[string]interface{}
   */
  private Map<String, Object> outputContent;

  /**
   * 运行状态
   * 对应Go: RunStatus string
   */
  private String runStatus;

  /**
   * 错误信息
   * 对应Go: ErrorMessage string
   */
  private String errorMessage;

  /**
   * 运行时间（毫秒）
   * 对应Go: RunTimeMs int64
   */
  private Long runTimeMs;

  /**
   * 创建时间
   * 对应Go: CreatedAt int64
   */
  private Long createdAt;
}
