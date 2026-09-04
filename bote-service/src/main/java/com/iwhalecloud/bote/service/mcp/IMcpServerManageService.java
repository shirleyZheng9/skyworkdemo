package com.iwhalecloud.bote.service.mcp;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.mcp.McpServerQueryParams;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import java.util.List;

/**
 * mcp管理服务
 *
 * @author auto
 * @since 2025-05-13
 */
public interface IMcpServerManageService {

  /**
   * 查询单个mcp
   *
   * @param tenantId 租户 ID
   * @param serverId mcp主键
   * @return mcp
   */
  McpServerDTO findMcpServer(Long tenantId, Long serverId);

  /**
   * 保存mcp
   *
   * @param mcpServer mcp
   * @return 结果
   */
  ResultVO<McpServerDTO> saveMcpServer(McpServerDTO mcpServer);

  /**
   * 删除mcp
   *
   * @param tenantId 租户 ID
   * @param serverId mcp主键
   * @return 结果
   */
  ResultVO<Void> deleteMcpServer(Long tenantId, Long serverId);

  /**
   * 查询mcp列表
   *
   * @param queryParams 查询条件
   * @return mcp列表
   */
  List<McpServerDTO> queryMcpServerList(McpServerQueryParams queryParams);

  /**
   * 查询mcp列表（分页）
   *
   * @param queryParams 查询条件
   * @return mcp分页列表
   */
  PageInfo<McpServerDTO> queryMcpServerPage(McpServerQueryParams queryParams);

  /**
   * 分页查询 MCP 服务基本信息
   */
  PageInfo<SimpleMcpServiceDTO> querySimpleMcpServerPage(McpServerQueryParams queryParams);

  /**
   * 更新mcp状态
   *
   * @param tenantId 租户 ID
   * @param serverId mcp主键
   * @param serverEffect mcp状态
   * @return 结果
   */
  ResultVO<Void> publishMcpServer(Long tenantId, Long serverId, String serverEffect);

  /**
   * 同步来自百应的 MCP 服务
   */
  void syncMcpServerFromBeyond(McpServerDTO server);
}
