package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.iwhalecloud.bote.service.base.IEditLockService;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DefaultPortalAuthProvider.logout 单元测试。
 *
 * <p>logout 内部经由 SignUtil 调用 SpringUtil.getBean(DefaultPortalProperties.class)，
 * 故走完整登出路径的用例需用 mockStatic(SpringUtil) 隔离 Spring 上下文。
 *
 * @author zhaolei2
 * @since 2026-07-08
 */
@ExtendWith(MockitoExtension.class)
class DefaultPortalAuthProviderTest {

  @Mock
  private ICacheClient cacheClient;
  @Mock
  private UserManageMapper userManageMapper;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private IEditLockService editLockService;

  private DefaultPortalAuthProvider provider() {
    return new DefaultPortalAuthProvider(new DefaultPortalProperties(), cacheClient,
        userManageMapper, passwordEncoder, editLockService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void logoutInvalidatesExistingSession() {
    DefaultPortalAuthProvider authProvider = provider();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("BOTE_SESSION", "sid"));
    request.addParameter("basePath", "/app");
    HttpSession session = mock(HttpSession.class);
    request.setSession(session);
    HttpServletResponse response = mock(HttpServletResponse.class);

    ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    when(cacheClient.opsForValue()).thenReturn(valueOps);
    when(valueOps.get("sid")).thenReturn(null);

    try (MockedStatic<SpringUtil> springUtil = Mockito.mockStatic(SpringUtil.class)) {
      springUtil.when(() -> SpringUtil.getBean(DefaultPortalProperties.class))
          .thenReturn(new DefaultPortalProperties());
      authProvider.logout(request, response);
    }

    verify(session).invalidate();
    verify(cacheClient).delete("sid");
    verify(editLockService).releaseAllKey(null);
    verify(response, times(2)).addCookie(any(Cookie.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void logoutWithoutSessionDoesNotFail() {
    DefaultPortalAuthProvider authProvider = provider();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("BOTE_SESSION", "sid"));
    // 未 setSession -> getSession(false) 返回 null
    HttpServletResponse response = mock(HttpServletResponse.class);

    ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    when(cacheClient.opsForValue()).thenReturn(valueOps);
    when(valueOps.get("sid")).thenReturn(null);

    try (MockedStatic<SpringUtil> springUtil = Mockito.mockStatic(SpringUtil.class)) {
      springUtil.when(() -> SpringUtil.getBean(DefaultPortalProperties.class))
          .thenReturn(new DefaultPortalProperties());
      authProvider.logout(request, response);
    }

    // session 为 null 不应抛异常；手动删除 cookie 仍执行
    verify(response, times(2)).addCookie(any(Cookie.class));
    verify(cacheClient).delete("sid");
  }

  @Test
  void logoutWithoutSessionIdSkipsCleanup() {
    DefaultPortalAuthProvider authProvider = provider();
    MockHttpServletRequest request = new MockHttpServletRequest();
    // 未设 BOTE_SESSION cookie -> getSessionId 返回 null，整个清理分支跳过
    HttpServletResponse response = mock(HttpServletResponse.class);

    authProvider.logout(request, response);

    verify(cacheClient, never()).delete(anyString());
    verify(editLockService, never()).releaseAllKey(any());
  }
}
