package com.iwhalecloud.bote.portal;

import com.iwhalecloud.bote.cache.PortalAdapterCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.workspace.SimpleWorkspaceDTO;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.base.CatalogManageMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import com.iwhalecloud.bote.mapper.skill.ServicePlatformManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.customized.IWorkerIdStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * 多门户默认租户兜底策略测试。
 */
class MultiPortalAdapterTest {

  @BeforeAll
  static void initializeSequenceGenerator() {
    ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
    IWorkerIdStrategy workerIdStrategy = mock(IWorkerIdStrategy.class);
    when(context.getBean(IWorkerIdStrategy.class)).thenReturn(workerIdStrategy);
    when(workerIdStrategy.getWorkerId(org.mockito.ArgumentMatchers.anyLong(),
      org.mockito.ArgumentMatchers.anyLong())).thenReturn(1L);
    SpringUtil.setApplicationContext(context);
  }

  @AfterAll
  static void clearSequenceGeneratorContext() {
    SpringUtil.setApplicationContext(null);
  }

  @Test
  void tgPortalSkipsDefaultTenantFallback() {
    LoginInfo loginInfo = LoginInfo.builder()
      .defaultTenantId(BaseConsts.DEFAULT_TENANT_ID)
      .attributes(Map.of(BaseConsts.ATTR_SKIP_DEFAULT_TENANT_FALLBACK, Boolean.TRUE))
      .build();

    assertNull(MultiPortalAdapter.getDefaultTenantFallback(loginInfo));
  }

  @Test
  void otherPortalsKeepExistingDefaultTenantFallback() {
    LoginInfo loginInfo = LoginInfo.builder()
      .defaultTenantId(BaseConsts.DEFAULT_TENANT_ID)
      .attributes(Collections.emptyMap())
      .build();

    assertEquals(BaseConsts.DEFAULT_TENANT_ID,
      MultiPortalAdapter.getDefaultTenantFallback(loginInfo));
  }

  @Test
  void tiangongProjectDedupRequiresDedicatedProviderMarker() {
    LoginInfo tiangongProjectLogin = LoginInfo.builder()
      .extTenantId(3001L)
      .attributes(Map.of(BaseConsts.ATTR_TIANGONG_PROJECT_LOGIN, Boolean.TRUE,
        "autoCreateTenant", Boolean.TRUE))
      .build();
    LoginInfo ordinaryAutoCreateLogin = LoginInfo.builder()
      .extTenantId(3001L)
      .attributes(Map.of("autoCreateTenant", Boolean.TRUE))
      .build();
    LoginInfo noExternalTenantId = LoginInfo.builder()
      .attributes(Map.of(BaseConsts.ATTR_TIANGONG_PROJECT_LOGIN, Boolean.TRUE))
      .build();
    LoginInfo nonBooleanMarker = LoginInfo.builder()
      .extTenantId(3001L)
      .attributes(Map.of(BaseConsts.ATTR_TIANGONG_PROJECT_LOGIN, "true"))
      .build();

    assertTrue(MultiPortalAdapter.isTiangongProjectLogin(tiangongProjectLogin));
    assertFalse(MultiPortalAdapter.isTiangongProjectLogin(ordinaryAutoCreateLogin));
    assertFalse(MultiPortalAdapter.isTiangongProjectLogin(noExternalTenantId));
    assertFalse(MultiPortalAdapter.isTiangongProjectLogin(nonBooleanMarker));
  }

  @Test
  void noTiangongCandidateCreatesTenantThroughExistingCreationPath() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(Collections.emptyList());

