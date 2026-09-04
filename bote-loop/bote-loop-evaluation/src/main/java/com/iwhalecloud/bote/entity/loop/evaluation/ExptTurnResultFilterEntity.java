package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果过滤器持久化对象
 * 迁移对应关系: Go语言model.ExptTurnResultFilter
 * - 功能: 实验轮次结果过滤器表的数据模型
 * - 表名: expt_turn_result_filter
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间id
 * * exptId: Long - 实验id
 * * itemId: Long - item_id
 * * itemIdx: Integer - item序号
 * * turnId: Long - turn_id
 * * status: Integer - 状态
 * * evalSetVersionId: Long - 评测集版本id
 * * createdDate: LocalDateTime - 创建日期
 * * evalTargetData: String - 评估对象数据(JSON)
 * * evaluatorScore: String - 评估器得分(JSON)
 * * annotationFloat: String - 浮点标注(JSON)
 * * annotationBool: String - 布尔标注(JSON)
 * * annotationString: String - 字符串标注(JSON)
 * * evaluatorScoreCorrected: Boolean - 评估器得分是否修正
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptTurnResultFilter结构体
 * - 使用MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持JSON字段存储
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go bool -> Java Boolean
 * - Go JSON字段 -> Java String存储JSON
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_turn_result_filter")
public class ExptTurnResultFilterEntity {

  /**
   * 主键ID
   * 迁移对应关系: Go语言ExptTurnResultFilter.ID
   */
  @Id
  private Long id;

  /**
   * 空间ID
   * 迁移对应关系: Go语言ExptTurnResultFilter.SpaceID
   */
  private Long spaceId;

  /**
   * 实验ID
   * 迁移对应关系: Go语言ExptTurnResultFilter.ExptID
   */
  private Long exptId;

  /**
   * 项目ID
   * 迁移对应关系: Go语言ExptTurnResultFilter.ItemID
   */
  private Long itemId;

  /**
   * 项目序号
   * 迁移对应关系: Go语言ExptTurnResultFilter.ItemIdx
   */
  private Integer itemIdx;

  /**
   * 轮次ID
   * 迁移对应关系: Go语言ExptTurnResultFilter.TurnID
   */
  private Long turnId;

  /**
   * 状态
   * 迁移对应关系: Go语言ExptTurnResultFilter.Status
   */
  private Integer status;

  /**
   * 评测集版本ID
   * 迁移对应关系: Go语言ExptTurnResultFilter.EvalSetVersionID
   */
  private Long evalSetVersionId;

  /**
   * 创建日期
   * 迁移对应关系: Go语言ExptTurnResultFilter.CreatedDate
   */
  private Date createdDate;

  /**
   * 评估对象数据(JSON)
   * 迁移对应关系: Go语言ExptTurnResultFilter.EvalTargetData
   */
  private String evalTargetData;

  /**
   * 评估器得分(JSON)
   * 迁移对应关系: Go语言ExptTurnResultFilter.EvaluatorScore
   */
  private String evaluatorScore;

  /**
   * 浮点标注(JSON)
   * 迁移对应关系: Go语言ExptTurnResultFilter.AnnotationFloat
   */
  private String annotationFloat;

  /**
   * 布尔标注(JSON)
   * 迁移对应关系: Go语言ExptTurnResultFilter.AnnotationBool
   */
  private String annotationBool;

  /**
   * 字符串标注(JSON)
   * 迁移对应关系: Go语言ExptTurnResultFilter.AnnotationString
   */
  private String annotationString;

  /**
   * 评估器得分是否修正
   * 迁移对应关系: Go语言ExptTurnResultFilter.EvaluatorScoreCorrected
   */
  private Boolean evaluatorScoreCorrected;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptTurnResultFilter.CreatedAt
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptTurnResultFilter.UpdatedAt
   */
  private Date updatedAt;
}
