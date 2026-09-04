package com.iwhalecloud.bote.mapper.mcp;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerQueryParams;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * mcp管理
 *
 * @author auto
 * @since 2025-05-13
 */
public interface McpServerManageMapper {
  /**
   * 校验mcp的名称唯一性
   *
   * @param mcpServer mcp
   * @return 结果
   */
  boolean existsMcpServerName(@Param("dto") McpServerDTO mcpServer, @Param("userId")Long userId);

  /**
   * 根据主键获取mcp
   *
   * @param serverId mcp主键
   * @param tenantId 租户ID
   * @return mcp
   */
  McpServerDTO getMcpServer(@Param("id") Long serverId, @Param("tenantId") Long tenantId, @Nullable @Param("statusCd") String statusCd);

  /**
   * 根据ID列表批量获取mcp服务
   *
   * @param serverIds mcp主键列表
   * @param tenantId 租户ID
   * @return mcp列表
   */
  List<McpServerDTO> getMcpServersByIds(@Param("serverIds") List<Long> serverIds, @Param("tenantId") Long tenantId);

  /**
   * 新增mcp
   *
   * @param mcpServer mcp
   * @return 结果
   */
  int insertMcpServer(@Param("dto") McpServerDTO mcpServer);

  /**
   * 批量新增mcp
   *
   * @param mcpServers mcp列表
   * @return 结果
   */
  int batchInsertMcpServer(@Param("list") List<McpServerDTO> mcpServers);

  /**
   * 修改mcp
   *
   * @param mcpServer mcp
   * @return 结果
   */
  int updateMcpServer(@Param("dto") McpServerDTO mcpServer);

  /**
   * 删除属性
   *
   * @param serverId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteMcpServer(@Param("serverId") Long serverId, @Param("updatorId") Long updatorId);

  /**
   * 获取mcp列表
   *
   * @param queryParams 查询条件
   * @return mcp列表
   */
  List<McpServerDTO> selectMcpServerList(@Param("query") McpServerQueryParams queryParams);

  /**
   * 获取mcp列表（分页）
   *
   * @param queryParams 查询条件
   * @return mcp分页列表
   */
  Page<McpServerDTO> selectMcpServerPage(@Param("query") McpServerQueryParams queryParams, RowBounds rowBounds);

  /**
   * 分页查询 MCP 服务基本信息
   */
  Page<SimpleMcpServiceDTO> selectSimpleMcpServerPage(@Param("query") McpServerQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据 ID 查询 MCP 服务的简单信息
   */
  @Nullable
  SimpleMcpServiceDTO selectSimpleMcpServerById(@Param("tenantId") Long tenantId, @Param("serverId") Long serverId);

  /**
   * 根据 ID 查询 MCP 服务的状态
   */
  @Nullable
  String selectServerEffectById(@Param("tenantId") Long tenantId, @Param("serverId") Long serverId);

  /**
   * 修改 MCP 服务状态
   *
   * @param serverId MCP 服务 ID
   * @param serverEffect 状态
   * @param updatorId 修改人 ID
   * @param tenantId 租户 ID
   */
  int updateMcpServerStatus(@Param("serverId") Long serverId, @Param("serverEffect") String serverEffect, @Param("updatorId") Long updatorId,
    @Param("tenantId") Long tenantId);
}
