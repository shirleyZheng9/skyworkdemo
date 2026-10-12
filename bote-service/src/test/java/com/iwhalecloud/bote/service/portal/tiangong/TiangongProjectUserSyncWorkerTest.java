package com.iwhalecloud.bote.service.portal.tiangong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserItem;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.portal.TenantManageMapper;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.config.properties.TiangongProjectSyncProperties;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bote.service.portal.IUserManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 天工项目成员事务工作器测试
 *
 * @author zhengxueli
 * @since 2024-09-23
 */
class TiangongProjectUserSyncWorkerTest {
  private TenantManageMapper tenantManageMapper;
  private UserManageMapper userManageMapper;
  private ITenantManageService tenantManageService;
  private IUserManageService userManageService;
  private TiangongProjectUserSyncWorker worker;

  @BeforeEach
  void setUp() {
    tenantManageMapper = mock(TenantManageMapper.class);
    userManageMapper = mock(UserManageMapper.class);
    tenantManageService = mock(ITenantManageService.class);
    userManageService = mock(IUserManageService.class);
    TiangongProjectSyncProperties properties = new TiangongProjectSyncProperties();
    worker = new TiangongProjectUserSyncWorker(
      tenantManageMapper,
      userManageMapper,
      userManageService,
      tenantManageService,
      properties);
    when(tenantManageMapper.selectActiveExternalSpaceIdByTenantId(9001L)).thenReturn("team-1");
  }

