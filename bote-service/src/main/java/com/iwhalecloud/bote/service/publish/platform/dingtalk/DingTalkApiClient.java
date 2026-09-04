package com.iwhalecloud.bote.service.publish.platform.dingtalk;

import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkrobot_1_0.models.OrgGroupSendHeaders;
import com.aliyun.dingtalkrobot_1_0.models.OrgGroupSendRequest;
import com.aliyun.dingtalkrobot_1_0.models.OrgGroupSendResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 钉钉API客户端
 * 基于LangBot中使用的真实钉钉API实现
 *
 * @author system
 * @since 2025-01-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class DingTalkApiClient {
  private static final Logger logger = LoggerFactory.getLogger(DingTalkApiClient.class);
  private final String appKey;
  private final String appSecret;
  private final String robotCode;
  private final com.aliyun.dingtalkoauth2_1_0.Client oauthClient;
  private final com.aliyun.dingtalkrobot_1_0.Client robotClient;
  private String accessToken;
  private long tokenExpireTime;

  public DingTalkApiClient(String appKey, String appSecret, String robotCode) {
    this.appKey = appKey;
    this.appSecret = appSecret;
    this.robotCode = robotCode;
    // 初始化OAuth客户端
    try {
      Config oauthConfig = new Config();
      oauthConfig.protocol = "https";
      oauthConfig.regionId = "central";
      this.oauthClient = new com.aliyun.dingtalkoauth2_1_0.Client(oauthConfig);
    }
    catch (Exception e) {
      logger.error("初始化钉钉OAuth客户端失败", e);
      throw new BssException("初始化钉钉OAuth客户端失败", e);
    }
    // 初始化机器人客户端
    try {
      Config robotConfig = new Config();
      robotConfig.protocol = "https";
      robotConfig.regionId = "central";
      this.robotClient = new com.aliyun.dingtalkrobot_1_0.Client(robotConfig);
    }
    catch (Exception e) {
      logger.error("初始化钉钉机器人客户端失败", e);
      throw new BssException("初始化钉钉机器人客户端失败", e);
    }
  }

  /**
   * 获取访问令牌
   * 使用钉钉官方OAuth SDK
   */
  public String getAccessToken() throws Exception {
    // 如果token有效，直接返回
    if (accessToken != null && System.currentTimeMillis() <= tokenExpireTime) {
      return accessToken;
    }
    // 获取新的访问令牌
    return refreshAccessToken();
  }

  /**
   * 刷新访问令牌
   */
  private String refreshAccessToken() {
    try {
      // 使用官方OAuth SDK获取访问令牌
      GetAccessTokenRequest request = new GetAccessTokenRequest()
        .setAppKey(appKey)
        .setAppSecret(appSecret);
      String newAccessToken = oauthClient.getAccessToken(request).getBody().getAccessToken();
      // 验证token是否有效
      if (newAccessToken == null || newAccessToken.isEmpty()) {
        logger.error("钉钉AccessToken获取失败：返回值为空");
        throw new BssException("获取钉钉AccessToken失败：返回值为空");
      }
      // 更新token和过期时间
      accessToken = newAccessToken;
      // 钉钉访问令牌通常有效期为7200秒（2小时）
      tokenExpireTime = System.currentTimeMillis() + (7200 - 300) * 1000L; // 提前5分钟刷新
      return accessToken;
    }
    catch (TeaException e) {
      if (!Common.empty(e.code) && !Common.empty(e.message)) {
        throw new BssException("钉钉API错误: " + e.code + " - " + e.message, e);
      }
      else {
        throw new BssException("钉钉API错误", e);
      }
    }
    catch (Exception e) {
      logger.error("获取钉钉AccessToken异常", e);
      throw new BssException("获取钉钉AccessToken异常", e);
    }
  }

  /**
   * 发送私聊消息
   * 使用LangBot中的真实API: <a href="https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend">...</a>
   */
  public boolean sendPrivateMessage(String userId, String content) {
    try {
      String token = getAccessToken();
      String url = "https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend";
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      //noinspection UastIncorrectHttpHeaderInspection
      headers.set("x-acs-dingtalk-access-token", token);
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("robotCode", robotCode);
      requestBody.put("userIds", new String[]{userId});
      requestBody.put("msgKey", "sampleMarkdown");
      requestBody.put("msgParam", JsonUtil.toJsonString(Map.of("text", content, "title", content.length() > 9 ? content.substring(0, 9) + "..." : content)));
      HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
      });
      Map<String, Object> response = responseEntity.getBody();
      if (response != null) {
        logger.info("钉钉私聊消息发送成功: userId={}", userId);
        return true;
      }
      else {
        logger.error("钉钉私聊消息发送失败: userId={}", userId);
        return false;
      }
    }
    catch (Exception e) {
      logger.error("发送钉钉私聊消息异常: userId={}", userId, e);
      return false;
    }
  }

  /**
   * 发送群聊消息
   * 使用钉钉官方机器人SDK
   */
  public boolean sendGroupMessage(String groupId, String content) {
    try {
      String token = getAccessToken();
      // 设置请求头
      OrgGroupSendHeaders headers = new OrgGroupSendHeaders();
      headers.setXAcsDingtalkAccessToken(token);
      // 设置请求体
      OrgGroupSendRequest request = new OrgGroupSendRequest();
      request.setMsgKey("sampleMarkdown");
      request.setRobotCode(robotCode);
      request.setOpenConversationId(groupId);
      // 设置消息参数
      Map<String, Object> msgParam = new HashMap<>();
      msgParam.put("text", content);
      msgParam.put("title", content.length() > 9 ? content.substring(0, 9) + "..." : content);
      request.setMsgParam(JsonUtil.toJsonString(msgParam));
      // 发送消息
      OrgGroupSendResponse response = robotClient.orgGroupSendWithOptions(request, headers,
        new com.aliyun.teautil.models.RuntimeOptions());
      if (response != null && response.getBody() != null) {
        logger.info("钉钉群聊消息发送成功: groupId={}, processQueryKey={}", groupId, response.getBody().getProcessQueryKey());
        return true;
      }
      else {
        logger.error("钉钉群聊消息发送失败: groupId={}, response={}", groupId, response);
        return false;
      }
    }
    catch (TeaException e) {
      logger.error("钉钉群聊消息发送TeaException: groupId={}, errCode={}, errorMessage={}",
        groupId, e.getCode(), e.getMessage(), e);
      return false;
    }
    catch (Exception e) {
      logger.error("发送钉钉群聊消息异常: groupId={}", groupId, e);
      return false;
    }
  }

  /**
   * 下载文件
   * 使用LangBot中的真实API: <a href="https://api.dingtalk.com/v1.0/robot/messageFiles/download">...</a>
   */
  public String downloadFile(String downloadCode) {
    try {
      String token = getAccessToken();
      String url = "https://api.dingtalk.com/v1.0/robot/messageFiles/download";
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      //noinspection UastIncorrectHttpHeaderInspection
      headers.set("x-acs-dingtalk-access-token", token);
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("downloadCode", downloadCode);
      requestBody.put("robotCode", robotCode);
      HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
      });
      Map<String, Object> response = responseEntity.getBody();
      if (response != null && response.get("downloadUrl") != null) {
        String downloadUrl = (String) response.get("downloadUrl");
        logger.info("钉钉文件下载URL获取成功: downloadCode={}", downloadCode);
        return downloadUrl;
      }
      else {
        logger.error("钉钉文件下载URL获取失败: downloadCode={}", downloadCode);
        return null;
      }
    }
    catch (Exception e) {
      logger.error("获取钉钉文件下载URL异常: downloadCode={}", downloadCode, e);
      return null;
    }
  }

  /**
   * 检查token是否有效
   */
  public boolean isTokenValid() {
    return accessToken != null && System.currentTimeMillis() < tokenExpireTime;
  }

  /**
   * 刷新token
   */
  public void refreshToken() {
    accessToken = null;
    tokenExpireTime = 0;
    try {
      getAccessToken();
    }
    catch (Exception e) {
      logger.error("刷新钉钉token失败", e);
    }
  }

  /**
   * 发送简单文本消息
   * 钉钉基础文本消息发送
   */
  public boolean sendSimpleTextMessage(String targetId, String targetType, String content) {
    try {
      if ("group".equals(targetType)) {
        return sendGroupMessage(targetId, content);
      }
      else {
        return sendPrivateMessage(targetId, content);
      }
    }
    catch (Exception e) {
      logger.error("发送钉钉简单文本消息异常: targetId={}", targetId, e);
      return false;
    }
  }
}
