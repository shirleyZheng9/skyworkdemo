package com.iwhalecloud.bote.controller.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.BotIconCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.HttpCacheUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.app.query.AiBotQueryParams;
import com.iwhalecloud.bote.dto.bot.BaseBotDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceStatusCountDTO;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSettingInfoDTO;
import com.iwhalecloud.bote.dto.bot.PublishedAppDTO;
import com.iwhalecloud.bote.dto.bot.SimpleAgentStrategyDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SquareBotDTO;
import com.iwhalecloud.bote.dto.bot.query.BeyondResourceQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotAuthQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.dto.bot.query.PublishedAppQueryParams;
import com.iwhalecloud.bote.service.bot.IBotManageService;
import com.iwhalecloud.bote.service.bot.IBotQueryService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用管理 controller
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/bot", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "应用：基础管理")
public class BotManageController {
  private final IBotManageService botManageService;
  private final IBotQueryService botQueryService;
  private final IRefreshCacheService refreshCacheService;
  private final BotIconCache botIconCache;

  @Operation(summary = "保存应用")
  @PostMapping("saveBot")
  public ResultVO<BotDTO> saveBot(@RequestBody @Valid BotDTO bot) {
    boolean isUpdate = bot.getBotId() != null;
    ResultVO<BotDTO> result = botManageService.saveBot(bot);
    if (isUpdate && result.isSuccess()) {
      refreshCache(bot);
    }
    return result;
  }

  /**
   * 刷新缓存
   */
  private void refreshCache(BotDTO bot) {
    // 刷新图标缓存
    refreshCacheService.refresh(CacheConsts.CACHE_NAME_BOT_ICON, bot.getTenantId() + ":" + bot.getBotId());
  }

  @Operation(summary = "保存应用欢迎页设置")
  @PostMapping("savePageInfo")
  public ResultVO<Void> savePageInfo(@RequestBody BotDTO bot) {
    Assert.notEmpty(bot.getPageBaseInfoJson(), "基础信息不能为空");
    Assert.notEmpty(bot.getPageSettingInfoJson(), "版式信息不能为空");
    return botManageService.savePageInfo(bot);
  }

  @Operation(summary = "保存Bot应用设置信息")
  @PostMapping("saveBotSettingInfo")
  public ResultVO<Void> saveBotSettingInfo(@RequestBody BotSettingInfoDTO botSettingInfoDTO) {
    Assert.notNull(botSettingInfoDTO.getBotId(), "智能应用ID不能为空");
    Assert.notNull(botSettingInfoDTO.getTenantId(), "租户ID不能为空");
    return botManageService.saveBotSettingInfo(botSettingInfoDTO);
  }

  @Operation(summary = "删除应用")
  @GetMapping("removeBot")
  public ResultVO<Void> deleteBot(@RequestParam(value = "tenantId", required = false) Long tenantId,
                                  @Parameter(description = "应用 ID", required = true) @RequestParam("botId") Long botId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    return botManageService.deleteBot(tenantId, botId);
  }

