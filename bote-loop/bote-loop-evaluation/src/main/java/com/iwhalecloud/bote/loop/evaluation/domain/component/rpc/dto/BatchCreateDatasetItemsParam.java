package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建数据集项目参数
 * 迁移对应关系: Go语言BatchCreateDatasetItemsParam结构体
 * - 功能: 批量创建数据集项目的参数对象
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * evaluationSetId: Long - 评估集ID
 * * items: List<EvaluationSetItem> - 项目列表
 * * skipInvalidItems: Boolean - 是否跳过无效项目
 * * allowPartialAdd: Boolean - 是否允许部分添加
 * <p>
 * Java实现说明:
 * - 对应Go的BatchCreateDatasetItemsParam结构体
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * - 支持跳过无效项目和部分添加功能
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go []*entity.EvaluationSetItem -> Java List<EvaluationSetItem>
 * - Go *bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCreateDatasetItemsParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言BatchCreateDatasetItemsParam.SpaceID
   */
  private Long spaceId;

  /**
   * 评估集ID
   * 迁移对应关系: Go语言BatchCreateDatasetItemsParam.EvaluationSetID
   */
  private Long evaluationSetId;

  /**
   * 项目列表
   * 迁移对应关系: Go语言BatchCreateDatasetItemsParam.Items
   */
  private List<EvaluationSetItem> items;

  /**
   * 是否跳过无效项目
   * 迁移对应关系: Go语言BatchCreateDatasetItemsParam.SkipInvalidItems
   * - 功能: 当项目中存在无效数据时，默认不会写入任何数据；设置skipInvalidItems=true会跳过无效数据，写入有效数据
   */
  private Boolean skipInvalidItems;

  /**
   * 是否允许部分添加
   * 迁移对应关系: Go语言BatchCreateDatasetItemsParam.AllowPartialAdd
   * - 功能: 批量写入项目如果超出数据集容量限制，默认不会写入任何数据；设置partialAdd=true会写入不超出容量限制的前N条
   */
  private Boolean allowPartialAdd;
}
