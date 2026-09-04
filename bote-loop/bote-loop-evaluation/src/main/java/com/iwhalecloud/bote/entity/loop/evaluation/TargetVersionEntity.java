package com.iwhalecloud.bote.entity.loop.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评估对象版本信息持久化对象
 * 迁移对应关系: Go语言model.TargetVersion
 * - 功能: 评估对象版本表的数据模型
 * - 表名: eval_target_version
 * - 字段定义:
 * * id: Long - target version id
 * * spaceId: Long - 空间id
 * * targetId: Long - target id
 * * sourceTargetVersion: String - source target version
 * * targetMeta: String - 具体内容, 每种静态规则类型对应一个解析方式, json
 * * inputSchema: String - 评估器输入结构信息, json
 * * outputSchema: String - 评估器输出结构信息, json
 * * createdBy: String - 创建人
 * * updatedBy: String - 更新人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: LocalDateTime - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.TargetVersion结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go gorm.DeletedAt -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *[]byte -> Java String (JSON存储为text类型)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetVersionEntity {

  /**
   * target version id
   * 迁移对应关系: Go语言TargetVersion.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, comment:target version id
   */
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言TargetVersion.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, uniqueIndex, comment:空间id
   */
  private Long spaceId;

  /**
   * target id
   * 迁移对应关系: Go语言TargetVersion.TargetID
   * - 功能: 目标标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null, uniqueIndex, comment:target id
   */
  private Long targetId;

  /**
   * source target version
   * 迁移对应关系: Go语言TargetVersion.SourceTargetVersion
   * - 功能: 来源目标版本
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(255), not null, uniqueIndex, comment:source target version
   */
  private String sourceTargetVersion;

  /**
   * 具体内容, 每种静态规则类型对应一个解析方式, json
   * 迁移对应关系: Go语言TargetVersion.TargetMeta
   * - 功能: 目标元数据
   * - 类型: Go的*[]byte对应Java的String
   * - 数据库: text, comment:具体内容, 每种静态规则类型对应一个解析方式, json
   */
  private String targetMeta;

  /**
   * 评估器输入结构信息, json
   * 迁移对应关系: Go语言TargetVersion.InputSchema
   * - 功能: 输入结构信息
   * - 类型: Go的*[]byte对应Java的String
   * - 数据库: text, comment:评估器输入结构信息, json
   */
  private String inputSchema;

  /**
   * 评估器输出结构信息, json
   * 迁移对应关系: Go语言TargetVersion.OutputSchema
   * - 功能: 输出结构信息
   * - 类型: Go的*[]byte对应Java的String
   * - 数据库: text, comment:评估器输出结构信息, json
   */
  private String outputSchema;

  /**
   * 创建人
   * 迁移对应关系: Go语言TargetVersion.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null, default:0, comment:创建人
   */
  private String createdBy;

  /**
   * 更新人
   * 迁移对应关系: Go语言TargetVersion.UpdatedBy
   * - 功能: 更新者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null, default:0, comment:更新人
   */
  private String updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言TargetVersion.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP, comment:创建时间
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言TargetVersion.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null, default CURRENT_TIMESTAMP, comment:更新时间
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言TargetVersion.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的gorm.DeletedAt对应Java的Date
   * - 数据库: timestamp, comment:删除时间
   */
  private Date deletedAt;
}
