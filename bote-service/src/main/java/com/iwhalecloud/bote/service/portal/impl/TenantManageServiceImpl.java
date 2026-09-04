package com.iwhalecloud.bote.service.portal.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.AccountEventTypeEnum;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleRecommendBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleRecommendBotDTO.BotInfo;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.portal.SimpleUserDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.bot.BotAuthManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.service.base.IAccountEventLogService;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 租户配置服务
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Service
@RequiredArgsConstructor
public class TenantManageServiceImpl implements ITenantManageService {

  /** 对外开放空间配置编码 */
  private static final String EXTERNAL_SPACE_CONFIG = "EXTERNAL_SPACE_CONFIG";
  /** 默认空间ID后缀 */
  private static final String DEFAULT_SPACE_ID_SUFFIX = "_default_space_id";

  // @formatter:off
  private final TenantManageMapper tenantManageMapper;
  private final ICatalogManageService catalogManageService;
  private final UserManageMapper userManageMapper;
  private final BotQueryMapper botQueryMapper;
  private final BotAuthManageMapper botAuthManageMapper;
  private final IAccountEventLogService accountEventLogService;
  private final WorkspaceManageMapper workspaceManageMapper;
  private final OrgUserRoleMapper orgUserRoleMapper;
  private final PasswordEncoder passwordEncoder;
  private final AttrSpecCache attrSpecCache;
  // @formatter:on

  @Override
  public String getTenantIcon(Long tenantId) {
    return tenantManageMapper.getTenantIcon(tenantId);
  }

  @Override
  @Nullable
  public TenantDTO getTenant(Long tenantId) {
    TenantDTO tenant = tenantManageMapper.getTenant(tenantId);
    if (tenant == null) {
      return null;
    }
    // 设置联网搜索开关
    tenant.setWebSearchEnabled(BooleanUtils.isTrue(SystemParameter.WEB_SEARCH_ENABLED.getBooleanValueFromDb()));
    // 设置对话窗口语音开关
    tenant.setChatVoiceEnabled(BooleanUtils.isTrue(SystemParameter.CHAT_VOICE_ENABLED.getBooleanValueFromDb()));
    // 补充推荐 BOT 数据
    if (StringUtils.isNotEmpty(tenant.getBotSettingInfo())) {
      SimpleRecommendBotDTO info = JsonUtil.parseJsonRequired(tenant.getBotSettingInfo(), SimpleRecommendBotDTO.class);
      Map<String, Object> settingInfo = new HashMap<>(2);
      settingInfo.put("display", BooleanUtils.isTrue(info.getDisplay()));
      settingInfo.put("list", queryRecommendedBots(tenantId, info.getList()));
      tenant.setBotSettingInfo(JsonUtil.toJsonString(settingInfo));
    }
    return tenant;
  }

  /**
   * 查询租户推荐的智能应用列表
   */
  private List<Map<String, Object>> queryRecommendedBots(Long tenantId, List<BotInfo> botInfoList) {
    if (CollectionUtils.isEmpty(botInfoList)) {
      return Collections.emptyList();
    }
    List<Long> botIds = botInfoList.stream().map(SimpleRecommendBotDTO.BotInfo::getBotId).map(Long::parseLong).distinct().collect(Collectors.toList());
    List<SimpleBotDTO> bots = botQueryMapper.selectSimpleBots(tenantId, botIds, BaseConsts.BOT_STATUS_PUBLISH);
    List<Map<String, Object>> recommendedBots = new ArrayList<>(bots.size());
    for (SimpleRecommendBotDTO.BotInfo dto : botInfoList) {
      Long botId = Long.parseLong(dto.getBotId());
      SimpleBotDTO bot = IterableUtils.find(bots, b -> b.getBotId().equals(botId));
      if (bot != null) {
        // 不用 BotDTO 对象输出，避免数据冗余、精度丢失问题
        Map<String, Object> map = new HashMap<>(8);
        map.put("botId", String.valueOf(bot.getBotId()));
        map.put("botName", bot.getBotName());
        map.put("botUse", bot.getBotUse());
        map.put("className", dto.getClassName());
        recommendedBots.add(map);
      }
    }
    return recommendedBots;
  }

