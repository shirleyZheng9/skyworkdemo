package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索数据集结果
 * 迁移对应关系: Go语言service.SearchDatasetsResults
 * - 功能: 搜索数据集的返回结果对象
 * - 字段定义: 搜索结果列表和分页信息
 * <p>
 * Java实现说明:
 * - 对应Go的service.SearchDatasetsResults结构体
 * - 使用Lombok注解简化代码
 * - 提供搜索结果和分页信息
 * - 支持Builder模式构建对象
 * - 支持有参和无参构造器
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片 -> Java List
 * - Go布尔值 -> Java Boolean
 * - Go字符串 -> Java String
 * - Go整数 -> Java Long
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDatasetsResult {

  /**
   * 数据集列表
   * 迁移对应关系: Go语言SearchDatasetsResults.DatasetWithSchemas
   * - 功能: 搜索结果的数据集列表
   * - 类型: []*DatasetWithSchema -> List<DatasetWithSchema>
   * - 用途: 包含数据集和模式信息的列表
   */
  private List<DatasetWithSchema> datasetWithSchemas;

  /**
   * 是否有更多数据
   * 迁移对应关系: Go语言SearchDatasetsResults.HasMore
   * - 功能: 标识是否还有更多数据
   * - 类型: bool -> Boolean
   * - 用途: 分页控制
   */
  private Boolean hasMore;

  /**
   * 下一页游标
   * 迁移对应关系: Go语言SearchDatasetsResults.NextCursor
   * - 功能: 下一页数据的游标
   * - 类型: string -> String
   * - 用途: 分页导航
   */
  private String nextCursor;

  /**
   * 总数量
   * 迁移对应关系: Go语言SearchDatasetsResults.Total
   * - 功能: 搜索结果的总数量
   * - 类型: int64 -> Long
   * - 用途: 分页信息显示
   */
  private Long total;
}
