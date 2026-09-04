package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

/**
 * {@link SessionUtil} 单元测试
 *
 * <p>覆盖 ThreadLocal 的 set/get/clear、getLoginInfo 不存在时抛异常、getOptionalUserId 默认值。
 * isSuperAdmin 通过 mockStatic(SpringUtil) + mock(DcParamCache) 覆盖，因为该方法依赖数据库参数。</p>
 */
class SessionUtilTest {

  @AfterEach
  void cleanUp() {
    SessionUtil.clearThreadLocal();
  }

  // ==================== sessionId ====================

  @Test
  void sessionId_setAndGet() {
    SessionUtil.setSessionId("sess-123");
    assertThat(SessionUtil.getSessionId()).isEqualTo("sess-123");
  }

  @Test
  void sessionId_setNull_removesValue() {
    SessionUtil.setSessionId("sess-123");
    SessionUtil.setSessionId(null);
    assertThat(SessionUtil.getSessionId()).isNull();
  }

  @Test
  void sessionId_notSet_returnsNull() {
    assertThat(SessionUtil.getSessionId()).isNull();
  }

  // ==================== loginInfo ====================

  @Test
  void setLoginInfo_nonNull_thenGetReturnsValue() {
    LoginInfo info = LoginInfo.builder().userId(42L).userName("alice").build();
    SessionUtil.setLoginInfo(info);

    assertThat(SessionUtil.getOptionalLoginInfo()).isSameAs(info);
    assertThat(SessionUtil.getLoginInfo()).isSameAs(info);
    // userId 应被写入 MDC
    assertThat(MDC.get("userId")).isEqualTo("42");
  }

  @Test
  void setLoginInfo_null_removesValue() {
    LoginInfo info = LoginInfo.builder().userId(42L).userName("alice").build();
    SessionUtil.setLoginInfo(info);
    SessionUtil.setLoginInfo(null);

    assertThat(SessionUtil.getOptionalLoginInfo()).isNull();
  }

  @Test
  void setLoginInfo_nullUserId_mdcNotSet() {
    LoginInfo info = LoginInfo.builder().userId(null).userName("alice").build();
    SessionUtil.setLoginInfo(info);

    assertThat(SessionUtil.getOptionalLoginInfo()).isSameAs(info);
    assertThat(MDC.get("userId")).isNull();
  }

  @Test
  void getLoginInfo_notSet_throwsBssException() {
    assertThatThrownBy(SessionUtil::getLoginInfo)
        .isInstanceOf(BssException.class);
  }

  // ==================== getOptionalUserId ====================

  @Test
  void getOptionalUserId_withDefault_notLoggedIn_returnsDefault() {
    assertThat(SessionUtil.getOptionalUserId(99L)).isEqualTo(99L);
  }

  @Test
  void getOptionalUserId_withDefault_loggedIn_returnsUserId() {
    LoginInfo info = LoginInfo.builder().userId(77L).build();
    SessionUtil.setLoginInfo(info);

    assertThat(SessionUtil.getOptionalUserId(99L)).isEqualTo(77L);
  }

  @Test
  void getOptionalUserId_noDefault_notLoggedIn_returnsNull() {
    assertThat(SessionUtil.getOptionalUserId()).isNull();
  }

  @Test
  void getOptionalUserId_noDefault_loggedIn_returnsUserId() {
    LoginInfo info = LoginInfo.builder().userId(77L).build();
    SessionUtil.setLoginInfo(info);

    assertThat(SessionUtil.getOptionalUserId()).isEqualTo(77L);
  }

  // ==================== clearThreadLocal ====================

  @Test
  void clearThreadLocal_removesAll() {
    SessionUtil.setSessionId("sess-1");
    SessionUtil.setLoginInfo(LoginInfo.builder().userId(1L).build());

    SessionUtil.clearThreadLocal();

    assertThat(SessionUtil.getSessionId()).isNull();
    assertThat(SessionUtil.getOptionalLoginInfo()).isNull();
    assertThat(MDC.get("userId")).isNull();
  }

  // ==================== isSuperAdmin ====================

  @Test
  void isSuperAdmin_matchingUserId_returnsTrue() {
    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class)) {
      DcParamCache cacheMock = mock(DcParamCache.class);
      when(cacheMock.getDcParamValByCode("SUPER_ADMIN", "1|admin")).thenReturn("1|admin");
      springMock.when(() -> SpringUtil.getBean(DcParamCache.class)).thenReturn(cacheMock);

      assertThat(SessionUtil.isSuperAdmin(1L)).isTrue();
    }
  }

  @Test
  void isSuperAdmin_nonMatchingUserId_returnsFalse() {
    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class)) {
      DcParamCache cacheMock = mock(DcParamCache.class);
      when(cacheMock.getDcParamValByCode("SUPER_ADMIN", "1|admin")).thenReturn("1|admin");
      springMock.when(() -> SpringUtil.getBean(DcParamCache.class)).thenReturn(cacheMock);

      assertThat(SessionUtil.isSuperAdmin(999L)).isFalse();
    }
  }

  @Test
  void isSuperAdmin_multipleAdmins_returnsTrueForAny() {
    try (MockedStatic<SpringUtil> springMock = mockStatic(SpringUtil.class)) {
      DcParamCache cacheMock = mock(DcParamCache.class);
      when(cacheMock.getDcParamValByCode("SUPER_ADMIN", "1|admin")).thenReturn("1|admin,2|superuser,10|testuser");
      springMock.when(() -> SpringUtil.getBean(DcParamCache.class)).thenReturn(cacheMock);

      assertThat(SessionUtil.isSuperAdmin(1L)).isTrue();
      assertThat(SessionUtil.isSuperAdmin(2L)).isTrue();
      assertThat(SessionUtil.isSuperAdmin(10L)).isTrue();
      assertThat(SessionUtil.isSuperAdmin(3L)).isFalse();
    }
  }
}
