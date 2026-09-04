package com.iwhalecloud.bote.service.agent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.agent.AiMcpDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 新增表记录启用mcp管理服务
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface IAiMcpManageService {

  /**
   * 保存新增表记录启用mcp
   *
   * @param mcp 新增表记录启用mcp
   * @return 结果
   */
  ResultVO<AiMcpDTO> saveAiMcp(AiMcpDTO mcp);

  /**
   * 查询通用智能体的mcp的列表
   * @param queryParams 查询入参
   * @return 返回mcp列表
   */
  PageInfo<McpServerDTO> queryAiMcpServerPage(AiQueryParams queryParams);

  /**
   * 禁用mcp
   * @param mcp 启用的mcp信息
   * @return mcp启用对象数据
   */
  ResultVO<AiMcpDTO> disabledBtAiMcp(AiMcpDTO mcp);

  /**
   * 删除mcp
   *
   * @param spaceId 空间 ID
   * @param serverId mcp主键
   * @return 结果
   */
  ResultVO<Void> deleteMcpServer(Long spaceId, Long serverId, Long botId);

  /**
   * 保存mcp
   *
   * @param mcpServer mcp
   * @return 结果
   */
  ResultVO<McpServerDTO> saveAiMcpServer(McpServerDTO mcpServer);
}
