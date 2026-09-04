package com.iwhalecloud.bote.portal;

import com.iwhalecloud.bote.cache.PortalAdapterCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.common.lock.LockHelper;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.LcdpApiUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpGatewayDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpGatewayDTO.EnvironmentInst;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServicePlatformManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 多门户适配器
 *
 * @author bianjp
 * @since 2024-10-28
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class MultiPortalAdapter {
  private final Logger logger = LoggerFactory.getLogger(MultiPortalAdapter.class);
  private final UserManageMapper userManageMapper;
  private final TenantManageMapper tenantManageMapper;
  private final CatalogManageMapper catalogManageMapper;
  private final PortalAdapterCache portalAdapterCache;
  private final ServicePlatformManageMapper platformManageMapper;
  private final ServiceGatewayManageMapper gatewayManageMapper;
  private final WorkspaceManageMapper workspaceManageMapper;
  private final IOrganizationManageService organizationManageService;
  private final DistributedLockFactory distributedLockFactory;


  /**
   * 获取鉴权提供者实现
   *
   * @param systemCode 系统编码
   * @return 鉴权提供者实现
   */
  public IAuthProvider getAuthProvider(String systemCode) {
    IAuthProvider authProvider = portalAdapterCache.getAuthProvider(systemCode);
    Assert.notNull(authProvider, "未知系统: " + systemCode);
    return authProvider;
  }

  /**
   * 同步用户
   *
   * @param loginInfo 登录信息
   * @param systemCode 系统编码
   * @param defaultRole 默认用户角色
   */
  @Transactional
  public void syncUser(LoginInfo loginInfo, String systemCode, @Nullable String defaultRole) {
    String extUserId = loginInfo.getUserId() != null ? loginInfo.getUserId().toString() : loginInfo.getExtUserId();
    // 同一门户系统 + 外部用户串行同步，避免并发重复建用户或写冲突
    DistributedLock lock = distributedLockFactory.getBizLock(PortalLockConsts.PORTAL_USER_SYNC_LOCK,
      LockHelper.formatBizLockKey(systemCode, extUserId));
    // 非阻塞抢锁：抢不到锁时降级为无锁执行，避免页面报错
    boolean locked = lock.tryLock();
    if (!locked) {
      logger.warn("用户同步锁未获取到: systemCode={}, extUserId={}", systemCode, extUserId);
      return;
    }
    try {
      UserEntity user = userManageMapper.getUserBySystemCodeAndExtUserId(systemCode, extUserId);
      // 不存在时新增用户
      if (user == null) {
        user = new UserEntity();
        user.setUserId(Sequences.USER_ID.next());
        user.setUserName(loginInfo.getUserName());
        user.setRealName(loginInfo.getRealName());
        user.setPhoneNo(loginInfo.getPhoneNo());
        user.setSystemCode(systemCode);
        user.setDefaultTenantId(loginInfo.getDefaultTenantId());
        user.setExtUserId(extUserId);
        user.setStatusCd(BaseConsts.STATUS_CD_VALID);
        userManageMapper.insertUser(user);
      }
      else {
        // 以用户表信息为准
        if (user.getDefaultTenantId() != null) {
          loginInfo.setDefaultTenantId(user.getDefaultTenantId());
        }
        // 用户信息有变更时更新用户，只更新几个基本信息字段
        if (isUserChanged(loginInfo, user)) {
          user.setUserName(loginInfo.getUserName());
          user.setRealName(loginInfo.getRealName());
          user.setPhoneNo(loginInfo.getPhoneNo());
          userManageMapper.updateUserBasicInfo(user);
        }
      }

      // 同步空间
      Long workSpaceId = syncWorkspace(loginInfo, user.getUserId());
      // 同步租户
      Long tenantId = syncTenant(loginInfo, user, workSpaceId);
      // 同步租户用户
      syncTenantUser(tenantId, user.getUserId(), defaultRole);

      loginInfo.setExtUserId(extUserId);
      loginInfo.setUserId(user.getUserId());
      loginInfo.setDefaultTenantId(tenantId);
    }
    finally {
      if (locked) {
        lock.unlock();
      }
    }
  }

  /**
   * 同步租户
   *
   * @param loginInfo 登录信息
   * @param user 用户信息
   */
  @Nullable
  private Long syncTenant(LoginInfo loginInfo, UserEntity user, @Nullable Long workSpaceId) {
    if (MapUtils.isEmpty(loginInfo.getAttributes()) || !BooleanUtils.isTrue(MapUtils.getBoolean(loginInfo.getAttributes(), "autoCreateTenant"))) {
      // 无开启自动创建租户功能，直接跳出
      return loginInfo.getDefaultTenantId() == null ? BaseConsts.DEFAULT_TENANT_ID : loginInfo.getDefaultTenantId();
    }
    String tenantCode = loginInfo.getTenantCode();
    String tenantName = loginInfo.getTenantName();

    logger.info("======> loginInfo: {}", loginInfo);
    logger.info("======> tenantCode: {} ,tenantName {}", tenantCode, tenantName);
    if (StringUtils.isAnyEmpty(tenantCode, tenantName)) {
      tenantName = user.getUserName() + "的租户";
      tenantCode = "tenant_" + user.getUserName();
      return null;
    }
    // 有 ext_tenant_id 时,加租户级分布式锁(同一 systemCode + ext_tenant_id 互斥),
    // 防止并发建重租户;ext_tenant_id 为空(非 OAuth 自动建租户)时不加锁,走原判重逻辑。
    if (loginInfo.getExtTenantId() != null) {
      DistributedLock tenantLock = distributedLockFactory.getBizLock(
        PortalLockConsts.PORTAL_TENANT_SYNC_LOCK,
        LockHelper.formatBizLockKey(loginInfo.getSystemCode(), String.valueOf(loginInfo.getExtTenantId())));
      // 非阻塞抢锁:抢不到锁时降级为返回默认租户,避免页面报错
      boolean locked = tenantLock.tryLock();
      if (!locked) {
        logger.warn("租户同步锁未获取到: systemCode={}, extTenantId={}", loginInfo.getSystemCode(), loginInfo.getExtTenantId());
        return loginInfo.getDefaultTenantId() == null ? BaseConsts.DEFAULT_TENANT_ID : loginInfo.getDefaultTenantId();
      }
      try {
        return doSyncTenant(loginInfo, user, tenantCode, tenantName, workSpaceId);
      }
      finally {
        tenantLock.unlock();
      }
    }
    return doSyncTenant(loginInfo, user, tenantCode, tenantName, workSpaceId);
  }

  /**
   * 执行租户同步:判重 + 新建/改名/复活。
   *
   * <p>判重优先按 {@code ext_tenant_id (+ space_id)}(含软删 00X,优先 00A),
   * 修复"项目改名 / 软删后再次登录建重租户"的缺陷;{@code ext_tenant_id} 为空时
   * 退回原 {@code (tenant_code, tenant_name)} 判重,保持旧行为。</p>
   */
  private Long doSyncTenant(LoginInfo loginInfo, UserEntity user, String tenantCode, String tenantName, @Nullable Long workSpaceId) {
    logger.info("======> 检查租户是否已存在 <=======");
    // 判重:有 ext_tenant_id 走 ext_id(+space_id, 含软删 00X, 优先 00A);否则走原 (code, name)
    TenantDTO tenant = loginInfo.getExtTenantId() != null
      ? tenantManageMapper.getTenantByExtTenantIdAndSpace(loginInfo.getExtTenantId(), workSpaceId)
      : tenantManageMapper.getTenantByCodeAndName(tenantCode, tenantName);
    if (tenant == null) {
      logger.info("======> 创建新租户 <=======");
      tenant = createNewTenant(loginInfo, user, tenantCode, tenantName, workSpaceId);
      logger.info("======> 创建新租户: {}", tenant);
      logger.info("======> 创建新租户成功 <=======");
      handleGatewaySync(loginInfo, tenant, user.getUserId());
      logger.info("======> 同步租户网关成功 <=======");
      // 设置租户级的预置分组
      initializeCatalogs(tenant);
      // 授予项目管理角色
      logger.info("======> 授予项目管理角色 <=======");
      syncTenantUser(tenant.getTenantId(), user.getUserId(), BaseConsts.ROLE_MANAGE);
      logger.info("======> 授予项目管理角色成功 <=======");
    }
    else if (BaseConsts.STATUS_CD_INVALID.equals(tenant.getStatusCd())) {
      // 命中软删(00X)→ 复活 + 改名 + 同步网关 + 重新授权
      logger.info("======> 复活软删租户: tenantId={}, tenantName={} <=======", tenant.getTenantId(), tenantName);
      tenantManageMapper.reactivateTenant(tenant.getTenantId(), tenantName, user.getUserId());
      tenant.setStatusCd(BaseConsts.STATUS_CD_VALID);
      tenant.setTenantName(tenantName);
      handleGatewaySync(loginInfo, tenant, user.getUserId());
      syncTenantUser(tenant.getTenantId(), user.getUserId(), BaseConsts.ROLE_MANAGE);
    }
    else {
      // 命中有效(00A)→ 改名(若变化)。原逻辑查不到改名后的租户直接新建,是建重租户的根因之一。
      if (!Objects.equals(tenantName, tenant.getTenantName())) {
        logger.info("======> 租户改名: {} -> {} <=======", tenant.getTenantName(), tenantName);
        tenantManageMapper.updateTenantName(tenant.getTenantId(), tenantName, user.getUserId());
        tenant.setTenantName(tenantName);
      }
      // 灵犀应用:systemType 为空时补 systemType + appId + 网关(保留原 else if LCDP 分支)
      if (BaseConsts.SYSTEM_TYPE_LCDP.equals(loginInfo.getSystemType()) && StringUtils.isEmpty(tenant.getSystemType())) {
        updateTenantAndSyncGateway(loginInfo, tenant, user.getUserId());
      }
    }
    return tenant.getTenantId();
  }

  private TenantDTO createNewTenant(LoginInfo loginInfo, UserEntity user, String tenantCode, String tenantName, @Nullable Long workSpaceId) {
    TenantDTO tenant;
    tenant = new TenantDTO();
    tenant.setTenantId(Sequences.TENANT_ID.next());
    tenant.setTenantCode(tenantCode);
    tenant.setTenantName(tenantName);
    tenant.setStatusCd(BaseConsts.STATUS_CD_VALID);
    tenant.setCreatorId(user.getUserId());
    tenant.setUpdatorId(user.getUserId());
    tenant.setExtTenantId(loginInfo.getExtTenantId());
    tenant.setSpaceId(workSpaceId);
    if (BaseConsts.SYSTEM_TYPE_LCDP.equals(loginInfo.getSystemType())) {
      tenant.setAppId(StringUtils.isNumeric(loginInfo.getTenantCode()) ? Long.valueOf(loginInfo.getTenantCode()) : null);
    }
    if (BaseConsts.SYSTEM_TYPE_BEYOND.equals(loginInfo.getSystemType())) {
      tenant.setSpaceId(Long.valueOf(SystemParameter.BEYOND_DEVELOP_SPACE_ID.getValueFromDb()));
    }
    tenant.setSystemType(loginInfo.getSystemType());
    tenantManageMapper.insertTenant(tenant);
    return tenant;
  }

  private void handleGatewaySync(LoginInfo loginInfo, TenantDTO tenant, Long userId) {
    if (BaseConsts.SYSTEM_TYPE_LCDP.equals(loginInfo.getSystemType())) {
      try {
        synGateway(loginInfo, tenant, userId);
      } catch (Exception e) {
        logger.error("同步租户网关失败: message={}", e.getMessage(), e);
      }
    }
  }

  private void initializeCatalogs(TenantDTO tenant) {
    for (String type : CatalogConsts.CATALOG_TYPES) {
      CatalogDTO catalog = new CatalogDTO();
      catalog.setCatalogId(Sequences.CATALOG_ID.next());
      catalog.setParCatalogId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
      catalog.setCatalogName("预置分组");
      catalog.setCatalogType(type);
      catalog.setStatusCd(BaseConsts.STATUS_CD_VALID);
      catalog.setCreatorId(1L);
      catalog.setUpdatorId(1L);
      catalog.setTenantId(tenant.getTenantId());
      catalogManageMapper.insertCatalog(catalog);
    }
  }

  private void updateTenantAndSyncGateway(LoginInfo loginInfo, TenantDTO tenant, Long userId) {
    tenant.setSystemType(loginInfo.getSystemType());
    tenant.setAppId(Long.valueOf(loginInfo.getTenantCode()));
    tenant.setUpdatorId(userId);
    tenantManageMapper.updateTenant(tenant);
    try {
      synGateway(loginInfo, tenant, userId);
    } catch (Exception e) {
      logger.error("同步租户网关失败: message={}", e.getMessage(), e);
    }
  }

  private void syncTenantUser(@Nullable Long tenantId, Long userId, @Nullable String defaultRole) {
    if (tenantId == null) {
      return;
    }
    if (tenantManageMapper.existsTenantUser(tenantId, userId)) {
      return;
    }
    String role = StringUtils.defaultIfEmpty(defaultRole, BaseConsts.ROLE_EDIT);
    TenantUserDTO tenantUser = new TenantUserDTO();
    tenantUser.setTenantUserId(Sequences.TENANT_USER_ID.next());
    tenantUser.setTenantId(tenantId);
    tenantUser.setUserId(userId);
    tenantUser.setUserRole(role);
    tenantUser.setStatusCd(BaseConsts.STATUS_CD_VALID);
    tenantManageMapper.insertTenantUser(tenantUser);
  }

  private void synGateway(LoginInfo loginInfo, TenantDTO tenant, Long userId) {
    List<LcdpGatewayDTO> lcdpGatewayList = LcdpApiUtil.queryGatewayList(loginInfo.getExtTenantId());
    if (CollectionUtils.isEmpty(lcdpGatewayList)) {
      return;
    }
    ServicePlatformDTO platform = new ServicePlatformDTO();
    Long platformId = Sequences.SERVICE_PLATFORM_ID.next();
    platform.setPlatformId(platformId);
    platform.setPlatformName("灵犀服务网关");
    platform.setPlatformCode("lcdpGateway");
    platform.setTenantId(tenant.getTenantId());
    platform.setStatusCd(BaseConsts.STATUS_CD_VALID);
    platform.setCreatorId(userId);
    platform.setUpdatorId(userId);
    platformManageMapper.insertServicePlatform(platform);
    saveServiceGatewayList(lcdpGatewayList, tenant.getTenantId(), platformId, userId);
  }

  private void saveServiceGatewayList(List<LcdpGatewayDTO> lcdpGatewayList, Long tenantId, Long platformId, Long userId) {
    List<ServiceGatewayDTO> gatewayList = new ArrayList<>();
    for (LcdpGatewayDTO lcdpGateway : lcdpGatewayList) {
      ServiceGatewayDTO gateway = new ServiceGatewayDTO();
      gateway.setGatewayId(Sequences.SERVICE_GATEWAY_ID.next());
      gateway.setEnvCode(lcdpGateway.getEnvCode());
      gateway.setTenantId(tenantId);
      gateway.setPlatformId(platformId);
      gateway.setStatusCd(BaseConsts.STATUS_CD_VALID);
      gateway.setCreatorId(userId);
      gateway.setUpdatorId(userId);
      for (EnvironmentInst environmentInst : lcdpGateway.getEnvironmentInsts()) {
        if ("SERVER_URL".equals(environmentInst.getAttrCode()) && "GATEWAY".equals(environmentInst.getEnvironmentType())) {
          gateway.setUrl(environmentInst.getAttrValue());
        }
      }
      gatewayList.add(gateway);
    }
    gatewayManageMapper.batchInsertServiceGateway(gatewayList);
  }

  /**
   * 检查用户信息是否有变化
   */
  private boolean isUserChanged(LoginInfo loginInfo, UserEntity user) {
    return !Objects.equals(loginInfo.getUserName(), user.getUserName()) || !Objects.equals(loginInfo.getRealName(), user.getRealName())
      || !Objects.equals(loginInfo.getPhoneNo(), user.getPhoneNo());
  }

  /**
   * 同步企业空间
   */
  @Nullable
  private Long syncWorkspace(LoginInfo loginInfo, Long userId) {
    Map<String, Object> attributes = loginInfo.getAttributes();
    if (MapUtils.isEmpty(attributes)) {
      return null;
    }
    // 无开启自动创项目功能，直接跳出
    if (!BooleanUtils.isTrue(MapUtils.getBoolean(attributes, "autoCreateTenant"))) {
      return null;
    }
    String extSpaceId = MapUtils.getString(attributes, "extSpaceId");
    if (StringUtils.isEmpty(extSpaceId)) {
      return null;
    }
    // 检查企业空间是否已存在
    SimpleWorkspaceDTO oldWorkspace = workspaceManageMapper.existWorkSpaceByExtSpaceId(extSpaceId);
    if (oldWorkspace != null) {
      return oldWorkspace.getSpaceId();
    }
    long spaceId = IDUtils.nextId();
    String spaceName = StringUtils.isEmpty(MapUtils.getString(attributes, "spaceName")) ?
      String.valueOf(spaceId) : MapUtils.getString(attributes, "spaceName");
    WorkspaceDTO workspace = new WorkspaceDTO();
    workspace.setSpaceId(spaceId);
    workspace.setSpaceName(spaceName);
    workspace.setCreatedTime(new Date());
    workspace.setStatusCd(BaseConsts.STATUS_CD_VALID);
    workspace.setExtSpaceId(extSpaceId);
    workspace.setCreatorId(userId);
    if (workspaceManageMapper.insertWorkspace(workspace) == 0) {
      SimpleWorkspaceDTO existing = workspaceManageMapper.existWorkSpaceByExtSpaceId(extSpaceId);
      if (existing != null) {
        return existing.getSpaceId();
      }
      existing = workspaceManageMapper.existWorkspaceBySpaceName(spaceName);
      return existing != null ? existing.getSpaceId() : null;
    }
    // 同步创建根组织
    organizationManageService.createRootOrgForWorkspace(spaceId, spaceName, userId);
    return spaceId;
  }
}
