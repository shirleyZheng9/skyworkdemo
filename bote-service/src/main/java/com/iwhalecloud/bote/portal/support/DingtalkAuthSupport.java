package com.iwhalecloud.bote.portal.support;

import com.iwhalecloud.bote.cache.ApiAuthCache;
import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.dto.base.SimpleApiAuthDTO;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import com.iwhalecloud.bote.portal.MultiPortalAdapter;
import com.iwhalecloud.bote.portal.adapter.DefaultPortalAuthProvider;
import com.iwhalecloud.bote.portal.config.properties.DingTalkLoginProperties;
import com.iwhalecloud.bote.portal.dto.DingTalkUserDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 提供钉钉登录认证方法, 作为平台自身的一种认证能力
 *
 * @author Aiqing
 * @since 2025/6/4
 */
public class DingtalkAuthSupport {

  private static final Logger logger = LoggerFactory.getLogger(DingtalkAuthSupport.class);

  private final DefaultPortalAuthProvider portalAuthProvider;
  private final DingTalkLoginProperties dingTalkLoginProperties;
  private final DingTalkDockClient dingTalkDockClient;
  private final ApiAuthCache apiAuthCache;
  private final MultiPortalAdapter multiPortalAdapter;
  private final UserManageMapper userManageMapper;

  public DingtalkAuthSupport(DingTalkLoginProperties properties,
                             DingTalkDockClient dingTalkDockClient,
                             DefaultPortalAuthProvider portalAuthProvider,
                             ApiAuthCache apiAuthCache,
                             MultiPortalAdapter multiPortalAdapter,
                             UserManageMapper userManageMapper) {
    this.dingTalkLoginProperties = properties;
    this.dingTalkDockClient = dingTalkDockClient;
    this.portalAuthProvider = portalAuthProvider;
    this.apiAuthCache = apiAuthCache;
    this.multiPortalAdapter = multiPortalAdapter;
    this.userManageMapper = userManageMapper;
  }

  /**
   * 处理钉钉登录
   *
   * @param request    request
   * @param response   response
   * @param code       钉钉认证临时code
   * @param innerAuth  临时code是否是免登获取
   * @param systemCode 门户编码
   * @return 登录成功信息
   */
  public ResultVO<LoginInfo> login(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Long tenantId,
                                   String code,
                                   Boolean innerAuth,
                                   String systemCode) {
    LoginInfo loginInfo = getUserInfoFromDingTalk(code, Boolean.TRUE.equals(innerAuth), systemCode, tenantId);
    portalAuthProvider.saveLoginInfo(request, response, loginInfo, request.getParameter("basePath"));
    return ResultVO.success(loginInfo);
  }

  /**
   * 构造条件钉钉oauth登录页面的地址
   * 参考链接 <a href="https://open.dingtalk.com/document/orgapp/tutorial-obtaining-user-personal-information">钉钉官方文档</a>
   * <p https://login.dingtalk.com/oauth2/auth?redirect_uri={0}&response_type=code&client_id=dingxxxxxxx&scope=openid&state=dddd&prompt=consent</p>
   *
   * @return 登录页面地址
   */
  public String getLoginUrl() {
    // 构造登录链接
    String redirectUrl = this.dingTalkLoginProperties.getRedirectUrl();
    return UriComponentsBuilder.fromUriString("https://login.dingtalk.com/oauth2/auth")
      .queryParam("redirect_uri", encodeUrl(redirectUrl))
      .queryParam("response_type", "code")
      .queryParam("client_id", dingTalkLoginProperties.getClientId())
      .queryParam("scope", "openid")
      .queryParam("prompt", "consent")
      .build()
      .toUriString();
  }

  private String encodeUrl(String url) {
    return URLEncoder.encode(url, StandardCharsets.UTF_8);
  }

  private LoginInfo getUserInfoFromDingTalk(String code, boolean innerAuth, String systemCode, Long tenantId) {
    Boolean devMode = dingTalkLoginProperties.getDevMode();
    if (Boolean.TRUE.equals(devMode)) {
      // 模拟授权token
      SimpleApiAuthDTO simpleApiAuthDTO = apiAuthCache.get(code);
      if (Objects.isNull(simpleApiAuthDTO)) {
        throw new BssException("用户不存在");
      }
      return simpleApiAuthDTO.toLoginInfo();
    }
    if (innerAuth) {
      return loginForDingTalkInnerAuth(code, systemCode, tenantId);
    }
    return loginForBrowserAuth(code, systemCode, tenantId);
  }

  /**
   * 在外部浏览器中钉钉授权后，根据临时授权码获取钉钉用户信息，关联本地用户（无用户进行新增）
   *
   * @param code       临时授权码
   * @param systemCode 关联用户门户编码
   * @param tenantId   当前访问
   * @return 用户登录信息
   */
  private LoginInfo loginForBrowserAuth(String code, String systemCode, Long tenantId) {
    // 1. 根据 authCode，调用服务端获取用户token接口，获取用户个人token。
    String accessToken = dingTalkDockClient.getUserAccessToken(code);
    // 2. 根据用户个人token，调用获取用户通讯录个人信息接口，获取授权用户个人信息。
    DingTalkUserDTO userDTO = dingTalkDockClient.getUserInfoWithUserAccessToken(accessToken);
    LoginInfo loginInfo = buildLoginInfo(userDTO, systemCode, tenantId);
    multiPortalAdapter.syncUser(loginInfo, systemCode, PrivConsts.ROLE_READONLY);
    return loginInfo;
  }

  private LoginInfo loginForDingTalkInnerAuth(String code, String systemCode, Long tenantId) {
    String accessToken = dingTalkDockClient.getAccessToken();
    String userid = dingTalkDockClient.getDingTalkUserIdWithAutoAuthCode(code, accessToken);
    // 查询用户是否已存在
    UserEntity userExist = userManageMapper.getUserBySystemCodeAndExtUserId(systemCode, userid);
    if (Objects.nonNull(userExist)) {
      return portalAuthProvider.buildLoginInfo(userExist);
    }
    DingTalkUserDTO dingTalkUserInfo = dingTalkDockClient.getDingTalkUserInfo(userid, accessToken);
    logger.atDebug().setMessage("get user from dingTalk, result:{}").addArgument(() -> JsonUtil.toJsonString(dingTalkUserInfo)).log();
    LoginInfo loginInfo = buildLoginInfo(dingTalkUserInfo, systemCode, tenantId);
    multiPortalAdapter.syncUser(loginInfo, systemCode, PrivConsts.ROLE_READONLY);
    return loginInfo;
  }

  /**
   * 构造登录信息
   */
  private LoginInfo buildLoginInfo(DingTalkUserDTO userInfo, String systemCode, Long tenantId) {
    LoginInfo loginInfo = new LoginInfo();
    String userName = Stream.of(userInfo.getJobNumber(), userInfo.getMobile(), userInfo.getMobile(), userInfo.getUnionId())
      .filter(StringUtils::isNotBlank)
      .findFirst()
      .orElse(UUIDUtils.randomFormatUuid());

    loginInfo.setUserName(userName);
    loginInfo.setUserId(Long.valueOf(userInfo.getUserId()));
    loginInfo.setRealName(userInfo.getNick());
    loginInfo.setEmail(userInfo.getEmail());
    loginInfo.setPhoneNo(userInfo.getMobile());
    loginInfo.setSystemCode(systemCode);
    loginInfo.setDefaultTenantId(tenantId);
    return loginInfo;
  }
}
