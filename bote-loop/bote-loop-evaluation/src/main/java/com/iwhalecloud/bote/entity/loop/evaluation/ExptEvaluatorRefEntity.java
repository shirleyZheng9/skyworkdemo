package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 实验评估器引用持久化对象
 * 迁移对应关系: Go语言model.ExptEvaluatorRef
 * - 功能: 实验评估器引用的数据模型
 * - 表名: expt_evaluator_ref
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间 id
 * * exptId: Long - 实验 id
 * * evaluatorId: Long - 评估器 id
 * * evaluatorVersionId: Long - 评估器版本 id
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptEvaluatorRef结构体
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
@Table(name = "expt_evaluator_ref")
public class ExptEvaluatorRefEntity {

  /**
   * id
   * 迁移对应关系: Go语言ExptEvaluatorRef.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间 id
   * 迁移对应关系: Go语言ExptEvaluatorRef.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 实验 id
   * 迁移对应关系: Go语言ExptEvaluatorRef.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptId;

  /**
   * 评估器 id
   * 迁移对应关系: Go语言ExptEvaluatorRef.EvaluatorID
   * - 功能: 评估器标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evaluatorId;

  /**
   * 评估器版本 id
   * 迁移对应关系: Go语言ExptEvaluatorRef.EvaluatorVersionID
   * - 功能: 评估器版本标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evaluatorVersionId;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptEvaluatorRef.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptEvaluatorRef.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptEvaluatorRef.DeletedAt
   * - 功能: 软删除时间
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;
}
