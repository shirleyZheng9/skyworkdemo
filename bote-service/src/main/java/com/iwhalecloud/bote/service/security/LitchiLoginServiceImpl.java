package com.iwhalecloud.bote.service.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bassc.basiccenter.exception.PortalCustomLoginException;
import com.iwhalecloud.bassc.basiccenter.service.ILitchiAuthLogService;
import com.iwhalecloud.bassc.basiccenter.service.ILitchiLoginService;
import com.iwhalecloud.bassc.basiccenter.service.impl.LitchiAuthLogServiceImpl;
import com.iwhalecloud.bassc.basiccenter.util.ext.HttpUtils;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.BoteAuthUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ExternalPortalUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.config.properties.KnowledgeGraphProperties;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.sms.SmsCodeDTO;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.portal.MultiPortalAdapter;
import com.iwhalecloud.bote.portal.adapter.CasPortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 登录鉴权服务
 *
 * @author tingyun.wang
 * @since 2025-07-03
 */
@Service
public final class LitchiLoginServiceImpl implements ILitchiLoginService {

  private static final Logger logger = LoggerFactory.getLogger(LitchiLoginServiceImpl.class);
  private final UserManageMapper userManageMapper;
  private final PasswordEncoder passwordEncoder;
  private final DefaultPortalProperties properties;
  private final ICacheClient cacheClient;
  private final ILitchiAuthLogService authLogService;
  private final MultiPortalAdapter multiPortalAdapter;
  private final KnowledgeGraphProperties knowledgeGraphProperties;

