package com.iwhalecloud.bote.controller.portal;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.portal.LoggedDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.MultiPortalAdapter;
import com.iwhalecloud.bote.portal.adapter.DefaultPortalAuthProvider;
import com.iwhalecloud.bote.service.portal.ISingleLoginService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录管理
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "门户：登录管理")
public class LoginController {

  private final IAuthProvider defaultAuthProvider;
  private final MultiPortalAdapter multiPortalAdapter;
  private final ApiAuthCache apiAuthCache;
  private final ISingleLoginService singleLoginService;

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public LoginController(IAuthProvider defaultAuthProvider, ObjectProvider<MultiPortalAdapter> multiPortalAdapterObjectProvider,
                         ApiAuthCache apiAuthCache, ISingleLoginService singleLoginService) {
    this.defaultAuthProvider = defaultAuthProvider;
    this.multiPortalAdapter = multiPortalAdapterObjectProvider.getIfAvailable();
    this.apiAuthCache = apiAuthCache;
    this.singleLoginService = singleLoginService;
  }

  /**
   * 检查是否登录
   */
  @IgnoreSession
  @IgnoreSign
  @GetMapping("logged")
  @Operation(summary = "检查是否登录")
  public ResultVO<LoggedDTO> logged(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(name = "systemCode", required = false) String systemCode) throws IOException {
    LoginInfo loginInfo = defaultAuthProvider.getLoginInfo(request);
    // 如果开启了多门户功能，使用外系统门户接口获取登录信息
    if (multiPortalAdapter != null && StringUtils.isNotEmpty(systemCode)) {
      loginInfo = getLoginInfoForMultiPortal(request, response, systemCode, loginInfo, (DefaultPortalAuthProvider) defaultAuthProvider);
      // 是否重定向
      if (loginInfo != null && StringUtils.isNotEmpty(loginInfo.getRedirectUrl())) {
        LoggedDTO redirectDTO = new LoggedDTO();
        redirectDTO.setLogin(false);
        redirectDTO.setLoginUrl(loginInfo.getRedirectUrl());
        redirectDTO.setRedirectUrl(loginInfo.getRedirectUrl());
        return ResultVO.success(redirectDTO);
      }
    }
    // 如果开启 API 鉴权功能，基于密钥归属的租户、用户，构造登录信息
    SimpleApiAuthDTO apiAuth = apiAuthCache.getApiAuth(request);
    if (apiAuth != null) {
      if (!apiAuth.isExpired()) {
        loginInfo = apiAuth.toLoginInfo();
      }
    }
    LoggedDTO dto = new LoggedDTO();
    dto.setLogin(loginInfo != null);
    dto.setLoginInfo(loginInfo);
    dto.setLoginUrl(defaultAuthProvider.getLoginUrl());

    if (loginInfo != null) {
      dto.setAdmin(SessionUtil.isSuperAdmin(loginInfo.getUserId()));
      // 补充签名需要的参数
      addSignParams(dto);
    }
    return ResultVO.success(dto);
  }

  /**
   * 获取多门户的登录信息
   */
  @Nullable
  private LoginInfo getLoginInfoForMultiPortal(HttpServletRequest request, HttpServletResponse response,
                                               String systemCode, @Nullable LoginInfo loginInfo,
                                               DefaultPortalAuthProvider defaultPortalAuthProvider) {
    IAuthProvider authProvider = multiPortalAdapter.getAuthProvider(systemCode);
    // 外系统的 sessionId
    String sessionId = authProvider.getSessionId(request);
    // 重定向地址
    String redirectUrl = authProvider.getRedirectUrl(request);
    // 如果没有外系统的 sessionId, 说明外系统退出登录了
    if (StringUtils.isEmpty(sessionId)) {
      // 本系统如果有登录信息需要退出登录
      if (loginInfo != null) {
        defaultPortalAuthProvider.logout(request, response);
      }
      if (StringUtils.isNotEmpty(redirectUrl)) {
        return LoginInfo.builder().redirectUrl(redirectUrl).build();
      }
      return null;
    }
    // 如果有本系统的登录信息，且外系统的 sessionId 没变化，直接返回
    else if (loginInfo != null && systemCode.equals(loginInfo.getSystemCode()) && Objects.equals(sessionId, loginInfo.getExtSessionId())) {
      return loginInfo;
    }

    // 如果没有登录信息，或者外系统的 sessionId 变化了，需要重新获取登录信息
    if (loginInfo != null) {
      // 删除本系统的登录信息
      defaultPortalAuthProvider.deleteLoginInfo(loginInfo.getToken());
    }
    // 获取外系统的登录信息
    LoginInfo newLoginInfo = authProvider.getLoginInfo(request);
    if (newLoginInfo != null) {
      // authProvider 中可能做了缓存，修改前拷贝一份以避免影响缓存
      newLoginInfo = newLoginInfo.toBuilder().build();
      newLoginInfo.setSystemCode(systemCode);
      newLoginInfo.setExtSessionId(sessionId);
      // 同步用户表（新增或更新用户信息）
      multiPortalAdapter.syncUser(newLoginInfo, systemCode, authProvider.getDefaultRole());
      // 生成本系统的 session, 避免频繁调用外系统接口、频繁同步用户表
      defaultPortalAuthProvider.saveLoginInfo(request, response, newLoginInfo, request.getParameter("basePath"));
      // 保存对接门户会话映射关系
      authProvider.saveExtSessionMapping(newLoginInfo);
    }
    return newLoginInfo;
  }

  /**
   * 补充签名需要的参数
   * 该接口用于外系统门户登录之后生成签名使用的，平台的默认门户走的是 info.js 接口
   */
  private void addSignParams(LoggedDTO loggedDTO) {
    loggedDTO.setSecretKey(SignUtil.getUserIdCookieName());
    loggedDTO.setSecretValue(loggedDTO.getLoginInfo().getUserId());
    loggedDTO.setServerTime(System.currentTimeMillis());
    loggedDTO.setSecurityMode(SystemParameter.SIGN_SECURITY_MODE.getValueFromDb());
  }

  @IgnoreSign
  @IgnoreSession
  @GetMapping("single")
  @Operation(summary = "单点访问指定页面")
  public void single(@RequestParam("accessToken") String accessToken, @RequestParam(value = "redirect", defaultValue = "/") String redirect,
                     @RequestParam Map<String, String> queryParams, HttpServletRequest request, HttpServletResponse response) throws IOException {
    singleLoginService.single(accessToken, redirect, queryParams, request, response);
  }

}
