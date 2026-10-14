package com.iwhalecloud.bote.service.portal.tiangong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserItem;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.bot.BotAuthManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.portal.config.properties.TiangongProjectSyncProperties;
import com.iwhalecloud.bote.service.base.IAccountEventLogService;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bote.service.portal.impl.TenantManageServiceImpl;
import com.cmsr.tiangong.base.vo.ResultVO;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 验证天工创建人绑定与原租户成员权限模型能够贯通。
 *
 * @author zhengxueli
 * @since 2026-09-29
 */
class TiangongCreatorMemberPermissionTest {

  private static final long TENANT_ID = 9001L;
  private static final long TECHNICAL_USER_ID = 1L;
  private static final long CREATOR_USER_ID = 501L;
  private static final long EXISTING_MEMBER_ID = 502L;

  @Test
  void boundBusinessCreator_canModifyMembersThroughOriginalPermissionCheckedService() {
    TenantManageMapper tenantManageMapper = mock(TenantManageMapper.class);
    UserManageMapper userManageMapper = mock(UserManageMapper.class);
    OrgUserRoleMapper orgUserRoleMapper = mock(OrgUserRoleMapper.class);
    BotAuthManageMapper botAuthManageMapper = mock(BotAuthManageMapper.class);

    TenantManageServiceImpl tenantManageService = new TenantManageServiceImpl(
      tenantManageMapper,
      mock(ICatalogManageService.class),
      userManageMapper,
      mock(BotQueryMapper.class),
      botAuthManageMapper,
      mock(IAccountEventLogService.class),
      mock(WorkspaceManageMapper.class),
      orgUserRoleMapper,
      mock(PasswordEncoder.class),
      mock(AttrSpecCache.class));

    TiangongProjectSyncProperties properties = new TiangongProjectSyncProperties();
    TiangongProjectUserSyncWorker worker = new TiangongProjectUserSyncWorker(
      tenantManageMapper,
      userManageMapper,
      mock(IUserManageService.class),
      tenantManageService,
      properties);

    AtomicReference<Long> creatorId = new AtomicReference<>(TECHNICAL_USER_ID);
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenAnswer(invocation -> List.of(tenant(creatorId.get())));
    when(tenantManageMapper.selectActiveExternalSpaceIdByTenantId(TENANT_ID)).thenReturn("team-1");
    when(tenantManageMapper.getTenant(TENANT_ID)).thenAnswer(invocation -> tenant(creatorId.get()));

    UserEntity creator = new UserEntity();
    creator.setUserId(CREATOR_USER_ID);
    creator.setExtUserId("tg-creator-1");
    creator.setStatusCd(BaseConsts.STATUS_CD_VALID);
    creator.setUserState(BaseConsts.USER_STATE_ENABLE);
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "tg-creator-1"))
      .thenReturn(List.of(creator));

    TenantUserDTO creatorMember = new TenantUserDTO();
    creatorMember.setTenantId(TENANT_ID);
    creatorMember.setUserId(CREATOR_USER_ID);
    creatorMember.setUserRole(BaseConsts.ROLE_EDIT);
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(TENANT_ID, CREATOR_USER_ID))
      .thenReturn(creatorMember);
    when(tenantManageMapper.bindCreatorIfAllowed(TENANT_ID, CREATOR_USER_ID, TECHNICAL_USER_ID))
      .thenAnswer(invocation -> {
        if (creatorId.get() != TECHNICAL_USER_ID) {
          return 0;
        }
        creatorId.set(CREATOR_USER_ID);
        return 1;
      });

    when(tenantManageMapper.getSpaceIdByTenantId(TENANT_ID)).thenReturn(7001L);
    when(orgUserRoleMapper.checkIsOrgAdmin(7001L, CREATOR_USER_ID)).thenReturn(false);
    when(tenantManageMapper.getUserRole(TENANT_ID, CREATOR_USER_ID)).thenReturn(BaseConsts.ROLE_EDIT);
    when(tenantManageMapper.getUserRole(TENANT_ID, EXISTING_MEMBER_ID)).thenReturn(BaseConsts.ROLE_EDIT);
    when(botAuthManageMapper.selectByUserId(EXISTING_MEMBER_ID)).thenReturn(List.of());
    when(botAuthManageMapper.selectByTenantId(TENANT_ID)).thenReturn(List.of());

    LoginInfo creatorSession = new LoginInfo();
    creatorSession.setUserId(CREATOR_USER_ID);
    creatorSession.setSystemCode("tiangong-noheader");

    TenantUserDTO memberUpdate = new TenantUserDTO();
    memberUpdate.setTenantUserId(8001L);
    memberUpdate.setTenantId(TENANT_ID);
    memberUpdate.setUserId(EXISTING_MEMBER_ID);
    memberUpdate.setUserRole(BaseConsts.ROLE_EDIT);

    try (MockedStatic<SessionUtil> sessionUtil = mockStatic(SessionUtil.class)) {
      sessionUtil.when(SessionUtil::getLoginInfo).thenReturn(creatorSession);
      sessionUtil.when(() -> SessionUtil.isSuperAdmin(CREATOR_USER_ID)).thenReturn(false);

      ResultVO<List<TenantUserDTO>> beforeBinding = tenantManageService.saveTenantUser(List.of(memberUpdate));
      assertFalse(beforeBinding.isSuccess());
      assertEquals("无权操作该租户的成员信息", beforeBinding.getResultMsg());

      TiangongProjectUserSyncResponse syncResponse = worker.upsert(upsertRequest());
      assertEquals("BOUND", syncResponse.getCreatorAction());
      assertEquals(CREATOR_USER_ID, creatorId.get());

      ResultVO<List<TenantUserDTO>> afterBinding = tenantManageService.saveTenantUser(List.of(memberUpdate));
      assertTrue(afterBinding.isSuccess());
    }

    verify(tenantManageMapper).updateTenantUser(eq(memberUpdate));
    verify(tenantManageMapper).bindCreatorIfAllowed(TENANT_ID, CREATOR_USER_ID, TECHNICAL_USER_ID);
  }

  private static TenantDTO tenant(Long creatorId) {
    TenantDTO tenant = new TenantDTO();
    tenant.setTenantId(TENANT_ID);
    tenant.setCreatorId(creatorId);
    return tenant;
  }

  private static TiangongProjectUserUpsertRequest upsertRequest() {
    TiangongProjectUserItem creator = new TiangongProjectUserItem();
    creator.setExtUserId("tg-creator-1");
    creator.setDisplayName("Creator");

    TiangongProjectUserUpsertRequest request = new TiangongProjectUserUpsertRequest();
    request.setRequestId("msg-bind-creator");
    request.setExternalSpaceId("team-1");
    request.setExternalProjectId(101L);
    request.setBoteTenantId(TENANT_ID);
    request.setCreatorExtUserId("tg-creator-1");
    request.setUsers(List.of(creator));
    return request;
  }
}