  @Test
  void existingManageMember_isNotDowngraded_andResponseMatchesContract() {
    TenantDTO tenant = tenant(9001L, 501L);
    UserEntity user = user(501L, "u1");
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1"))
      .thenReturn(List.of(user));
    TenantUserDTO existingMember = new TenantUserDTO();
    existingMember.setUserRole(BaseConsts.ROLE_MANAGE);
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 501L)).thenReturn(existingMember);

    TiangongProjectUserSyncResponse response = worker.upsert(upsertRequest());

    assertEquals("msg-1", response.getRequestId());
    assertEquals(9001L, response.getTenantId());
    assertEquals("UNCHANGED", response.getCreatorAction());
    assertEquals("EXISTS", response.getUsers().getFirst().getMemberAction());
    verify(tenantManageService, never()).saveTenantUserForInternalIntegration(anyList());
  }

  @Test
  void conflictingUserName_returnsStableBusinessError() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9001L, 1L)));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1"))
      .thenReturn(List.of());
    when(userManageMapper.existsUserName("tiangong-noheader", "u1")).thenReturn(true);

    TiangongProjectSyncException error = assertThrows(
      TiangongProjectSyncException.class, () -> worker.upsert(upsertRequest()));

    assertEquals("TG_USER_NAME_CONFLICT", error.getCode());
  }

  @Test
  void missingOrAmbiguousProject_returnsStableBusinessErrorsWithoutWrites() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L)).thenReturn(List.of());
    assertCode("TG_PROJECT_NOT_FOUND", () -> worker.upsert(upsertRequest()));

    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9001L, 1L), tenant(9002L, 1L)));
    assertCode("TG_PROJECT_AMBIGUOUS", () -> worker.upsert(upsertRequest()));

    verify(userManageService, never()).saveUser(any());
    verify(tenantManageService, never()).saveTenantUserForInternalIntegration(anyList());
  }

  @Test
  void mismatchedInternalTenant_returnsStableBusinessErrorWithoutWrites() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9999L, 1L)));

    assertCode("TG_TENANT_MISMATCH", () -> worker.upsert(upsertRequest()));

    verify(userManageService, never()).saveUser(any());
    verify(tenantManageService, never()).saveTenantUserForInternalIntegration(anyList());
  }

  @Test
  void mismatchedProjectSpace_returnsStableBusinessErrorWithoutWrites() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9001L, 1L)));
    when(tenantManageMapper.selectActiveExternalSpaceIdByTenantId(9001L)).thenReturn("team-2");

    assertCode("TG_PROJECT_SPACE_MISMATCH", () -> worker.upsert(upsertRequest()));

    verify(userManageService, never()).saveUser(any());
    verify(tenantManageService, never()).saveTenantUserForInternalIntegration(anyList());
  }

  @Test
  void ambiguousOrDisabledBusinessUser_returnsStableBusinessErrors() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9001L, 1L)));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1"))
      .thenReturn(List.of(user(501L, "u1"), user(502L, "u1")));
    assertCode("TG_USER_AMBIGUOUS", () -> worker.upsert(upsertRequest()));

    UserEntity disabled = user(501L, "u1");
    disabled.setUserState(BaseConsts.USER_STATE_DISABLE);
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1"))
      .thenReturn(List.of(disabled));
    assertCode("TG_USER_DISABLED", () -> worker.upsert(upsertRequest()));

    UserEntity deleted = user(501L, "u1");
    deleted.setStatusCd("00X");
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1"))
      .thenReturn(List.of(deleted));
    assertCode("TG_USER_DISABLED", () -> worker.upsert(upsertRequest()));

    verify(tenantManageService, never()).saveTenantUserForInternalIntegration(anyList());
  }

  @Test
  void existingUserWithNullUserState_isTreatedAsEnabled() {
    // 天工登录自动创建的存量用户 user_state 为空，应与登录校验一致视为在用：正常加入成员并绑定创建人
    TenantDTO tenant = tenant(9001L, 1L);
    UserEntity user = user(501L, "u1");
    user.setUserState(null);
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L)).thenReturn(List.of(tenant));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of(user));
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 501L)).thenReturn(null);
    when(tenantManageService.saveTenantUserForInternalIntegration(anyList())).thenReturn(ResultVO.success(List.<TenantUserDTO>of()));
    when(tenantManageMapper.bindCreatorIfAllowed(9001L, 501L, 1L)).thenReturn(1);

    TiangongProjectUserSyncResponse response = worker.upsert(upsertRequest());

    assertEquals("CREATED", response.getUsers().getFirst().getMemberAction());
    assertEquals(501L, response.getUsers().getFirst().getUserId());
    assertEquals("BOUND", response.getCreatorAction());
    verify(userManageService, never()).saveUser(any());
  }

  @Test
  void userCreateFailureOrMissingReadBack_returnsStableBusinessError() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9001L, 1L)));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of());
    when(userManageMapper.existsUserName("tiangong-noheader", "u1")).thenReturn(false);
    when(userManageService.saveUser(any(UserDTO.class))).thenReturn(ResultVO.fail("create failed"));
    assertCode("TG_USER_CREATE_FAILED", () -> worker.upsert(upsertRequest()));

    when(userManageService.saveUser(any(UserDTO.class))).thenReturn(ResultVO.success(new UserDTO()));
    assertCode("TG_USER_CREATE_FAILED", () -> worker.upsert(upsertRequest()));

    verify(tenantManageService, never()).saveTenantUserForInternalIntegration(anyList());
  }

  @Test
  void memberUpsertFailure_doesNotAttemptCreatorBinding() {
    TenantDTO tenant = tenant(9001L, 1L);
    UserEntity user = user(501L, "u1");
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L)).thenReturn(List.of(tenant));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of(user));
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 501L)).thenReturn(null);
    when(tenantManageService.saveTenantUserForInternalIntegration(anyList())).thenReturn(ResultVO.fail("member failed"));

    assertCode("TG_MEMBER_UPSERT_FAILED", () -> worker.upsert(upsertRequest()));

    verify(tenantManageMapper, never()).bindCreatorIfAllowed(any(), any(), any());
  }

  @Test
  void creatorConflict_returnsStableBusinessErrorAfterMemberResolution() {
    TenantDTO tenant = tenant(9001L, 700L);
    UserEntity user = user(501L, "u1");
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L)).thenReturn(List.of(tenant));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of(user));
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 501L)).thenReturn(new TenantUserDTO());
    when(tenantManageMapper.bindCreatorIfAllowed(9001L, 501L, 1L)).thenReturn(0);

    assertCode("TG_CREATOR_CONFLICT", () -> worker.upsert(upsertRequest()));
  }

  @Test
  void removeMissingUserOrMembership_isIdempotentNotFound() {
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L))
      .thenReturn(List.of(tenant(9001L, 1L)));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of());
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u2"))
      .thenReturn(List.of(user(502L, "u2")));
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 502L)).thenReturn(null);

    TiangongProjectUserSyncResponse response = worker.remove(removeRequest("u1", "u2"));

    assertEquals("NOT_FOUND", response.getUsers().get(0).getMemberAction());
    assertEquals("NOT_FOUND", response.getUsers().get(1).getMemberAction());
    verify(tenantManageService, never()).deleteTenantUserForInternalIntegration(any(), anyList());
  }

  @Test
  void removeCreatorOrDeleteFailure_returnsStableBusinessErrors() {
    TenantDTO tenant = tenant(9001L, 501L);
    UserEntity user = user(501L, "u1");
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L)).thenReturn(List.of(tenant));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of(user));
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 501L)).thenReturn(new TenantUserDTO());
    assertCode("TG_CREATOR_REMOVE_FORBIDDEN", () -> worker.remove(removeRequest("u1")));

    tenant.setCreatorId(1L);
    when(tenantManageService.deleteTenantUserForInternalIntegration(eq(9001L), anyList()))
      .thenReturn(ResultVO.fail("delete failed"));
    assertCode("TG_MEMBER_REMOVE_FAILED", () -> worker.remove(removeRequest("u1")));
  }

  @Test
  void successfulRemove_returnsRemoved() {
    TenantDTO tenant = tenant(9001L, 1L);
    UserEntity user = user(501L, "u1");
    when(tenantManageMapper.selectActiveTiangongTenantsByProjectId(101L)).thenReturn(List.of(tenant));
    when(userManageMapper.selectUsersBySystemCodeAndExtUserId("tiangong-noheader", "u1")).thenReturn(List.of(user));
    when(tenantManageMapper.getTenantUserByTenantIdAndUserId(9001L, 501L)).thenReturn(new TenantUserDTO());
    when(tenantManageService.deleteTenantUserForInternalIntegration(eq(9001L), anyList()))
      .thenReturn(ResultVO.success());

    TiangongProjectUserSyncResponse response = worker.remove(removeRequest("u1"));

    assertEquals("REMOVED", response.getUsers().getFirst().getMemberAction());
  }

  private static void assertCode(String expectedCode, org.junit.jupiter.api.function.Executable executable) {
    TiangongProjectSyncException error = assertThrows(TiangongProjectSyncException.class, executable);
    assertEquals(expectedCode, error.getCode());
  }

  private static TiangongProjectUserRemoveRequest removeRequest(String... extUserIds) {
    TiangongProjectUserRemoveRequest request = new TiangongProjectUserRemoveRequest();
    request.setRequestId("msg-remove");
    request.setExternalSpaceId("team-1");
    request.setExternalProjectId(101L);
    request.setBoteTenantId(9001L);
    request.setExtUserIds(List.of(extUserIds));
    return request;
  }

  private static TiangongProjectUserUpsertRequest upsertRequest() {
    TiangongProjectUserItem item = new TiangongProjectUserItem();
    item.setExtUserId("u1");
    item.setDisplayName("Alice");
    TiangongProjectUserUpsertRequest request = new TiangongProjectUserUpsertRequest();
    request.setRequestId("msg-1");
    request.setExternalSpaceId("team-1");
    request.setExternalProjectId(101L);
    request.setBoteTenantId(9001L);
    request.setCreatorExtUserId("u1");
    request.setUsers(List.of(item));
    return request;
  }

  private static TenantDTO tenant(Long tenantId, Long creatorId) {
    TenantDTO tenant = new TenantDTO();
    tenant.setTenantId(tenantId);
    tenant.setCreatorId(creatorId);
    return tenant;
  }

  private static UserEntity user(Long userId, String extUserId) {
    UserEntity user = new UserEntity();
    user.setUserId(userId);
    user.setExtUserId(extUserId);
    user.setStatusCd(BaseConsts.STATUS_CD_VALID);
    user.setUserState(BaseConsts.USER_STATE_ENABLE);
    return user;
  }
}
