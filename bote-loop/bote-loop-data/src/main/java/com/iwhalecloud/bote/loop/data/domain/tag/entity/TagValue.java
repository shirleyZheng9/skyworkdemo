package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.entity.loop.data.tag.TagValueEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签值实体
 * 迁移对应关系: Go语言entity.TagValue
 * - 功能: 存储标签值信息
 * - 字段定义:
 * * ID: int64 - 主键ID
 * * AppID: int32 - 应用ID
 * * SpaceID: int64 - 空间ID
 * * TagKeyID: int64 - 标签键ID
 * * TagValueID: int64 - 标签值ID
 * * TagValueName: string - 标签值名称
 * * Description: *string - 描述
 * * Status: TagStatus - 状态
 * * VersionNum: *int32 - 版本数字
 * * ParentValueID: int64 - 父标签值ID
 * * Children: []*TagValue - 子标签值列表
 * * IsSystem: bool - 是否系统标签
 * * CreatedBy: *string - 创建者
 * * CreatedAt: time.Time - 创建时间
 * * UpdatedBy: *string - 更新者
 * * UpdatedAt: time.Time - 更新时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.TagValue结构体
 * - 使用Java类定义，包含标签值字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java列表
 * - Go time.Time -> Java LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagValue {

  /**
   * 主键ID
   * 迁移对应关系: Go语言entity.TagValue.ID (int64)
   * - 功能: 数据库主键
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 唯一标识
   */
  @JsonProperty("id")
  private Long id;

  /**
   * 应用ID
   * 迁移对应关系: Go语言entity.TagValue.AppID (int32)
   * - 功能: 应用标识
   * - 类型: Go的int32类型对应Java的Integer类型
   * - 用途: 多应用隔离
   */
  @JsonProperty("app_id")
  private Integer appId;

  /**
   * 空间ID
   * 迁移对应关系: Go语言entity.TagValue.SpaceID (int64)
   * - 功能: 工作空间标识
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 多租户隔离
   */
  @JsonProperty("space_id")
  private Long spaceId;

  /**
   * 标签键ID
   * 迁移对应关系: Go语言entity.TagValue.TagKeyID (int64)
   * - 功能: 关联的标签键标识
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 建立关联关系
   */
  @JsonProperty("tag_key_id")
  private Long tagKeyId;

  /**
   * 标签值ID
   * 迁移对应关系: Go语言entity.TagValue.TagValueID (int64)
   * - 功能: 业务标签值标识
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 业务逻辑标识
   */
  @JsonProperty("tag_value_id")
  private Long tagValueId;

  /**
   * 标签值名称
   * 迁移对应关系: Go语言entity.TagValue.TagValueName (string)
   * - 功能: 标签值显示名称
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 用户界面显示
   */
  @JsonProperty("tag_value_name")
  private String tagValueName;

  /**
   * 描述
   * 迁移对应关系: Go语言entity.TagValue.Description (*string)
   * - 功能: 标签值描述信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 说明标签值含义
   */
  @JsonProperty("description")
  private String description;

  /**
   * 状态
   * 迁移对应关系: Go语言entity.TagValue.Status (TagStatus)
   * - 功能: 标签值状态
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 控制标签值可用性
   */
  @JsonProperty("status")
  private TagStatus status;

  /**
   * 版本数字
   * 迁移对应关系: Go语言entity.TagValue.VersionNum (*int32)
   * - 功能: 版本数字标识
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 版本管理
   */
  @JsonProperty("version_num")
  private Integer versionNum;

  /**
   * 父标签值ID
   * 迁移对应关系: Go语言entity.TagValue.ParentValueID (int64)
   * - 功能: 父级标签值标识
   * - 类型: Go的int64类型对应Java的Long类型
   * - 用途: 构建标签值层级关系
   */
  @JsonProperty("parent_value_id")
  private Long parentValueId;

  /**
   * 子标签值列表
   * 迁移对应关系: Go语言entity.TagValue.Children ([]*TagValue)
   * - 功能: 子级标签值
   * - 类型: Go的切片类型对应Java的列表
   * - 用途: 构建标签值树结构
   */
  @JsonProperty("children")
  private List<TagValue> children;

  /**
   * 是否系统标签
   * 迁移对应关系: Go语言entity.TagValue.IsSystem (bool)
   * - 功能: 系统标签标识
   * - 类型: Go的bool类型对应Java的Boolean类型
   * - 用途: 区分系统标签和用户标签
   */
  @JsonProperty("is_system")
  private Boolean isSystem;

  /**
   * 创建者
   * 迁移对应关系: Go语言entity.TagValue.CreatedBy (*string)
   * - 功能: 创建者标识
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 记录创建者
   */
  @JsonProperty("created_by")
  private String createdBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言entity.TagValue.CreatedAt (time.Time)
   * - 功能: 创建时间
   * - 类型: Go的time.Time类型对应Java的LocalDateTime类型
   * - 用途: 记录创建时间
   */
  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  /**
   * 更新者
   * 迁移对应关系: Go语言entity.TagValue.UpdatedBy (*string)
   * - 功能: 更新者标识
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 记录更新者
   */
  @JsonProperty("updated_by")
  private String updatedBy;

  /**
   * 更新时间
   * 迁移对应关系: Go语言entity.TagValue.UpdatedAt (time.Time)
   * - 功能: 更新时间
   * - 类型: Go的time.Time类型对应Java的LocalDateTime类型
   * - 用途: 记录更新时间
   */
  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  public TagValueEntity toPO() {
    return TagValueEntity.builder().build();
  }
}
