package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 实验轮次结果过滤键映射持久化对象
 * 迁移对应关系: Go语言model.ExptTurnResultFilterKeyMapping
 * - 功能: 实验轮次结果过滤键映射的数据模型
 * - 表名: expt_turn_result_filter_key_mapping
 * - 字段定义:
 * * id: Long - 自增主键
 * * spaceId: Long - 空间id
 * * exptId: Long - 实验id
 * * fromField: String - 筛选项唯一键，评估器: evaluator_version_id，人工标准：tag_key_id
 * * toKey: String - ck侧的map key，评估器：key1 ~ key10，人工标准：key1 ~ key100
 * * fieldType: Integer - 映射类型，Evaluator —— 1，人工标注—— 2
 * * createdAt: LocalDateTime - 创建时间
 * * deletedAt: Long - 删除时间
 * * createdBy: String - 创建人
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptTurnResultFilterKeyMapping结构体
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
@Table(name = "bt_expt_filter_key_map")
public class ExptTurnResultFilterKeyMappingEntity {

  /**
   * 自增主键
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint, not null
   */
  private Long spaceId;

  /**
   * 实验id
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint, not null
   */
  private Long exptId;

  /**
   * 筛选项唯一键，评估器: evaluator_version_id，人工标准：tag_key_id
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.FromField
   * - 功能: 筛选项唯一键
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String fromField;

  /**
   * ck侧的map key，评估器：key1 ~ key10，人工标准：key1 ~ key100
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.ToKey
   * - 功能: ck侧的map key
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String toKey;

  /**
   * 映射类型，Evaluator —— 1，人工标注—— 2
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.FieldType
   * - 功能: 映射类型
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int, not null
   */
  private Integer fieldType;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.DeletedAt
   * - 功能: 软删除时间
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * 创建人
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255)
   */
  private String createdBy;
}
