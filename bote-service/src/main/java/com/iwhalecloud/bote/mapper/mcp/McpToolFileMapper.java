package com.iwhalecloud.bote.mapper.mcp;

import com.iwhalecloud.bote.entity.mcp.McpToolFileEntity;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * MCP 工具文件
 *
 * @author bianjp
 * @since 2025-06-09
 */
public interface McpToolFileMapper {

  /**
   * 插入 MCP 工具文件
   */
  int insertMcpToolFile(@Param("dto") McpToolFileEntity entity);

  /**
   * 根据会话 ID 列表批量查询文件 ID 列表
   */
  List<Long> selectFileIdsBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 批量失效文件表中会话关联 MCP 工具文件
   */
  int disableFilesBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 根据会话 ID 列表批量删除记录
   */
  int deleteBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 失效文件表中工作流调试生成的 MCP 工具文件
   */
  int disableTestFiles(@Param("sessionId") Long sessionId, @Param("maxCreatedTime") Date maxCreatedTime);

  /**
   * 删除工作流调试生成的 MCP 工具文件
   */
  int deleteTestFiles(@Param("sessionId") Long sessionId, @Param("maxCreatedTime") Date maxCreatedTime);

}
