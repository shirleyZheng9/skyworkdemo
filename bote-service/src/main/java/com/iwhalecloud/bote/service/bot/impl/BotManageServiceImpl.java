package com.iwhalecloud.bote.service.bot.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.agent.AiSkillDTO;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.BotSettingInfoDTO;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.bot.SimpleAgentStrategyDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotApplyParams;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.mapper.agent.AiModelManageMapper;
import com.iwhalecloud.bote.mapper.agent.AiWorkspaceManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotAuthManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.service.agent.IAiSkillManageService;
import com.iwhalecloud.bote.service.agent.IAiWorkspaceManageService;
import com.iwhalecloud.bote.service.base.ILabelManageService;
import com.iwhalecloud.bote.service.bot.IBotManageService;
import com.iwhalecloud.bote.service.bot.IBotRelaManageService;
import com.iwhalecloud.bote.service.chat.IChatThemeService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 机器人管理服务
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Service
@RequiredArgsConstructor
public class BotManageServiceImpl implements IBotManageService {

  // @formatter:off
  private final BotManageMapper botManageMapper;
  private final TenantManageMapper tenantManageMapper;
  private final BotAuthManageMapper botAuthManageMapper;
  private final BotSceneManageMapper sceneManageMapper;
  private final ILabelManageService labelManageService;
  private final IBotRelaManageService botRelaManageService;
  private final ITenantSettingInfoManageService settingInfoManageService;
  private final IChatThemeService chatThemeService;
  private final AiWorkspaceManageMapper workspaceManageMapper;
  private final IAiWorkspaceManageService workspaceManageService;
  private final AiModelManageMapper aiModelManageMapper;
  private final IAiSkillManageService aiSkillManageService;
  // @formatter:on

