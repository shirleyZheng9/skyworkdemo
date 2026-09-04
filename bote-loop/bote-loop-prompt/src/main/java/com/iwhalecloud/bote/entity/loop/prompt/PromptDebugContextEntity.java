package com.iwhalecloud.bote.entity.loop.prompt;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt调试上下文持久化对象
 * 迁移对应关系: Go语言model.PromptDebugContext
 * - 功能: 用户调试prompt上下文信息表的数据模型
 * - 表名: prompt_debug_context
 * - 字段定义:
 * * id: Long - 主键ID
 * * promptId: Long - prompt id
 * * userId: String - user id
 * * mockContexts: String - 上下文信息，json格式
 * * mockVariables: String - mock变量值，json格式
 * * mockTools: String - mock tool结果，json格式
 * * debugConfig: String - 调试配置
 * * compareConfig: String - 训练场配置
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * * deletedAt: Long - 删除时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.PromptDebugContext结构体
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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_prompt_debug_context")
public class PromptDebugContextEntity {

  /**
   * 主键ID
   * 迁移对应关系: Go语言PromptDebugContext.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * prompt id
   * 迁移对应关系: Go语言PromptDebugContext.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20), not null
   */
  private Long promptId;

  /**
   * user id
   * 迁移对应关系: Go语言PromptDebugContext.UserID
   * - 功能: 用户标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String userId;

  /**
   * 上下文信息，json格式
   * 迁移对应关系: Go语言PromptDebugContext.MockContexts
   * - 功能: 调试上下文信息
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String mockContexts;

  /**
   * mock变量值，json格式
   * 迁移对应关系: Go语言PromptDebugContext.MockVariables
   * - 功能: 模拟变量值
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String mockVariables;

  /**
   * mock tool结果，json格式
   * 迁移对应关系: Go语言PromptDebugContext.MockTools
   * - 功能: 模拟工具结果
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String mockTools;

  /**
   * 调试配置
   * 迁移对应关系: Go语言PromptDebugContext.DebugConfig
   * - 功能: 调试配置信息
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String debugConfig;

  /**
   * 训练场配置
   * 迁移对应关系: Go语言PromptDebugContext.CompareConfig
   * - 功能: 训练场配置信息
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String compareConfig;

  /**
   * 创建时间
   * 迁移对应关系: Go语言PromptDebugContext.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言PromptDebugContext.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;

  /**
   * 删除时间
   * 迁移对应关系: Go语言PromptDebugContext.DeletedAt
   * - 功能: 软删除时间戳
   * - 类型: Go的soft_delete.DeletedAt对应Java的Long
   * - 数据库: bigint(20), not null, default 0
   */
  private Long deletedAt;
}
