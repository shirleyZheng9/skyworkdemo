package com.iwhalecloud.bote.service.portal.tiangong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserItem;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.portal.PortalLockConsts;
import com.iwhalecloud.bote.portal.config.properties.TiangongProjectSyncProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;

/**
 * 天工项目成员同步门面测试
 *
 * @author zhengxueli
 * @since 2024-09-23
 */
class TiangongProjectUserSyncServiceImplTest {

  @Test
  void nonConfiguredTechnicalAccount_isRejectedBeforeProcessingRequest() {
    TiangongProjectSyncProperties properties = new TiangongProjectSyncProperties();
    TiangongProjectUserSyncServiceImpl service = new TiangongProjectUserSyncServiceImpl(
      mock(DistributedLockFactory.class), mock(TiangongProjectUserSyncWorker.class), properties);
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setUserId(2L);
    loginInfo.setSystemCode("DEFAULT");

    try (MockedStatic<SessionUtil> sessionUtil = mockStatic(SessionUtil.class)) {
      sessionUtil.when(SessionUtil::getLoginInfo).thenReturn(loginInfo);

      TiangongProjectSyncException error = assertThrows(
        TiangongProjectSyncException.class,
        () -> service.upsert(new TiangongProjectUserUpsertRequest()));

      assertEquals("TG_SYNC_FORBIDDEN", error.getCode());
    }
  }

  @Test
  void upsertMissingProjectFields_returnsRequestInvalidBeforeTakingLocks() {
    DistributedLockFactory lockFactory = mock(DistributedLockFactory.class);
    TiangongProjectUserSyncWorker worker = mock(TiangongProjectUserSyncWorker.class);
    TiangongProjectUserSyncServiceImpl service = service(lockFactory, worker);

    assertCodeWithTechnicalSession("TG_REQUEST_INVALID", () -> service.upsert(new TiangongProjectUserUpsertRequest()));

    verify(lockFactory, never()).getBizLock(anyString(), anyString());
    verify(worker, never()).upsert(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void upsertRejectsDuplicateUsersAndCreatorOutsideBatch() {
    TiangongProjectUserSyncServiceImpl service = service(mock(DistributedLockFactory.class), mock(TiangongProjectUserSyncWorker.class));
    TiangongProjectUserUpsertRequest duplicate = validUpsert();
    duplicate.setUsers(List.of(user("u1"), user(" u1 ")));
    assertCodeWithTechnicalSession("TG_REQUEST_INVALID", () -> service.upsert(duplicate));

    TiangongProjectUserUpsertRequest missingCreator = validUpsert();
    missingCreator.setCreatorExtUserId("u2");
    assertCodeWithTechnicalSession("TG_REQUEST_INVALID", () -> service.upsert(missingCreator));
  }

  @Test
  void removeRejectsBlankAndDuplicateUsers() {
    TiangongProjectUserSyncServiceImpl service = service(mock(DistributedLockFactory.class), mock(TiangongProjectUserSyncWorker.class));
    TiangongProjectUserRemoveRequest blank = validRemove();
    blank.setExtUserIds(List.of(" "));
    assertCodeWithTechnicalSession("TG_REQUEST_INVALID", () -> service.remove(blank));

    TiangongProjectUserRemoveRequest duplicate = validRemove();
    duplicate.setExtUserIds(new java.util.ArrayList<>(List.of("u1", " u1 ")));
    assertCodeWithTechnicalSession("TG_REQUEST_INVALID", () -> service.remove(duplicate));
  }

  @Test
  void lockContention_returnsSyncBusyAndDoesNotInvokeWorker() {
    DistributedLockFactory lockFactory = mock(DistributedLockFactory.class);
    DistributedLock projectLock = mock(DistributedLock.class);
    TiangongProjectUserSyncWorker worker = mock(TiangongProjectUserSyncWorker.class);
    when(lockFactory.getBizLock(anyString(), anyString())).thenReturn(projectLock);
    when(projectLock.tryLock()).thenReturn(false);

    TiangongProjectUserSyncServiceImpl service = service(lockFactory, worker);
    assertCodeWithTechnicalSession("TG_SYNC_BUSY", () -> service.upsert(validUpsert()));

    verify(worker, never()).upsert(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void projectLock_isScopedByProjectIdInsteadOfExternalSpace() {
    DistributedLockFactory lockFactory = mock(DistributedLockFactory.class);
    DistributedLock lock = mock(DistributedLock.class);
    TiangongProjectUserSyncWorker worker = mock(TiangongProjectUserSyncWorker.class);
    when(lockFactory.getBizLock(anyString(), anyString())).thenReturn(lock);
    when(lock.tryLock()).thenReturn(true);

    TiangongProjectUserSyncServiceImpl service = service(lockFactory, worker);
    assertWithTechnicalSession(() -> service.upsert(validUpsert()));

    verify(lockFactory).getBizLock(PortalLockConsts.TIANGONG_PROJECT_USER_SYNC_LOCK, "project:101");
  }

  private static TiangongProjectUserSyncServiceImpl service(
    DistributedLockFactory lockFactory, TiangongProjectUserSyncWorker worker) {
    return new TiangongProjectUserSyncServiceImpl(lockFactory, worker, new TiangongProjectSyncProperties());
  }

  private static void assertCodeWithTechnicalSession(String expectedCode, Executable executable) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setUserId(1L);
    loginInfo.setSystemCode("DEFAULT");
    try (MockedStatic<SessionUtil> sessionUtil = mockStatic(SessionUtil.class)) {
      sessionUtil.when(SessionUtil::getLoginInfo).thenReturn(loginInfo);
      TiangongProjectSyncException error = assertThrows(TiangongProjectSyncException.class, executable);
      assertEquals(expectedCode, error.getCode());
    }
  }

  private static void assertWithTechnicalSession(Executable executable) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setUserId(1L);
    loginInfo.setSystemCode("DEFAULT");
    try (MockedStatic<SessionUtil> sessionUtil = mockStatic(SessionUtil.class)) {
      sessionUtil.when(SessionUtil::getLoginInfo).thenReturn(loginInfo);
      org.junit.jupiter.api.Assertions.assertDoesNotThrow(executable);
    }
  }

  private static TiangongProjectUserUpsertRequest validUpsert() {
    TiangongProjectUserUpsertRequest request = new TiangongProjectUserUpsertRequest();
    request.setRequestId("msg-1");
    request.setExternalSpaceId("team-1");
    request.setExternalProjectId(101L);
    request.setCreatorExtUserId("u1");
    request.setUsers(new java.util.ArrayList<>(List.of(user("u1"))));
    return request;
  }

  private static TiangongProjectUserRemoveRequest validRemove() {
    TiangongProjectUserRemoveRequest request = new TiangongProjectUserRemoveRequest();
    request.setRequestId("msg-1");
    request.setExternalSpaceId("team-1");
    request.setExternalProjectId(101L);
    request.setExtUserIds(new java.util.ArrayList<>(List.of("u1")));
    return request;
  }

  private static TiangongProjectUserItem user(String extUserId) {
    TiangongProjectUserItem user = new TiangongProjectUserItem();
    user.setExtUserId(extUserId);
    user.setDisplayName("Alice");
    return user;
  }
}
