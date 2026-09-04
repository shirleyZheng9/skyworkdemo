package com.iwhalecloud.bote.entity.loop.evaluation;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验数据项结果持久化对象
 * 迁移对应关系: Go语言model.ExptItemResult
 * - 功能: 实验数据项结果的数据模型
 * - 表名: expt_item_result
 * - 字段定义:
 * * id: Long - id
 * * spaceId: Long - 空间 id
 * * exptId: Long - 实验 id
 * * exptRunId: Long - 实验运行 id
 * * itemId: Long - item_id
 * * itemIdx: Integer - item 序号
 * * status: Integer - 状态
 * * errMsg: byte[] - 错误信息
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * * logId: String - 日志 id
 * <p>
 * Java实现说明:
 * - 对应Go的model.ExptItemResult结构体
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
 * - Go *int32 -> Java Integer
 * - Go *[]byte -> Java byte[]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expt_item_result")
public class ExptItemResultEntity {

  /**
   * id
   * 迁移对应关系: Go语言ExptItemResult.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey
   */
  @Id
  private Long id;

  /**
   * 空间 id
   * 迁移对应关系: Go语言ExptItemResult.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * 实验 id
   * 迁移对应关系: Go语言ExptItemResult.ExptID
   * - 功能: 实验标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptId;

  /**
   * 实验运行 id
   * 迁移对应关系: Go语言ExptItemResult.ExptRunID
   * - 功能: 实验运行标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long exptRunId;

  /**
   * item_id
   * 迁移对应关系: Go语言ExptItemResult.ItemID
   * - 功能: 数据项标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long itemId;

  /**
   * item 序号
   * 迁移对应关系: Go语言ExptItemResult.ItemIdx
   * - 功能: 数据项序号
   * - 类型: Go的*int32对应Java的Integer
   * - 数据库: int(11) unsigned
   */
  private Integer itemIdx;

  /**
   * 状态
   * 迁移对应关系: Go语言ExptItemResult.Status
   * - 功能: 状态
   * - 类型: Go的int32对应Java的Integer
   * - 数据库: int(11) unsigned, not null
   */
  private Integer status;

  /**
   * 错误信息
   * 迁移对应关系: Go语言ExptItemResult.ErrMsg
   * - 功能: 错误信息
   * - 类型: Go的*[]byte对应Java的byte[]
   * - 数据库: blob binary
   */
  private String errMsg;

  /**
   * 创建时间
   * 迁移对应关系: Go语言ExptItemResult.CreatedAt
   * - 功能: 创建时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言ExptItemResult.UpdatedAt
   * - 功能: 更新时间
   * - 类型: Go的time.Time对应Java的LocalDateTime
   * - 数据库: timestamp, not null, default:CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言ExptItemResult.DeletedAt
   * - 功能: 删除时间
   * - 类型: Go的gorm.DeletedAt对应Java的Long
   * - 数据库: timestamp
   */
  private Long deletedAt;

  /**
   * 日志 id
   * 迁移对应关系: Go语言ExptItemResult.LogID
   * - 功能: 日志标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String logId;
}
