package com.iwhalecloud.bote.service.bot.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.app.query.AiBotQueryParams;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.bot.BaseBotDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceDTO;
import com.iwhalecloud.bote.dto.bot.BeyondResourceStatusCountDTO;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.PublishedAppDTO;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.bot.SimpleAgentStrategyDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.SimpleRecommendQuestionDTO;
import com.iwhalecloud.bote.dto.bot.SimpleRecommendQuestionDTO.GroupInfo;
import com.iwhalecloud.bote.dto.bot.SimpleRecommendQuestionDTO.QuestionInfo;
import com.iwhalecloud.bote.dto.bot.SquareBotDTO;
import com.iwhalecloud.bote.dto.bot.SquareBotSkillRowDTO;
import com.iwhalecloud.bote.dto.bot.query.BeyondResourceQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotAuthQueryParams;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.dto.bot.query.PublishedAppQueryParams;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.dto.skill.SkillSquareDetailVO;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import com.iwhalecloud.bote.mapper.app.WebAppMapper;
import com.iwhalecloud.bote.mapper.bot.BotAuthManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotFavorManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotUserExperienceManageMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationMemberMapper;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.mapper.portal.TenantQueryMapper;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.bot.IBotQueryService;
import com.iwhalecloud.bote.service.bot.IBotUserExperienceManageService;
import com.iwhalecloud.bote.service.skill.IAgentSkillManageService;
import com.iwhalecloud.bote.service.skill.ISkillSquareService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 机器人查询服务
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Service
@RequiredArgsConstructor
public class BotQueryServiceImpl implements IBotQueryService {

  // @formatter:off
  private final BotQueryMapper botQueryMapper;
  private final WebAppMapper webAppMapper;
  private final BotUserExperienceManageMapper botUserExperienceManageMapper;
  private final BotSceneManageMapper botSceneManageMapper;
  private final SceneQueryMapper sceneQueryMapper;
  private final BotAuthManageMapper botAuthManageMapper;
  private final BotFavorManageMapper botFavorManageMapper;
  private final BotRelaManageMapper botRelaManageMapper;
  private final IBotUserExperienceManageService botUserExperienceManageService;
  private final ICatalogManageService catalogManageService;
  private final TenantSettingInfoManageMapper settingInfoManageMapper;
  private final TenantQueryMapper tenantQueryMapper;
  private final WorkspaceManageMapper workspaceManageMapper;
  private final ISkillSquareService skillSquareService;
  private final IAgentSkillManageService agentSkillManageService;
  private final OrganizationMemberMapper organizationMemberMapper;
  private final AttrSpecCache attrSpecCache;
  // @formatter:on

  /**
   * 资源类型：智能应用
   */
  private static final String RESOURCE_TYPE_APPLICATION = "APPLICATION";

  /**
   * 资源类型：智能体
   */
  private static final String RESOURCE_TYPE_AGENT = "AGENT";

  /**
   * 智能应用详情URL模板
   */
  private static final String BOT_DETAIL_URL_TEMPLATE = "/botDetails/config/scenes?tenantId=%d&botId=%d";

  /**
   * 智能体（knowledge/scene类型）详情URL模板
   */
  private static final String AGENT_CONFIG_URL_TEMPLATE = "/agentConfig?sceneId=%d&tenantId=%d";

  /**
   * 智能体（chatflow类型）详情URL模板
   */
  private static final String CHATFLOW_SCENE_URL_TEMPLATE = "/bot/sceneManage/complexScene?sceneId=%d&tenantId=%d";