  public LitchiLoginServiceImpl(UserManageMapper userManageMapper, PasswordEncoder passwordEncoder, DefaultPortalProperties properties,
    CacheFactory cacheFactory, ILitchiAuthLogService authLogService, ObjectProvider<MultiPortalAdapter> multiPortalAdapterObjectProvider,
    ObjectProvider<KnowledgeGraphProperties> knowledgeGraphProperties) {
    this.userManageMapper = userManageMapper;
    this.passwordEncoder = passwordEncoder;
    this.properties = properties;
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LOGIN);
    this.authLogService = authLogService;
    this.multiPortalAdapter = multiPortalAdapterObjectProvider.getIfAvailable();
    this.knowledgeGraphProperties = knowledgeGraphProperties.getIfAvailable();
  }

  /** 将 sessionId保存在当前线程中 */
  private static final ThreadLocal<Pair<String, String>> sessionIdThreadLocal = new ThreadLocal<>();

  @Override
  public String onLoginHandlerReturnPwd(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
    // 获取登录用户信息
    User user = (User) userDetails;
    String username = user.getUsername();
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    String password = AesUtil.aesDecrypt(authentication.getCredentials().toString(), encryptionKey);
    UserEntity userInfo = getUserInfoByCode(username);
    // 检查账号是否有效
    checkUserValid(userInfo);
    // 账号密码校验
    int loginFailCount = Optional.ofNullable(userInfo.getLoginFailCount()).orElse(0);
    if (!passwordEncoder.matches(password, userInfo.getPassword())) {
      // 更新账号登录错误次数
      modifyLoginFailCount(userInfo.getUserId(), loginFailCount + 1);
      throw new BadCredentialsException("用户名或密码错误");
    }
    // 校验短信验证码
    checkSmsCode(userInfo);
    // 登录成功，重置用户登录连续错误次数
    if (loginFailCount > 0) {
      modifyLoginFailCount(userInfo.getUserId(), 0);
    }
    // 成功验证短信验证码之后需要移除，防止重复使用
    removeSmsCode(userInfo.getUserId());
    return password;
  }

  /**
   * 校验短信验证码
   */
  private void checkSmsCode(UserEntity userInfo) {
    if (!SystemParameter.SMS_CODE_ENABLED.getBooleanValueFromDb()) {
      return;
    }
    // 获取请求参数中的短信验证码
    HttpServletRequest request = HttpUtils.getRequest();
    String smsCode = request.getParameter("smsCode");
    if (StringUtils.isEmpty(smsCode)) {
      throw new PortalCustomLoginException("短信验证码不能为空");
    }
    // 从缓存中获取用户对应的短信验证码信息
    String smsCodeJson = cacheClient.opsForValue().get(BaseConsts.SMS_CODE_PREFIX + userInfo.getUserId());
    if (StringUtils.isEmpty(smsCodeJson)) {
      throw new PortalCustomLoginException("短信验证码不存在或已失效");
    }
    SmsCodeDTO smsCodeDTO = JsonUtil.parseJson(smsCodeJson, SmsCodeDTO.class);
    if (smsCodeDTO == null) {
      throw new PortalCustomLoginException("短信验证码解析出错，请联系管理员");
    }
    // 校验用户短信验证码
    if (!smsCode.equals(smsCodeDTO.getSmsCode())) {
      throw new PortalCustomLoginException("短信验证码错误");
    }
  }

  /**
   * 删除短信验证码
   */
  private void removeSmsCode(Long userId) {
    cacheClient.delete(BaseConsts.SMS_CODE_PREFIX + userId);
  }

  /**
   * 检查用户是否有效
   */
  private void checkUserValid(UserEntity userInfo) {
    // 校验账号是否禁用
    if (BaseConsts.USER_STATE_DISABLE.equals(userInfo.getUserState())) {
      throw new DisabledException("当前账号已被禁用");
    }
    // 校验账号是否锁定
    if (BaseConsts.IS_LOCKED_TRUE.equals(userInfo.getIsLocked())) {
      throw new LockedException("当前账号已被锁定");
    }
    // 校验账号是否过期失效
    boolean accountNonExpired = (userInfo.getUserExpDate() == null) || userInfo.getUserExpDate().after(new Date());
    if (!accountNonExpired) {
      throw new AccountExpiredException("当前账号已过期");
    }
    // 校验密码是否过期
    Integer pwdExpDays = SystemParameter.PWD_EXP_DAYS.getIntegerValueFromDb();
    if (pwdExpDays != null && pwdExpDays > 0) {
      // 缺失密码修改时间时，强制修改密码
      Date pwdUpdatedTime = userInfo.getPwdUpdatedTime();
      if (pwdUpdatedTime == null) {
        throw new CredentialsExpiredException("密码已过期，请修改密码");
      }
      // 计算密码修改时间是否过期
      LocalDate pwdUpdateDate = pwdUpdatedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      if (LocalDate.now().isAfter(pwdUpdateDate.plusDays(pwdExpDays))) {
        throw new CredentialsExpiredException("密码已过期，请修改密码");
      }
    }
  }

  /**
   * 修改登录失败次数
   */
  private void modifyLoginFailCount(Long userId, Integer loginFailCount) {
    // 修改登录失败次数
    userManageMapper.updateUserLoginFailCount(userId, loginFailCount);
    // 如果登录次数超过阈值则锁定账户
    Integer loginLimit = SystemParameter.LOGIN_EXCEED_RETRY_LIMIT.getIntegerValueFromDb();
    if (loginLimit != null && loginFailCount >= loginLimit) {
      userManageMapper.updateUserLockState(userId, BaseConsts.IS_LOCKED_TRUE);
    }
  }

  /**
   * 根据用户编码查询用户信息
   */
  private UserEntity getUserInfoByCode(String userCode) {
    UserEntity userInfo = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, userCode);
    if (userInfo == null) {
      throw new BadCredentialsException("用户名或密码错误");
    }
    return userInfo;
  }

  @Override
  public void onLoginSuccess(User user, Map<String, Object> result) {
    // 获取用户信息
    UserEntity userInfo = getUserInfoByCode(user.getUsername());
    LoginInfo loginInfo = new LoginInfo();
    BeanUtils.copyProperties(userInfo, loginInfo);
    // 获取 sessionId
    HttpServletRequest request = HttpUtils.getRequest();
    String sessionId = request.getSession().getId();
    loginInfo.setToken(sessionId);
    // 修改响应参数
    Map<String, Object> respData = JsonUtil.convert(ResultVO.success(loginInfo), new TypeReference<Map<String, Object>>() {
    });
    result.clear();
    result.putAll(respData);
    // 添加用户ID的cookie: 用于签名
    addUserIdCookie(String.valueOf(loginInfo.getUserId()), request.getParameter("basePath"), false);
    // 添加用户登录信息Cookie到缓存，用于平台接口鉴权
    cacheClient.opsForValue().set(sessionId, JsonUtil.toJsonStringCompact(loginInfo), properties.getTimeout());
    // 写入登录日志
    saveLog(LitchiAuthLogServiceImpl.LitchiLogEvent.LOGIN_SUCCESS, userInfo);
  }

  @SuppressFBWarnings({ "HTTPONLY_COOKIE", "INSECURE_COOKIE" })
  private void addUserIdCookie(String userId, String basePath, boolean isDelete) {
    Cookie userIdCookie = new Cookie(SignUtil.getUserIdCookieName(), userId);
    userIdCookie.setPath(StringUtils.defaultIfEmpty(basePath, "/"));
    userIdCookie.setHttpOnly(false);
    userIdCookie.setSecure(SpringUtil.getProperty("app.security.cookieSecure", Boolean.class, false));
    if (isDelete) {
      userIdCookie.setMaxAge(0);
    }
    HttpUtils.getResponse().addCookie(userIdCookie);
  }

  @Override
  public void onLogoutHandler(User user) {
    // 获取 sessionId
    HttpServletRequest request = HttpUtils.getRequest();
    String sessionId = request.getSession().getId();
    // 删除 session 缓存信息
    if (StringUtils.isNotEmpty(sessionId)) {
      sessionIdThreadLocal.set(Pair.of(sessionId, cacheClient.opsForValue().get(sessionId)));
      cacheClient.delete(sessionId);
    }
    UserEntity userEntity = getUserInfoForLogout(user.getUsername());
    // 移除用户ID的cookie
    if (userEntity.getUserId() != null) {
      addUserIdCookie(String.valueOf(userEntity.getUserId()), request.getParameter("basePath"), true);
    }
    // 删除知识图谱 cookie
    if (knowledgeGraphProperties != null) {
      String basePath = request.getParameter("basePath");
      Cookie kgCookie = new Cookie(knowledgeGraphProperties.getCookieName(), null);
      kgCookie.setPath(basePath);
      kgCookie.setHttpOnly(true);
      kgCookie.setMaxAge(0);
      HttpUtils.getResponse().addCookie(kgCookie);
    }
    // 写入日志
    saveLog(LitchiAuthLogServiceImpl.LitchiLogEvent.LOGOUT_SUCCESS, userEntity);
  }

  @Override
  public void onLogoutSuccess(User user, Map<String, Object> result) {
    // 修改响应参数
    Map<String, Object> logoutData = JsonUtil.convert(ResultVO.success(), new TypeReference<Map<String, Object>>() {
    });
    result.clear();
    result.putAll(logoutData);
    if (!user.getUsername().contains(":") || multiPortalAdapter == null) {
      return;
    }
    // 接入门户重定向
    String systemCode = Arrays.asList(user.getUsername().split(":")).get(0);
    IAuthProvider authProvider = multiPortalAdapter.getAuthProvider(systemCode);
    if (authProvider instanceof CasPortalAuthProvider) {
      Pair<String, String> sessionPair = sessionIdThreadLocal.get();
      String redirectUrl = ExternalPortalUtil.getCasRedirectUrl(sessionPair.getLeft(), "logout");
      if (StringUtils.isNotEmpty(redirectUrl)) {
        ExternalPortalUtil.clearCasSystemCache(sessionPair.getRight());
        result.put("resultObject", redirectUrl);
      }
    }
    else {
      HttpServletRequest request = ServletUtil.getRequest();
      Assert.notNull(request, "获取不到 HTTP 请求对象");
      String redirectUrl = authProvider.getRedirectUrl(request);
      result.put("resultObject", redirectUrl);
    }
  }

  /**
   * 保存日志
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  private void saveLog(LitchiAuthLogServiceImpl.LitchiLogEvent event, UserEntity userInfo) {
    try {
      TransactionUtil.executeNew(() -> authLogService.addAuthLog(event, userInfo.getUserName(), userInfo.getUserId(), userInfo.getRealName(), null));
    }
    catch (Exception e) {
      logger.error("Failed to save log. err={}", ExpUtil.getMsg(e));
    }
  }

  /**
   * 获取登出用户信息
   */
  private UserEntity getUserInfoForLogout(String userCode) {
    UserEntity userInfo;
    if (StringUtils.isEmpty(userCode)) {
      return new UserEntity();
    }

    // 第三方门户的用户编码会包含 BoteAuthUtil.SYSTEM_CODE_USER_SEP 分割符，该分隔符不会出现在默认门户的用户编码上
    if (userCode.contains(BoteAuthUtil.SYSTEM_CODE_USER_SEP)) {
      String[] split = userCode.split(BoteAuthUtil.SYSTEM_CODE_USER_SEP);
      if (split.length >= 2) {
        String systemCode = split[0];
        String username = split[1];
        userInfo = userManageMapper.getUserByCode(systemCode, username);
      }
      else {
        // 格式不正确的第三方门户编码，回退到默认门户处理
        userInfo = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, userCode);
      }
    }
    else {
      // 平台默认门户
      userInfo = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, userCode);
    }

    // 确保用户信息不为空，日志至少记录用户编码
    if (userInfo == null) {
      userInfo = new UserEntity();
      userInfo.setUserName(userCode);
    }

    return userInfo;
  }

}
