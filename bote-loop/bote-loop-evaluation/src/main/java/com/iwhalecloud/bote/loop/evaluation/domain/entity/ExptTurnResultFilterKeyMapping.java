package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤键映射领域对象
 * 迁移对应关系: Go语言entity.ExptTurnResultFilterKeyMapping
 * - 功能: 实验轮次结果过滤键映射的领域模型
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * exptId: Long - 实验ID
 * * fromField: String - 筛选项唯一键，评估器: evaluator_version_id，人工标准：tag_key_id
 * * toKey: String - ck侧的map key，评估器：key1 ~ key10，人工标准：key1 ~ key100
 * * fieldType: FieldTypeMapping - 映射类型，Evaluator —— 1，人工标注—— 2
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ExptTurnResultFilterKeyMapping结构体
 * - 使用Lombok注解简化代码
 * - 支持过滤键映射功能
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go FieldTypeMapping -> Java FieldTypeMapping枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterKeyMapping {

  /**
   * 主键ID
   */
  private Long id;

  /**
   * 空间ID
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - JSON标签: space_id
   */
  private Long spaceId;

  /**
   * 实验ID
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - JSON标签: expt_id
   */
  private Long exptId;

  /**
   * 筛选项唯一键
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.FromField
   * - 功能: 筛选项唯一键，评估器: evaluator_version_id，人工标准：tag_key_id
   * - 类型: Go的string对应Java的String
   * - JSON标签: from_field
   */
  private String fromField;

  /**
   * ck侧的map key
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.ToKey
   * - 功能: ck侧的map key，评估器：key1 ~ key10，人工标准：key1 ~ key100
   * - 类型: Go的string对应Java的String
   * - JSON标签: to_key
   */
  private String toKey;

  /**
   * 映射类型
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMapping.FieldType
   * - 功能: 映射类型，Evaluator —— 1，人工标注—— 2
   * - 类型: Go的FieldTypeMapping对应Java的FieldTypeMapping枚举
   * - JSON标签: field_type
   */
  private FieldTypeMapping fieldType;
}
