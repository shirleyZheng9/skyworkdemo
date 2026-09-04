package com.iwhalecloud.bote.service.agent.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiMcpDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.mapper.agent.AiMcpManageMapper;
import com.iwhalecloud.bote.service.agent.IAiMcpManageService;
import com.iwhalecloud.bote.service.mcp.IMcpServerManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 新增表记录启用mcp管理服务实现
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Service
@RequiredArgsConstructor
public class AiMcpManageServiceImpl implements IAiMcpManageService {

  private final IMcpServerManageService mcpServerManageService;
  private final AiMcpManageMapper aiMcpManageMapper;
  private final GeneraAgentIdCache generaAgentIdCache;

  @Override
  @Transactional
  public ResultVO<AiMcpDTO> saveAiMcp(AiMcpDTO mcp) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    AiMcpDTO old = aiMcpManageMapper.getAiMcp(mcp.getSpaceId(), mcp.getBotId(), mcp.getMcpId(), userId);
    if (old != null) {
      return ResultVO.success(old);
    }
    mcp.setId(Sequences.AI_MCP_ID.next());
    mcp.setStatusCd(CommonConsts.STATUS_CD_VALID);
    mcp.setCreatorId(userId);
    aiMcpManageMapper.insertAiMcp(mcp);
    return ResultVO.success(mcp);
  }

  @Override
  public PageInfo<McpServerDTO> queryAiMcpServerPage(AiQueryParams queryParams) {
    Long userId = generaAgentIdCache.getBotOnwerUserId(queryParams.getSpaceId(), queryParams.getBotId(), SessionUtil.getLoginInfo().getUserId());
    queryParams.setUserId(userId);
    // noinspection resource
    return aiMcpManageMapper.selectAiMcpServerPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<AiMcpDTO> disabledBtAiMcp(AiMcpDTO mcp) {
    AiMcpDTO aiMcpDTO = aiMcpManageMapper.getAiMcp(mcp.getSpaceId(), mcp.getBotId(), mcp.getMcpId(), SessionUtil.getLoginInfo().getUserId());
    Assert.notNull(aiMcpDTO, "mcp未启用");
    aiMcpManageMapper.deleteAiMcp(aiMcpDTO.getId(), SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success(aiMcpDTO);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteMcpServer(Long spaceId, Long serverId, Long botId) {
    AiMcpDTO old = aiMcpManageMapper.getAiMcp(spaceId, botId, serverId, SessionUtil.getLoginInfo().getUserId());
    Assert.isNull(old, "mcp启用状态，不能删除");
    ResultVO<Void> voidResultVO = mcpServerManageService.deleteMcpServer(spaceId, serverId);
    Assert.isTrue(voidResultVO.isSuccess(), voidResultVO.getResultMsg());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<McpServerDTO> saveAiMcpServer(McpServerDTO mcpServer) {
    return mcpServerManageService.saveMcpServer(mcpServer);
  }
}
