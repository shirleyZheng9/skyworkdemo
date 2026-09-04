package com.iwhalecloud.bote.controller.agent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiMcpDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.service.agent.IAiMcpManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BoteClaw 配置管理 controller
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/generalAgent/mcp", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "BoteClaw 配置管理-mcp")
public class AiMcpGeneralAgentManageController {

  // @formatter:off
  private final IAiMcpManageService mcpManageService;
  private final IRefreshCacheService refreshCacheService;
  // @formatter:on

  @Operation(summary = "保存新增表记录启用 MCP")
  @PostMapping("enableBtAiMcp")
  public ResultVO<AiMcpDTO> enableBtAiMcp(@RequestBody AiMcpDTO mcp) {
    Assert.notNull(mcp.getSpaceId(), "空间 ID 不能为空");
    Assert.notNull(mcp.getMcpId(), "mcp ID 不能为空");
    ResultVO<AiMcpDTO> result = mcpManageService.saveAiMcp(mcp);
    if (result.isSuccess()) {
      Long userId = SessionUtil.getLoginInfo().getUserId();
      String key = mcp.getSpaceId() + CacheConsts.COLON + mcp.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "禁用 MCP")
  @PostMapping("disabledBtAiMcp")
  public ResultVO<AiMcpDTO> disabledBtAiMcp(@RequestBody AiMcpDTO mcp) {
    Assert.notNull(mcp.getSpaceId(), "空间 ID 不能为空");
    Assert.notNull(mcp.getMcpId(), "mcp ID 不能为空");
    if (mcp.getBotId() == null) {
      mcp.setBotId(BaseConsts.BOTE_AI_ID);
    }
    ResultVO<AiMcpDTO> result = mcpManageService.disabledBtAiMcp(mcp);
    if (result.isSuccess()) {
      AiMcpDTO resultObject = result.getResultObject();
      Long userId = SessionUtil.getLoginInfo().getUserId();
      String key = resultObject.getSpaceId() + CacheConsts.COLON + resultObject.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "删除通用智能体 MCP 服务")
  @GetMapping("deleteAiMcpServer")
  public ResultVO<Void> deleteMcpServer(@RequestParam(value = "spaceId", required = false) Long spaceId, @RequestParam(name = "serverId") Long serverId, @RequestParam(name = "botId", required = false) Long botId) {
    Assert.notNull(serverId, "主键 ID 不能为空");
    ResultVO<Void> result = mcpManageService.deleteMcpServer(spaceId, serverId, botId);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MCP_CLIENT, spaceId + ":" + serverId);
    }
    return result;
  }

  @Operation(summary = "分页查询 AI 通用智能体的 MCP 服务")
  @PostMapping("queryAiMcpServerPage")
  public ResultVO<PageInfo<McpServerDTO>> queryAiMcpServerPage(@RequestBody AiQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    return ResultVO.success(mcpManageService.queryAiMcpServerPage(queryParams));
  }

  @Operation(summary = "保存 通用智能体MCP 服务")
  @PostMapping("saveAiMcpServer")
  public ResultVO<McpServerDTO> saveAiMcpServer(@RequestBody McpServerDTO mcpServer) {
    mcpServer.setDataFrom(BaseConsts.DATA_FROM_PORTAL_BOT);
    mcpServer.setTenantId(mcpServer.getSpaceId());
    boolean isUpdate = mcpServer.getServerId() != null;
    mcpServer.saveJsonConfig();
    ResultVO<McpServerDTO> result = mcpManageService.saveAiMcpServer(mcpServer);
    if (isUpdate && result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_MCP_CLIENT, mcpServer.getSpaceId() + ":" + mcpServer.getServerId());
    }
    return result;
  }

}
