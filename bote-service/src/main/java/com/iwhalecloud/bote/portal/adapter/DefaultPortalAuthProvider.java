package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.BoteAuthUtil;
import com.iwhalecloud.bote.common.util.DataMaskUtil;
import com.iwhalecloud.bote.common.util.SendSmsCodeUtil;
import com.iwhalecloud.bote.common.util.SignUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.sms.SmsCodeDTO;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.config.properties.DefaultPortalProperties;
import com.iwhalecloud.bote.service.base.IEditLockService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * 博特门户鉴权提供者实现
 *
 * @author bianjp
 * @since 2024-08-22
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DefaultPortalAuthProvider extends AbstractAuthProvider {
  private final DefaultPortalProperties properties;
  private final ICacheClient cacheClient;
  private final UserManageMapper userManageMapper;
  private final PasswordEncoder passwordEncoder;
  private final IEditLockService editLockService;

  /**
   * 登录
   */
  public ResultVO<LoginInfo> login(HttpServletRequest request, HttpServletResponse response, String userName, String password) {
    UserEntity userInfo = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, userName);
    if (userInfo == null) {
      return BaseErrorConstant.USER_NAME_NOT_EXISTS.toResult(userName);
    }
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    if (!passwordEncoder.matches(AesUtil.aesDecrypt(password, encryptionKey), userInfo.getPassword())) {
      return BaseErrorConstant.LOGIN_FAIL.toResult();
    }
    LoginInfo loginInfo = buildLoginInfo(userInfo);
    saveLoginInfo(request, response, loginInfo, request.getParameter("basePath"));
    return ResultVO.success(loginInfo);
  }

  /**
   * 保存登录信息，设置 cookie
   */
  public void saveLoginInfo(HttpServletRequest request, HttpServletResponse response, LoginInfo loginInfo, @Nullable String basePath) {
    String sessionId = BoteAuthUtil.setInfoAndRebuildSession(loginInfo, request, response);
    loginInfo.setToken(sessionId);
    response.addCookie(buildCookie(SignUtil.getUserIdCookieName(), String.valueOf(loginInfo.getUserId()), basePath, false, false));
    cacheClient.opsForValue().set(sessionId, JsonUtil.toJsonStringCompact(loginInfo), properties.getTimeout());
  }

  /**
   * 登录用户修改个人信息，更新 session 信息
   */
  public void updateLoginInfo(String sessionId, LoginInfo loginInfo) {
    cacheClient.opsForValue().set(sessionId, JsonUtil.toJsonStringCompact(loginInfo), properties.getTimeout());
  }

  /**
   * 注销登录
   */
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    String sessionId = getSessionId(request);
    if (StringUtils.isNotEmpty(sessionId)) {
      LoginInfo loginInfo = getLoginInfo(sessionId);
      String basePath = request.getParameter("basePath");
      cacheClient.delete(sessionId);
      // 失效 HTTP 会话，由 Spring Session 经 CookieSerializer 用固定 path 删除 BOTE_SESSION cookie
      // （与写入同源，path 天然一致；同时清理 SecurityContext 等会话级状态）
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
      // 兼容历史残留：按请求 basePath 再补一个删除头，清理旧 path 上的 BOTE_SESSION 副本
      response.addCookie(buildCookie(properties.getCookieName(), null, basePath, true, true));
      response.addCookie(buildCookie(SignUtil.getUserIdCookieName(), null, basePath, false, false));
      // 退出登录释放编辑锁
      editLockService.releaseAllKey(loginInfo == null ? null : loginInfo.getUserId());
    }
  }

  /**
   * 删除登录信息
   */
  public void deleteLoginInfo(String sessionId) {
    cacheClient.delete(sessionId);
  }

  /**
   * 构造 session cookie
   */
  @SuppressFBWarnings({ "INSECURE_COOKIE", "HTTPONLY_COOKIE" })
  private Cookie buildCookie(String cookieName, @Nullable String cookieValue, @Nullable String basePath, boolean delete, boolean httpOnly) {
    if (StringUtils.isEmpty(basePath)) {
      basePath = "/";
    }
    Cookie cookie = new Cookie(cookieName, cookieValue);
    cookie.setPath(basePath);
    cookie.setHttpOnly(httpOnly);
    if (delete) {
      cookie.setMaxAge(0);
    }
    return cookie;
  }

  /**
   * 构造登录信息
   */
  public LoginInfo buildLoginInfo(UserEntity userInfo) {
    LoginInfo loginInfo = new LoginInfo();
    loginInfo.setUserName(userInfo.getUserName());
    loginInfo.setUserId(userInfo.getUserId());
    loginInfo.setRealName(userInfo.getRealName());
    loginInfo.setUserType(userInfo.getUserType());
    loginInfo.setDefaultTenantId(userInfo.getDefaultTenantId());
    loginInfo.setEmail(userInfo.getEmail());
    loginInfo.setPhoneNo(userInfo.getPhoneNo());
    loginInfo.setSystemCode(userInfo.getSystemCode());
    return loginInfo;
  }

  @Override
  @Nullable
  public String getLoginUrl() {
    return null;
  }

  @Override
  protected String getCookieName() {
    return properties.getCookieName();
  }

  @Override
  @Nullable
  protected String getParamName() {
    return null;
  }

  @Override
  @Nullable
  public LoginInfo getLoginInfo(String sessionId) {
    String value = cacheClient.opsForValue().get(sessionId);
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    LoginInfo loginInfo = JsonUtil.parseJsonRequired(value, LoginInfo.class);
    // 续期 session
    cacheClient.expire(sessionId, properties.getTimeout().getSeconds(), TimeUnit.SECONDS);
    return loginInfo;
  }

  /**
   * 发送短信验证码
   *
   * @param username 用户编码
   * @param password 用户密码
   * @return 用户的脱敏手机号码
   */
  public String sendSmsCode(String username, String password) {
    Assert.hasText(username, "用户名不能为空");
    Assert.hasText(password, "用户密码不能为空");
    if (!SystemParameter.SMS_CODE_ENABLED.getBooleanValueFromDb()) {
      throw new BssException("短信验证码功能未开启");
    }
    // 获取用户信息
    UserEntity userInfo = userManageMapper.getUserByCode(BaseConsts.PORTAL_SYSTEM_CODE_DEFAULT, username);
    if (userInfo == null) {
      throw new BssException("用户不存在");
    }
    // 校验账号密码
    String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    if (!passwordEncoder.matches(AesUtil.aesDecrypt(password, encryptionKey), userInfo.getPassword())) {
      throw new BssException("账号密码错误，请检查用户名和密码是否正确");
    }
    // 检查用户是否有配置手机号码
    if (StringUtils.isEmpty(userInfo.getPhoneNo())) {
      throw new BssException("验证码发送失败，用户未绑定手机号码");
    }
    // 校验验证码发送请求
    checkSendSmsCodeRequest(userInfo.getUserId());
    // 生成6位短信验证码
    String smsCode = RandomStringUtils.secure().nextNumeric(6);
    Integer smsCodeTimeout = SystemParameter.SMS_CODE_TIMEOUT.getIntegerValueFromDb();
    String smsCodeTemplate = SystemParameter.SMS_CODE_TEMPLATE.getValueFromDb();
    String smsContent = String.format(smsCodeTemplate, smsCode, smsCodeTimeout);
    // 发送短信验证码并保存到缓存中
    SendSmsCodeUtil.sendSmsCode(userInfo.getPhoneNo(), smsContent);
    String smsCodeJson = JsonUtil.toJsonStringCompact(new SmsCodeDTO(smsCode));
    cacheClient.opsForValue().set(BaseConsts.SMS_CODE_PREFIX + userInfo.getUserId(), smsCodeJson, Duration.ofMinutes(smsCodeTimeout));
    logger.info("短信验证码发送成功: userId={}, username={}, smsCode={}", userInfo.getUserId(), userInfo.getUserName(), smsCode);
    // 返回脱敏后的手机号码
    return DataMaskUtil.maskPhoneKeepBoth(userInfo.getPhoneNo());
  }

  /**
   * 校验验证码发送请求，比如发送间隔，防止短信轰炸
   */
  private void checkSendSmsCodeRequest(Long userId) {
    // 检查是否存在用户对应的短信验证码
    String smsCodeJson = cacheClient.opsForValue().get(BaseConsts.SMS_CODE_PREFIX + userId);
    if (StringUtils.isEmpty(smsCodeJson)) {
      return;
    }
    // 解析验证码信息内容
    SmsCodeDTO smsCodeDTO = JsonUtil.parseJson(smsCodeJson, SmsCodeDTO.class);
    if (smsCodeDTO == null || smsCodeDTO.getRequestTime() == null) {
      logger.error("短信验证码解析出错，smsCodeJson = {}", smsCodeJson);
      throw new BssException("短信验证码解析出错，请联系管理员");
    }
    // 校验发送间隔是否超过 1 分钟，防止短信轰炸
    if ((System.currentTimeMillis() - smsCodeDTO.getRequestTime().getTime()) <= 60000) {
      logger.info("短信发送过于频繁，smsCodeJson = {}", smsCodeJson);
      throw new BssException("短信发送过于频繁，请60秒后再试");
    }
  }

}
