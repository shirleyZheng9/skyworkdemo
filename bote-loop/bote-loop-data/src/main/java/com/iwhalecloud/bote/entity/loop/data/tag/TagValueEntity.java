package com.iwhalecloud.bote.entity.loop.data.tag;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag value元数据表持久化对象
 * 迁移对应关系: Go语言model.TagValue
 * - 功能: tag value元数据表的数据模型
 * - 表名: tag_value
 * - 字段定义:
 * * id: Long - 主键id
 * * appId: Integer - application id
 * * spaceId: Long - 归属space id,做分片键
 * * tagKeyId: Long - tag id，唯一标识一个标签
 * * tagValueId: Long - tag value id，唯一标识一个标签
 * * tagValueName: String - tag value名称
 * * description: String - tag value描述
 * * parentValueId: Long - 级联标签场景,上层tag value id
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * versionNum: Integer - tag自增版本
 * * status: String - 状态,active,inactive,deprecated
 * * createdBy: String - 创建者
 * * updatedBy: String - 更新者
 * <p>
 * Java实现说明:
 * - 对应Go的model.TagValue结构体
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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tag_value")
public class TagValueEntity {

  /**
   * 主键id
   * 迁移对应关系: Go语言TagValue.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * application id
   * 迁移对应关系: Go语言TagValue.AppID
   * - 功能: 应用标识
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer appId;

  /**
   * 归属space id,做分片键
   * 迁移对应关系: Go语言TagValue.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * tag id，唯一标识一个标签
   * 迁移对应关系: Go语言TagValue.TagKeyID
   * - 功能: 标签唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long tagKeyId;

  /**
   * tag value id，唯一标识一个标签
   * 迁移对应关系: Go语言TagValue.TagValueID
   * - 功能: 标签值唯一标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long tagValueId;

  /**
   * tag value名称
   * 迁移对应关系: Go语言TagValue.TagValueName
   * - 功能: 标签值名称
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null
   */
  private String tagValueName;

  /**
   * tag value描述
   * 迁移对应关系: Go语言TagValue.Description
   * - 功能: 标签值描述
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(2000)
   */
  private String description;

  /**
   * 级联标签场景,上层tag value id
   * 迁移对应关系: Go语言TagValue.ParentValueID
   * - 功能: 父级标签值ID
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long parentValueId;

  /**
   * 创建时间
   * 迁移对应关系: Go语言TagValue.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private LocalDateTime createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言TagValue.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP
   */
  private LocalDateTime updatedAt;

  /**
   * tag自增版本
   * 迁移对应关系: Go语言TagValue.VersionNum
   * - 功能: 版本号
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11), not null
   */
  private Integer versionNum;

  /**
   * 状态,active,inactive,deprecated
   * 迁移对应关系: Go语言TagValue.Status
   * - 功能: 标签值状态
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(32), not null
   */
  private String status;

  /**
   * 创建者
   * 迁移对应关系: Go语言TagValue.CreatedBy
   * - 功能: 创建人
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64)
   */
  private String createdBy;

  /**
   * 更新者
   * 迁移对应关系: Go语言TagValue.UpdatedBy
   * - 功能: 更新人
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64)
   */
  private String updatedBy;
}