  @Override
  public SimpleBotDTO getBotById(Long botId, Long tenantId) {
    SimpleBotDTO bot = botQueryMapper.getBot(botId, tenantId);
    if (bot != null) {
      // 欢迎页设置
      setPageSettingInfo(bot);
      // 收藏信息
      bot.setIsFavor(botQueryMapper.existsBotFavor(tenantId, botId, SessionUtil.getLoginInfo().getUserId()));
      // 会话辅助信息
      bot.setExperiences(CollectionUtils.emptyIfNull(botQueryMapper.selectBotUserExperience(tenantId, botId)).stream()
        .collect(Collectors.groupingBy(SimpleBotUserExperienceDTO::getType)));
      // 对话时的默认智能体
      List<SimpleBotSceneRelDTO> scenes = botRelaManageMapper.selectSimpleBotSceneRelList(tenantId,
        Collections.singletonList(botId));
      if (CollectionUtils.isNotEmpty(scenes)) {
        if (scenes.size() == 1) {
          bot.setDefaultSceneId(scenes.get(0).getSceneId());
          bot.setDefaultSceneName(scenes.get(0).getSceneName());
        }
        else {
          SimpleBotSceneRelDTO scene = IterableUtils.find(scenes, p -> BaseConsts.TRUE.equals(p.getIsDefault()));
          if (scene != null) {
            bot.setDefaultSceneId(scene.getSceneId());
            bot.setDefaultSceneName(scene.getSceneName());
          }
        }
      }
      // 租户设置信息
      bot.setFuncSwitch(getFuncSwitchSettingInfo(botId, tenantId));
      // 关联网页信息
      bot.setWebApp(webAppMapper.selectWebAppByBotId(tenantId, botId));
      // 设置是否为BoteClaw
      if (BaseConsts.DATA_FROM_PORTAL_BOT.equals(bot.getDataFrom()) || BaseConsts.BOTE_AI_ID.equals(bot.getBotId())) {
        bot.setIsBoteClaw(BaseConsts.TRUE);
      }
    }
    return bot;
  }

  @Override
  public SimpleBotDTO getSimpleBot(Long tenantId, Long botId) {
    return botQueryMapper.selectSimpleBot(tenantId, botId);
  }

  /**
   * 获取bot的func_switch设置信息
   */
  private Map<String, Object> getFuncSwitchSettingInfo(Long botId, Long tenantId) {
    TenantQueryParams queryParams = new TenantQueryParams();
    queryParams.setBotId(botId);
    queryParams.setTenantId(tenantId);
    queryParams.setFuncTypes(Collections.singletonList(BaseConsts.FUNC_TYPE_FUNC_SWITCH));
    List<TenantSettingInfoDTO> settingInfoList = settingInfoManageMapper.selectTenantSettingInfoListByFuncTypes(
      queryParams);
    if (CollectionUtils.isNotEmpty(settingInfoList)) {
      return JsonUtil.parseJson(settingInfoList.get(0).getSettingInfo(), new TypeReference<Map<String, Object>>() {
      });
    }
    return null;
  }

  @Override
  public SimpleBotDTO getDefaultBot(Long tenantId) {
    SimpleBotDTO bot = botQueryMapper.getDefaultBot(tenantId);
    if (bot == null) {
      bot = botQueryMapper.getFirstBot(tenantId);
    }
    return bot;
  }