  @Override
  public PageInfo<TenantDTO> queryTenantPage(TenantQueryParams queryParams) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    queryParams.setUserId(userId);
    //noinspection resource
    PageInfo<TenantDTO> pageInfo = tenantManageMapper.selectTenantPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
    if (SessionUtil.isSuperAdmin(userId)) {
      CollectionUtils.emptyIfNull(pageInfo.getList()).forEach(p -> p.setUserRole(BaseConsts.ROLE_MANAGE));
    }
    return pageInfo;
  }

  @Override
  public List<TenantDTO> queryTenantList(TenantQueryParams queryParams) {
    if (SessionUtil.isSuperAdmin(SessionUtil.getLoginInfo().getUserId()) || BaseConsts.TRUE.equals(queryParams.getIsBotSquare())) {
      // 超级管理员查询所有租户
      queryParams.setUserId(null);
    }
    else {
      queryParams.setUserId(SessionUtil.getLoginInfo().getUserId());
    }
    List<TenantDTO> tenantList = tenantManageMapper.selectTenantList(queryParams);
    if (BaseConsts.TRUE.equals(queryParams.getIsBotSquare())) {
      CollectionUtils.emptyIfNull(tenantList).removeIf(tenant -> tenant.getTenantId().equals(queryParams.getTenantId()));
    }
    return tenantList;
  }

  @Override
  @Transactional
  public ResultVO<TenantDTO> saveTenant(TenantDTO tenant) {
    // 校验编码唯一性
    if (tenantManageMapper.existsTenantCode(tenant)) {
      return BaseErrorConstant.CHECK_CODE.toResult(tenant.getTenantCode());
    }
    tenant.setStatusCd(BaseConsts.STATUS_CD_VALID);
    TenantDTO old = tenant.getTenantId() == null ? null : tenantManageMapper.getTenant(tenant.getTenantId());
    if (old != null) {
      tenant.setAppId(old.getAppId());
      tenant.setSystemType(old.getSystemType());
    }
    DataDifference<TenantDTO> difference = DataDifferenceStarter.computeSave(old, tenant, false, null);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 新增租户时，添加当前用户为租户成员
    if (old == null) {
      TenantDTO savedTenant = difference.getToSaveData();
      TenantUserDTO tenantUser = new TenantUserDTO();
      tenantUser.setTenantId(savedTenant.getTenantId());
      tenantUser.setUserId(SessionUtil.getLoginInfo().getUserId());
      tenantUser.setUserRole(BaseConsts.ROLE_MANAGE);
      saveTenantUser(Collections.singletonList(tenantUser));

      // 设置租户级的预置分组
      for (String type : CatalogConsts.CATALOG_TYPES) {
        CatalogDTO catalog = new CatalogDTO();
        catalog.setTenantId(savedTenant.getTenantId());
        catalog.setCatalogType(type);
        catalog.setCatalogName("预置分组");
        catalogManageService.saveCatalog(catalog);
      }
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<TenantDTO> saveTenantDetail(TenantDTO tenant) {
    TenantDTO old = tenant.getTenantId() == null ? null : tenantManageMapper.getTenant(tenant.getTenantId());
    Assert.notNull(old, "查询不到有效的租户信息");
    tenant.setTenantCode(old.getTenantCode());
    tenant.setTenantName(old.getTenantName());
    tenant.setSpaceId(old.getSpaceId());
    tenant.setStatusCd(BaseConsts.STATUS_CD_VALID);
    // 推荐 BOT 设置，移除冗余节点信息
    if (StringUtils.isNotEmpty(tenant.getBotSettingInfo())) {
      tenant.setBotSettingInfo(JsonUtil.toJsonString(JsonUtil.parseJsonRequired(tenant.getBotSettingInfo(), SimpleRecommendBotDTO.class)));
    }
    DataDifference<TenantDTO> difference = DataDifferenceStarter.computeSave(old, tenant, false, null);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deteleTenant(Long tenantId) {
    // 补充校验：存在有效机器人，不允许删除
    tenantManageMapper.deleteTenant(tenantId, SessionUtil.getLoginInfo().getUserId());
    userManageMapper.deleteDefaultTenantId(tenantId);
    return ResultVO.success();
  }

  @Override
  public PageInfo<TenantUserDTO> queryTenantUserPage(TenantQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return tenantManageMapper.selectTenantUserPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<List<TenantUserDTO>> saveTenantUser(List<TenantUserDTO> tenantUserList) {
    if (CollectionUtils.isEmpty(tenantUserList)) {
      return ResultVO.success(tenantUserList);
    }
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    if (!checkTenantUserPermission(tenantUserList, currentUserId)) {
      return ResultVO.fail("无权操作该租户的成员信息");
    }

    for (TenantUserDTO tenantUser : tenantUserList) {
      if (tenantUser.getTenantUserId() == null) {
        if (tenantManageMapper.existsTenantUser(tenantUser.getTenantId(), tenantUser.getUserId())) {
          return ResultVO.fail("用户已存在");
        }
        // 如果当前用户不存在其他租户，默认当前租户为默认租户
        if (!tenantManageMapper.existsTenantByUserId(tenantUser.getUserId())) {
          userManageMapper.updateTenantIdByUserId(tenantUser.getUserId(), tenantUser.getTenantId(), SessionUtil.getLoginInfo().getUserId());
        }
        tenantUser.setTenantUserId(Sequences.TENANT_USER_ID.next());
        tenantUser.setStatusCd(BaseConsts.STATUS_CD_VALID);
        tenantUser.setCreatorId(SessionUtil.getLoginInfo().getUserId());
        tenantUser.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
        tenantManageMapper.insertTenantUser(tenantUser);
        // 记录用户权限变更日志
        saveAccountEventLog(AccountEventTypeEnum.ADD_PERMISSION, tenantUser, true);
      }
      else {
        // 记录用户权限变更日志
        saveAccountEventLog(AccountEventTypeEnum.MOD_PERMISSION, tenantUser, false);

        tenantUser.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
        tenantManageMapper.updateTenantUser(tenantUser);
      }
      // 删除该用户被单独授权
      deleteUserBotAuth(tenantUser.getUserId(), tenantUser.getTenantId());
    }
    return ResultVO.success(tenantUserList);
  }

  @Override
  @Transactional
  public ResultVO<TenantUserDTO> addTenantUser(Long tenantId, Long userId, String userRole) {
    if (tenantManageMapper.existsTenantUser(tenantId, userId)) {
      return ResultVO.fail("用户已存在");
    }
    TenantUserDTO tenantUser = new TenantUserDTO();
    tenantUser.setTenantUserId(Sequences.TENANT_USER_ID.next());
    tenantUser.setTenantId(tenantId);
    tenantUser.setUserId(userId);
    tenantUser.setUserRole(userRole);
    tenantUser.setStatusCd(BaseConsts.STATUS_CD_VALID);
    tenantUser.setCreatorId(userId);
    tenantManageMapper.insertTenantUser(tenantUser);
    // 记录用户权限变更日志
    saveAccountEventLog(AccountEventTypeEnum.ADD_PERMISSION, tenantUser, true);
    return ResultVO.success(tenantUser);
  }

  private void deleteUserBotAuth(Long userId, Long tenantId) {
    // 查询该用户的所有机器人授权
    List<BotAuthDTO> userAuths = botAuthManageMapper.selectByUserId(userId);
    // 查询该租户下的所有机器人授权
    List<BotAuthDTO> tenantAuths = botAuthManageMapper.selectByTenantId(tenantId);
    for (BotAuthDTO userAuth : userAuths) {
      boolean coveredByTenant = tenantAuths.stream().anyMatch(tenantAuth -> tenantAuth.getBotId().equals(userAuth.getBotId()));
      if (coveredByTenant) {
        // 删除该用户的独立授权
        botAuthManageMapper.deleteAuthByUserIdAndBotId(userId, userAuth.getBotId(), SessionUtil.getLoginInfo().getUserId());
      }
    }
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteTenantUser(Long tenantId, List<Long> userIds) {
    // 权限检查：只有超级管理员、企业管理員或租户管理员可以删除租户成员
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();

    // 如果不是超级管理员，且没有权限（checkTenantPermission返回true表示无权限），则拒绝操作
    if (!SessionUtil.isSuperAdmin(currentUserId) && checkTenantPermission(tenantId, currentUserId)) {
      return ResultVO.fail("无权操作该租户的成员信息");
    }

    // 记录用户权限变更日志
    batchSaveDelAccountEventLog(tenantId, userIds);

    tenantManageMapper.deleteTenantUser(tenantId, userIds, SessionUtil.getLoginInfo().getUserId());
    List<SimpleUserDTO> userList = userManageMapper.selectUserListByUserIds(tenantId, userIds);
    if (CollectionUtils.isNotEmpty(userList)) {
      userManageMapper.deleteUserDefaultTenantId(userList.stream().map(SimpleUserDTO::getUserId).collect(Collectors.toList()),
        SessionUtil.getLoginInfo().getUserId());
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<List<TenantUserDTO>> saveTenantUserForBeyond(List<TenantUserDTO> tenantUserList) {
    if (CollectionUtils.isEmpty(tenantUserList)) {
      return ResultVO.success(tenantUserList);
    }
    String systemCode = SessionUtil.getLoginInfo().getSystemCode();
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    for (TenantUserDTO tenantUser : tenantUserList) {
      if (StringUtils.isNotEmpty(tenantUser.getUserCode())) {
        UserEntity user = userManageMapper.getOneUserByCode(systemCode, tenantUser.getUserCode());
        if (user == null) {
          // 用户不存在时，创建新用户
          user = new UserEntity();
          user.setUserId(Sequences.USER_ID.next());
          user.setUserName(tenantUser.getUserCode());
          user.setSystemCode(systemCode);
          user.setStatusCd(BaseConsts.STATUS_CD_VALID);
          user.setCreatorId(currentUserId);
          user.setUpdatorId(currentUserId);
          if (StringUtils.isNotEmpty(tenantUser.getBeyondUserId())) {
            user.setExtUserId(tenantUser.getBeyondUserId());
          }
          user.setPassword(passwordEncoder.encode(SystemParameter.USER_INIT_PASSWORD.getValueFromDb()));
          if (userManageMapper.insertUser(user) == 0) {
            user = userManageMapper.getOneUserByCode(systemCode, tenantUser.getUserCode());
            if (user == null && StringUtils.isNotEmpty(tenantUser.getBeyondUserId())) {
              user = userManageMapper.getUserBySystemCodeAndExtUserId(systemCode, tenantUser.getBeyondUserId());
            }
          }
        }
        if (user == null) {
          return ResultVO.fail("创建或解析用户失败: " + tenantUser.getUserCode());
        }
        tenantUser.setUserId(user.getUserId());
      }
      if (tenantUser.getTenantId() != null && tenantUser.getUserId() != null) {
        TenantUserDTO existingTenantUser = tenantManageMapper.getTenantUserByTenantIdAndUserId(
          tenantUser.getTenantId(),
          tenantUser.getUserId()
        );
        if (existingTenantUser != null) {
          tenantUser.setTenantUserId(existingTenantUser.getTenantUserId());
        }
      }
    }
    return saveTenantUser(tenantUserList);
  }

  @Override
  @Transactional
  public ResultVO<Void> removeTenantUserForBeyond(Long tenantId, List<String> userCodes) {
    if (CollectionUtils.isEmpty(userCodes)) {
      return ResultVO.fail("用户编码列表不能为空");
    }
    List<Long> userIds = new ArrayList<>();
    for (String userCode : userCodes) {
      if (StringUtils.isEmpty(userCode)) {
        continue;
      }
      String systemCode = SessionUtil.getLoginInfo().getSystemCode();
      UserEntity user = userManageMapper.getUserByCode(systemCode, userCode);
      if (user == null) {
        continue;
      }
      if (tenantManageMapper.existsTenantUser(tenantId, user.getUserId())) {
        userIds.add(user.getUserId());
      }
    }
    if (CollectionUtils.isEmpty(userIds)) {
      return ResultVO.success();
    }
    userIds = userIds.stream().distinct().collect(Collectors.toList());
    return deleteTenantUser(tenantId, userIds);
  }

  @Override
  public List<SimpleWorkspaceDTO> qryAuthorizedWorkspaces(Long userId) {
    List<SimpleWorkspaceDTO> workspaces = new ArrayList<>();
    List<SimpleTenantDTO> tenants;
    boolean superAdmin = SessionUtil.isSuperAdmin(userId);
    if (superAdmin) {
      workspaces = workspaceManageMapper.selectSimpleWorkspaces();
      tenants = tenantManageMapper.selectSimpleTenants(null);
    }
    else {
      // 部分访客用户，允许跨空间加入项目
      Map<Long, List<SimpleWorkspaceDTO>> group = CollectionUtils.emptyIfNull(workspaceManageMapper.selectSimpleWorkspaceByUserId(userId))
        .stream().collect(Collectors.groupingBy(SimpleWorkspaceDTO::getSpaceId));
      if (!MapUtils.isEmpty(group)) {
        for (Entry<Long, List<SimpleWorkspaceDTO>> entry : group.entrySet()) {
          SimpleWorkspaceDTO dto = IterableUtils.find(entry.getValue(), p -> StringUtils.isNotEmpty(p.getRoleCode()));
          if (dto != null) {
            workspaces.add(dto);
          }
          else {
            workspaces.add(entry.getValue().getFirst());
          }
        }
      }
      tenants = tenantManageMapper.selectSimpleTenantsByUserId(null, userId);
    }
    if (CollectionUtils.isEmpty(workspaces)) {
      return Collections.emptyList();
    }
    // 兼容空间 ID 为空的存量项目
    Map<Long, List<SimpleTenantDTO>> groupTenants = CollectionUtils.emptyIfNull(tenants).stream()
      .collect(Collectors.groupingBy(tenant -> tenant.getSpaceId() != null ? tenant.getSpaceId() : 0L));

    for (SimpleWorkspaceDTO space : workspaces) {
      space.setTenants(groupTenants.get(space.getSpaceId()));
      String roleCode = "";
      if (superAdmin) {
        roleCode = PrivConsts.ROLE_SUPER_ADMIN;
      }
      else if ("admin".equals(space.getRoleCode())) {
        roleCode = PrivConsts.ROLE_SPACE_ADMIN;
      }
      space.setRoleCode(roleCode);
    }

    // 置顶默认的企业项目
    topDefaultTenant(workspaces);
    return workspaces;
  }

  @Override
  public String getUserRole(Long spaceId, @Nullable Long tenantId, Long userId) {
    String roleCode = PrivConsts.ROLE_USE;
    if (SessionUtil.isSuperAdmin(userId)) {
      roleCode = PrivConsts.ROLE_SUPER_ADMIN;
    }
    else {
      if (orgUserRoleMapper.checkIsOrgAdmin(spaceId, userId)) {
        roleCode = PrivConsts.ROLE_SPACE_ADMIN;
      }
      if (!PrivConsts.ROLE_SPACE_ADMIN.equals(roleCode)) {
        List<String> roleCodes = tenantManageMapper.queryUserRole(spaceId, tenantId, userId);
        if (CollectionUtils.isNotEmpty(roleCodes)) {
          if (roleCodes.contains(PrivConsts.ROLE_MANAGE)) {
            roleCode = PrivConsts.ROLE_MANAGE;
          }
          else if (roleCodes.contains(PrivConsts.ROLE_EDIT)) {
            roleCode = PrivConsts.ROLE_EDIT;
          }
          else if (roleCodes.contains(PrivConsts.ROLE_READONLY)) {
            roleCode = PrivConsts.ROLE_READONLY;
          }
          else {
            // 兼容自定义角色场景
            roleCode = roleCodes.getFirst();
          }
        }
      }
    }
    return roleCode;
  }

  @Override
  public List<SimpleTenantDTO> querySpaceTenantList(Long spaceId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return tenantManageMapper.queryTenantList(spaceId);
  }

  @Override
  @Nullable
  public Long getFirstTenantId(Long spaceId, Long userId) {
    List<Long> tenantIds;
    boolean superAdmin = SessionUtil.isSuperAdmin(userId);
    if (superAdmin) {
      tenantIds = CollectionUtils.emptyIfNull(tenantManageMapper.selectSimpleTenants(spaceId)).stream().map(SimpleTenantDTO::getTenantId).toList();
    }
    else {
      tenantIds = CollectionUtils.emptyIfNull(tenantManageMapper.selectSimpleTenantsByUserId(spaceId, userId)).stream()
        .map(SimpleTenantDTO::getTenantId).toList();
    }
    if (CollectionUtils.isEmpty(tenantIds)) {
      return null;
    }
    // 优先使用用户配置的默认租户
    Long defaultTenantId = userManageMapper.getUser(userId).getDefaultTenantId();
    if (defaultTenantId != null && tenantIds.contains(defaultTenantId)) {
      return defaultTenantId;
    }
    return tenantIds.getFirst();
  }

  /**
   * 校验单个租户的操作权限
   * 检查当前用户是否是租户创建者、超级管理员、企业管理員或租户管理员
   *
   * @param tenantId 租户ID
   * @param currentUserId 当前用户ID
   * @return 如果没有权限返回true，否则返回false（注意：此方法返回true表示无权限）
   */
  private boolean checkTenantPermission(Long tenantId, Long currentUserId) {
    // 如果是租户创建者，有权限
    TenantDTO tenant = tenantManageMapper.getTenant(tenantId);
    if (tenant != null && tenant.getCreatorId() != null && tenant.getCreatorId().equals(currentUserId)) {
      return false;
    }
    Long spaceId = tenantManageMapper.getSpaceIdByTenantId(tenantId);
    // 如果是企业管理员，有权限
    if (spaceId != null && orgUserRoleMapper.checkIsOrgAdmin(spaceId, currentUserId)) {
      return false;
    }
    // 检查是否有 MANAGE 权限
    String userRole = tenantManageMapper.getUserRole(tenantId, currentUserId);
    // 如果有MANAGE权限，返回false（有权限）；否则返回true（无权限）
    return !BaseConsts.ROLE_MANAGE.equals(userRole);
  }

  /**
   * 校验租户用户操作权限
   * 检查当前用户是否是超级管理员、企业管理員或租户管理员
   *
   * @param tenantUserList 租户用户列表
   * @param currentUserId 当前用户ID
   * @return 如果有权限返回true，否则返回false
   */
  private boolean checkTenantUserPermission(List<TenantUserDTO> tenantUserList, Long currentUserId) {
    // 超级管理员直接通过
    if (SessionUtil.isSuperAdmin(currentUserId)) {
      return true;
    }

    List<Long> tenantIds = tenantUserList.stream()
      .map(TenantUserDTO::getTenantId)
      .filter(Objects::nonNull)
      .distinct()
      .toList();

    // 检查所有租户的权限：必须对所有租户都有权限才能操作
    for (Long tenantId : tenantIds) {
      if (checkTenantPermission(tenantId, currentUserId)) {
        return false;
      }
    }
    return true; // 所有租户都有权限
  }

  /**
   * 批量保存用户权限删除的账号事件日志
   */
  private void batchSaveDelAccountEventLog(Long tenantId, List<Long> userIds) {
    for (Long userId : userIds) {
      TenantUserDTO tenantUser = tenantManageMapper.getTenantUserByTenantIdAndUserId(tenantId, userId);
      tenantUser.setUserRole("");
      saveAccountEventLog(AccountEventTypeEnum.DEL_PERMISSION, tenantUser, false);
    }
  }

  /**
   * 保存用户权限账号事件日志
   */
  private void saveAccountEventLog(AccountEventTypeEnum eventType, TenantUserDTO tenantUser, boolean isAdd) {
    String oldUserRole = isAdd ? "" : tenantManageMapper.getUserRole(tenantUser.getTenantId(), tenantUser.getUserId());
    Map<String, Object> map = new HashMap<>();
    map.put("userId", tenantUser.getUserId());
    map.put("userName", tenantUser.getUserName());
    map.put("tenantUserId", tenantUser.getTenantId());
    map.put("oldUserRole", oldUserRole);
    map.put("newUserRole", tenantUser.getUserRole());
    accountEventLogService.addAccountEventLog(eventType, JsonUtil.toJsonString(map));
  }

  private void topDefaultTenant(List<SimpleWorkspaceDTO> workspaces) {
    Long defaultTenantId = SessionUtil.getLoginInfo().getDefaultTenantId();
    if (defaultTenantId != null) {
      // 找到包含默认租户的工作空间
      SimpleWorkspaceDTO defaultWorkspace = null;
      for (SimpleWorkspaceDTO workspace : workspaces) {
        if (CollectionUtils.emptyIfNull(workspace.getTenants()).stream().anyMatch(tenant -> tenant.getTenantId().equals(defaultTenantId))) {
          defaultWorkspace = workspace;
          break;
        }
      }
      if (defaultWorkspace != null) {
        // 将默认工作空间移到首位
        workspaces.remove(defaultWorkspace);
        workspaces.addFirst(defaultWorkspace);
        // 在工作空间内，将默认租户移到首位
        List<SimpleTenantDTO> tenants = defaultWorkspace.getTenants();
        SimpleTenantDTO defaultTenant = CollectionUtils.emptyIfNull(tenants).stream().filter(tenant -> tenant.getTenantId().equals(defaultTenantId)).findFirst().orElse(null);
        if (defaultTenant != null) {
          tenants.remove(defaultTenant);
          tenants.addFirst(defaultTenant);
        }
      }
    }
  }

  @Override
  @Transactional
  public ResultVO<TenantDTO> saveTenantForExternal(TenantDTO tenant) {
    Assert.notNull(tenant, "租户信息不能为空");
    Assert.hasText(tenant.getSpaceCode(), "空间编码不能为空");
    String spaceCode = tenant.getSpaceCode();
    List<SimpleAttrDTO> attrList = attrSpecCache.get(CommonConsts.PLATFORM_TENANT_ID, EXTERNAL_SPACE_CONFIG);
    if (CollectionUtils.isEmpty(attrList)) {
      return ResultVO.fail("静态数据配置 " + EXTERNAL_SPACE_CONFIG + " 不存在");
    }
    String attrValueName = spaceCode.toLowerCase() + DEFAULT_SPACE_ID_SUFFIX;
    SimpleAttrDTO matchedAttr = attrList.stream()
        .filter(attr -> attrValueName.equals(attr.getAttrValueName()))
        .findFirst()
        .orElse(null);
    if (matchedAttr == null || StringUtils.isEmpty(matchedAttr.getAttrValue())) {
      return ResultVO.fail("未找到空间编码对应的配置，spaceCode=" + spaceCode);
    }
    long spaceId;
    try {
      spaceId = Long.parseLong(matchedAttr.getAttrValue());
    }
    catch (NumberFormatException e) {
      return ResultVO.fail("空间ID格式错误，spaceCode=" + spaceCode + ", spaceId=" + matchedAttr.getAttrValue());
    }
    tenant.setSpaceId(spaceId);
    return saveTenant(tenant);
  }
}
