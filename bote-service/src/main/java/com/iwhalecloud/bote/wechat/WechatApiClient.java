package com.iwhalecloud.bote.wechat;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.PublishProperties;
import com.iwhalecloud.bote.dto.wechat.WechatSendTextRequest;
import com.iwhalecloud.bote.dto.wechat.WechatSendTextResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信API客户端
 *
 * @author lizuyin
 * @since 2025-08-12
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WechatApiClient {

  private static final Logger logger = LoggerFactory.getLogger(WechatApiClient.class);

  private final PublishProperties publishProperties;

  private final WechatAuthHelper wechatAuthHelper;

  /**
   * 发送文本消息
   *
   * @return 发送结果响应
   */
  public WechatSendTextResponse sendText(String openId, String content, String appid, String secret) {
    Assert.hasText(openId, "OpenID不能为空");
    Assert.hasText(content, "消息内容不能为空");
    Assert.hasText(appid, "小程序AppID不能为空");
    Assert.hasText(secret, "小程序AppSecret不能为空");
    String apiUrl = publishProperties.getSendCustomMessageApiUrl();
    Assert.hasText(apiUrl, "微信API地址未配置");

    try {
      HttpHeaders headers = new HttpHeaders();
      WechatSendTextRequest request = new WechatSendTextRequest();
      request.setToUser(openId);
      request.getText().setContent(content);
      request.setMsgType("text");
      Map<String, String> params = new HashMap<>();
      params.put("access_token", wechatAuthHelper.getAccessToken(appid, secret));
      return HttpUtil.post(apiUrl, params, request, new ParameterizedTypeReference<WechatSendTextResponse>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to send WeChat text message: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(),
        responseBody);
      throw new BssException("发送微信文本消息失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to send WeChat text message: url={}", apiUrl, e);
      throw new BssException("发送微信文本消息失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 从响应体提取错误信息
   */
  private String extractErrorMsg(String responseBody, HttpStatusCodeException httpStatusCodeException) {
    if (responseBody.startsWith("{") && Strings.CS.contains(responseBody, "errmsg")) {
      try {
        if (StringUtils.isNotEmpty(responseBody)) {
          return responseBody;
        }
      }
      catch (Exception e) {
        logger.debug("Failed to parse WeChat API response", e);
      }
    }
    return httpStatusCodeException.getStatusCode().toString();
  }
}
