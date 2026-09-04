package com.iwhalecloud.bote.controller.a2a;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.A2aUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aAgentQueryParams;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.service.a2a.IA2aAgentManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.a2a.spec.AgentCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
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
 * A2A 服务管理
 *
 * @author bianjp
 * @since 2025-09-08
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/a2aAgent", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "A2A 服务管理")
public class A2aAgentManageController {
  private final IA2aAgentManageService a2aAgentManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个 A2A 服务")
  @GetMapping("findA2aAgent")
  public ResultVO<A2aAgentDTO> findA2aAgent(@RequestParam("tenantId") Long tenantId, @RequestParam("agentId") Long agentId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(agentId, "A2A 服务 ID 不能为空");
    A2aAgentDTO agent = a2aAgentManageService.findA2aAgent(tenantId, agentId);
    agent.parseJsonConfig();
    return ResultVO.success(agent);
  }

  @Operation(summary = "保存 A2A 服务")
  @PostMapping("saveA2aAgent")
  public ResultVO<A2aAgentDTO> saveA2aAgent(@RequestBody A2aAgentDTO agent) {
    ResultVO<A2aAgentDTO> result = a2aAgentManageService.saveA2aAgent(agent);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_INTENT, agent.getTenantId().toString());
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE, agent.getTenantId() + ":" + agent.getAgentId());
    }
    return result;
  }

  @Operation(summary = "删除 A2A 服务")
  @PostMapping("deleteA2aAgent")
  public ResultVO<Void> deleteA2aAgent(@RequestParam("tenantId") Long tenantId, @RequestParam("agentId") Long agentId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(agentId, "A2A 服务 ID 不能为空");
    ResultVO<Void> result = a2aAgentManageService.deleteA2aAgent(tenantId, agentId, SessionUtil.getLoginInfo().getUserId());
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_INTENT, tenantId.toString());
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE, tenantId + ":" + agentId);
    }
    return result;
  }

  @Operation(summary = "查询 A2A 服务列表")
  @PostMapping("queryA2aAgentList")
  public ResultVO<List<A2aAgentDTO>> queryA2aAgentList(@RequestBody A2aAgentQueryParams queryParams) {
    return ResultVO.success(a2aAgentManageService.queryA2aAgentList(queryParams));
  }

  @Operation(summary = "分页查询 A2A 服务")
  @PostMapping("queryA2aAgentPage")
  public ResultVO<PageInfo<A2aAgentDTO>> queryA2aAgentPage(@RequestBody A2aAgentQueryParams queryParams) {
    return ResultVO.success(a2aAgentManageService.queryA2aAgentPage(queryParams));
  }

  @Operation(summary = "获取智能体卡片")
  @GetMapping("getAgentCardByUrl")
  public ResultVO<AgentCardInfo> getAgentCardByUrl(@RequestParam("agentCardUrl") String agentCardUrl,
                                                   @RequestParam(name = "platformId", required = false) Long platformId,
                                                   @RequestParam(name = "tenantId", required = false) Long tenantId) {
    Assert.hasLength(agentCardUrl, "智能体卡片地址不能为空");
    Map<String, String> headers = A2aUtil.getPlatformHeaders(tenantId, platformId);
    AgentCard agentCard = A2aUtil.getAgentCard(agentCardUrl, headers);
    String agentIcon = A2aUtil.getAgentIcon(agentCard);
    List<HeaderItem> authHeaders = A2aUtil.getAuthHeaders(agentCard);
    return ResultVO.success(new AgentCardInfo(agentCard, agentIcon, authHeaders));
  }

  /**
   * 智能体卡片和图标
   */
  public record AgentCardInfo(@Schema(description = "智能体卡片") AgentCard agentCard,
                              @Schema(description = "智能体图标") String agentIcon,
                              @Schema(description = "鉴权请求头") List<HeaderItem> authHeaders) {
  }
}
