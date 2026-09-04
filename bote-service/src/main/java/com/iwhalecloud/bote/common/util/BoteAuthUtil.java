package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bassc.basiccenter.service.ILitchiAuthLogService;
import com.iwhalecloud.bassc.basiccenter.service.impl.LitchiAuthLogServiceImpl;
import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * 鉴权服务辅助工具
 *
 * @author tingyun.wang
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class BoteAuthUtil {
  private static final Logger logger = LoggerFactory.getLogger(BoteAuthUtil.class);

  private static final ILitchiAuthLogService authLogService = SpringUtil.getBean(ILitchiAuthLogService.class);
  private static final ApiAuthCache apiAuthCache = SpringUtil.getBean(ApiAuthCache.class);
  private static final SecurityContextRepository securityContextRepository = SpringUtil.getBean(SecurityContextRepository.class);

  /** 门户systemCode和用户编码分割符 */
  public static final String SYSTEM_CODE_USER_SEP = ":";

  private BoteAuthUtil() {
  }

  /**
   * 将用户信息添加到鉴权里面，同时重新生成新的 session
   * 该接口只提供给第三方门户登录时使用，博特自身的门户登录不走这个接口
   *
   * @param loginInfo 用户信息
   * @return sessionId
   */
  public static String setInfoAndRebuildSession(LoginInfo loginInfo, HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    session = request.getSession(true);
    // 添加用户信息到鉴权
    String uniqueUserName = buildUniqueUserName(loginInfo);
    User user = new User(uniqueUserName, "", true, true, true, true, new ArrayList<>());
    UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(user, null,
      user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authResult);
    // Spring Security 6 中需要手动保存 SecurityContext
    securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
    // 添加登录日志
    saveLoginLog(uniqueUserName);
    return session.getId();
  }

  /**
   * 构建唯一用户名称
   */
  private static String buildUniqueUserName(LoginInfo loginInfo) {
    // 如果存量数据中，有些门户不按协议规范传用户编码的情况，则使用 userId 作为用户标识
    String userName = StringUtils.isNotEmpty(loginInfo.getUserName()) ? loginInfo.getUserName() : String.valueOf(loginInfo.getUserId());
    // 使用 systemCode 构建唯一复合用户名
    String systemCode = loginInfo.getSystemCode();
    return StringUtils.isEmpty(systemCode) ? userName : systemCode + SYSTEM_CODE_USER_SEP + userName;
  }

  /**
   * 保存登录日志
   */
  private static void saveLoginLog(String username) {
    try {
      TransactionUtil.executeNew(() -> authLogService.addAuthLog(LitchiAuthLogServiceImpl.LitchiLogEvent.LOGIN_SUCCESS, username));
    }
    catch (Exception e) {
      logger.error("Failed to save login log. err={}", ExpUtil.getMsg(e));
    }
  }

  /**
   * 是否跳过鉴权：
   * 不能走鉴权框架默认鉴权以及不做签名校验的请求方式请在此添加
   *
   * @param request 请求对象
   * @return 是否跳过鉴权结果
   */
  public static boolean shouldSkipAuth(HttpServletRequest request) {
    // API 鉴权令牌
    if (apiAuthCache.getApiAuth(request) != null) {
      return true;
    }
    // 多门户CODE的鉴权模式,目前百应系统会传入
    String systemCode = request.getHeader(BaseConsts.HEADER_SYSTEM_CODE);
    if (StringUtils.isNotEmpty(systemCode)) {
      return true;
    }
    // 后续更多的自定义鉴权请在此添加
    return false;
  }

}
