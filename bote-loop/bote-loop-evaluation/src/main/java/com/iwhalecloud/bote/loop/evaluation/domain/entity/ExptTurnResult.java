package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果实体
 * 迁移对应关系: Go语言ExptTurnResult
 * - 功能: 实验轮次结果数据结构
 * - 字段: id, spaceId, exptId, exptRunId, itemId, turnId, status, traceId, logId, targetResultId, evaluatorResults, errMsg, turnIdx
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResult结构体
 * - 使用Lombok注解简化代码
 * - JSON序列化使用camelCase命名（研发规范2）
 * - 实现ToRunLogDO方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go *EvaluatorResults -> Java EvaluatorResults
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResult {
  private Long id;

  private Long spaceId;

  private Long exptId;

  private Long exptRunId;

  private Long itemId;

  private Long turnId;

  private Integer status;

  private Long traceId;

  private String logId;

  private Long targetResultId;

  private EvaluatorResults evaluatorResults;

  private String errMsg;

  private Integer turnIdx;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptTurnResult.CreatedAt
   */
  private Date createdAt;

  /**
   * 转换为运行日志DO
   * 迁移对应关系: Go语言ExptTurnResult.ToRunLogDO()
   */
  public ExptTurnResultRunLog toRunLogDO() {
    return ExptTurnResultRunLog.builder()
      .id(this.id)
      .spaceId(this.spaceId)
      .exptId(this.exptId)
      .exptRunId(this.exptRunId)
      .itemId(this.itemId)
      .turnId(this.turnId)
      .status(TurnRunState.values()[this.status])
      .traceId(this.traceId)
      .logId(this.logId)
      .targetResultId(this.targetResultId)
      .evaluatorResultIds(this.evaluatorResults)
      .errMsg(this.errMsg)
      .build();
  }
}