  @Override
  public PageInfo<BotDTO> queryBotPage(BotQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    queryParams.setCatalogItemList(
      catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(),
        CatalogConsts.TYPE_BOT));
    // 转换策略类型查询参数
    convertStrategyTypeParam(queryParams);
    // noinspection resource
    PageInfo<BotDTO> pageInfo = botQueryMapper.selectBotPage(queryParams, rowBounds).toPageInfo();
    setIsFavor(pageInfo.getList(), queryParams.getTenantId());
    // 补充关联智能体
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      List<Long> botIds = pageInfo.getList().stream().map(BotDTO::getBotId).collect(Collectors.toList());
      Map<Long, List<BotSceneRelDTO>> group = CollectionUtils.emptyIfNull(
          botRelaManageMapper.selectBotSceneRelList(queryParams.getTenantId(), botIds)).stream()
        .collect(Collectors.groupingBy(BotSceneRelDTO::getBotId));
      for (BotDTO bot : pageInfo.getList()) {
        bot.setScenes(group.get(bot.getBotId()));
        bot.setStrategyType(parseStrategyType(bot.getAgentStrategy()));
        bot.setAgentStrategy(null);
      }
    }
    return pageInfo;
  }

  /**
   * 转换策略类型查询参数
   */
  private void convertStrategyTypeParam(BotQueryParams queryParams) {
    if (StringUtils.isEmpty(queryParams.getStrategyType())) {
      return;
    }
    queryParams.setStrategyTypeNone(BaseConsts.AGENT_STRATEGY_NONE.equals(queryParams.getStrategyType()));
    queryParams.setStrategyType(String.format("\"type\":\"%s\"", queryParams.getStrategyType()));
  }

  /**
   * 解析智能应用策略类型
   */
  private String parseStrategyType(String agentStrategy) {
    if (StringUtils.isEmpty(agentStrategy)) {
      return BaseConsts.AGENT_STRATEGY_NONE;
    }
    SimpleAgentStrategyDTO strategy = JsonUtil.parseJson(agentStrategy, SimpleAgentStrategyDTO.class);
    if (strategy == null) {
      return BaseConsts.AGENT_STRATEGY_NONE;
    }
    return strategy.getType();
  }

  @Override
  public PageInfo<BotDTO> beyondQueryBotPage(BotQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    queryParams.setCatalogItemList(
      catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(),
        CatalogConsts.TYPE_BOT));
    // noinspection resource
    PageInfo<BotDTO> pageInfo = botQueryMapper.beyondQueryBotPage(queryParams, rowBounds).toPageInfo();
    setIsFavor(pageInfo.getList(), queryParams.getTenantId());
    // 补充关联智能体
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      List<Long> botIds = pageInfo.getList().stream().map(BotDTO::getBotId).collect(Collectors.toList());
      Map<Long, List<BotSceneRelDTO>> group = CollectionUtils.emptyIfNull(
          botRelaManageMapper.selectBotSceneRelList(queryParams.getTenantId(), botIds)).stream()
        .collect(Collectors.groupingBy(BotSceneRelDTO::getBotId));
      for (BotDTO bot : pageInfo.getList()) {
        bot.setScenes(group.get(bot.getBotId()));
      }
    }
    return pageInfo;
  }

  @Override
  public PageInfo<BeyondResourceDTO> beyondQueryResourcePage(BeyondResourceQueryParams queryParams) {
    Assert.notNull(queryParams, "入参不能为空");
    Assert.notNull(queryParams.getTenantId(), "租户 ID 不能为空");
    RowBounds rowBounds = queryParams.buildRowBounds();
    Long userId = SessionUtil.getOptionalUserId();
    // noinspection resource
    PageInfo<BeyondResourceDTO> pageInfo = botQueryMapper.beyondQueryResourcePage(queryParams, userId, rowBounds)
      .toPageInfo();

    // 拼装 detailUrl
    Long tenantId = queryParams.getTenantId();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      for (BeyondResourceDTO resource : pageInfo.getList()) {
        String detailUrl = buildDetailUrl(resource, tenantId);
        resource.setDetailUrl(detailUrl);
      }
    }

    return pageInfo;
  }

  /**
   * 根据资源类型和场景类型拼装 detailUrl
   */
  private String buildDetailUrl(BeyondResourceDTO resource, Long tenantId) {
    String resourceType = resource.getResourceType();
    Long resourceId = resource.getResourceId();

    if (RESOURCE_TYPE_APPLICATION.equals(resourceType)) {
      return String.format(BOT_DETAIL_URL_TEMPLATE, tenantId, resourceId);
    }
    else if (RESOURCE_TYPE_AGENT.equals(resourceType)) {
      String sceneType = resource.getSceneType();
      if (SceneConsts.SCENE_TYPE_KNOWLEDGE.equals(sceneType) || SceneConsts.SCENE_TYPE_SCENE.equals(sceneType) || SceneConsts.SCENE_TYPE_CLAW.equals(
        sceneType)) {
        return String.format(AGENT_CONFIG_URL_TEMPLATE, resourceId, tenantId);
      }
      else if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(sceneType)) {
        return String.format(CHATFLOW_SCENE_URL_TEMPLATE, resourceId, tenantId);
      }
    }

    return null;
  }

  @Override
  public BeyondResourceStatusCountDTO beyondGetTotalCountOnAllStatus(BeyondResourceQueryParams queryParams) {
    Assert.notNull(queryParams, "入参不能为空");
    Assert.notNull(queryParams.getTenantId(), "租户 ID 不能为空");
    Long userId = SessionUtil.getOptionalUserId();
    return botQueryMapper.beyondGetTotalCountOnAllStatus(queryParams, userId);
  }

  @Override
  public List<BaseBotDTO> queryBotAndSceneList(Long tenantId) {
    List<BaseBotDTO> botList = botQueryMapper.selectBaseBotList(tenantId);
    // 补充关联智能体
    if (CollectionUtils.isNotEmpty(botList)) {
      List<Long> botIds = botList.stream().map(BaseBotDTO::getBotId).collect(Collectors.toList());
      Map<Long, List<SimpleBotSceneRelDTO>> group = CollectionUtils.emptyIfNull(
          botRelaManageMapper.selectSimpleBotSceneRelList(tenantId, botIds)).stream()
        .collect(Collectors.groupingBy(com.iwhalecloud.bote.dto.bot.SimpleBotSceneRelDTO::getBotId));
      for (BaseBotDTO bot : botList) {
        bot.setScenes(group.get(bot.getBotId()));
      }
    }
    return botList;
  }

  @Override
  public List<BotDTO> queryBotList(BotQueryParams queryParams) {
    List<BotDTO> bots = botQueryMapper.selectBotList(queryParams);
    setIsFavor(bots, queryParams.getTenantId());
    if (CollectionUtils.isNotEmpty(queryParams.getBotIds())) {
      Map<Long, BotDTO> botMap = bots.stream().collect(Collectors.toMap(BotDTO::getBotId, b -> b, (a, b) -> a));
      bots = queryParams.getBotIds().stream()
        .map(botMap::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }
    return bots;
  }

  @Override
  public List<BotDTO> queryCommonBotList() {
    return botQueryMapper.selectCommonBotList();
  }

  @Override
  public PageInfo<BotDTO> queryUserBotPage(BotQueryParams queryParams) {
    queryParams.setBotStatus(BaseConsts.BOT_STATUS_PUBLISH);
    queryParams.setTenantId(null);
    queryParams.setMemberId(SessionUtil.getLoginInfo().getUserId());
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return botQueryMapper.selectBotPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public List<BotDTO> queryUserBotList(BotQueryParams queryParams) {
    List<BotDTO> list = new ArrayList<>();
    List<BotDTO> bots = new ArrayList<>();
    queryParams.setMemberId(SessionUtil.getLoginInfo().getUserId());
    bots.addAll(botQueryMapper.selectUserBotList(queryParams));

    queryParams.setPageSize(5);
    PageInfo<BotDTO> pages = queryUserBotPage(queryParams);
    bots.addAll(pages.getList());

    // 去重处理
    List<Long> ids = new ArrayList<>();
    for (BotDTO bot : bots) {
      if (!ids.contains(bot.getBotId())) {
        list.add(bot);
        ids.add(bot.getBotId());
      }
    }
    return list.size() < 4 ? list : list.subList(0, 4);
  }

  @Override
  public List<BotDTO> queryPopularBotList(BotQueryParams queryParams) {
    return botQueryMapper.selectPopularBotList(queryParams);
  }

  @Override
  public PageInfo<BotAuthDTO> queryAuthBotPage(BotAuthQueryParams queryParams) {
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    return botAuthManageMapper.selectBotAuthPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<SimpleBotDTO> queryBotListForIntent(Long tenantId) {
    List<SimpleBotDTO> bots = botQueryMapper.selectBotListForIntent(tenantId);
    if (CollectionUtils.isEmpty(bots)) {
      return Collections.emptyList();
    }
    List<SimpleBotSceneDTO> scenes = sceneQueryMapper.selectSceneListByTenantId(tenantId, SceneConsts.EXCLUDE_LABELS);
    Map<Long, List<SimpleBotSceneDTO>> group = CollectionUtils.emptyIfNull(scenes).stream()
      .collect(Collectors.groupingBy(SimpleBotSceneDTO::getBotId));
    // FIXME 补充问题
    for (SimpleBotDTO bot : bots) {
      bot.setScenes(group.get(bot.getBotId()));
    }
    return bots;
  }

  @Override
  public List<SceneIntentDTO> querySceneListForIntent(Long tenantId, @Nullable Long botId) {
    List<SceneIntentDTO> scenes;
    if (botId == null) {
      scenes = sceneQueryMapper.selectSceneIntentListByTenantId(tenantId, SceneConsts.EXCLUDE_LABELS);
    }
    else {
      scenes = sceneQueryMapper.selectSceneListByBotId(tenantId, botId, SceneConsts.EXCLUDE_LABELS);
    }
    // 补充问题
    return scenes;
  }

  @Override
  public Map<String, Object> getPageSettingIcon(Long tenantId, Long botId) {
    SimpleBotDTO bot = botQueryMapper.getBotPageSettingIcon(tenantId, botId);
    if (bot == null) {
      return null;
    }
    Map<String, Object> map = null;
    if (StringUtils.isNotEmpty(bot.getPageSettingIcon())) {
      map = JsonUtil.parseJson(bot.getPageSettingIcon(), new TypeReference<Map<String, Object>>() {
      });
    }
    if (map == null) {
      map = new HashMap<>();
    }
    if (StringUtils.isEmpty(MapUtils.getString(map, "welcomeIcon"))) {
      map.put("welcomeIcon", bot.getBotIcon());
    }
    return map;
  }

  @Override
  public PageInfo<BaseBotDTO> queryAiBotPage(AiBotQueryParams params) {
    //noinspection resource
    return botQueryMapper.selectAiBotPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<BotAuthDTO> queryBotAuthPageForManage(BotAuthQueryParams queryParams) {
    // noinspection resource
    PageInfo<BotAuthDTO> pageInfo = botAuthManageMapper.selectPageForManage(queryParams, queryParams.buildRowBounds())
      .toPageInfo();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      List<Long> authIds = pageInfo.getList().stream().map(BotAuthDTO::getAuthId).toList();
      pageInfo.setList(botAuthManageMapper.selectListByAuthIds(authIds));
    }
    return pageInfo;
  }

  @Override
  public PageInfo<PublishedAppDTO> queryPublishedAppPage(PublishedAppQueryParams params) {
    // 根据 spaceId（实际为外系统 ext_space_id）解析灵犀 space_id
    SimpleWorkspaceDTO workspace = workspaceManageMapper.existWorkSpaceByExtSpaceId(String.valueOf(params.getSpaceId()));
    if (workspace == null || workspace.getSpaceId() == null) {
      PageInfo<PublishedAppDTO> emptyPage = new PageInfo<>(Collections.emptyList());
      emptyPage.setPageNum(params.getPageNum() != null ? params.getPageNum() : 1);
      emptyPage.setPageSize(params.getPageSize() != null ? params.getPageSize() : 20);
      emptyPage.setTotal(0);
      return emptyPage;
    }
    Long tenantId = tenantQueryMapper.getTenantIdByExtTenantId(params.getExtTenantId(), workspace.getSpaceId());
    if (tenantId == null) {
      PageInfo<PublishedAppDTO> emptyPage = new PageInfo<>(Collections.emptyList());
      emptyPage.setPageNum(params.getPageNum() != null ? params.getPageNum() : 1);
      emptyPage.setPageSize(params.getPageSize() != null ? params.getPageSize() : 20);
      emptyPage.setTotal(0);
      return emptyPage;
    }
    // noinspection resource
    return botQueryMapper.selectPublishedAppPage(params, tenantId, params.buildRowBounds())
      .toPageInfo();
  }

  private static void applySkillInstallFields(SquareBotDTO dto,
                                              @Nullable String squareVerNorm,
                                              Map<Long, String> installedByBot) {
    Long bid = dto.getBotId();
    String installedVer = bid == null ? null : installedByBot.get(bid);
    boolean installed = installedVer != null;
    dto.setSkillInstalled(installed);
    if (squareVerNorm == null || !installed) {
      dto.setSkillVersionHasUpdate(null);
      return;
    }
    dto.setSkillVersionHasUpdate(!normalizeSkillVersion(installedVer).equals(squareVerNorm));
  }

  /**
   * 与 {@link com.iwhalecloud.bote.service.skill.impl.SkillSquareServiceImpl} 安装逻辑一致：空版本按 1.0.0
   */
  private static String normalizeSkillVersion(String version) {
    String t = StringUtils.trimToNull(version);
    return t != null ? t : "1.0.0";
  }

  @Override
  public List<SquareBotDTO> listClawBotsInSpace(Long skillId, Long tenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    List<SquareBotDTO> list = botQueryMapper.selectSquareBotDTOList(tenantId, userId);
    fillSkillSquareInstallStateIfNeeded(list, skillId, tenantId);
    return list;
  }

  @Override
  public List<SimpleBotDTO> queryBoteCrawList(Long spaceId, Boolean containAuthBot) {
    List<SimpleBotDTO> botList;
    // 如果要求包含用户授权应用列表则走搜索接口
    if (containAuthBot) {
      BotQueryParams params = new BotQueryParams();
      params.setSpaceId(spaceId);
      botList = searchBoteClaw(params).getResultObject();
    }
    else {
      // 否则只返回用户创建的应用列表
      Long userId = SessionUtil.getLoginInfo().getUserId();
      botList = botQueryMapper.selectUserBoteCrawList(spaceId, userId);
    }
    // 补充平台预置通用智能体
    List<SimpleAttrDTO> attrs = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, ChatConsts.CHAT_DEFAULT_PLAT_BOT);
    for (SimpleAttrDTO attr : CollectionUtils.emptyIfNull(attrs)) {
      Long botId = Long.valueOf(attr.getAttrValue());
      SimpleBotDTO bot = new SimpleBotDTO();
      bot.setBotId(botId);
      bot.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
      bot.setBotName(attr.getAttrValueName());
      bot.setBotUse(attr.getAttrValueName());
      botList.add(bot);
    }
    return botList;
  }

  @Override
  public PageInfo<BotDTO> queryBoteClawPage(BotQueryParams queryParams) {
    queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    return botQueryMapper.selectBoteClawPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public ResultVO<List<SimpleBotDTO>> searchBoteClaw(BotQueryParams params) {
    Assert.notNull(params.getSpaceId(), "空间 ID 不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    params.setTenantId(params.getSpaceId());
    params.setUserId(userId);
    params.setIsAdmin(SessionUtil.isSuperAdmin(userId));
    // 查询用户归属的组织列表
    Set<Long> orgIds = new HashSet<>();
    orgIds.add(-1L);
    List<OrganizationUserDTO> organizationUserList = organizationMemberMapper.selectUserOrgByUserId(params.getSpaceId(), userId);
    if (CollectionUtils.isNotEmpty(organizationUserList)) {
      orgIds.addAll(organizationUserList.stream().map(OrganizationUserDTO::getOrgId).toList());
    }
    params.setOrgIds(orgIds.stream().toList());
    // 执行BoteClaw搜索
    List<SimpleBotDTO> botList = botQueryMapper.searchBoteClaw(params);
    // 根据 botId 去重
    botList = botList.stream().collect(Collectors.collectingAndThen(
      Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SimpleBotDTO::getBotId))),
      ArrayList::new
    ));
    return ResultVO.success(botList);
  }

  /**
   * 在传入 skillId 时填充 {@link SquareBotDTO#getSkillInstalled()}、{@link SquareBotDTO#getSkillVersionHasUpdate()}。
   */
  private void fillSkillSquareInstallStateIfNeeded(List<SquareBotDTO> bots, Long skillId, Long tenantId) {
    if (skillId == null || CollectionUtils.isEmpty(bots)) {
      return;
    }
    List<Long> botIds = bots.stream().map(SquareBotDTO::getBotId).filter(Objects::nonNull).distinct().toList();
    SkillSquareDetailVO square = skillSquareService.getDetail(skillId);
    String squareVerNorm = square != null ? normalizeSkillVersion(square.getVersion()) : null;
    Map<Long, String> installedByBot = loadInstalledSkillVersionByBot(tenantId, skillId, botIds);
    for (SquareBotDTO dto : bots) {
      applySkillInstallFields(dto, squareVerNorm, installedByBot);
    }
  }

  private Map<Long, String> loadInstalledSkillVersionByBot(Long tenantId, Long skillSquareId, List<Long> botIds) {
    if (botIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<SquareBotSkillRowDTO> rows = agentSkillManageService.listInstalledSkillVersionForSquareBots(tenantId, skillSquareId, botIds);
    if (CollectionUtils.isEmpty(rows)) {
      return Collections.emptyMap();
    }
    return rows.stream()
      .filter(Objects::nonNull)
      .filter(r -> r.getBotId() != null)
      .collect(Collectors.toMap(
        SquareBotSkillRowDTO::getBotId,
        r -> StringUtils.defaultString(r.getSkillVersion()),
        (a, b) -> a));
  }

  private void setIsFavor(List<BotDTO> bots, Long tenantId) {
    if (CollectionUtils.isEmpty(bots)) {
      return;
    }
    List<Long> favorBotIds = botFavorManageMapper.selectFavoriteBotIdsByUserId(SessionUtil.getLoginInfo().getUserId(),
      tenantId);
    for (BotDTO bot : bots) {
      bot.setIsFavor(favorBotIds.contains(bot.getBotId()) ? BaseConsts.TRUE : BaseConsts.FALSE);
    }
  }

  private void setPageSettingInfo(SimpleBotDTO bot) {
    if (StringUtils.isEmpty(bot.getPageSettingInfo())) {
      return;
    }
    SimpleRecommendQuestionDTO info = JsonUtil.parseJsonRequired(bot.getPageSettingInfo(),
      SimpleRecommendQuestionDTO.class);
    if ("1000".equals(info.getThemeOpt())) {
      checkQuickLinkSettingInfo(bot.getTenantId(), info);
    }
    else {
      if (info.getQuestionSettingInfo() != null) {
        info.getQuestionSettingInfo()
          .setList(checkQuestionSettingInfo(bot.getTenantId(), info.getQuestionSettingInfo().getList()));
      }

      if (info.getScenesSettingInfo() != null) {
        List<Map<String, Object>> items = new ArrayList<>(info.getScenesSettingInfo().getList());
        List<Long> publishSceneIds = new ArrayList<>();
        for (Map<String, Object> map : CollectionUtils.emptyIfNull(info.getScenesSettingInfo().getList())) {
          Long relId = MapUtils.getLong(map, "relId");
          BotSceneRelDTO sceneRel = botRelaManageMapper.getBotSceneRel(bot.getTenantId(), relId);
          if (sceneRel != null) {
            map.put("sceneName", sceneRel.getSceneName());
            map.put("prologue", sceneRel.getPrologue());
            // 收集已上架的智能体ID列表
            publishSceneIds.add(sceneRel.getSceneId());
          }
        }
        // 过滤已下架的推荐智能体
        items = items.stream().filter(item -> publishSceneIds.contains(MapUtils.getLong(item, "sceneId")))
          .collect(Collectors.toList());
        info.getScenesSettingInfo().setList(items);
      }

      if (info.getPointSettingInfo() != null) {
        info.getPointSettingInfo()
          .setList(checkQuestionSettingInfo(bot.getTenantId(), info.getPointSettingInfo().getList()));
      }
    }
    bot.setPageSettingInfoJson(
      JsonUtil.parseJson(JsonUtil.toJsonString(info), new TypeReference<Map<String, Object>>() {
      }));
    bot.setPageSettingInfo(null);
  }

  private void checkQuickLinkSettingInfo(Long tenantId, SimpleRecommendQuestionDTO info) {
    if (info.getQuickLinkSettingInfo() == null) {
      return;
    }
    for (GroupInfo group : CollectionUtils.emptyIfNull(info.getQuickLinkSettingInfo().getList())) {
      List<QuestionInfo> items = new ArrayList<>(group.getItems().size());
      for (QuestionInfo dto : CollectionUtils.emptyIfNull(group.getItems())) {
        if ("scene".equals(dto.getType())) {
          BotSceneEntity scene = botSceneManageMapper.selectSceneBasicInfo(tenantId, Long.valueOf(dto.getId()));
          if (scene != null && SceneConsts.SCENE_STATUS_PUBLISH.equals(scene.getSceneStatus())) {
            dto.setTitle(scene.getSceneName());
            dto.setIcon(scene.getSceneIcon());
            dto.setPrologue(scene.getPrologue());
            items.add(dto);
          }
        }
        else {
          BotUserExperienceDTO experience = botUserExperienceManageMapper.getBotUserExperience(tenantId,
            Long.valueOf(dto.getId()));
          if (experience != null) {
            if (BaseConsts.USER_EXPERIENCE_TYPE_POINT.equals(dto.getType())) {
              dto.setTitle(experience.getTitle());
              dto.setContent(experience.getContent());
              if (StringUtils.isNotEmpty(experience.getPageTemplateJson())) {
                dto.setPageContentInfo(JsonUtil.readTree(experience.getPageTemplateJson()));
              }
            }
            else {
              dto.setTitle(experience.getContent());
              dto.setContent(experience.getContent());
            }
            items.add(dto);
          }
        }
      }
      group.setItems(items);
    }
  }

  private List<Map<String, Object>> checkQuestionSettingInfo(Long tenantId, List<Map<String, Object>> list) {
    if (CollectionUtils.isEmpty(list)) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> items = new ArrayList<>(list.size());
    for (Map<String, Object> map : list) {
      Long id = MapUtils.getLong(map, "experienceId");
      BotUserExperienceDTO dto = botUserExperienceManageService.getBotUserExperience(tenantId, id);
      if (dto != null) {
        String title = BaseConsts.USER_EXPERIENCE_TYPE_POINT.equals(dto.getType()) ? dto.getTitle() : dto.getContent();
        Map<String, Object> object = JsonUtil.convert(dto, new TypeReference<Map<String, Object>>() {
        });
        object.put("title", title);
        object.put("content", dto.getContent());
        items.add(object);
      }
    }
    return items;
  }

}
