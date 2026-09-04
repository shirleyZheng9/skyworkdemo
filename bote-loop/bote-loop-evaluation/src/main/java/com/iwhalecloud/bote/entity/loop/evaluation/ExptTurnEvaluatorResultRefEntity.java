package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 实验轮次评估器结果引用持久化对象
 * 迁移对应关系: Go语言model.ExptTurnEvaluatorResultRef
 * - 功能: 实验轮次评估器结果引用的数据模型
 * - 表名: expt_turn_evaluator_result_ref
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间 id
 * * exptTurnResultId: Long - 实验 turn result id
 * * evaluatorVersionId: Long - 评估器版本 id
 * * evaluatorResultId: Long - 评估器结果 id
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * exptId: Long - 实验 id
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptTurnEvaluatorResultRef结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go soft_delete.DeletedAt -> Java Long
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_turn_evaluator_result_ref")
public class ExptTurnEvaluatorResultRefEntity {

  /**
   * id
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间 id
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 实验 turn result id
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.ExptTurnResultID
   * - 功能: 实验轮次结果标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptTurnResultId;

  /**
   * 评估器版本 id
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.EvaluatorVersionID
   * - 功能: 评估器版本标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evaluatorVersionId;

  /**
   * 评估器结果 id
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.EvaluatorResultID
   * - 功能: 评估器结果标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evaluatorResultId;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private LocalDateTime createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private LocalDateTime updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.DeletedAt
   * - 功能: 软删除时间
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * 实验 id
   * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptId;
}
