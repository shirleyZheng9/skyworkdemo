package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BizCategory;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Session;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建数据集参数
 * 迁移对应关系: Go语言CreateDatasetParam结构体
 * - 功能: 创建数据集的参数对象
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * name: String - 数据集名称
 * * desc: String - 数据集描述
 * * evaluationSetItems: EvaluationSetSchema - 评估集项目模式
 * * bizCategory: BizCategory - 业务分类
 * * session: Session - 会话信息
 * <p>
 * Java实现说明:
 * - 对应Go的CreateDatasetParam结构体
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *string -> Java String
 * - Go *entity.EvaluationSetSchema -> Java EvaluationSetSchema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDatasetParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言CreateDatasetParam.SpaceID
   */
  private Long spaceId;

  /**
   * 数据集名称
   * 迁移对应关系: Go语言CreateDatasetParam.Name
   */
  private String name;

  /**
   * 数据集描述
   * 迁移对应关系: Go语言CreateDatasetParam.Desc
   */
  private String desc;

  /**
   * 评估集项目模式
   * 迁移对应关系: Go语言CreateDatasetParam.EvaluationSetItems
   */
  private EvaluationSetSchema evaluationSetItems;

  /**
   * 业务分类
   * 迁移对应关系: Go语言CreateDatasetParam.BizCategory
   */
  private BizCategory bizCategory;

  /**
   * 会话信息
   * 迁移对应关系: Go语言CreateDatasetParam.Session
   */
  private Session session;

  /**
   * 目录ID
   */
  private Long catalogItemId;
}
