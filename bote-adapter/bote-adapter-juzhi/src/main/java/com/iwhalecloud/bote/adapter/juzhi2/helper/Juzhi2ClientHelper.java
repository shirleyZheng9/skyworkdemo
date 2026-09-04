package com.iwhalecloud.bote.adapter.juzhi2.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.adapter.juzhi.JuzhiApiHelper;
import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.sse.EventSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

/**
 * 二级聚智客户端工具类
 *
 * @author bianjp
 * @since 2025-05-14
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class Juzhi2ClientHelper {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi2ClientHelper.class);

  private Juzhi2ClientHelper() {
  }

  /** 系统参数名称: 聚智平台地址（/openapi/ 的上一层） */
  private static final String PARAM_URL = "JUZHI2_URL";
  /** 系统参数名称: 聚智平台应用 ID */
  private static final String PARAM_APP_ID = "JUZHI2_APP_ID";
  /** 系统参数名称: 聚智平台应用密钥 */
  private static final String PARAM_APP_SECRET = "JUZHI2_APP_SECRET";
  /** 系统参数名称: 聚智平台应用密钥 */
  private static final String PARAM_KNOWLEDGE_RECALL_ASSISTANT_CODE = "JUZHI2_KNOWLEDGE_RECALL_ASSISTANT_CODE";
  /** 系统参数名称: 聚智平台应用密钥 */
  private static final String PARAM_KNOWLEDGE_RECALL_START_NODE = "JUZHI2_KNOWLEDGE_RECALL_START_NODE";
  /** 系统参数名称: 聚智平台OCR工作流的开始节点 ID */
  private static final String PARAM_OCR_START_NODE = "JUZHI2_OCR_START_NODE";
  /** 系统参数名称: 聚智平台OCR工作流编码 */
  private static final String PARAM_OCR_ASSISTANT_CODE = "JUZHI2_OCR_ASSISTANT_CODE";
  /** 聚智对话型工作流API地址 */
  private static final String CHAT_API_URL_PATH = "/openapi/flames/api/v1/chat";
  /** 聚智文件上传API地址 */
  private static final String FILE_API_URL_PATH = "/openapi/flames/file/v2/upload";
  /** 系统参数名称: 聚智平台DICT项目团队的文件解析工作流的开始节点 ID */
  private static final String PARAM_DICT_FILE_PARSE_START_NODE = "JUZHI2_DICT_FILE_PARSE_START_NODE";
  /** 系统参数名称: 聚智平台DICT项目团队的文件解析工作流编码 */
  private static final String PARAM_DICT_FILE_PARSE_ASSISTANT_CODE = "JUZHI2_DICT_FILE_PARSE_ASSISTANT_CODE";

  private static final DcParamCache dcParamCache = SpringUtil.getBean(DcParamCache.class);

  /**
   * 获取会话接口地址
   */
  public static HttpUrl getChatApiUrl() {
    return buildApiUrl(CHAT_API_URL_PATH);
  }

  /**
   * 获取文件上传接口地址
   */
  public static HttpUrl getFileUploadApiUrl() {
    return buildApiUrl(FILE_API_URL_PATH);
  }

  /**
   * 构建二级聚智API地址URL
   * @param urlPath 请求路径
   * @return HttpUrl
   */
  private static HttpUrl buildApiUrl(String urlPath) {
    String url = dcParamCache.getDcParamValByCode(PARAM_URL);
    if (StringUtils.isEmpty(url)) {
      throw new BssException("未配置聚智平台地址，请联系管理员");
    }
    //noinspection HttpUrlsUsage
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      throw new BssException("聚智平台地址配置错误，请联系管理员");
    }
    HttpUrl apiUrl = HttpUrl.parse(StringUtils.stripEnd(url, "/") + urlPath);
    if (apiUrl == null) {
      throw new BssException("聚智平台地址不合法，请联系管理员");
    }
    return apiUrl;
  }

  /**
   * 获取应用 ID
   */
  public static String getAppId() {
    String appId = dcParamCache.getDcParamValByCode(PARAM_APP_ID);
    if (StringUtils.isEmpty(appId)) {
      throw new BssException("未配置聚智平台应用 ID，请联系管理员");
    }
    return appId;
  }

  /**
   * 获取应用密钥
   */
  public static String getAppSecret() {
    String appSecret = dcParamCache.getDcParamValByCode(PARAM_APP_SECRET);
    if (StringUtils.isEmpty(appSecret)) {
      throw new BssException("未配置聚智平台应用密钥，请联系管理员");
    }
    return appSecret;
  }

  /**
   * 获取知识检索工作流的智能体编码
   */
  public static String getKnowledgeRecallAssistantCode() {
    String appSecret = dcParamCache.getDcParamValByCode(PARAM_KNOWLEDGE_RECALL_ASSISTANT_CODE);
    if (StringUtils.isEmpty(appSecret)) {
      throw new BssException("未配置聚智平台知识检索工作流的智能体编码，请联系管理员");
    }
    return appSecret;
  }

  /**
   * 获取文件解析工作流的智能体编码
   */
  public static String getOcrAssistantCode() {
    String assistantCode = dcParamCache.getDcParamValByCode(PARAM_OCR_ASSISTANT_CODE);
    if (StringUtils.isEmpty(assistantCode)) {
      throw new BssException("未配置聚智平台文件解析工作流的智能体编码，请联系管理员");
    }
    return assistantCode;
  }

  /**
   * 获取DICT团队的文件解析作流的智能体编码
   */
  public static String getDictFileParseAssistantCode() {
    String assistantCode = dcParamCache.getDcParamValByCode(PARAM_DICT_FILE_PARSE_ASSISTANT_CODE);
    if (StringUtils.isEmpty(assistantCode)) {
      throw new BssException("未配置聚智平台DICT文件解析工作流的智能体编码，请联系管理员");
    }
    return assistantCode;
  }

  /**
   * 获取知识检索工作流的开始节点 ID
   */
  public static String getKnowledgeRecallStartNodeId() {
    String appSecret = dcParamCache.getDcParamValByCode(PARAM_KNOWLEDGE_RECALL_START_NODE);
    if (StringUtils.isEmpty(appSecret)) {
      throw new BssException("未配置聚智平台知识检索工作流的开始节点 ID，请联系管理员");
    }
    return appSecret;
  }

  /**
   * 获取文件解析工作流的开始节点 ID
   */
  public static String getOcrStartNodeId() {
    String startNode = dcParamCache.getDcParamValByCode(PARAM_OCR_START_NODE);
    if (StringUtils.isEmpty(startNode)) {
      throw new BssException("未配置聚智平台OCR工作流的开始节点ID，请联系管理员");
    }
    return startNode;
  }

  /**
   * 获取文件解析工作流的开始节点 ID
   */
  public static String getDictFileParseStartNodeId() {
    String startNode = dcParamCache.getDcParamValByCode(PARAM_DICT_FILE_PARSE_START_NODE);
    if (StringUtils.isEmpty(startNode)) {
      throw new BssException("未配置聚智平台DICT文件解析工作流的开始节点ID，请联系管理员");
    }
    return startNode;
  }

  /**
   * 构造新的日志 ID
   */
  public static String newTraceId() {
    return StringUtils.remove(UUID.randomUUID().toString(), '-');
  }

  /**
   * 签名请求地址
   */
  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  public static HttpUrl signUrl(String assistantCode) {
    return signUrl(assistantCode, getChatApiUrl(), HttpMethod.GET.name());
  }

  /**
   * 签名请求地址，支持动态URL和请求方式
   */
  @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
  public static HttpUrl signUrl(String assistantCode, HttpUrl url, String method) {
    String appId = getAppId();
    String appSecret = getAppSecret();
    // 请求时间，使用 RFC1123 格式
    String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now().atZone(ZoneId.of("GMT")));
    // 签名字符串
    String sourceText = String.format("host: %s\ndate: %s\n%s %s HTTP/1.1", url.host(), date, method, url.encodedPath());
    // 计算签名
    String signature = Base64.encodeBase64String(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, appSecret).hmac(sourceText));
    // 生成鉴权参数
    String authText = String.format("hmac api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
      appId, signature);
    String authorization = Base64.encodeBase64String(authText.getBytes(StandardCharsets.UTF_8));

    // 添加 query 参数
    Builder urlBuilder = url.newBuilder();
    urlBuilder.addQueryParameter("authorization", authorization);
    urlBuilder.addQueryParameter("date", date);
    urlBuilder.addQueryParameter("host", url.host());
    urlBuilder.addQueryParameter("assistantCode", assistantCode);
    return urlBuilder.build();
  }

  /**
   * 构造请求参数
   */
  public static Map<String, Object> buildRequestParams(String traceId, String assistantCode, Map<String, Object> payload) {
    Map<String, Object> header = new HashMap<>();
    header.put("appId", getAppId());
    header.put("assistantCode", assistantCode);
    header.put("traceId", traceId);
    return ImmutableMap.of("header", header, "payload", payload);
  }

  /**
   * 调用聚智的 WebSocket 接口
   */
  public static EventSource invokeApi(String traceId, HttpUrl url, Map<String, Object> params, BiConsumer<String, JsonNode> messageHandler, Consumer<BssException> completionHandler) {
    logger.debug("Start juzhi websocket request: traceId={}, url={}, params={}", traceId, url, params);
    WebSocketListener listener = new Juzhi2WebSocketListener(traceId, params, messageHandler, completionHandler);
    WebSocket webSocket = ModelHttpClient.getClient().newWebSocket(new Request.Builder().url(url).build(), listener);
    return new EventSource() {
      @Override
      public void cancel() {
        webSocket.cancel();
      }

      @Override
      public Request request() {
        return webSocket.request();
      }
    };
  }

  /**
   * 调用聚智的 WebSocket 接口并阻塞等待回复结束
   */
  public static void invokeApiAndWait(String traceId, HttpUrl url, Map<String, Object> params, BiConsumer<String, JsonNode> messageHandler) {
    // 收集异常
    AtomicReference<BssException> exceptionHolder = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    // 完成回调
    Consumer<BssException> completionHandler = e -> {
      exceptionHolder.set(e);
      countDownLatch.countDown();
    };

    logger.debug("Start juzhi websocket request: traceId={}, url={}, params={}", traceId, url, params);
    WebSocketListener listener = new Juzhi2WebSocketListener(traceId, params, messageHandler, completionHandler);
    WebSocket webSocket = ModelHttpClient.getClient().newWebSocket(new Request.Builder().url(url).build(), listener);
    SseUtil.requestListener.accept(webSocket);

    // 阻塞等待调用完成
    try {
      boolean success = countDownLatch.await(ModelHttpClient.getClient().callTimeoutMillis(), TimeUnit.MILLISECONDS);
      if (!success) {
        throw new BssException("调用聚智大模型接口超时");
      }
    }
    catch (InterruptedException e) {
      throw new BssException("调用聚智大模型接口中断: " + StringUtils.defaultString(e.getMessage()), e);
    }
    // 调用失败
    if (exceptionHolder.get() != null) {
      throw exceptionHolder.get();
    }
  }

  /**
   * 上传文件到聚智平台
   */
  public static String uploadFile(InputStream fileStream, String fileName, String assistantCode) {
    try {
      String requestTime = DateUtil.formatCompact();
      // 构建文件上传参数
      Map<String, Object> fileMap = new HashMap<>();
      fileMap.put("fileName", fileName);
      fileMap.put("file", Base64.encodeBase64String(IOUtils.toByteArray(fileStream)));
      Map<String, Object> payload = new HashMap<>();
      payload.put("payload", fileMap);
      RequestBody requestBody = JuzhiApiHelper.buildOkHttpRequestBody(JsonUtil.toJsonString(payload));
      // 调用文件上传接口
      HttpUrl fileUploadApiUrl = getFileUploadApiUrl();
      HttpUrl url = signUrl(assistantCode, fileUploadApiUrl, HttpMethod.POST.name());
      logger.debug("Request juzhi file upload api start: url={}, requestTime={}, bodyLength={}", url, requestTime, requestBody.contentLength());
      JsonNode response = ModelHttpClient.post(url, null, requestBody, null, JsonNode.class);
      logger.debug("Request juzhi file upload api end: requestTime={}, response={}", requestTime, response);
      // 获取文件ID内容
      JsonNode jsonNode = response.path("payload").path("id");
      if (jsonNode.isMissingNode() || jsonNode.isNull() || StringUtils.isEmpty(jsonNode.asText())) {
        logger.warn("Request juzhi file upload but no file id response. requestTime={}, response={}", requestTime, response);
        throw new BssException("执行聚智平台文件上传接口返回的文件id为空");
      }
      return jsonNode.asText();
    }
    catch (BssException be) {
      throw be;
    }
    catch (Exception e) {
      throw new BssException("执行聚智平台文件上传时失败: " + ExpUtil.getMsg(e), e);
    }
  }

}
