package com.iwhalecloud.bote.mapper.agent;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.agent.AiMcpDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 新增表记录启用mcp管理
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface AiMcpManageMapper {

  AiMcpDTO getAiMcp(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("mcpId") Long mcpId, @Param("userId") Long userId);

  int insertAiMcp(@Param("dto") AiMcpDTO mcp);

  int updateAiMcp(@Param("dto") AiMcpDTO mcp);

  int deleteAiMcp(@Param("id") Long id, @Param("updatorId") Long updatorId);

  Page<McpServerDTO> selectAiMcpServerPage(@Param("query") AiQueryParams queryParams, RowBounds rowBounds);
}