  @Operation(summary = "查询应用列表（分页）")
  @PostMapping("queryBotPage")
  public ResultVO<PageInfo<BotDTO>> queryBotPage(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryBotPage(queryParams));
  }

  @Operation(summary = "查询智能应用列表（分页），用于百应平台")
  @PostMapping("beyondQueryBotPage")
  public ResultVO<PageInfo<BotDTO>> beyondQueryBotPage(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botQueryService.beyondQueryBotPage(queryParams));
  }

  @Operation(summary = "查询智能应用与智能体混合列表（分页），用于百应平台")
  @PostMapping("beyondQueryMixedPage")
  public ResultVO<PageInfo<BeyondResourceDTO>> beyondQueryResourcePage(@RequestBody BeyondResourceQueryParams queryParams) {
    return ResultVO.success(botQueryService.beyondQueryResourcePage(queryParams));
  }

  @Operation(summary = "获取指定过滤条件下的上下架状态数量，用于百应平台")
  @PostMapping("beyondGetTotalCountOnAllStatus")
  public ResultVO<BeyondResourceStatusCountDTO> beyondGetTotalCountOnAllStatus(@RequestBody BeyondResourceQueryParams queryParams) {
    return ResultVO.success(botQueryService.beyondGetTotalCountOnAllStatus(queryParams));
  }

  @Operation(summary = "查询应用和场景列表（分页）")
  @GetMapping("queryBotAndSceneList")
  public ResultVO<List<BaseBotDTO>> queryBotAndSceneList(
    @Parameter(description = "租户 ID", required = true) @RequestParam("tenantId") Long tenantId) {
    return ResultVO.success(botQueryService.queryBotAndSceneList(tenantId));
  }

  @Operation(summary = "查询当前用户参与开发的应用列表，用于灵犀运行态")
  @PostMapping("queryUserBotPage")
  public ResultVO<PageInfo<BotDTO>> queryUserBotPage(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryUserBotPage(queryParams));
  }

  @Operation(summary = "查询当前用户最近访问的前4个应用，用于灵犀运行态")
  @PostMapping("queryUserBotList")
  public ResultVO<List<BotDTO>> queryUserBotList(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryUserBotList(queryParams));
  }

  @Operation(summary = "查询推荐的应用，用于灵犀运行态")
  @PostMapping("queryPopularBotList")
  public ResultVO<List<BotDTO>> queryPopularBotList(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryPopularBotList(queryParams));
  }

  @Operation(summary = "查询应用列表")
  @PostMapping("queryBotList")
  public ResultVO<List<BotDTO>> queryBotList(@RequestBody BotQueryParams queryParams) {
    Assert.notNull(queryParams.getTenantId(), "租户 ID 不能为空");
    return ResultVO.success(botQueryService.queryBotList(queryParams));
  }

  @Operation(summary = "查询通用的应用列表", description = "用于应用广场")
  @PostMapping("queryCommonBotList")
  public ResultVO<List<BotDTO>> queryCommonBotList() {
    return ResultVO.success(botQueryService.queryCommonBotList());
  }

  @Operation(summary = "查询应用信息")
  @GetMapping("getBot")
  @RequestCacheable(method = "buildBotEtagKeys")
  public ResultVO<SimpleBotDTO> getBot(@Parameter(description = "应用 ID", required = true) @RequestParam("botId") Long botId,
                                       @Parameter(description = "租户 ID", required = true) @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(botQueryService.getBotById(botId, tenantId));
  }

  @Operation(summary = "获取智能应用图标")
  @GetMapping(value = "botIcon", produces = MediaType.ALL_VALUE)
  @IgnoreSign
  @IgnoreSession
  public void getBotIcon(@RequestParam("tenantId") Long tenantId, @RequestParam("botId") Long botId,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(botId, "智能应用 ID 不能为空");
    botIconCache.sendIcon(tenantId, botId, request, response);
  }

  @Operation(summary = "查询应用欢迎页图标")
  @GetMapping("getPageSettingIcon")
  @RequestCacheable(method = "buildBotEtagKeys")
  public ResultVO<Map<String, Object>> getPageSettingIcon(@Parameter(description = "应用 ID", required = true) @RequestParam("botId") Long botId,
                                                          @Parameter(description = "租户 ID", required = true) @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(botQueryService.getPageSettingIcon(tenantId, botId));
  }

  @Operation(summary = "根据类型查询应用欢迎页图标")
  @GetMapping("getPageSettingIconByType")
  @RequestCacheable(method = "buildPageSettingIconEtagKeys")
  @IgnoreSign
  @IgnoreSession
  public void getPageSettingIconByType(@Parameter(description = "应用 ID", required = true) @RequestParam("botId") Long botId,
                                       @Parameter(description = "租户 ID", required = true) @RequestParam("tenantId") Long tenantId,
                                       @Parameter(description = "类型", required = true) @RequestParam("type") String type, HttpServletResponse response) throws IOException {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.hasText(type, "类型不能为空");
    Map<String, Object> map = botQueryService.getPageSettingIcon(tenantId, botId);
    if (MapUtils.isEmpty(map) || !map.containsKey(type)) {
      HttpCacheUtil.sendError(response, "未配置图标");
      return;
    }
    String icon = MapUtils.getString(map, type);
    IconUtil.sendBase64Icon(response, icon);
  }

  /**
   * 构造应用的 ETag 键
   */
  @SuppressWarnings("unused")
  protected List<Object> buildBotEtagKeys(Long botId, Long tenantId) {
    SimpleBotDTO bot = botQueryService.getSimpleBot(tenantId, botId);
    if (bot == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(bot.getTenantId(), bot.getBotId(), bot.getUpdatedTime());
  }

  /**
   * 构造页面图标的 ETag 键
   */
  @SuppressWarnings("unused")
  protected List<Object> buildPageSettingIconEtagKeys(Long botId, Long tenantId, String type, HttpServletResponse response) {
    return buildBotEtagKeys(botId, tenantId);
  }

  @GetMapping("publishBot")
  public ResultVO<Void> publishBot(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("botId") Long botId,
                                   @RequestParam("botsStatus") String botsStatus) {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.hasText(botsStatus, "应用状态不能为空");
    return botManageService.publishBot(tenantId, botId, botsStatus);
  }

  @Operation(summary = "应用授权")
  @PostMapping("authBot")
  public ResultVO<Void> authBot(@RequestBody BotAuthDTO botAuth) {
    return botManageService.authBot(botAuth);
  }

  @Operation(summary = "查询授权的应用列表")
  @GetMapping("queryAuthedBotList")
  public ResultVO<Map<String, List<BotAuthDTO>>> queryAuthedBotList(@RequestParam("botId") Long botId) {
    return botManageService.queryAuthedBotList(botId);
  }

  @Operation(summary = "查询授权的应用列表(分页)")
  @PostMapping("queryAuthBotPage")
  public ResultVO<PageInfo<BotAuthDTO>> queryAuthBotPage(@RequestBody BotAuthQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryAuthBotPage(queryParams));
  }

  @Operation(summary = "查询智能体规划策略")
  @GetMapping("getAgentStrategy")
  public ResultVO<SimpleAgentStrategyDTO> getAgentStrategy(@RequestParam(value = "tenantId", required = false) Long tenantId,
                                                           @RequestParam("botId") Long botId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    return ResultVO.success(botManageService.getAgentStrategy(tenantId, botId));
  }

  @Operation(summary = "保存智能体规划策略")
  @PostMapping("saveAgentStrategy")
  public ResultVO<Void> saveAgentStrategy(@RequestBody BotDTO bot) {
    return botManageService.saveAgentStrategy(bot);
  }

  @Operation(summary = "分页查询AI助理列表")
  @PostMapping("queryAiBotPage")
  public ResultVO<PageInfo<BaseBotDTO>> queryAiBotPage(@RequestBody AiBotQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryAiBotPage(queryParams));
  }

  @Operation(summary = "分页查询已上架智能应用列表")
  @PostMapping("queryPublishedAppPage")
  public ResultVO<PageInfo<PublishedAppDTO>> queryPublishedAppPage(@RequestBody @Valid PublishedAppQueryParams params) {
    return ResultVO.success(botQueryService.queryPublishedAppPage(params));
  }

  @Operation(summary = "分页查询租户发布应用")
  @PostMapping("getBotAuthPageForManage")
  public ResultVO<PageInfo<BotAuthDTO>> getBotAuthPageForManage(@RequestBody BotAuthQueryParams queryParams) {
    return ResultVO.success(botQueryService.queryBotAuthPageForManage(queryParams));
  }

  @Operation(summary = "修改应用发布广场状态")
  @PostMapping("modifyBotAuthStatus")
  public ResultVO<Void> modifyBotAuthStatus(@RequestBody BotAuthDTO botAuthDTO) {
    botManageService.modifyBotAuthStatus(botAuthDTO.getBotId(), botAuthDTO.getAuthStatus());
    return ResultVO.success();
  }

  @Operation(summary = "删除应用发布广场")
  @GetMapping("deleteBotAuth")
  public ResultVO<Void> deleteBotAuth(@RequestParam("botId") Long botId) {
    botManageService.deleteBotAuth(botId);
    return ResultVO.success();
  }

  @Operation(summary = "编辑应用发布广场信息")
  @PostMapping("modifyBotAuthInfo")
  public ResultVO<Void> modifyBotAuthInfo(@RequestBody BotAuthDTO botAuthDTO) {
    botManageService.modifyBotAuthInfo(botAuthDTO);
    return ResultVO.success();
  }

  @Operation(summary = "获取当前用户空间智能体列表")
  @GetMapping("listClawBotsInSpace")
  public ResultVO<List<SquareBotDTO>> listClawBotsInSpace(@RequestParam(name = "skillId", required = false) Long skillId,
                                                          @RequestParam(name = "tenantId", required = false) Long tenantId) {
    List<SquareBotDTO> squareBotDTOS = botQueryService.listClawBotsInSpace(skillId, tenantId);
    return ResultVO.success(squareBotDTOS);
  }

  @Operation(summary = "获取BoteClaw应用列表")
  @GetMapping("getBoteCrawList")
  public ResultVO<List<SimpleBotDTO>> getBoteCrawList(@RequestParam("spaceId") Long spaceId,
                                                      @RequestParam(name = "containAuthBot", required = false) Boolean containAuthBot) {
    return ResultVO.success(botQueryService.queryBoteCrawList(spaceId, containAuthBot));
  }

  @Operation(summary = "保存BoteClaw应用")
  @PostMapping("saveBoteClaw")
  public ResultVO<BotDTO> saveBoteClaw(@RequestBody @Valid BotDTO bot) {
    ResultVO<BotDTO> result = botManageService.saveBoteClaw(bot);
    if (result.isSuccess()) {
      // 刷新应用缓存：应用图标等
      refreshCache(bot);
      // 刷新通用智能体缓存
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT_ID);
      Long userId = SessionUtil.getLoginInfo().getUserId();
      // 刷新模型缓存
      String key = bot.getSpaceId() + CacheConsts.COLON + bot.getBotId() + CacheConsts.COLON + userId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GENERAL_AGENT, key);
    }
    return result;
  }

  @Operation(summary = "获取BoteClaw应用")
  @GetMapping("getBoteClaw")
  public ResultVO<BotDTO> getBoteClaw(@RequestParam("spaceId") Long spaceId, @RequestParam("botId") Long botId) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    Assert.notNull(botId, "应用 ID 不能为空");
    return ResultVO.success(botManageService.findBoteClaw(spaceId, botId));
  }

  @Operation(summary = "分页查询BoteClaw应用列表")
  @PostMapping("queryBoteClawPage")
  public ResultVO<PageInfo<BotDTO>> queryBoteClawPage(@RequestBody BotQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    queryParams.setTenantId(queryParams.getSpaceId());
    return ResultVO.success(botQueryService.queryBoteClawPage(queryParams));
  }

  @Operation(summary = "AI 门户通用搜索")
  @PostMapping("searchForAiPortal")
  public ResultVO<List<SimpleBotDTO>> searchForAiPortal(@RequestBody BotQueryParams params) {
    return botQueryService.searchBoteClaw(params);
  }

}
