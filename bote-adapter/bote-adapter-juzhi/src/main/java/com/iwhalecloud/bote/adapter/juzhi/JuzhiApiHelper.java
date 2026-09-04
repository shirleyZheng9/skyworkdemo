package com.iwhalecloud.bote.adapter.juzhi;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.llm.client.util.OkHttpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 聚智能开接口辅助类
 *
 * @author bianjp
 * @since 2025-05-29
 */
public final class JuzhiApiHelper {
  private static final Logger logger = LoggerFactory.getLogger(JuzhiApiHelper.class);

  private JuzhiApiHelper() {
  }

  /** 系统参数名称: 一级聚智平台接口地址 */
  private static final String PARAM_SAVE_DIALOG_URL = "JUZHI1_API_URL";
  /** 系统参数名称: 一级聚智应用标识 */
  private static final String PARAM_APP_KEY = "JUZHI1_APP_KEY";
  /** 系统参数名称: 一级聚智应用私钥 */
  private static final String PARAM_PRIVATE_KEY = "JUZHI1_PRIVATE_KEY";
  /** 系统参数名称: 一级聚智省份编码 */
  private static final String PARAM_PROVINCE = "JUZHI1_PROVINCE";
  /** 系统参数名称: 一级聚智登录用户名 */
  private static final String PARAM_USERNAME = "JUZHI1_USERNAME";
  /** 系统参数名称: 是否开启智脑对话记录保存 */
  private static final String PARAM_SAVE_DIALOG_ENABLED = "JUZHI1_SAVE_DIALOG_ENABLED";
  /** 系统参数名称: 智脑对话记录保存能力编码 */
  private static final String PARAM_SAVE_DIALOG_FUNC_CODE = "JUZHI1_SAVE_DIALOG_FUNC_CODE";
  /** 系统参数名称: 智脑对话记录保存省份编码（与调用大模型的省份编码不同） */
  private static final String PARAM_SAVE_DIALOG_PROVINCE = "JUZHI1_SAVE_DIALOG_PROVINCE";
  private static final DcParamCache dcParamCache = SpringUtil.getBean(DcParamCache.class);

  /**
   * 获取线程池
   */
  public static AsyncTaskExecutor getThreadPool() {
    return ThreadPools.getCommon();
  }

  /**
   * 获取一级聚智接口地址
   */
  public static HttpUrl getApiUrl() {
    String apiUrl = dcParamCache.getDcParamValByCode(PARAM_SAVE_DIALOG_URL);
    Assert.isTrue(StringUtils.isNotEmpty(apiUrl), "未配置一级聚智接口地址，请联系管理员");
    HttpUrl httpUrl = HttpUrl.parse(apiUrl);
    Assert.notNull(httpUrl, "一级聚智接口地址不合法，请联系管理员");
    return httpUrl;
  }

  /**
   * 获取一级聚智应用标识
   */
  private static String getAppKey() {
    String appKey = dcParamCache.getDcParamValByCode(PARAM_APP_KEY);
    Assert.isTrue(StringUtils.isNotEmpty(appKey), "未配置一级聚智应用标识，请联系管理员");
    return appKey;
  }

  /**
   * 获取一级聚智应用私钥
   */
  private static String getPrivateKey() {
    String privateKey = dcParamCache.getDcParamValByCode(PARAM_PRIVATE_KEY);
    Assert.isTrue(StringUtils.isNotEmpty(privateKey), "未配置一级聚智应用私钥，请联系管理员");
    return privateKey;
  }

  /**
   * 获取一级聚智省份编码
   */
  public static String getProvince() {
    String province = dcParamCache.getDcParamValByCode(PARAM_PROVINCE);
    Assert.isTrue(StringUtils.isNotEmpty(province), "未配置一级聚智省份编码，请联系管理员");
    return province;
  }

  /**
   * 获取智脑对话记录保存省份编码
   */
  private static String getSaveDialogProvince() {
    String province = dcParamCache.getDcParamValByCode(PARAM_SAVE_DIALOG_PROVINCE);
    Assert.isTrue(StringUtils.isNotEmpty(province), "未配置智脑对话记录保存省份编码，请联系管理员");
    return province;
  }

