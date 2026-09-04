package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询数据集参数
 * 迁移对应关系: Go语言ListDatasetsParam结构体
 * - 功能: 查询数据集列表的参数对象
 * - 字段定义:
 * * spaceId: Long - 空间ID
 * * evaluationSetIds: List<Long> - 评估集ID列表
 * * name: String - 数据集名称
 * * creators: List<String> - 创建者列表
 * * pageNumber: Integer - 页码
 * * pageSize: Integer - 页大小
 * * pageToken: String - 分页令牌
 * * orderBys: List<OrderBy> - 排序条件列表
 * <p>
 * Java实现说明:
 * - 对应Go的ListDatasetsParam结构体
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go int64 -> Java Long
 * - Go []int64 -> Java List<Long>
 * - Go *string -> Java String
 * - Go []string -> Java List<String>
 * - Go *int32 -> Java Integer
 * - Go []*entity.OrderBy -> Java List<OrderBy>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDatasetsParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言ListDatasetsParam.SpaceID
   */
  private Long spaceId;

  /**
   * 评估集ID列表
   * 迁移对应关系: Go语言ListDatasetsParam.EvaluationSetIDs
   */
  private List<Long> evaluationSetIds;

  /**
   * 数据集名称
   * 迁移对应关系: Go语言ListDatasetsParam.Name
   */
  private String name;

  /**
   * 创建者列表
   * 迁移对应关系: Go语言ListDatasetsParam.Creators
   */
  private List<String> creators;
  private String catalogItemId;

  /**
   * 页码
   * 迁移对应关系: Go语言ListDatasetsParam.PageNumber
   */
  private Integer pageNumber;

  /**
   * 页大小
   * 迁移对应关系: Go语言ListDatasetsParam.PageSize
   */
  private Integer pageSize;

  /**
   * 分页令牌
   * 迁移对应关系: Go语言ListDatasetsParam.PageToken
   */
  private String pageToken;

  /**
   * 排序条件列表
   * 迁移对应关系: Go语言ListDatasetsParam.OrderBys
   */
  private List<OrderBy> orderBys;
}
