package com.iwhalecloud.bote.entity.loop.data.tag;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag元数据表持久化对象
 * 迁移对应关系: Go语言model.TagKey
 * - 功能: tag元数据表的数据模型
 * - 表名: tag_key
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - application id
 * * spaceId: Long - 归属space id,做分片键
 * * versionNum: Integer - tag自增版本
 * * version: String - tag版本文案
 * * tagKeyId: Long - tag id，唯一标识一个标签
 * * tagKeyName: String - tag名称
 * * description: String - tag描述
 * * parentKeyId: Long - 级联标签场景,上层tag key id
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * changeLog: String - 变更日志
 * * status: String - tag状态,active,inactive,deprecated
 * * tagType: String - tag类型,tag,option
 * * createdBy: String - 创建者
 * * updatedBy: String - 更新者
 * * tagTargetType: String - tag目标对象列表,resource|dataset_item,多个值由,分隔
 * * contentType: String - 内容类型: 自由文本,连续分值,分类,布尔值
 * * spec: String - 标签规格
 * <p>
 * Java实现说明:
 * - 对应Go的model.TagKey结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go datatypes.JSON -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tag_key")
public class TagKeyEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言TagKey.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * application id
   * 迁移对应关系: Go语言TagKey.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer appId;

  /**
   * 归属space id,做分片键
   * 迁移对应关系: Go语言TagKey.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * tag自增版本
   * 迁移对应关系: Go语言TagKey.VersionNum
   * - 功能: 版本号
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer versionNum;

  /**
   * tag版本文案
   * 迁移对应关系: Go语言TagKey.Version
   * - 功能: 版本描述
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(64), not null
   */
  private String version;

  /**
   * tag id，唯一标识一个标签
   * 迁移对应关系: Go语言TagKey.TagKeyID
   * - 功能: 标签唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long tagKeyId;

  /**
   * tag名称
   * 迁移对应关系: Go语言TagKey.TagKeyName
   * - 功能: 标签名称
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String tagKeyName;

  /**
   * tag描述
   * 迁移对应关系: Go语言TagKey.Description
   * - 功能: 标签描述
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(2000)
   */
  private String description;

  /**
   * 级联标签场景,上层tag key id
   * 迁移对应关系: Go语言TagKey.ParentKeyID
   * - 功能: 父级标签ID
   * - 类型: Go的*int64对应Java的Long
   * - 数据库: bigint(20) unsigned
   */
  private Long parentKeyId;

  /**
   * 创建时间
   * 迁移对应关系: Go语言TagKey.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null
   */
  private LocalDateTime createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言TagKey.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null
   */
  private LocalDateTime updatedAt;

  /**
   * 变更日志
   * 迁移对应关系: Go语言TagKey.ChangeLog
   * - 功能: 变更记录
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json, not null
   */
  private String changeLog;

  /**
   * tag状态,active,inactive,deprecated
   * 迁移对应关系: Go语言TagKey.Status
   * - 功能: 标签状态
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(32), not null
   */
  private String status;

  /**
   * tag类型,tag,option
   * 迁移对应关系: Go语言TagKey.TagType
   * - 功能: 标签类型
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(32), not null
   */
  private String tagType;

  /**
   * 创建者
   * 迁移对应关系: Go语言TagKey.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64)
   */
  private String createdBy;

  /**
   * 更新者
   * 迁移对应关系: Go语言TagKey.UpdatedBy
   * - 功能: 更新人
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64)
   */
  private String updatedBy;

  /**
   * tag目标对象列表,resource|dataset_item,多个值由,分隔
   * 迁移对应关系: Go语言TagKey.TagTargetType
   * - 功能: 标签目标类型
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(256), not null
   */
  private String tagTargetType;

  /**
   * 内容类型: 自由文本,连续分值,分类,布尔值
   * 迁移对应关系: Go语言TagKey.ContentType
   * - 功能: 内容类型
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64), default:category
   */
  private String contentType;

  /**
   * 标签规格
   * 迁移对应关系: Go语言TagKey.Spec
   * - 功能: 标签规格配置
   * - 类型: Go的datatypes.JSON对应Java的String
   * - 数据库: json
   */
  private String spec;
}
