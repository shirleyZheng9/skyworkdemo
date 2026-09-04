package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * 知识中台接口辅助类
 *
 * @author lxs
 * @since 2025/07/14
 */
@Component
@RequiredArgsConstructor
public class KnowledgePlatformApiHelper {
  private static final Logger logger = LoggerFactory.getLogger(KnowledgePlatformApiHelper.class);

  /** 知识中台提供密钥 */
  @Value("${zhxy.appKey:}")
  private String appKey;
  @Value("${zhxy.secretKey:}")
  private String secretKey;

  /** 请求头：时间戳 */
  private static final String TIME_STAMP = "TimeStamp";
  /** 请求头：AppKey */
  private static final String APP_KEY = "AppKey";
  /** 请求头：接口签名随机数 */
  private static final String NONCE = "Nonce";
  /** 请求头：签名 */
  private static final String SIGN = "Sign";

  /**
   * 构造带有令牌信息的请求头部
   */
  public HttpHeaders buildHeader(String params) {
    HttpHeaders headers = buildBasicHeader();
    try {
      String timestamp = Long.toString(System.currentTimeMillis());
      String nonce = RandomStringUtils.insecure().nextAlphanumeric(32);
      headers.set(TIME_STAMP, timestamp);
      headers.set(APP_KEY, appKey);
      headers.set(NONCE, nonce);
      headers.set(SIGN, getSignature(timestamp, nonce, params));
    }
    catch (Exception e) {
      logger.error("构造带有令牌信息的请求头部失败", e);
    }
    return headers;
  }

  public HttpHeaders buildBasicHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  private String getSignature(String timestamp, String nonce, String params) {
    StringBuilder str = new StringBuilder();
    // 按参数名称排序、拼接
    str.append("nonce=").append(nonce);
    if (StringUtils.isNotEmpty(params)) {
      str.append("&params=").append(params);
    }
    str.append("&timestamp=").append(timestamp);
    // key 固定放在后面
    str.append("&key=").append(secretKey);
    return DigestUtils.md5Hex(str.toString());
  }

}
