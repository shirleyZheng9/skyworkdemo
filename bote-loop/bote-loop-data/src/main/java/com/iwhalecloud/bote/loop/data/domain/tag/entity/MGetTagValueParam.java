package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取标签值参数实体
 * 迁移对应关系: Go语言entity.MGetTagValueParam
 * - 功能: 批量获取标签值的查询参数
 * - 字段定义:
 * * Paginator: *pagination.Paginator - 分页器
 * * SpaceID: int64 - 空间ID
 * * IDs: []int64 - ID列表
 * * Status: *TagStatus - 状态
 * * TagKeyID: *int64 - 标签键ID
 * * Version: *int32 - 版本
 * * TagValueID: []int64 - 标签值ID列表
 * <p>
 * Java实现说明:
 * - 对应Go的entity.MGetTagValueParam结构体
 * - 使用Java类定义，包含查询参数字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MGetTagValueParam {

  /**
   * 分页器
   * 迁移对应关系: Go语言entity.MGetTagValueParam.Paginator (*pagination.Paginator)
   * - 功能: 分页查询参数
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 控制分页查询
   */
  @JsonProperty("paginator")
  private Object paginator; // 需要根据实际分页器类型调整

  /**
   * 空间ID
   * 迁移对应关系: Go语言entity.MGetTagValueParam.SpaceID (int64)
   * - 功能: 工作空间标识
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 多租户隔离
   */
  @JsonProperty("space_id")
  private Long spaceId;

  /**
   * ID列表
   * 迁移对应关系: Go语言entity.MGetTagValueParam.IDs ([]int64)
   * - 功能: 要查询的ID列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 批量查询指定ID
   */
  @JsonProperty("ids")
  private List<Long> ids;

  /**
   * 状态
   * 迁移对应关系: Go语言entity.MGetTagValueParam.Status (*TagStatus)
   * - 功能: 状态过滤
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 按状态过滤
   */
  @JsonProperty("status")
  private TagStatus status;

  /**
   * 标签键ID
   * 迁移对应关系: Go语言entity.MGetTagValueParam.TagKeyID (*int64)
   * - 功能: 标签键ID过滤
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 按标签键过滤
   */
  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  /**
   * 版本
   * 迁移对应关系: Go语言entity.MGetTagValueParam.Version (*int32)
   * - 功能: 版本过滤
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 按版本过滤
   */
  @JsonProperty("version")
  private Integer version;

  /**
   * 标签值ID列表
   * 迁移对应关系: Go语言entity.MGetTagValueParam.TagValueID ([]int64)
   * - 功能: 标签值ID过滤列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 按标签值ID过滤
   */
  @JsonProperty("tag_value_id")
  private List<Long> tagValueId;
}
