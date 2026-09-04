package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量获取数据集项目参数
 * 迁移对应关系: Go语言BatchGetDatasetItemsParam结构体
 * - 功能: 批量获取数据集项目的参数对象
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * evaluationSetId: Long - 评估集ID
 * * itemIds: List<Long> - 项目ID列表
 * * versionId: Long - 版本ID
 * <p>
 * Java实现说明:
 * - 对应Go的BatchGetDatasetItemsParam结构体
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go []int64 -> Java List<Long>
 * - Go *int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetDatasetItemsParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言BatchGetDatasetItemsParam.SpaceID
   */
  private Long spaceId;

  /**
   * 评估集ID
   * 迁移对应关系: Go语言BatchGetDatasetItemsParam.EvaluationSetID
   */
  private Long evaluationSetId;

  /**
   * 项目ID列表
   * 迁移对应关系: Go语言BatchGetDatasetItemsParam.ItemIDs
   */
  private List<Long> itemIds;

  /**
   * 版本ID
   * 迁移对应关系: Go语言BatchGetDatasetItemsParam.VersionID
   */
  private Long versionId;
}
