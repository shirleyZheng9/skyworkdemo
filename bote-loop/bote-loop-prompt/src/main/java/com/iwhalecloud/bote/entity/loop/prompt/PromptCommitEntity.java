package com.iwhalecloud.bote.entity.loop.prompt;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt提交版本持久化对象
 * 迁移对应关系: Go语言model.PromptCommit
 * - 功能: Commit表的数据模型
 * - 表名: prompt_commit
 * - 字段定义:
 * * id: Long - 主键ID
 * * spaceId: Long - 空间ID
 * * promptId: Long - Prompt ID
 * * promptKey: String - Prompt key
 * * templateType: String - 模版类型
 * * messages: String - 托管消息列表
 * * modelConfig: String - 模型配置
 * * variableDefs: String - 变量定义
 * * tools: String - tools
 * * toolCallConfig: String - tool调用配置
 * * version: String - 版本
 * * baseVersion: String - 来源版本
 * * committedBy: String - 提交人
 * * description: String - 提交版本描述
 * * createdAt: LocalDateTime - 创建时间
 * * updatedAt: LocalDateTime - 更新时间
 * <p>
 * Java实现说明:
 * - 对应Go的model.PromptCommit结构体
 * - 使用普通MyBatis注解风格
 * - 使用Lombok注解简化代码
 * - 不支持软删除（Go版本没有DeletedAt字段）
 * - 数据库映射通过MyBatis XML文件配置
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go time.Time -> Java LocalDateTime
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bt_prompt_commit")
public class PromptCommitEntity {

  /**
   * 主键ID
   * 迁移对应关系: Go语言PromptCommit.ID
   * - 功能: 主键标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, primaryKey, autoIncrement
   */
  @Id
  private Long id;

  /**
   * 空间ID
   * 迁移对应关系: Go语言PromptCommit.SpaceID
   * - 功能: 空间标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long spaceId;

  /**
   * Prompt ID
   * 迁移对应关系: Go语言PromptCommit.PromptID
   * - 功能: Prompt标识
   * - 类型: Go的int64对应Java的Long
   * - 数据库: bigint(20) unsigned, not null
   */
  private Long promptId;

  /**
   * Prompt key
   * 迁移对应关系: Go语言PromptCommit.PromptKey
   * - 功能: Prompt键值
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String promptKey;

  /**
   * 模版类型
   * 迁移对应关系: Go语言PromptCommit.TemplateType
   * - 功能: 模版类型
   * - 类型: Go的*string对应Java的String
   * - 数据库: varchar(64), default normal
   */
  private String templateType;

  /**
   * 托管消息列表
   * 迁移对应关系: Go语言PromptCommit.Messages
   * - 功能: 消息列表JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String messages;

  /**
   * 模型配置
   * 迁移对应关系: Go语言PromptCommit.ModelConfig
   * - 功能: 模型配置JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String modelConfig;

  /**
   * 变量定义
   * 迁移对应关系: Go语言PromptCommit.VariableDefs
   * - 功能: 变量定义JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String variableDefs;

  /**
   * tools
   * 迁移对应关系: Go语言PromptCommit.Tools
   * - 功能: 工具定义JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: longtext
   */
  private String tools;

  /**
   * tool调用配置
   * 迁移对应关系: Go语言PromptCommit.ToolCallConfig
   * - 功能: 工具调用配置JSON
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String toolCallConfig;

  /**
   * 版本
   * 迁移对应关系: Go语言PromptCommit.Version
   * - 功能: 版本号
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String version;

  /**
   * 来源版本
   * 迁移对应关系: Go语言PromptCommit.BaseVersion
   * - 功能: 基于的版本
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String baseVersion;

  /**
   * 提交人
   * 迁移对应关系: Go语言PromptCommit.CommittedBy
   * - 功能: 提交者标识
   * - 类型: Go的string对应Java的String
   * - 数据库: varchar(128), not null
   */
  private String committedBy;

  /**
   * 提交版本描述
   * 迁移对应关系: Go语言PromptCommit.Description
   * - 功能: 版本描述信息
   * - 类型: Go的*string对应Java的String
   * - 数据库: text
   */
  private String description;

  /**
   * 创建时间
   * 迁移对应关系: Go语言PromptCommit.CreatedAt
   * - 功能: 记录创建时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date createdAt;

  /**
   * 更新时间
   * 迁移对应关系: Go语言PromptCommit.UpdatedAt
   * - 功能: 记录更新时间
   * - 类型: Go的time.Time对应Java的Date
   * - 数据库: timestamptz, not null, default CURRENT_TIMESTAMP
   */
  private Date updatedAt;
}
