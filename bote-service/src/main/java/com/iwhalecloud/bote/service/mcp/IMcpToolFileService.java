package com.iwhalecloud.bote.service.mcp;

import java.util.List;

/**
 * MCP 工具文件管理服务
 *
 * @author bianjp
 * @since 2025-06-10
 */
public interface IMcpToolFileService {

  /**
   * 根据会话 ID 列表批量删除 MCP 工具文件
   */
  void deleteBySessionIds(List<Long> sessionIds);

  /**
   * 根据会话 ID 删除 MCP 工具文件
   */
  void deleteBySessionId(Long sessionId);

  /**
   * 删除工作流调试产生的 MCP 工具文件
   */
  void deleteTestFiles();

}
