package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器领域对象
 * 迁移对应关系: Go语言entity.ExptTurnResultFilterEntity
 * - 功能: 实验轮次结果过滤器的领域模型
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * exptId: Long - 实验ID
 * * itemId: Long - 数据项ID
 * * itemIdx: Integer - 数据项序号
 * * turnId: Long - 轮次ID
 * * status: ItemRunState - 状态枚举
 * * evalTargetData: Map<String, String> - 评估目标数据
 * * evaluatorScore: Map<String, Double> - 评估器得分
 * * annotationFloat: Map<String, Double> - 浮点型注释
 * * annotationBool: Map<String, Boolean> - 布尔型注释
 * * annotationString: Map<String, String> - 字符串型注释
 * * evaluatorScoreCorrected: Boolean - 评估器得分是否修正
 * * evalSetVersionId: Long - 评估集版本ID
 * * createdDate: LocalDateTime - 创建日期
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ExptTurnResultFilterEntity结构体
 * - 使用Lombok注解简化代码
 * - 支持复杂的数据类型映射
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go map[string]string -> Java Map<String, String>
 * - Go map[string]float64 -> Java Map<String, Double>
 * - Go map[string]bool -> Java Map<String, Boolean>
 * - Go int32 -> Java Integer
 * - Go bool -> Java Boolean
 * - Go time.Time -> Java Date
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultFilterEntityDO {

  /**
   * 空间ID
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long spaceId;

  /**
   * 实验ID
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long exptId;

  /**
   * 数据项ID
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.ItemID
   * - 功能: 数据项标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long itemId;

  /**
   * 数据项序号
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.ItemIdx
   * - 功能: 数据项在实验中的序号
   * - 类型: Go的int32对应Java的Integer
   */
  private Integer itemIdx;

  /**
   * 轮次ID
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.TurnID
   * - 功能: 轮次标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long turnId;

  /**
   * 状态
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.Status
   * - 功能: 数据项运行状态
   * - 类型: Go的entity.ItemRunState对应Java的ItemRunState枚举
   */
  private ItemRunState status;

  /**
   * 评估目标数据
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.EvalTargetData
   * - 功能: 评估目标相关数据
   * - 类型: Go的map[string]string对应Java的Map<String, String>
   */
  private Map<String, String> evalTargetData;

  /**
   * 评估器得分
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.EvaluatorScore
   * - 功能: 评估器评分数据
   * - 类型: Go的map[string]float64对应Java的Map<String, Double>
   */
  private Map<String, Double> evaluatorScore;

  /**
   * 浮点型注释
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.AnnotationFloat
   * - 功能: 浮点型注释数据
   * - 类型: Go的map[string]float64对应Java的Map<String, Double>
   */
  private Map<String, Double> annotationFloat;

  /**
   * 布尔型注释
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.AnnotationBool
   * - 功能: 布尔型注释数据
   * - 类型: Go的map[string]bool对应Java的Map<String, Boolean>
   */
  private Map<String, Boolean> annotationBool;

  /**
   * 字符串型注释
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.AnnotationString
   * - 功能: 字符串型注释数据
   * - 类型: Go的map[string]string对应Java的Map<String, String>
   */
  private Map<String, String> annotationString;

  /**
   * 评估器得分是否修正
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.EvaluatorScoreCorrected
   * - 功能: 标识评估器得分是否经过修正
   * - 类型: Go的bool对应Java的Boolean
   */
  private Boolean evaluatorScoreCorrected;

  /**
   * 评估集版本ID
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.EvalSetVersionID
   * - 功能: 评估集版本标识
   * - 类型: Go的int64对应Java的Long
   */
  private Long evalSetVersionId;

  /**
   * 创建日期
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.CreatedDate
   * - 功能: 数据创建日期
   * - 类型: Go的time.Time对应Java的LocalDateTime
   */
  private Date createdDate;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity.UpdatedAt
   * - 功能: 记录最后更新时间
   * - 类型: Go的time.Time对应Java的Date
   */
  private Date updatedAt;
}