  @Override
  @Transactional
  public ResultVO<BotDTO> saveBot(BotDTO bot) {
    Pair<BotDTO, ResultVO<BotDTO>> preprocessing = preprocessing(bot);
    BotDTO old = preprocessing.getLeft();
    ResultVO<BotDTO> right = preprocessing.getRight();
    if (right != null) {
      return right;
    }
    DataDifference<BotDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, bot, false, bot.getTenantId(), OperClassEnum.BOT);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    if (BaseConsts.TRUE.equals(bot.getIsDefault())) {
      // 确保默认机器人唯一
      botManageMapper.clearDefaultBot(bot.getTenantId(), bot.getBotId(), SessionUtil.getLoginInfo().getUserId());
    }
    // 如果是创建智能应用则添加一条默认主题
    BotDTO saveBot = difference.getToSaveData();
    if (old == null) {
      chatThemeService.createDefaultTheme(saveBot.getBotId(), saveBot.getTenantId());
    }
    return ResultVO.success(saveBot);
  }

  /**
   * 对机器人的前置处理
   * @param bot 机器人信息
   * @return 返回旧数据或者处理结果
   */
  private Pair<BotDTO, ResultVO<BotDTO>> preprocessing(BotDTO bot) {
    if (botManageMapper.existsBotName(bot)) {
      return Pair.of(null, BaseErrorConstant.CHECK_NAME.toResult(bot.getBotName()));
    }
    bot.setStatusCd(BaseConsts.STATUS_CD_VALID);
    if (StringUtils.isEmpty(bot.getBotStatus())) {
      bot.setBotStatus(BaseConsts.BOT_STATUS_UNPUBLISH);
    }
    if (bot.getCatalogItemId() == null) {
      bot.setCatalogItemId(CatalogConsts.BOT_CATALOG_ITEM_ID);
    }
    BotDTO old = bot.getBotId() == null ? null : findBot(bot.getTenantId(), bot.getBotId());

    if (old != null) {
      // 欢迎页设置参数不可调整
      bot.setPageBaseInfo(old.getPageBaseInfo());
      bot.setPageSettingInfo(old.getPageSettingInfo());
      if (BaseConsts.TRUE.equals(bot.getIsDefault()) && !BaseConsts.BOT_STATUS_PUBLISH.equals(bot.getBotStatus())) {
        return Pair.of(null, BaseErrorConstant.BOT_DEFAULT_FAIL.toResult(bot.getBotName()));
      }
    }
    return Pair.of(old, null);
  }

  @Override
  @Transactional
  public ResultVO<Void> savePageInfo(BotDTO bot) {
    BotDTO old = findBot(bot.getTenantId(), bot.getBotId());
    if (old == null) {
      return ResultVO.fail("查询不到有效的智能应用");
    }
    BotDTO copyBot = JsonUtil.parseJsonRequired(JsonUtil.toJsonString(old), BotDTO.class);
    copyBot.setPrologue(MapUtils.getString(bot.getPageBaseInfoJson(), "prologue"));
    copyBot.setPageSettingInfo(JsonUtil.toJsonString(bot.getPageSettingInfoJson()));
    DataDifference<BotDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, copyBot, false, old.getTenantId(), OperClassEnum.BOT);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> saveBotSettingInfo(BotSettingInfoDTO settingInfoDTO) {
    Long tenantId = settingInfoDTO.getTenantId();
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 保存页面设置信息
    BotDTO botDTO = new BotDTO();
    botDTO.setBotId(settingInfoDTO.getBotId());
    botDTO.setTenantId(tenantId);
    botDTO.setUpdatorId(userId);
    botDTO.setPageSettingInfo(JsonUtil.toJsonString(settingInfoDTO.getPageSettingInfoJson()));
    if (MapUtils.isNotEmpty(settingInfoDTO.getPageSettingIcon())) {
      // 按需修改欢迎页图标
      botDTO.setPageSettingIcon(JsonUtil.toJsonString(settingInfoDTO.getPageSettingIcon()));
    }
    botManageMapper.updatePageSettingInfo(botDTO);
    // 保存func_switch
    if (settingInfoDTO.getFuncSwitch() != null) {
      settingInfoManageService.saveFuncSwitchInfo(tenantId, settingInfoDTO.getBotId(), JsonUtil.toJsonString(settingInfoDTO.getFuncSwitch()));
    }
    // 切换主题
    if (settingInfoDTO.getThemeId() != null) {
      ChatThemeDTO chatThemeDTO = new ChatThemeDTO();
      chatThemeDTO.setBotId(settingInfoDTO.getBotId());
      chatThemeDTO.setTenantId(settingInfoDTO.getTenantId());
      chatThemeDTO.setThemeId(settingInfoDTO.getThemeId());
      chatThemeService.switchTheme(chatThemeDTO);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteBot(Long tenantId, Long botId) {
    botManageMapper.deleteBot(tenantId, botId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.BOT.name()).clear(tenantId, botId);
    return ResultVO.success();
  }

  @Nullable
  @Override
  public BotDTO findBot(Long tenantId, @Nullable Long botId) {
    if (botId == null) {
      return null;
    }
    return botManageMapper.getBot(tenantId, botId);
  }

  @Override
  public ResultVO<Void> publishBot(Long tenantId, Long botId, String botsStatus) {
    String isDefault = BaseConsts.FALSE;
    if (BaseConsts.BOT_STATUS_PUBLISH.equals(botsStatus)) {
      if (!botRelaManageService.existsBotSceneRel(tenantId, botId)) {
        return ResultVO.fail("未配置智能体的智能应用不允许上架");
      }
      isDefault = null;
    }
    int affectedRows = botManageMapper.updateBotStatus(tenantId, botsStatus, botId, SessionUtil.getLoginInfo().getUserId(), isDefault);
    if (affectedRows == 0) {
      return ResultVO.fail("智能应用不存在");
    }
    ResourceElementFactory.get(OperClassEnum.BOT.name()).submit(tenantId, botId);
    return ResultVO.success();
  }

  /**
   * <p>1、authTenantId == -1，userId == -1，orgId == -1 所有人员可见</p>
   * <p>2、authTenantId == -1，userId !=-1，orgId == -1部分用户可见</p>
   * <p>3、authTenantId != -1，userId == -1，orgId == -1 部分平台可见</p>
   * <p>4、authTenantId == -1，userId == -1，orgId != -1 部分组织可见</p>
   *
   * @param botAuth 机器人授权信息
   * @return 结果
   */
  @Override
  @Transactional
  public ResultVO<Void> authBot(BotAuthDTO botAuth) {
    List<BotAuthDTO> existingAuths = botAuthManageMapper.selectBotAuthList(botAuth.getBotId());
    List<Long> deleteAuthIds = new ArrayList<>();

    ResultVO<Void> validationResult = validate(botAuth, existingAuths, deleteAuthIds);
    if (!validationResult.isSuccess()) {
      return validationResult;
    }

    BotDTO bot = findBot(botAuth.getTenantId(), botAuth.getBotId());
    if (bot == null) {
      return ResultVO.fail("机器人不存在");
    }

    List<Long> updateAuthIds = computeAuthChanges(botAuth, existingAuths, deleteAuthIds);
    botAuth.setAuthIds(updateAuthIds);
    int effectedRows = applyAuthChanges(botAuth, bot, deleteAuthIds, updateAuthIds);
    return effectedRows == 0 ? BaseErrorConstant.NO_DIFFERENCE.toResult() : ResultVO.success();
  }

  /**
   * 验证授权信息
   *
   * @param botAuth 授权信息
   * @param existingAuths 现有授权列表
   * @param deleteAuthIds 需要删除的授权ID列表
   * @return 验证结果
   */
  private ResultVO<Void> validate(BotAuthDTO botAuth, List<BotAuthDTO> existingAuths, List<Long> deleteAuthIds) {
    if (!SessionUtil.isSuperAdmin(SessionUtil.getLoginInfo().getUserId()) && !BaseConsts.ROLE_MANAGE.equals(
      tenantManageMapper.getUserRole(botAuth.getTenantId(), SessionUtil.getLoginInfo().getUserId()))) {
      return ResultVO.fail("非管理员不允许授权");
    }
    if (BaseConsts.BOT_AUTH_TYPE_ALL.equals(botAuth.getAuthType())) {
      deleteAuthIds.addAll(CollectionUtils.emptyIfNull(existingAuths).stream().map(BotAuthDTO::getAuthId).collect(Collectors.toList()));
    }
    else {
      // 切换到自定义授权，如果有全员授权则删除
      CollectionUtils.emptyIfNull(existingAuths).stream()
        .filter(BotAuthDTO::isAuthorizedToAllUsers)
        .findFirst()
        .ifPresent(existing -> deleteAuthIds.add(existing.getAuthId()));
    }
    return ResultVO.success();
  }

  /**
   * 计算授权变更
   *
   * @param botAuth 新的授权信息
   * @param existingAuths 现有授权列表
   * @param deleteAuthIds 需要删除的授权ID列表
   * @return 需要更新的授权ID列表
   */
  private List<Long> computeAuthChanges(BotAuthDTO botAuth, List<BotAuthDTO> existingAuths, List<Long> deleteAuthIds) {
    List<Long> updateAuthIds = new ArrayList<>();
    Set<Long> existingIds = new HashSet<>();
    existingAuths.forEach(existing -> {
      if (deleteAuthIds.contains(existing.getAuthId())) {
        return;
      }
      // 判断现有授权是否需要删除
      if (shouldDeleteAuth(existing, botAuth)) {
        deleteAuthIds.add(existing.getAuthId());
      }
      else {
        // 收集已存在的授权ID，用于后续过滤新增授权
        collectExistingAuthId(existing, existingIds);
        // 判断授权信息是否变更，需要更新
        if (isAuthInfoChanged(existing, botAuth)) {
          updateAuthIds.add(existing.getAuthId());
        }
      }
    });

    // 过滤出真正需要新增的授权ID（排除已存在的）
    botAuth.setUserIds(CollectionUtils.emptyIfNull(botAuth.getUserIds()).stream().filter(id -> !existingIds.contains(id)).collect(Collectors.toList()));
    botAuth.setTenantIds(CollectionUtils.emptyIfNull(botAuth.getTenantIds()).stream().filter(id -> !existingIds.contains(id)).collect(Collectors.toList()));
    botAuth.setAuthOrgIds(CollectionUtils.emptyIfNull(botAuth.getAuthOrgIds()).stream().filter(id -> !existingIds.contains(id)).collect(Collectors.toList()));
    return updateAuthIds;
  }

  /**
   * 判断现有授权是否需要删除
   * 当授权对象（用户/租户/组织）不在新的授权列表中时，需要删除该授权
   *
   * @param existing 现有授权信息
   * @param botAuth 新的授权信息
   * @return 是否需要删除
   */
  private boolean shouldDeleteAuth(BotAuthDTO existing, BotAuthDTO botAuth) {
    boolean userNotMatch = existing.getUserId() != -1L && !CollectionUtils.emptyIfNull(botAuth.getUserIds()).contains(existing.getUserId());
    boolean tenantNotMatch = existing.getAuthTenantId() != -1L && !CollectionUtils.emptyIfNull(botAuth.getTenantIds()).contains(existing.getAuthTenantId());
    boolean orgNotMatch = existing.getOrgId() != null && existing.getOrgId() != -1L && !CollectionUtils.emptyIfNull(botAuth.getAuthOrgIds()).contains(existing.getOrgId());
    return userNotMatch || tenantNotMatch || orgNotMatch;
  }

  /**
   * 收集现有授权的ID到集合中
   * 用于后续判断哪些是新增的授权对象
   *
   * @param existing 现有授权信息
   * @param existingIds 用于存储已存在ID的集合
   */
  private void collectExistingAuthId(BotAuthDTO existing, Set<Long> existingIds) {
    if (existing.getUserId() != -1L) {
      existingIds.add(existing.getUserId());
    }
    else if (existing.getAuthTenantId() != -1L) {
      existingIds.add(existing.getAuthTenantId());
    }
    else if (existing.getOrgId() != null && existing.getOrgId() != -1L) {
      existingIds.add(existing.getOrgId());
    }
  }

  /**
   * 判断授权的基本信息是否发生变更
   * 包括目录ID、机器人图标、机器人描述
   *
   * @param existing 现有授权信息
   * @param botAuth 新的授权信息
   * @return 信息是否变更
   */
  private boolean isAuthInfoChanged(BotAuthDTO existing, BotAuthDTO botAuth) {
    return !Objects.equals(existing.getCatalogItemId(), botAuth.getCatalogItemId()) ||
      !Objects.equals(existing.getBotIcon(), botAuth.getBotIcon()) ||
      !Objects.equals(existing.getBotDesc(), botAuth.getBotDesc());
  }

  /**
   * 应用授权变更
   *
   * @param botAuth 授权信息
   * @param bot 机器人信息
   * @param deleteAuthIds 需要删除的授权ID列表
   * @param updateAuthIds 需要更新的授权ID列表
   * @return 影响行数
   */
  private int applyAuthChanges(BotAuthDTO botAuth, BotDTO bot, List<Long> deleteAuthIds, List<Long> updateAuthIds) {
    int effectedRows = 0;
    List<BotAuthDTO> newAuths = createBotAuth(botAuth, bot);
    if (CollectionUtils.isNotEmpty(newAuths)) {
      effectedRows += botAuthManageMapper.batchInsertBotAuth(newAuths);
    }
    if (CollectionUtils.isNotEmpty(deleteAuthIds)) {
      effectedRows += botAuthManageMapper.batchDeleteAuth(deleteAuthIds, SessionUtil.getLoginInfo().getUserId());
    }
    if (CollectionUtils.isNotEmpty(updateAuthIds)) {
      effectedRows += botAuthManageMapper.batchUpdateBotAuth(botAuth);
    }
    return effectedRows;
  }

  private List<BotAuthDTO> createBotAuth(BotAuthDTO botAuth, BotDTO bot) {
    List<BotAuthDTO> botAuths = new ArrayList<>();
    if (BaseConsts.BOT_AUTH_TYPE_ALL.equals(botAuth.getAuthType())) {
      botAuths.add(createBotAuth(botAuth, -1L, -1L, -1L, bot));
    }
    else {
      botAuth.getUserIds().forEach(userId -> botAuths.add(createBotAuth(botAuth, userId, -1L, -1L, bot)));
      botAuth.getTenantIds().forEach(tenantId -> botAuths.add(createBotAuth(botAuth, -1L, tenantId, -1L, bot)));
      botAuth.getAuthOrgIds().forEach(orgId -> botAuths.add(createBotAuth(botAuth, -1L, -1L, orgId, bot)));
    }
    return botAuths;
  }

  private BotAuthDTO createBotAuth(BotAuthDTO botAuth, Long userId, Long tenantId, Long orgId, BotDTO bot) {
    BotAuthDTO newBotAuth = new BotAuthDTO();
    newBotAuth.setBotId(botAuth.getBotId());
    newBotAuth.setAuthTenantId(tenantId);
    newBotAuth.setUserId(userId);
    newBotAuth.setOrgId(orgId);
    newBotAuth.setStatusCd(BaseConsts.STATUS_CD_VALID);
    newBotAuth.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    newBotAuth.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    newBotAuth.setCatalogItemId(botAuth.getCatalogItemId());
    newBotAuth.setOwnerTenantId(bot.getTenantId());
    newBotAuth.setAuthId(Sequences.BOT_AUTH_ID.next());
    newBotAuth.setBotDesc(botAuth.getBotDesc());
    newBotAuth.setBotIcon(botAuth.getBotIcon());
    newBotAuth.setBotName(bot.getBotName());
    newBotAuth.setAuthPublisher(botAuth.getAuthPublisher());
    newBotAuth.setAuthStatus(BaseConsts.TRUE);
    newBotAuth.setAuthOperator(userId);
    newBotAuth.setDataFrom(bot.getDataFrom());
    return newBotAuth;
  }

  @Override
  public ResultVO<Map<String, List<BotAuthDTO>>> queryAuthedBotList(Long botId) {
    List<BotAuthDTO> botAuthList = botAuthManageMapper.selectBotAuthList(botId);
    Map<String, List<BotAuthDTO>> result = new HashMap<>();
    if (CollectionUtils.isEmpty(botAuthList)) {
      return ResultVO.success(result);
    }
    List<BotAuthDTO> botAuthAllList = new ArrayList<>();
    List<BotAuthDTO> botAuthCustomList = new ArrayList<>();
    for (BotAuthDTO botAuth : botAuthList) {
      if (botAuth.isAuthorizedToAllUsers()) {
        botAuth.setAuthType(BaseConsts.BOT_AUTH_TYPE_ALL);
        botAuthAllList.add(botAuth);
      }
      else if (botAuth.isAuthorizedToPartUsers()) {
        botAuth.setAuthType(BaseConsts.BOT_AUTH_TYPE_CUSTOM);
        botAuth.setAuthSubType(BaseConsts.BOT_AUTH_TYPE_PART_USER);
        botAuthCustomList.add(botAuth);
      }
      else if (botAuth.isAuthorizedToPartTenants()) {
        botAuth.setAuthType(BaseConsts.BOT_AUTH_TYPE_CUSTOM);
        botAuth.setAuthSubType(BaseConsts.BOT_AUTH_TYPE_PART_TENANT);
        botAuthCustomList.add(botAuth);
      }
      else if (botAuth.isAuthorizedToPartOrgs()) {
        botAuth.setAuthType(BaseConsts.BOT_AUTH_TYPE_CUSTOM);
        botAuth.setAuthSubType(BaseConsts.BOT_AUTH_TYPE_PART_ORG);
        botAuthCustomList.add(botAuth);
      }
    }
    result.put(BaseConsts.BOT_AUTH_TYPE_ALL, botAuthAllList);
    result.put(BaseConsts.BOT_AUTH_TYPE_CUSTOM, botAuthCustomList);
    return ResultVO.success(result);
  }


  @Transactional
  public ResultVO<Long> apply(BotApplyParams apply, BotSceneDTO scene) {
    Long id;
    // 发起申请，并自动审批通过，暂时不保存申请记录
    List<Long> botIds = apply.getBotIds();
    if (CollectionUtils.isEmpty(botIds)) {
      botIds = new ArrayList<>();
    }
    if (BaseConsts.TRUE.equals(apply.getIsCreate())) {
      BotDTO bot = new BotDTO();
      bot.setBotName(scene.getSceneName());
      bot.setBotUse(scene.getSceneDesc());
      bot.setBotIcon(scene.getSceneIcon());
      bot.setBotStatus(BaseConsts.TRUE.equals(apply.getIsPublish()) ? BaseConsts.BOT_STATUS_PUBLISH : BaseConsts.BOT_STATUS_UNPUBLISH);
      bot.setTenantId(scene.getTenantId());
      ResultVO<BotDTO> result = saveBot(bot);
      if (!result.isSuccess()) {
        return ResultVO.fail("发起申请，生成新的智能体应用出现异常:" + result.getResultMsg());
      }
      id = result.getResultObject().getBotId();
      botIds.add(id);
    }
    else {
      id = botIds.get(0);
    }
    for (Long botId : botIds) {
      botRelaManageService.addBotSceneRel(scene.getTenantId(), botId, Collections.singletonList(apply.getSceneId()));
    }
    return ResultVO.success(id);
  }

  @Transactional
  @Override
  public ResultVO<Void> saveAgentStrategy(BotDTO bot) {
    bot.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    if (bot.getAgentStrategyJson() != null) {
      bot.setAgentStrategy(JsonUtil.toJsonString(bot.getAgentStrategyJson()));
    }
    botManageMapper.updateAgentStrategy(bot);
    // 自定义规划模式记录了智能应用与规划智能体的实体关联关系，修改策略时需要重新计算关联
    ResourceElementFactory.get(OperClassEnum.BOT.name()).submit(bot.getTenantId(), bot.getBotId());
    return ResultVO.success();
  }

  @Override
  public SimpleAgentStrategyDTO getAgentStrategy(Long tenantId, Long botId) {
    BotDTO bot = botManageMapper.getBot(tenantId, botId);
    if (StringUtils.isNotEmpty(bot.getAgentStrategy())) {
      SimpleAgentStrategyDTO strategy = JsonUtil.parseJson(bot.getAgentStrategy(), SimpleAgentStrategyDTO.class);
      if (strategy == null) {
        return null;
      }
      // 纠正智能体参数
      if (strategy.getPlanAgent() != null) {
        setPlanAgent(tenantId, strategy);
      }
      if (CollectionUtils.isNotEmpty(strategy.getAgents())) {
        BotQueryParams params = new BotQueryParams();
        params.setTenantId(tenantId);
        params.setBotId(botId);
        List<BotSceneRelDTO> scenes = botRelaManageService.queryBotSceneRelList(params);
        List<SimpleBotSceneDTO> agents = new ArrayList<>();
        for (SimpleBotSceneDTO scene : strategy.getAgents()) {
          BotSceneRelDTO dto = IterableUtils.find(CollectionUtils.emptyIfNull(scenes), p -> Objects.equals(scene.getSceneId(), p.getSceneId()));
          if (dto != null) {
            scene.setSceneName(dto.getSceneName());
            agents.add(scene);
          }
        }
        strategy.setAgents(agents);
      }
      return strategy;
    }
    return null;
  }

  @Override
  @Transactional
  public void modifyBotAuthStatus(Long botId, String authStatus) {
    botAuthManageMapper.updateAuthStatus(botId, authStatus, SessionUtil.getLoginInfo().getUserId());
  }

  @Override
  @Transactional
  public void deleteBotAuth(Long botId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 更新授权操作信息
    botAuthManageMapper.updateAuthOperateInfo(botId, userId);
    // 取消授权
    botAuthManageMapper.deleteAuthByBotId(botId, userId);
  }

  @Override
  @Transactional
  public void modifyBotAuthInfo(BotAuthDTO authDTO) {
    // 更新授权应用基本信息
    Long userId = SessionUtil.getLoginInfo().getUserId();
    authDTO.setUpdatorId(userId);
    authDTO.setAuthOperator(userId);
    botAuthManageMapper.updateBotAuthBasicInfo(authDTO);
  }

  @Override
  public BotDTO findBoteClaw(Long spaceId, Long botId) {
    BotDTO bot = botManageMapper.getBot(spaceId, botId);
    if (bot != null) {
      // 获取提示词文件列表
      AiQueryParams queryParams = new AiQueryParams();
      queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
      queryParams.setSpaceId(spaceId);
      queryParams.setBotId(botId);
      bot.setWorkspaces(workspaceManageService.queryAiWorkspaceList(queryParams));
      // 获取关联的模型
      List<AiModelDTO> aiModelList = aiModelManageMapper.selectAiModelList(queryParams);
      if (CollectionUtils.isNotEmpty(aiModelList)) {
        bot.setAiModel(aiModelList.getFirst());
      }
      // 获取关联的技能
      bot.setSkills(aiSkillManageService.queryListByBotId(spaceId, botId));
    }
    return bot;
  }

  @Override
  @Transactional
  public ResultVO<BotDTO> saveBoteClaw(BotDTO bot) {
    Assert.notNull(bot.getSpaceId(), "空间 ID 不能为空");
    // 运行态中，空间 ID 作为租户 ID
    if (bot.getTenantId() == null) {
      bot.setTenantId(bot.getSpaceId());
    }
    // AI门户创建的智能应用，设置默认上架状态
    if (bot.getBotStatus() == null) {
      bot.setBotStatus(BaseConsts.BOT_STATUS_PUBLISH);
    }
    // 保存应用
    bot.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    ResultVO<BotDTO> saveResult = saveBot(bot);
    if (!saveResult.isSuccess() && !BaseErrorConstant.NO_DIFFERENCE.getErrorConstant().getCode().equals(saveResult.getResultCode())) {
      return saveResult;
    }
    // 保存提示词文件列表
    saveWorkspaces(bot);
    // 保存关联的模型
    saveAiModel(bot);
    // 保存启用的技能
    saveBoteSkill(bot);
    return ResultVO.success();
  }

  /**
   * 保存关联的技能
   */
  private void saveBoteSkill(BotDTO bot) {
    // 查询已关联的技能
    List<AiSkillDTO> oldSkillList = aiSkillManageService.queryListByBotId(bot.getSpaceId(), bot.getBotId());
    // 提取技能 ID 列表
    List<Long> oldSkillIdList = oldSkillList.stream().map(AiSkillDTO::getId).toList();

    // 处理新增技能
    if (CollectionUtils.isNotEmpty(bot.getSkills())) {
      for (AiSkillDTO skill : bot.getSkills()) {
        if (oldSkillIdList.contains(skill.getId())) {
          continue;
        }
        skill.setBotId(bot.getBotId());
        skill.setSpaceId(bot.getSpaceId());
        aiSkillManageService.saveAiSkill(skill);
      }
    }

    // 处理移除技能（在旧列表中但不在新列表中的技能）
    if (CollectionUtils.isNotEmpty(oldSkillList)) {
      Set<Long> newSkillIds = CollectionUtils.emptyIfNull(bot.getSkills()).stream()
        .map(AiSkillDTO::getId)
        .collect(Collectors.toSet());
      AiSkillDTO aiSkillDTO = new AiSkillDTO();
      aiSkillDTO.setBotId(bot.getBotId());
      aiSkillDTO.setSpaceId(bot.getSpaceId());
      for (AiSkillDTO oldSkill : oldSkillList) {
        if (!newSkillIds.contains(oldSkill.getId())) {
          aiSkillDTO.setSkillId(oldSkill.getSkillId());
          // 需要先禁用
          aiSkillManageService.disabledBtAiSkill(aiSkillDTO);
          // 然后再移除
          aiSkillManageService.deleteAgentSkill(bot.getSpaceId(), oldSkill.getSkillId());
        }
      }
    }
  }

  /**
   * 保存提示词文件列表
   */
  private void saveWorkspaces(BotDTO bot) {
    if (CollectionUtils.isNotEmpty(bot.getWorkspaces())) {
      Long userId = SessionUtil.getLoginInfo().getUserId();
      for (AiWorkspaceDTO workspace : bot.getWorkspaces()) {
        AiWorkspaceDTO old = workspace.getId() == null ? null : workspaceManageMapper.getAiWorkspace(workspace.getId());
        if (old != null) {
          old.setFileContent(workspace.getFileContent());
          old.setUpdatorId(userId);
          workspaceManageMapper.updateAiWorkspace(old);
          continue;
        }
        workspace.setId(Sequences.AI_WORKSPACE_ID.next());
        workspace.setSpaceId(bot.getSpaceId());
        workspace.setBotId(bot.getBotId());
        workspace.setStatusCd(CommonConsts.STATUS_CD_VALID);
        workspace.setCreatorId(userId);
        workspaceManageMapper.insertAiWorkspace(workspace);
      }
    }
  }

  /**
   * 保存关联的模型
   */
  public void saveAiModel(BotDTO bot) {
    AiModelDTO model = bot.getAiModel();
    if (model == null) {
      return;
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    AiModelDTO old = aiModelManageMapper.getAiModel(bot.getSpaceId(), bot.getBotId(), userId, model.getModelType());
    if (old != null) {
      old.setModelId(model.getModelId());
      old.setUpdatorId(userId);
      aiModelManageMapper.updateAiModel(old);
      return;
    }
    model.setId(Sequences.AI_MODEL_ID.next());
    model.setSpaceId(bot.getSpaceId());
    model.setBotId(bot.getBotId());
    model.setCreatorId(userId);
    model.setStatusCd(CommonConsts.STATUS_CD_VALID);
    aiModelManageMapper.insertAiModel(model);
  }

  private void setPlanAgent(Long tenantId, SimpleAgentStrategyDTO strategy) {
    boolean illegal = true;
    // 判断是否带有规划标签
    List<LabelObjectRelDTO> labels = labelManageService.queryLabelObjectRelList(Collections.singletonList(strategy.getPlanAgent().getSceneId()),
      BaseConsts.LABEL_TYPE_SCENE, tenantId);
    if (IterableUtils.matchesAny(labels, p -> SceneConsts.SCENE_LABEL_PLAN_AGENT.equals(p.getLabelName()))) {
      BotSceneDTO scene = sceneManageMapper.getScene(tenantId, strategy.getPlanAgent().getSceneId());
      if (scene != null) {
        strategy.getPlanAgent().setSceneName(scene.getSceneName());
        illegal = false;
      }
    }
    if (illegal) {
      strategy.setPlanAgent(null);
    }
  }
}
