package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新数据集参数
 * 迁移对应关系: Go语言service.UpdateDatasetParam
 * - 功能: 更新数据集的参数对象
 * - 字段定义: 各种更新数据集所需的字段
 * <p>
 * Java实现说明:
 * - 对应Go的service.UpdateDatasetParam结构体
 * - 使用Lombok注解简化代码
 * - 提供数据集更新所需的参数
 * - 支持Builder模式构建对象
 * - 支持有参和无参构造器
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go指针 -> Java包装类型
 * - Go字段 -> Java属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDatasetParam {

  /**
   * 空间ID
   * 迁移对应关系: Go语言UpdateDatasetParam.SpaceID
   * - 功能: 标识数据集所属的空间
   * - 类型: int64 -> Long
   * - 用途: 空间隔离
   */
  private Long spaceId;

  /**
   * 数据集ID
   * 迁移对应关系: Go语言UpdateDatasetParam.DatasetID
   * - 功能: 标识要更新的数据集
   * - 类型: int64 -> Long
   * - 用途: 数据集标识
   */
  private Long datasetId;

  /**
   * 数据集名称
   * 迁移对应关系: Go语言UpdateDatasetParam.Name
   * - 功能: 数据集的新名称
   * - 类型: string -> String
   * - 用途: 数据集名称更新
   */
  private String name;

  /**
   * 数据集描述
   * 迁移对应关系: Go语言UpdateDatasetParam.Description
   * - 功能: 数据集的新描述
   * - 类型: *string -> String
   * - 用途: 数据集描述更新
   * - 注意: 可为空，表示不更新描述
   */
  private String description;

  /**
   * 更新者
   * 迁移对应关系: Go语言UpdateDatasetParam.UpdatedBy
   * - 功能: 标识更新操作的用户
   * - 类型: string -> String
   * - 用途: 审计日志记录
   */
  private String updatedBy;

  /**
   * 目录ID
   */
  private Long catalogItemId;

  /**
   * 最新的操作
   */
  private DatasetOpType lastOperation;
}
