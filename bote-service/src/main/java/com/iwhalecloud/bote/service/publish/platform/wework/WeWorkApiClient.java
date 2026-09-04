package com.iwhalecloud.bote.service.publish.platform.wework;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
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
import org.springframework.web.client.RestTemplate;

/**
 * 企业微信API客户端 - 直接调用企业微信HTTP API
 * 参考LangBot的WecomClient实现
 *
 * @author system
 * @since 2025-01-09
 */
public class WeWorkApiClient {
  private static final Logger logger = LoggerFactory.getLogger(WeWorkApiClient.class);
  private final RestTemplate restTemplate = HttpUtil.getRestTemplate();
  private static final String BASE_URL = "https://qyapi.weixin.qq.com/cgi-bin";

  /**
   * 发送文本消息
   *
   * @param accessToken 访问令牌
   * @param userId 用户ID
   * @param agentId 应用ID
   * @param content 消息内容
   * @return 是否发送成功
   */
  public boolean sendTextMessage(String accessToken, String userId, Long agentId, String content) {
    try {
      String url = BASE_URL + "/message/send?access_token=" + accessToken;
      Map<String, Object> params = new HashMap<>();
      params.put("touser", userId);
      params.put("msgtype", "text");
      params.put("agentid", agentId);
      Map<String, Object> text = new HashMap<>();
      text.put("content", content);
      params.put("text", text);
      params.put("safe", 0);
      params.put("enable_id_trans", 0);
      params.put("enable_duplicate_check", 0);
      params.put("duplicate_check_interval", 1800);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
      });
      Map<String, Object> result = response.getBody();
      if (result != null) {
        Integer errcode = (Integer) result.get("errcode");
        if (errcode != null && errcode == 0) {
          logger.debug("发送企业微信文本消息成功: userId={}, content={}", userId, content);
          return true;
        }
        else {
          logger.error("发送企业微信文本消息失败: {}", result);
          return false;
        }
      }
      return false;
    }
    catch (Exception e) {
      logger.error("发送企业微信文本消息异常: userId={}, content={}", userId, content, e);
      return false;
    }
  }

  /**
   * 获取access_token
   *
   * @param corpid 企业ID
   * @param secret 应用密钥
   * @return access_token
   */
  public String getAccessToken(String corpid, String secret) {
    try {
      String url = BASE_URL + "/gettoken?corpid=" + corpid + "&corpsecret=" + secret;
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
      });
      Map<String, Object> data = response.getBody();
      if (data != null && data.containsKey("access_token")) {
        String accessToken = (String) data.get("access_token");
        logger.debug("获取企业微信access_token成功");
        return accessToken;
      }
      else {
        logger.error("获取企业微信access_token失败: {}", data);
        throw new BssException("获取access_token失败: " + data);
      }
    }
    catch (Exception e) {
      logger.error("获取企业微信access_token异常", e);
      throw new BssException("获取access_token异常", e);
    }
  }

  /**
   * 发送图片消息
   *
   * @param accessToken 访问令牌
   * @param userId 用户ID
   * @param agentId 应用ID
   * @param mediaId 媒体ID
   * @return 是否发送成功
   */
  public boolean sendImageMessage(String accessToken, String userId, Long agentId, String mediaId) {
    try {
      String url = BASE_URL + "/message/send?access_token=" + accessToken;
      Map<String, Object> params = new HashMap<>();
      params.put("touser", userId);
      params.put("msgtype", "image");
      params.put("agentid", agentId);
      Map<String, Object> image = new HashMap<>();
      image.put("media_id", mediaId);
      params.put("image", image);
      params.put("safe", 0);
      params.put("enable_id_trans", 0);
      params.put("enable_duplicate_check", 0);
      params.put("duplicate_check_interval", 1800);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
      });
      Map<String, Object> result = response.getBody();
      if (result != null) {
        Integer errcode = (Integer) result.get("errcode");
        if (errcode != null && errcode == 0) {
          logger.debug("发送企业微信图片消息成功: userId={}, mediaId={}", userId, mediaId);
          return true;
        }
        else {
          logger.error("发送企业微信图片消息失败: {}", result);
          return false;
        }
      }
      return false;
    }
    catch (Exception e) {
      logger.error("发送企业微信图片消息异常: userId={}, mediaId={}", userId, mediaId, e);
      return false;
    }
  }

  /**
   * 获取用户列表
   *
   * @param accessToken 访问令牌
   * @return 用户ID列表
   */
  public String[] getUserList(String accessToken) {
    try {
      String url = BASE_URL + "/user/list_id?access_token=" + accessToken;
      Map<String, Object> params = new HashMap<>();
      params.put("cursor", "");
      params.put("limit", 10000);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
      ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
      });
      Map<String, Object> result = response.getBody();
      if (result != null) {
        Integer errcode = (Integer) result.get("errcode");
        if (errcode != null && errcode == 0) {
          // 解析用户列表
          // 这里需要根据实际返回格式解析
          logger.debug("获取企业微信用户列表成功");
          return new String[0]; // 临时返回空数组
        }
        else {
          logger.error("获取企业微信用户列表失败: {}", result);
          return new String[0];
        }
      }
      return new String[0];
    }
    catch (Exception e) {
      logger.error("获取企业微信用户列表异常", e);
      return new String[0];
    }
  }

  /**
   * 发送消息给所有用户
   *
   * @param accessToken 访问令牌
   * @param agentId 应用ID
   * @param content 消息内容
   * @return 是否发送成功
   */
  public boolean sendToAll(String accessToken, int agentId, String content) {
    // 获取用户列表
    String[] userIds = getUserList(accessToken);
    if (userIds.length == 0) {
      logger.warn("没有找到用户，无法发送消息");
      return false;
    }
    // 构建用户ID字符串
    StringBuilder userIdsStr = new StringBuilder();
    for (int i = 0; i < userIds.length; i++) {
      if (i > 0) {
        userIdsStr.append("|");
      }
      userIdsStr.append(userIds[i]);
    }
    String url = BASE_URL + "/message/send?access_token=" + accessToken;
    Map<String, Object> params = new HashMap<>();
    params.put("touser", userIdsStr.toString());
    params.put("msgtype", "text");
    params.put("agentid", agentId);
    Map<String, Object> text = new HashMap<>();
    text.put("content", content);
    params.put("text", text);
    params.put("safe", 0);
    params.put("enable_id_trans", 0);
    params.put("enable_duplicate_check", 0);
    params.put("duplicate_check_interval", 1800);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
    });
    Map<String, Object> result = response.getBody();
    if (result != null) {
      Integer errcode = (Integer) result.get("errcode");
      if (errcode != null && errcode == 0) {
        logger.debug("发送企业微信群发消息成功: content={}", content);
        return true;
      }
      else {
        logger.error("发送企业微信群发消息失败: {}", result);
        return false;
      }
    }
    return false;
  }
}
