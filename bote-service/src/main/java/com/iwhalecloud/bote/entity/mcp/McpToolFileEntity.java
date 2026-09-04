package com.iwhalecloud.bote.entity.mcp;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * MCP 工具文件
 *
 * @author bianjp
 * @since 2025-06-09
 */
@Getter
@Setter
@ToString
public class McpToolFileEntity {
  /** 主键 */
  private Long id;
  /** 租户 ID */
  private Long tenantId;
  /** 文件 ID */
  private Long fileId;
  /** 会话 ID */
  private Long sessionId;
  /** 事务 ID */
  private Long transactionId;
  /** 上下文 ID */
  private String contextId;
  /** MCP 服务 ID */
  private Long mcpServerId;
  /** MCP 工具名称 */
  private String mcpToolName;
  /** 状态 */
  private String statusCd;
  /** 创建人 */
  private Long creatorId;
  /** 创建时间 */
  private Date createdTime;
  /** 更新时间 */
  private Date updatedTime;
}
