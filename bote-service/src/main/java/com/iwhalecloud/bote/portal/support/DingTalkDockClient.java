package com.iwhalecloud.bote.portal.support;

import com.aliyun.dingtalkcontact_1_0.Client;
import com.aliyun.dingtalkcontact_1_0.models.GetUserHeaders;
import com.aliyun.dingtalkcontact_1_0.models.GetUserResponse;
import com.aliyun.dingtalkcontact_1_0.models.GetUserResponseBody;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponseBody;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetUserTokenResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.portal.config.properties.DingTalkLoginProperties;
import com.iwhalecloud.bote.portal.dto.DingTalkUserDTO;
import com.iwhalecloud.bote.portal.support.model.DingTalkResponse;
import com.iwhalecloud.bote.portal.support.model.OapiUserGetbyunionidRequest;
import com.iwhalecloud.bote.portal.support.model.OapiUserGetbyunionidResponse;
import com.iwhalecloud.bote.portal.support.model.OapiV2UserGetRequest;
import com.iwhalecloud.bote.portal.support.model.OapiV2UserGetResponse;
import com.iwhalecloud.bote.portal.support.model.OapiV2UserGetuserinfoRequest;
import com.iwhalecloud.bote.portal.support.model.UserGetByCodeResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * 对接钉钉接口
 *
 * @author Aiqing
 * @since 2024/3/5
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public final class DingTalkDockClient {

  private static final Logger logger = LoggerFactory.getLogger(DingTalkDockClient.class);

  private static final String DING_TALK_ACCESS_TOKEN_KEY = "PORTAL:dingTalk:accessToken";

  private final DingTalkLoginProperties dingTalkLoginProperties;
  private final ICacheClient cacheClient;

  private final com.aliyun.dingtalkoauth2_1_0.Client oauthClient;

  public DingTalkDockClient(DingTalkLoginProperties dingTalkLoginProperties, ICacheClient cacheClient) {
    this.dingTalkLoginProperties = dingTalkLoginProperties;
    this.cacheClient = cacheClient;
    Config config = new Config();
    config.protocol = "https";
    config.regionId = "central";
    try {
      this.oauthClient = new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }
    catch (Exception e) {
      throw new IllegalStateException("init dingtalkoauth2_1_0 client error", e);
    }
  }

  private static HttpHeaders getJsonReqHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  private Client createContractClient() {
    Config config = new Config();
    config.protocol = "https";
    config.regionId = "central";
    try {
      return new Client(config);
    }
    catch (Exception e) {
      throw new IllegalStateException("init dingtalkcontact_1_0 client error", e);
    }
  }

  /**
   * 获取API调用的accessToken
   *
   * @return 应用access token
   */
  public String getAccessToken() {
    if (Objects.nonNull(cacheClient)) {
      String tokenCached = cacheClient.opsForValue().get(DING_TALK_ACCESS_TOKEN_KEY);
      if (!StringUtils.isEmpty(tokenCached)) {
        return tokenCached;
      }
    }
    GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest();
    getAccessTokenRequest.setAppKey(dingTalkLoginProperties.getClientId());
    getAccessTokenRequest.setAppSecret(dingTalkLoginProperties.getClientSecret());
    try {
      GetAccessTokenResponse accessTokenResponse = this.oauthClient.getAccessToken(getAccessTokenRequest);
      GetAccessTokenResponseBody tokenBody = accessTokenResponse.getBody();
      String accessToken = tokenBody.getAccessToken();
      Long expireIn = tokenBody.getExpireIn();
      if (Objects.nonNull(cacheClient)) {
        // 缓存
        cacheClient.opsForValue().set(DING_TALK_ACCESS_TOKEN_KEY, accessToken, expireIn / 2, TimeUnit.SECONDS);
      }
      return accessToken;
    }
    catch (Exception exception) {
      processException(exception, "查询accessToken失败");
      return null;
    }
  }

  /**
   * 获取钉钉用户ID
   *
   * @param code 临时授权码
   * @param accessToken 服务端认证token
   * @return 钉钉用户ID
   */
  public String getDingTalkUserIdWithAutoAuthCode(String code, String accessToken) {
    UserGetByCodeResponse userGetByCodeResponse = this.requestDingTalkUserId(code, accessToken);
    if (Objects.isNull(userGetByCodeResponse)) {
      throw new BssException("验证钉钉用户身份失败");
    }
    return userGetByCodeResponse.getUserid();
  }

  private UserGetByCodeResponse requestDingTalkUserId(String code, String accessToken) {
    String reqUrl = dingTalkLoginProperties.getServerUrl() + "/topapi/v2/user/getuserinfo?access_token=" + accessToken;

    OapiV2UserGetuserinfoRequest req = new OapiV2UserGetuserinfoRequest();
    req.setCode(code);

    HttpHeaders headers = getJsonReqHeaders();
    ParameterizedTypeReference<DingTalkResponse<UserGetByCodeResponse>> typeRef = new ParameterizedTypeReference<>() {
    };
    DingTalkResponse<UserGetByCodeResponse> dingTalkResponse = HttpUtil.post(reqUrl, req, typeRef, headers);
    if (Objects.isNull(dingTalkResponse)) {
      throw new BssException("查询钉钉用户信息失败");
    }
    if (!dingTalkResponse.isSuccess()) {
      logger.error("免登码换取用户信息失败, code:{}, error:{}", dingTalkResponse.getErrCode(), dingTalkResponse.getErrMsg());
      throw new BssException("认证失败");
    }
    return dingTalkResponse.getResult();
  }

  public DingTalkUserDTO getDingTalkUserInfo(String userId, String accessToken) {
    String reqUrl = dingTalkLoginProperties.getServerUrl() + "/topapi/v2/user/get?access_token=" + accessToken;
    OapiV2UserGetRequest req = new OapiV2UserGetRequest();
    req.setUserid(userId);
    req.setLanguage("zh_CN");

    HttpHeaders headers = getJsonReqHeaders();
    ParameterizedTypeReference<DingTalkResponse<OapiV2UserGetResponse>> typeRef = new ParameterizedTypeReference<>() {
    };
    DingTalkResponse<OapiV2UserGetResponse> dingTalkResponse = HttpUtil.post(reqUrl, req, typeRef, headers);

    if (Objects.isNull(dingTalkResponse)) {
      throw new BssException("查询钉钉用户信息失败");
    }
    if (!dingTalkResponse.isSuccess()) {
      logger.error("查询钉钉用户信息失败， code:{}, error:{}", dingTalkResponse.getErrCode(), dingTalkResponse.getErrMsg());
      throw new BssException("查询钉钉用户信息失败");
    }

    OapiV2UserGetResponse result = dingTalkResponse.getResult();
    return convertFromUserResponse(result);
  }

  /**
   * 获取钉钉用户的userId; 一个用户，unionId是单个应用内唯一的， userId是企业内唯一的
   *
   * @param unionId unionId
   * @param accessToken 应用accessToken
   * @return 用户在钉钉的userId
   */
  public String getUserIdByUnionId(String unionId, String accessToken) {
    String reqUrl = dingTalkLoginProperties.getServerUrl() + "/topapi/user/getbyunionid?access_token=" + accessToken;
    OapiUserGetbyunionidRequest req = new OapiUserGetbyunionidRequest();
    req.setUnionid(unionId);

    HttpHeaders headers = getJsonReqHeaders();
    ParameterizedTypeReference<DingTalkResponse<OapiUserGetbyunionidResponse>> typeRef = new ParameterizedTypeReference<>() {
    };
    DingTalkResponse<OapiUserGetbyunionidResponse> dingTalkResponse = HttpUtil.post(reqUrl, req, typeRef, headers);
    if (Objects.isNull(dingTalkResponse)) {
      throw new BssException("查询钉钉用户信息失败");
    }
    if (!dingTalkResponse.isSuccess()) {
      logger.error("查询钉钉用户信息失败， unionId:{}, code:{}, error:{}", unionId, dingTalkResponse.getErrCode(), dingTalkResponse.getErrMsg());
      throw new BssException("查询钉钉用户信息失败");
    }
    OapiUserGetbyunionidResponse result = dingTalkResponse.getResult();
    return result.getUserid();
  }

  /**
   * 根据用户的访问凭证查询用户信息
   *
   * @param accessToken 访问凭证
   * @return 用户信息
   */
  public DingTalkUserDTO getUserInfoWithUserAccessToken(String accessToken) {
    GetUserHeaders userHeaders = new GetUserHeaders();
    userHeaders.xAcsDingtalkAccessToken = accessToken;
    Client client = createContractClient();
    try {
      GetUserResponse userResponse = client.getUserWithOptions("me", userHeaders, new RuntimeOptions());
      GetUserResponseBody body = userResponse.getBody();
      return convertFromUserResponse(body);
    }
    catch (Exception ex) {
      TeaException err = new TeaException(ex.getMessage(), ex);
      if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
        logger.error("查询钉钉用户信息失败, code:{}, message:{}", err.code, err.message);
      }
      else {
        logger.error("查询钉钉用户信息失败", ex);
      }
      throw new BssException("查询钉钉用户信息失败", ex);
    }
  }

  private DingTalkUserDTO convertFromUserResponse(GetUserResponseBody body) {
    DingTalkUserDTO userDTO = new DingTalkUserDTO();
    BeanUtils.copyProperties(body, userDTO);
    String unionId = body.getUnionId();
    String accessToken = this.getAccessToken();
    String userIdByUnionId = this.getUserIdByUnionId(unionId, accessToken);
    userDTO.setUserId(userIdByUnionId);
    return userDTO;
  }

  private DingTalkUserDTO convertFromUserResponse(OapiV2UserGetResponse body) {
    DingTalkUserDTO userDTO = new DingTalkUserDTO();
    userDTO.setUserId(body.getUserid());
    userDTO.setNick(body.getName());
    userDTO.setAvatarUrl(body.getAvatar());
    userDTO.setEmail(body.getEmail());
    userDTO.setMobile(body.getMobile());
    userDTO.setUnionId(body.getUnionid());
    userDTO.setJobNumber(body.getJobNumber());
    return userDTO;
  }


  private void processException(Exception exception, String message) {
    if (exception instanceof TeaException) {
      TeaException err = (TeaException) exception;
      if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
        logger.error("{}, {}", message, err.message);
        throw new BssException(err.code, err.message, exception);
      }
      throw new BssException(message, err);
    }
    TeaException err = new TeaException(exception.getMessage(), exception);
    if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
      throw new BssException(err.code, err.message);
    }
    throw new BssException(message, exception);
  }

  /**
   * 获取用户个人token
   *
   * @param code 临时授权码
   * @return 钉钉用户个人token
   */
  public String getUserAccessToken(String code) {
    GetUserTokenRequest getUserTokenRequest = new GetUserTokenRequest()
      .setClientId(dingTalkLoginProperties.getClientId())
      .setClientSecret(dingTalkLoginProperties.getClientSecret())
      .setCode(code)
      .setGrantType("authorization_code");
    try {
      GetUserTokenResponse userToken = oauthClient.getUserToken(getUserTokenRequest);
      GetUserTokenResponseBody body = userToken.getBody();
      return body.getAccessToken();
    }
    catch (TeaException teaEx) {
      logger.error("查询钉钉用户accessToken失败, code:{}, message:{}",
        teaEx.code, teaEx.message);
      throw new BssException("查询钉钉用户身份失败", teaEx);
    }
    catch (Exception ex) {
      logger.error("查询钉钉用户accessToken失败", ex);
      throw new BssException("查询钉钉用户身份失败", ex);
    }
  }
}
