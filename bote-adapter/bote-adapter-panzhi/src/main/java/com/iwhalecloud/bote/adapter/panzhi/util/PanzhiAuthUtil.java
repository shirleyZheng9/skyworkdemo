package com.iwhalecloud.bote.adapter.panzhi.util;

import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 磐智能力超市鉴权工具类 提供磐智能力超市接口的鉴权参数生成和验证功能
 *
 * @author qian.sisheng
 * @since 2025-01-14
 */
public final class PanzhiAuthUtil {
  private static final Logger logger = LoggerFactory.getLogger(PanzhiAuthUtil.class.getName());
  /** 获取WebSocket URL */
  private static final String WEBSOCKET_URL = "PANZHI_WEBSOCKET_URL";
  /** 应用ID */
  private static final String APP_ID = "PANZHI_APP_ID";
  /** 应用密钥 */
  private static final String APP_KEY = "PANZHI_APP_KEY";

  private static final DcParamCache dcParamCache = SpringUtil.getBean(DcParamCache.class);

  private PanzhiAuthUtil() {
  }

  /**
   * 创建会话参数
   */
  public static Map<String, Object> getSessionParams() {
    // 默认会话参数
    Map<String, Object> sessionParam = new HashMap<>();
    sessionParam.put("aue", "raw");
    sessionParam.put("rst", "plain");
    sessionParam.put("rse", "utf8");
    sessionParam.put("eos", "1800");
    sessionParam.put("bos", "10000");
    sessionParam.put("rate", "16k");
    return sessionParam;
  }

  /**
   * 生成鉴权请求头
   *
   * @return 鉴权请求头Map
   */
  public static Map<String, String> generateAuthHeaders() {
    // 获取应用ID和应用密钥
    String appId = getAppId();
    String appKey = getAppKey();
    String csid = generateCsid();
    // 当前UTC时间戳（秒）
    String xCurTime = String.valueOf(System.currentTimeMillis() / 1000);
    // 构建x-server-param
    String xServerParam = generateServerParam(appId, csid);
    // 计算校验和
    String xCheckSum = calculateCheckSum(appKey, xCurTime, xServerParam);

    // 设置请求头
    Map<String, String> headers = new HashMap<>();
    headers.put("appKey", appKey);
    headers.put("x-server-param", xServerParam);
    headers.put("x-checksum", xCheckSum);
    headers.put("x-curtime", xCurTime);
    headers.put("Content-Type", "application/json");
    logger.debug("Generate panzhi header params: appId={}, csid={}, xCurTime={}", appId, csid, xCurTime);
    return headers;
  }

  /**
   * 生成服务器参数（Base64编码的JSON）
   *
   * @param appId 应用ID
   * @param csid 会话ID
   * @return Base64编码的服务器参数
   */
  public static String generateServerParam(String appId, String csid) {
    Map<String, String> serverParam = new HashMap<>();
    serverParam.put("appid", appId);
    serverParam.put("csid", csid);
    return Base64.encodeBase64String(JsonUtil.toJsonString(serverParam).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 计算校验和 校验和 = MD5(appSecret + xCurTime + xServerParam)
   *
   * @param appSecret 应用密钥
   * @param xCurTime 当前时间戳
   * @param xServerParam 服务器参数
   * @return MD5校验和
   */
  public static String calculateCheckSum(String appSecret, String xCurTime, String xServerParam) {
    return DigestUtils.md5Hex(appSecret + xCurTime + xServerParam);
  }

  /**
   * 生成会话ID 格式: appid + capabilityname + uuid
   *
   * @return 会话ID（64位）
   */
  public static String generateCsid() {
    String appId = getAppId();
    String capabilityName = getCapabilityName();
    // 生成32位UUID
    String uuid = UUID.randomUUID().toString().replace("-", "");
    String csid = appId + capabilityName + uuid;
    logger.debug("Generate panzhi csid: appId={}, capabilityName={}, uuid={}, csid={}", appId, capabilityName, uuid, csid);
    return csid;
  }

  /**
   * 根据webSocketUrl的path映射截取capabilityName
   */
  public static String getCapabilityName() {
    String webSocketUrl = getWebSocketUrl();
    String apiPath = webSocketUrl.substring(StringUtils.ordinalIndexOf(webSocketUrl, "/", 3));
    String capabilityName = "";
    String[] pathSplit = StringUtils.split(apiPath, "/");
    // 能力名称（24位，不足补0）
    if (pathSplit[0].length() < 24) {
      capabilityName = StringUtils.rightPad(pathSplit[0], 24, '0');
    }
    else if (pathSplit[0].length() > 24) {
      capabilityName = pathSplit[0].substring(0, 24);
    }
    return capabilityName;
  }

  /**
   * 获取WebSocket连接URL
   */
  public static String getWebSocketUrl() {
    String webSocketUrl = dcParamCache.getDcParamValByCode(WEBSOCKET_URL);
    if (StringUtils.isEmpty(webSocketUrl)) {
      throw new BssException("未配置磐智语音转写 URL，请联系管理员");
    }
    return webSocketUrl;
  }

  /**
   * 获取应用ID
   */
  public static String getAppId() {
    String appId = dcParamCache.getDcParamValByCode(APP_ID);
    if (StringUtils.isEmpty(appId)) {
      throw new BssException("未配置磐智应用 ID，请联系管理员");
    }
    return appId;
  }

  /**
   * 获取应用密钥
   */
  public static String getAppKey() {
    String appKey = dcParamCache.getDcParamValByCode(APP_KEY);
    if (StringUtils.isEmpty(appKey)) {
      throw new BssException("未配置磐智应用密钥，请联系管理员");
    }
    return appKey;
  }

}