  /**
   * 获取一级聚智地市编码
   *
   * @param defaultCity 默认地市
   */
  public static String getCity(String defaultCity) {
    // 优先根据登录信息获取地址编码
    String regionId = getRegionId();
    if (regionId != null) {
      String city = SpringUtil.getProperty("juzhi1.city.code." + regionId);
      if (StringUtils.isNotEmpty(city)) {
        return city;
      }
    }
    // 取不到时返回默认地市
    return defaultCity;
  }

  /**
   * 从登录信息获取区域 ID
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private static String getRegionId() {
    LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
    if (loginInfo == null || StringUtils.isEmpty(loginInfo.getExtUserId()) || MapUtils.isEmpty(loginInfo.getAttributes())) {
      return null;
    }
    Map<String, Object> userInfo = (Map<String, Object>) MapUtils.getMap(loginInfo.getAttributes(), "userInfo");
    if (userInfo != null) {
      String regionLevel = MapUtils.getString(userInfo, "regionLevel");
      // 地市
      if ("2".equals(regionLevel)) {
        return MapUtils.getString(userInfo, "postRegionId");
      }
      // 区县
      if ("3".equals(regionLevel)) {
        return MapUtils.getString(userInfo, "parRegionId");
      }
    }
    return null;
  }

  /**
   * 获取一级聚智用户名
   */
  private static String getUsername() {
    // 优先取登录信息中的用户名
    LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
    if (loginInfo != null && StringUtils.isNotEmpty(loginInfo.getExtUserId())) {
      return loginInfo.getUserName();
    }
    // 没有登录信息时取系统参数中配置的默认值
    String username = dcParamCache.getDcParamValByCode(PARAM_USERNAME);
    Assert.isTrue(StringUtils.isNotEmpty(username), "未配置一级聚智用户名，请联系管理员");
    return username;
  }

  /**
   * 上报调用大模型的记录，供集团统计
   *
   * @param requestTime 请求时间
   * @param messages 消息列表
   * @param answer 回复内容
   */
  public static void saveDialog(String requestTime, List<Message> messages, String answer) {
    // 找出最后一条用户消息内容，找不到时不处理
    String question = extractQuestion(messages);
    if (StringUtils.isEmpty(question)) {
      return;
    }
    saveDialog(requestTime, question, answer, true);
  }

