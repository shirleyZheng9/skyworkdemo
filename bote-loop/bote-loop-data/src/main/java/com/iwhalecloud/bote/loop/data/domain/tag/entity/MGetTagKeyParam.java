package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取标签键参数实体
 * 迁移对应关系: Go语言entity.MGetTagKeyParam
 * - 功能: 批量获取标签键的查询参数
 * - 字段定义:
 * * Paginator: *pagination.Paginator - 分页器
 * * SpaceID: int64 - 空间ID
 * * IDs: []int64 - ID列表
 * * TagType: *TagType - 标签类型
 * * Status: []TagStatus - 状态列表
 * * TagKeyIDs: []int64 - 标签键ID列表
 * * CreatedBys: []string - 创建者列表
 * * TagDomainTypes: []TagTargetType - 目标类型列表
 * * TagContentTypes: []TagContentType - 内容类型列表
 * * TagKeyName: *string - 标签键名称
 * * TagKeyNameLike: string - 标签键名称模糊匹配
 * <p>
 * Java实现说明:
 * - 对应Go的entity.MGetTagKeyParam结构体
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
public class MGetTagKeyParam {

  /**
   * 分页器
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.Paginator (*pagination.Paginator)
   * - 功能: 分页查询参数
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 控制分页查询
   */
  @JsonProperty("paginator")
  private Paginator paginator; // 需要根据实际分页器类型调整

  /**
   * 空间ID
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.SpaceID (int64)
   * - 功能: 工作空间标识
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 多租户隔离
   */
  @JsonProperty("space_id")
  private Long spaceId;

  /**
   * ID列表
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.IDs ([]int64)
   * - 功能: 要查询的ID列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 批量查询指定ID
   */
  @JsonProperty("ids")
  private List<Long> ids;

  /**
   * 标签类型
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.TagType (*TagType)
   * - 功能: 标签类型过滤
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 按类型过滤
   */
  @JsonProperty("tag_type")
  private TagType tagType;

  /**
   * 状态列表
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.Status ([]TagStatus)
   * - 功能: 状态过滤列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 按状态过滤
   */
  @JsonProperty("status")
  private List<TagStatus> status;

  /**
   * 标签键ID列表
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.TagKeyIDs ([]int64)
   * - 功能: 标签键ID过滤列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 按标签键ID过滤
   */
  @JsonProperty("tag_key_ids")
  private List<Long> tagKeyIds;

  /**
   * 创建者列表
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.CreatedBys ([]string)
   * - 功能: 创建者过滤列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 按创建者过滤
   */
  @JsonProperty("created_bys")
  private List<String> createdBys;

  /**
   * 目标类型列表
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.TagDomainTypes ([]TagTargetType)
   * - 功能: 目标类型过滤列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 按目标类型过滤
   */
  @JsonProperty("tag_domain_types")
  private List<TagTargetType> tagDomainTypes;

  /**
   * 内容类型列表
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.TagContentTypes ([]TagContentType)
   * - 功能: 内容类型过滤列表
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 按内容类型过滤
   */
  @JsonProperty("tag_content_types")
  private List<TagContentType> tagContentTypes;

  /**
   * 标签键名称
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.TagKeyName (*string)
   * - 功能: 精确匹配的标签键名称
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 精确名称查询
   */
  @JsonProperty("tag_key_name")
  private String tagKeyName;

  /**
   * 标签键名称模糊匹配
   * 迁移对应关系: Go语言entity.MGetTagKeyParam.TagKeyNameLike (string)
   * - 功能: 模糊匹配的标签键名称
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 模糊名称查询
   */
  @JsonProperty("tag_key_name_like")
  private String tagKeyNameLike;
}
