package com.iwhalecloud.bote.wechat;

import com.iwhalecloud.bote.cache.WechatAuthCache;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.config.properties.PublishProperties;
import com.iwhalecloud.bote.dto.wechat.WechatAccessTokenResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信鉴权工具类
 *
 * @author lizuyin
 * @since 2025-08-13
 */
@Component
@RequiredArgsConstructor
public class WechatAuthHelper {

  private static final Logger logger = LoggerFactory.getLogger(WechatAuthHelper.class);

  private final PublishProperties publishProperties;
  private final WechatAuthCache wechatAuthCache;

  /**
   * 获取微信访问令牌
   *
   * <p>流程：</p>
   * <ol>
   *   <li>优先从缓存获取token，如果存在且有效则直接返回</li>
   *   <li>缓存未命中或已过期，则调用微信接口获取新的access_token</li>
   *   <li>成功获取后，将新token保存到缓存中</li>
   * </ol>
   *
   * @param appid  小程序唯一凭证
   * @param secret 小程序唯一凭证密钥
   * @return 访问令牌
   */
  public String getAccessToken(String appid, String secret) {
    Assert.hasText(appid, "小程序AppID不能为空");
    Assert.hasText(secret, "小程序AppSecret不能为空");

    String appKey = buildAppKey(appid, secret);
    String token = wechatAuthCache.get(appKey);

    if (StringUtils.isNotEmpty(token)) {
      logger.debug("Successfully obtained WeChat access_token from cache: appid={}", appid);
      return token;
    }

    return refreshToken(appid, secret, appKey);
  }

  /**
   * 刷新微信访问令牌
   *
   * @param appid 小程序唯一凭证
   * @param secret 小程序唯一凭证密钥
   * @return 访问令牌
   */
  public String refreshToken(String appid, String secret) {
    Assert.hasText(appid, "小程序AppID不能为空");
    Assert.hasText(secret, "小程序AppSecret不能为空");

    String appKey = buildAppKey(appid, secret);

    return refreshToken(appid, secret, appKey);
  }

  /**
   * 微信 access_token 的有效期为7200秒（2小时），有效期内重复获取会返回相同结果并自动续期，过期后获取会返回新的access_token
   *
   * @param appid 小程序唯一凭证
   * @param secret 小程序唯一凭证密钥
   * @param appKey 缓存key
   * @return token
   */
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  @SuppressWarnings("PMD.GuardLogStatement")
  private String refreshToken(String appid, String secret, String appKey) {
    try {
      String apiUrl = publishProperties.getGetAccessTokenApiUrl();
      Map<String, String> params = new HashMap<>();
      params.put("grant_type", "client_credential");
      params.put("appid", appid);
      params.put("secret", secret);
      WechatAccessTokenResponse response = HttpUtil.get(apiUrl, params,
          new ParameterizedTypeReference<>() {
          });
      String accessToken = response.getAccessToken();
      if (StringUtils.isNotEmpty(accessToken)) {
        wechatAuthCache.save(appKey, accessToken);
        logger.debug("Successfully obtained new WeChat access_token: appid={}, expires_in={}", appid, response.getExpiresIn());
        return accessToken;
      }
      else {
        logger.error("Failed to obtain WeChat access_token: appid={}, response={}", appid, response);
        throw new BssException("获取微信access_token失败: 响应数据异常");
      }
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      logger.error("Failed to obtain WeChat access_token: appid={}, status={}, response={}",
          appid, e.getStatusCode().value(), responseBody);
      throw new BssException("获取微信access_token失败: " + e.getStatusCode() + ", " + ExpUtil.getMsg(e), e);
    }
    catch (Exception e) {
      logger.error("Failed to obtain WeChat access_token: appid={}", appid, e);
      throw new BssException("获取微信access_token失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 构建缓存key
   *
   * @param appid 小程序唯一凭证
   * @param secret 小程序唯一凭证密钥
   * @return 缓存key
   */
  private String buildAppKey(String appid, String secret) {
    return appid + "_" + secret;
  }

}