    Long tenantId = invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L);

    assertNotNull(tenantId);
    verify(tenantMapper).listTenantCandidatesByExtTenantId(3001L);
    verify(tenantMapper).insertTenant(org.mockito.ArgumentMatchers.argThat(created ->
      created.getExtTenantId().equals(3001L)
        && created.getSpaceId().equals(2001L)
        && BaseConsts.STATUS_CD_VALID.equals(created.getStatusCd())));
    verify(tenantMapper, never()).getTenantByExtTenantIdAndSpace(3001L, 2001L);
  }

  @Test
  void activeTiangongTenantInCurrentWorkspaceIsReused() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 2001L, BaseConsts.STATUS_CD_VALID);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(List.of(tenant));

    Long tenantId = invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L);

    assertEquals(9001L, tenantId);
  }

  @Test
  void activeTiangongTenantInAnotherWorkspaceIsRejected() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 1001L, BaseConsts.STATUS_CD_VALID);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(List.of(tenant));

    BssException error = assertThrows(BssException.class,
      () -> invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L));

    assertEquals("该项目已存在于其他团队", error.getMessage());
  }

  @Test
  void tiangongProjectWithoutWorkspaceIsRejectedBeforeAnyTenantLookup() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);

    BssException error = assertThrows(BssException.class,
      () -> invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), null));

    assertEquals("天工团队工作空间同步失败，请稍后重试", error.getMessage());
    verify(tenantMapper, never()).listTenantCandidatesByExtTenantId(3001L);
  }

  @Test
  void invalidTiangongTenantInCurrentWorkspaceIsReactivated() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 2001L, BaseConsts.STATUS_CD_INVALID);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(List.of(tenant));
    when(tenantMapper.reactivateTiangongTenant(9001L, "项目一", 2001L, 8001L)).thenReturn(1);
    when(tenantMapper.existsTenantUser(9001L, 8001L)).thenReturn(true);

    Long tenantId = invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L);

    assertEquals(9001L, tenantId);
    assertEquals(BaseConsts.STATUS_CD_VALID, tenant.getStatusCd());
    assertEquals(2001L, tenant.getSpaceId());
    verify(tenantMapper).reactivateTiangongTenant(9001L, "项目一", 2001L, 8001L);
  }

  @Test
  void invalidTiangongTenantInAnotherWorkspaceIsRejectedWithoutReactivation() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 1001L, BaseConsts.STATUS_CD_INVALID);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(List.of(tenant));

    BssException error = assertThrows(BssException.class,
      () -> invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L));

    assertEquals("该项目存在其他团队的历史数据，请联系管理员完成项目迁移", error.getMessage());
    assertEquals(BaseConsts.STATUS_CD_INVALID, tenant.getStatusCd());
    assertEquals(1001L, tenant.getSpaceId());
    verify(tenantMapper, never()).reactivateTiangongTenant(anyLong(), anyString(), anyLong(), anyLong());
  }

  @Test
  void failedTiangongReactivationIsRejectedWithoutChangingInMemoryTenant() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 2001L, BaseConsts.STATUS_CD_INVALID);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(List.of(tenant));
    when(tenantMapper.reactivateTiangongTenant(9001L, "项目一", 2001L, 8001L)).thenReturn(0);

    BssException error = assertThrows(BssException.class,
      () -> invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L));

    assertEquals("天工项目租户状态已发生变化，请重试", error.getMessage());
    assertEquals(BaseConsts.STATUS_CD_INVALID, tenant.getStatusCd());
    assertEquals(2001L, tenant.getSpaceId());
  }

  @Test
  void multipleTiangongTenantCandidatesAreRejected() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(List.of(
      tenant(9001L, 1001L, BaseConsts.STATUS_CD_VALID),
      tenant(9002L, 2001L, BaseConsts.STATUS_CD_INVALID)));

    assertThrows(BssException.class,
      () -> invokeDoSyncTenant(adapter(tenantMapper), tiangongLogin(), 2001L));
  }

  @Test
  void crossWorkspaceFailureTriggersRollbackAtSyncUserTransactionBoundary() {
    TenantDTO tenant = tenant(9001L, 1001L, BaseConsts.STATUS_CD_VALID);
    SyncFixture fixture = syncFixture(List.of(tenant));

    BssException error = assertThrows(BssException.class,
      () -> fixture.proxy().syncUser(syncLogin(), "tiangong-noheader", "EDIT"));

    assertEquals("该项目已存在于其他团队", error.getMessage());
    assertEquals(1, fixture.transactionManager().rollbackCount);
    assertEquals(0, fixture.transactionManager().commitCount);
    verify(fixture.workspaceMapper()).insertWorkspace(any());
  }

  @Test
  void invalidCrossWorkspaceFailureTriggersRollbackAtSyncUserTransactionBoundary() {
    TenantDTO tenant = tenant(9001L, 1001L, BaseConsts.STATUS_CD_INVALID);
    SyncFixture fixture = syncFixture(List.of(tenant));

    BssException error = assertThrows(BssException.class,
      () -> fixture.proxy().syncUser(syncLogin(), "tiangong-noheader", "EDIT"));

    assertEquals("该项目存在其他团队的历史数据，请联系管理员完成项目迁移", error.getMessage());
    assertEquals(1, fixture.transactionManager().rollbackCount);
    assertEquals(0, fixture.transactionManager().commitCount);
    verify(fixture.workspaceMapper()).insertWorkspace(any());
    verify(fixture.tenantMapper(), never())
      .reactivateTiangongTenant(anyLong(), anyString(), anyLong(), anyLong());
  }

  @Test
  void multipleCandidatesTriggerRollbackAtSyncUserTransactionBoundary() {
    SyncFixture fixture = syncFixture(List.of(
      tenant(9001L, 1001L, BaseConsts.STATUS_CD_VALID),
      tenant(9002L, 2001L, BaseConsts.STATUS_CD_INVALID)));

    assertThrows(BssException.class,
      () -> fixture.proxy().syncUser(syncLogin(), "tiangong-noheader", "EDIT"));

    assertEquals(1, fixture.transactionManager().rollbackCount);
    assertEquals(0, fixture.transactionManager().commitCount);
    verify(fixture.workspaceMapper()).insertWorkspace(any());
  }

  @Test
  void successfulReactivationCommitsAtSyncUserTransactionBoundary() {
    SyncFixture fixture = syncFixture(List.of(tenant(9001L, 2001L, BaseConsts.STATUS_CD_INVALID)));
    SimpleWorkspaceDTO workspace = new SimpleWorkspaceDTO();
    workspace.setSpaceId(2001L);
    when(fixture.workspaceMapper().existWorkSpaceByExtSpaceId("2001")).thenReturn(workspace);
    when(fixture.tenantMapper().reactivateTiangongTenant(anyLong(), anyString(), anyLong(), anyLong()))
      .thenReturn(1);
    when(fixture.tenantMapper().existsTenantUser(9001L, 8001L)).thenReturn(true);

    fixture.proxy().syncUser(syncLogin(), "tiangong-noheader", "EDIT");

    assertEquals(0, fixture.transactionManager().rollbackCount);
    assertEquals(1, fixture.transactionManager().commitCount);
    verify(fixture.tenantMapper()).reactivateTiangongTenant(anyLong(), anyString(), anyLong(), anyLong());
  }

  @Test
  void nonTiangongLoginKeepsOriginalExternalTenantAndWorkspaceLookup() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 2001L, BaseConsts.STATUS_CD_VALID);
    when(tenantMapper.getTenantByExtTenantIdAndSpace(3001L, 2001L)).thenReturn(tenant);
    LoginInfo login = LoginInfo.builder().extTenantId(3001L)
      .attributes(Map.of("autoCreateTenant", Boolean.TRUE)).build();

    assertEquals(9001L, invokeDoSyncTenant(adapter(tenantMapper), login, 2001L));

    verify(tenantMapper).getTenantByExtTenantIdAndSpace(3001L, 2001L);
    verify(tenantMapper, never()).listTenantCandidatesByExtTenantId(3001L);
  }

  @Test
  void nonTiangongInvalidTenantKeepsOriginalReactivationMethod() {
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    TenantDTO tenant = tenant(9001L, 2001L, BaseConsts.STATUS_CD_INVALID);
    when(tenantMapper.getTenantByExtTenantIdAndSpace(3001L, 2001L)).thenReturn(tenant);
    when(tenantMapper.existsTenantUser(9001L, 8001L)).thenReturn(true);
    LoginInfo login = LoginInfo.builder().extTenantId(3001L)
      .attributes(Map.of("autoCreateTenant", Boolean.TRUE)).build();

    assertEquals(9001L, invokeDoSyncTenant(adapter(tenantMapper), login, 2001L));

    verify(tenantMapper).reactivateTenant(9001L, "项目一", 8001L);
    verify(tenantMapper, never()).reactivateTiangongTenant(9001L, "项目一", 2001L, 8001L);
  }

  private LoginInfo tiangongLogin() {
    return LoginInfo.builder()
      .extTenantId(3001L)
      .attributes(Map.of(BaseConsts.ATTR_TIANGONG_PROJECT_LOGIN, Boolean.TRUE))
      .build();
  }

  private LoginInfo syncLogin() {
    return LoginInfo.builder()
      .extUserId("1001")
      .userName("1001")
      .realName("天工用户")
      .extTenantId(3001L)
      .tenantCode("auto3001")
      .tenantName("项目一")
      .systemCode("tiangong-noheader")
      .attributes(Map.of(
        "autoCreateTenant", Boolean.TRUE,
        "extSpaceId", "2001",
        "spaceName", "研发团队",
        BaseConsts.ATTR_TIANGONG_PROJECT_LOGIN, Boolean.TRUE))
      .build();
  }

  private SyncFixture syncFixture(List<TenantDTO> candidates) {
    UserManageMapper userMapper = mock(UserManageMapper.class);
    TenantManageMapper tenantMapper = mock(TenantManageMapper.class);
    WorkspaceManageMapper workspaceMapper = mock(WorkspaceManageMapper.class);
    DistributedLockFactory lockFactory = mock(DistributedLockFactory.class);
    DistributedLock userLock = mock(DistributedLock.class);
    DistributedLock tenantLock = mock(DistributedLock.class);
    UserEntity user = new UserEntity();
    user.setUserId(8001L);
    user.setUserName("1001");
    user.setRealName("天工用户");
    when(userMapper.getUserBySystemCodeAndExtUserId("tiangong-noheader", "1001")).thenReturn(user);
    when(lockFactory.getBizLock(anyString(), anyString())).thenReturn(userLock, tenantLock);
    when(userLock.tryLock()).thenReturn(true);
    when(tenantLock.tryLock()).thenReturn(true);
    when(workspaceMapper.insertWorkspace(any())).thenReturn(1);
    when(tenantMapper.listTenantCandidatesByExtTenantId(3001L)).thenReturn(candidates);

    MultiPortalAdapter target = new MultiPortalAdapter(userMapper, tenantMapper, mock(CatalogManageMapper.class),
      mock(PortalAdapterCache.class), mock(ServicePlatformManageMapper.class), mock(ServiceGatewayManageMapper.class),
      workspaceMapper, mock(IOrganizationManageService.class), lockFactory);
    RecordingTransactionManager transactionManager = new RecordingTransactionManager();
    ProxyFactory proxyFactory = new ProxyFactory(target);
    TransactionInterceptor interceptor = new TransactionInterceptor();
    interceptor.setTransactionManager(transactionManager);
    interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
    proxyFactory.addAdvice(interceptor);
    MultiPortalAdapter proxy = (MultiPortalAdapter) proxyFactory.getProxy();
    return new SyncFixture(proxy, tenantMapper, workspaceMapper, transactionManager);
  }

  private TenantDTO tenant(Long tenantId, Long spaceId, String statusCd) {
    TenantDTO tenant = new TenantDTO();
    tenant.setTenantId(tenantId);
    tenant.setTenantName("项目一");
    tenant.setSpaceId(spaceId);
    tenant.setStatusCd(statusCd);
    return tenant;
  }

  private MultiPortalAdapter adapter(TenantManageMapper tenantMapper) {
    return new MultiPortalAdapter(mock(UserManageMapper.class), tenantMapper, mock(CatalogManageMapper.class),
      mock(PortalAdapterCache.class), mock(ServicePlatformManageMapper.class), mock(ServiceGatewayManageMapper.class),
      mock(WorkspaceManageMapper.class), mock(IOrganizationManageService.class), mock(DistributedLockFactory.class));
  }

  private Long invokeDoSyncTenant(MultiPortalAdapter adapter, LoginInfo loginInfo, Long workSpaceId) {
    UserEntity user = new UserEntity();
    user.setUserId(8001L);
    return ReflectionTestUtils.invokeMethod(adapter, "doSyncTenant", loginInfo, user, "auto3001", "项目一",
      workSpaceId);
  }

  private record SyncFixture(MultiPortalAdapter proxy, TenantManageMapper tenantMapper,
                             WorkspaceManageMapper workspaceMapper,
                             RecordingTransactionManager transactionManager) {
  }

  private static final class RecordingTransactionManager extends AbstractPlatformTransactionManager {
    private static final long serialVersionUID = 1L;
    private int commitCount;
    private int rollbackCount;

    @Override
    protected Object doGetTransaction() {
      return new Object();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
      // No external resource is required; this test verifies Spring's transaction decision.
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
      commitCount++;
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
      rollbackCount++;
    }
  }
}
