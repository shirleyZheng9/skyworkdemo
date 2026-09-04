package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评估器版本信息持久化对象
 * 迁移对应关系: Go语言model.EvaluatorVersion
 * - 功能: 评估器版本信息的数据模型
 * - 表名: evaluator_version
 * - 字段定义:
 * * id: Long - idgen id
 * * spaceId: Long - 空间id
 * * evaluatorType: Integer - 评估器类型
 * * evaluatorId: Long - 评估器id
 * * version: String - 版本号
 * * description: String - 版本描述
 * * metainfo: byte[] - 具体内容
 * * receiveChatHistory: Boolean - 是否需求传递上下文
 * * inputSchema: byte[] - 评估器结构信息
 * * createdBy: String - 创建人
 * * updatedBy: String - 更新人
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.EvaluatorVersion结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 支持软删除功能
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go soft_delete.DeletedAt -> Java Long
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *string -> Java String
 * - Go *bool -> Java Boolean
 * - Go *[]byte -> Java byte[]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "evaluator_version")
public class EvaluatorVersionEntity {

  /**
   * idgen id
   * 迁移对应关系: Go语言EvaluatorVersion.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间id
   * 迁移对应关系: Go语言EvaluatorVersion.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 评估器类型
   * 迁移对应关系: Go语言EvaluatorVersion.EvaluatorType
   * - 功能: 评估器类型标识
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11) unsigned
   */
  private Integer evaluatorType;

  /**
   * 评估器id
   * 迁移对应关系: Go语言EvaluatorVersion.EvaluatorID
   * - 功能: 评估器标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long evaluatorId;

  /**
   * 版本号
   * 迁移对应关系: Go语言EvaluatorVersion.Version
   * - 功能: 版本标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String version;

  /**
   * 版本描述
   * 迁移对应关系: Go语言EvaluatorVersion.Description
   * - 功能: 版本描述信息
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(500)
   */
  private String description;

  /**
   * 具体内容
   * 迁移对应关系: Go语言EvaluatorVersion.Metainfo
   * - 功能: 具体内容JSON
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: blob binary
   */
  private String metainfo;

  /**
   * 是否需求传递上下文
   * 迁移对应关系: Go语言EvaluatorVersion.ReceiveChatHistory
   * - 功能: 上下文传递标识
   * - 类型: Go的*bool对应Java的Boolean
   * - 数据库: tinyint(1)
   */
  private Boolean receiveChatHistory;

  /**
   * 评估器结构信息
   * 迁移对应关系: Go语言EvaluatorVersion.InputSchema
   * - 功能: 评估器结构信息JSON
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: blob binary
   */
  private String inputSchema;

  /**
   * 创建人
   * 迁移对应关系: Go语言EvaluatorVersion.CreatedBy
   * - 功能: 创建者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String createdBy;

  /**
   * 更新人
   * 迁移对应关系: Go语言EvaluatorVersion.UpdatedBy
   * - 功能: 更新者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String updatedBy;

  /**
   * 创建时间
   * 迁移对应关系: Go语言EvaluatorVersion.CreatedAt
   * - 功能: 创建时间戳
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言EvaluatorVersion.UpdatedAt
   * - 功能: 更新时间戳
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamp, not null
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言EvaluatorVersion.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Date deletedAt;
}