  /**
   * 上报调用大模型的记录，供集团统计
   *
   * @param requestTime 请求时间
   * @param question 问题
   * @param answer 回复内容
   * @param isLlm 是否是大模型调用
   */
  public static void saveDialog(String requestTime, String question, String answer, boolean isLlm) {
    // 未开启开关时不处理
    if (!BaseConsts.TRUE.equals(dcParamCache.getDcParamValByCode(PARAM_SAVE_DIALOG_ENABLED))) {
      return;
    }

    // 异步调用接口，避免拖慢大模型调用
    getThreadPool().submit(() -> {
      String url = "";
      String requestBodyStr = "";
      try {
        // 缺少配置时不处理
        url = dcParamCache.getDcParamValByCode(PARAM_SAVE_DIALOG_URL);
        String funcCode = dcParamCache.getDcParamValByCode(PARAM_SAVE_DIALOG_FUNC_CODE);
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(funcCode)) {
          logger.warn("Skip saving juzhi dialog, missing configuration: url={}, funcCode={}", url, funcCode);
          return;
        }
        // url 不合法时不处理
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
          logger.warn("Skip saving juzhi dialog, invalid api url: url={}", url);
          return;
        }
        Map<String, Object> busiInfo = new LinkedHashMap<>();
        busiInfo.put("PROVINCE_CODE", getSaveDialogProvince());
        busiInfo.put("REQUEST_TIME", requestTime);
        busiInfo.put("REQUEST_NAME", getUsername());
        // 是否成功(1: 成功, 2: 失败)
        busiInfo.put("REQUEST_FLAG", "1");
        busiInfo.put("REQUEST", question);
        busiInfo.put("ANSWER", StringUtils.defaultString(answer));
        // 场景类型. 1: 大模型, 2: 知识问答
        busiInfo.put("TYPE", isLlm ? "1" : "2");
        requestBodyStr = buildRequestBody(DateUtil.formatCompact(), funcCode, "智脑对话记录保存", busiInfo);
        RequestBody requestBody = buildOkHttpRequestBody(requestBodyStr);
        logger.debug("Request juzhi save dialog api start: requestTime={}", requestTime);
        JsonNode response = ModelHttpClient.post(httpUrl, null, requestBody, null, JsonNode.class);
        logger.debug("Request juzhi save dialog api end: requestTime={}, response={}", requestTime, response);
      }
      catch (Exception e) {
        logger.error("Failed to save juzhi dialog: url={}, request={}", url, requestBodyStr, e);
      }
    });
  }

  /**
   * 获取最后一条用户消息内容
   */
  @Nullable
  private static String extractQuestion(List<Message> messages) {
    if (CollectionUtils.isNotEmpty(messages)) {
      Message message = messages.get(messages.size() - 1);
      if (message instanceof UserMessage && ((UserMessage) message).getContent() instanceof String) {
        return (String) ((UserMessage) message).getContent();
      }
    }
    return null;
  }

  /**
   * 构造请求体
   *
   * @param requestTime 请求时间
   * @param funcCode 能力编码
   * @param funcName 能力名称
   * @param busiInfo 业务参数
   * @return 请求体
   */
  public static String buildRequestBody(String requestTime, String funcCode, String funcName, Map<String, Object> busiInfo) {
    Map<String, Object> oprInfo = new LinkedHashMap<>();
    oprInfo.put("FUNC_CODE", funcCode);
    oprInfo.put("GROUP_ID", "");
    oprInfo.put("LOGIN_NO", getUsername());
    oprInfo.put("OP_NOTE", funcName);
    oprInfo.put("VERSION", "06");
    Map<String, Object> body = ImmutableMap.of("OPR_INFO", oprInfo, "BUSI_INFO", busiInfo);
    Map<String, Object> root = ImmutableMap.of("BODY", body, "HEADER", Collections.emptyMap());
    Map<String, Object> context = ImmutableMap.of("ROOT", root);

    Map<String, String> params = new LinkedHashMap<>();
    params.put("appKey", getAppKey());
    params.put("timeStamp", requestTime);
    params.put("context", JsonUtil.toJsonString(context));
    String sign = signParams(params);
    params.put("sign", urlEncode(sign));
    return params.entrySet().stream()
      .map(e -> e.getKey() + "=" + e.getValue())
      .collect(Collectors.joining("&"));
  }

  /**
   * 构造 okhttp 的请求体
   *
   * @param body 请求体内容
   * @return 请求体对象
   */
  public static RequestBody buildOkHttpRequestBody(String body) {
    // 请求体实际不是 json 格式，但能开平台要求传 json
    return OkHttpUtil.jsonBody(body);
  }

  /**
   * 计算接口签名
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  private static String signParams(Map<String, String> params) {
    String privateKeyStr = getPrivateKey();
    // 按 key 名称排序
    String content = params.entrySet().stream()
      .sorted(Entry.comparingByKey())
      .map(e -> e.getKey() + "=" + e.getValue())
      .collect(Collectors.joining("&"));
    try {
      byte[] md5Hash = DigestUtils.md5Hex(urlEncode(content)).getBytes(StandardCharsets.UTF_8);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyStr));
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
      Signature signature = Signature.getInstance("SHA1WithRSA");
      signature.initSign(privateKey);
      signature.update(md5Hash);
      return Base64.encodeBase64String(signature.sign());
    }
    catch (Exception e) {
      throw new BssException("计算聚智接口签名失败: " + e.getMessage(), e);
    }
  }


  /**
   * 编码 URL 参数值
   */
  private static String urlEncode(String value) {
    if (StringUtils.isEmpty(value)) {
      return "";
    }
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

}
