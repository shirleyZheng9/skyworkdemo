package com.iwhalecloud.bote.portal;

import com.iwhalecloud.bote.dto.portal.LoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;

/**
 * 鉴权提供者接口
 *
 * @author bianjp
 * @since 2020-11-10
 */
public interface IAuthProvider {

  /**
   * 获取登录地址
   *
   * @return 登录地址
   */
  @Nullable
  String getLoginUrl();

  /**
   * 根据请求对象获取会话 ID
   *
   * @param request 请求对象
   * @return 会话 ID
   */
  @Nullable
  String getSessionId(HttpServletRequest request);

  /**
   * 根据 sessionId 获取登录信息
   *
   * @param sessionId session ID
   * @return 登录信息。没有登录时返回 null
   */
  @Nullable
  LoginInfo getLoginInfo(String sessionId);

  /**
   * 根据请求对象获取登录信息
   *
   * @param request 请求对象
   * @return 登录信息。没有登录时返回 null
   */
  @Nullable
  LoginInfo getLoginInfo(HttpServletRequest request);

  /**
   * 获取新用户的默认用户角色
   *
   * @return 用户角色
   */
  @Nullable
  String getDefaultRole();

  /**
   * 获取门户重定向地址
   *
   * @param request 请求对象
   * @return 重定向地址，有值时表示需要重定向
   */
  @Nullable
  String getRedirectUrl(HttpServletRequest request);

  /**
   * 保存对接门户会话映射关系
   *
   * @param loginInfo 登录信息
   */
  void saveExtSessionMapping(LoginInfo loginInfo);
}
