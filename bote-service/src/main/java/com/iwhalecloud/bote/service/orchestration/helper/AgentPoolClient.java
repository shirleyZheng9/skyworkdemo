package com.iwhalecloud.bote.service.orchestration.helper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.net.URI;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Agent Pool 客户端
 *
 * @author bianjp
 * @since 2026-01-14
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class AgentPoolClient {
  private static final Logger logger = LoggerFactory.getLogger(AgentPoolClient.class);

  /**
   * 获取 Chrome DevTools Protocol 远程连接地址
   *
   * @param userCode 用户编码
   * @param chatId 会话 ID
   * @return CDP 地址
   */
  public String getCdpUrl(String userCode, String chatId) {
    return getUrl("cdp", userCode, chatId, null);
  }

  /**
   * 获取 VNC 页面地址
   *
   * @param userCode 用户编码
   * @param chatId 会话 ID
   * @return VNC 页面地址
   */
  public String getVncUrl(String userCode, String chatId, boolean readonly) {
    String url = getUrl("vnc", userCode, chatId, null);
    //noinspection SpellCheckingInspection
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
      // 解码参数值以避免重复转义
      .replaceQueryParams(HttpUtil.extractQueryParams(url))
      // 添加自动连接参数
      .replaceQueryParam("autoconnect", true);
    if (readonly) {
      builder.replaceQueryParam("view_only", true);
    }
    return builder.encode().build().toUriString();
  }

  /**
   * 获取 CDP 或 VNC 地址
   */
  @SuppressWarnings("LoggingSimilarMessage")
  private String getUrl(String urlType, String userCode, String chatId, @Nullable String filepath) {
    Assert.hasLength(userCode, "用户编码不能为空");
    Assert.hasLength(chatId, "会话 ID 不能为空");
    String url = buildApiUrl("api/whalestack/sandbox/" + urlType);
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
      .queryParam("user_code", userCode)
      .queryParam("chat_id", chatId);
    if (filepath != null) {
      builder.queryParam("file_path", filepath);
    }
    URI uri = builder.encode().build().toUri();
    try {
      HttpEntity<?> requestEntity = new HttpEntity<>(buildHeaders());
      logger.trace("Get {} url start: url={}, user_code={}, chat_id={}", urlType, url, userCode, chatId);
      ApiResult<String> result = HttpUtil.getRestTemplate().exchange(uri, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<ApiResult<String>>() {
      }).getBody();
      Assert.notNull(result, () -> "获取 %s 地址失败，响应为空".formatted(urlType));
      Assert.isTrue(result.isSuccess(), () -> "获取 %s 地址失败: %s".formatted(urlType, result.getMessage()));
      String resultUrl = result.getData();
      Assert.hasLength(resultUrl, () -> "获取 %s 地址失败，地址为空: code=%s, message=%s".formatted(urlType, result.getCode(), result.getMessage()));
      logger.trace("Get {} url success: user_code={}, chat_id={}, url={}", urlType, userCode, chatId, resultUrl);
      return resultUrl;
    }
    catch (HttpStatusCodeException e) {
      String body = readResponseBody(e);
      logger.error("Failed to get {} url: url={},  chat_id={}, status={}, response={}", urlType, url, chatId, e.getStatusCode().value(), body);
      throw new BssException("获取 " + urlType + " 地址失败: " + extractErrorMessage(uri, e, body), e);
    }
    catch (BssException e) {
      logger.error("Failed to get {} url: url={}, user_code={}, chat_id={}, error={}", urlType, url, userCode, chatId, e.getMessage());
      throw e;
    }
    catch (IllegalArgumentException e) {
      logger.error("Failed to get {} url: url={}, user_code={}, chat_id={}, error={}", urlType, url, userCode, chatId, e.getMessage());
      throw new BssException(e);
    }
    catch (Exception e) {
      logger.error("Failed to get {} url: url={}, user_code={}, chat_id={}", urlType, url, userCode, chatId, e);
      throw new BssException("获取 " + urlType + " 地址失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 下载文件
   *
   * @param userCode 用户编码
   * @param chatId 会话 ID
   * @param filepath 沙箱中的文件路径
   * @param savePath 本地保存路径
   */
  public void downloadFile(String userCode, String chatId, String filepath, File savePath) {
    Assert.hasLength(userCode, "用户编码不能为空");
    Assert.hasLength(chatId, "会话 ID 不能为空");
    Assert.hasLength(filepath, "文件路径不能为空");
    // 获取下载链接
    String url = getUrl("download", userCode, chatId, filepath);
    // 链接中的 path 参数值对 "/" 做了不必要的转义，需要解码一下，否则请求会报错
    URI uri = UriComponentsBuilder.fromUriString(url).replaceQueryParams(HttpUtil.extractQueryParams(url)).encode().build().toUri();
    ResponseExtractor<Void> responseExtractor = response -> {
      if (response.getStatusCode().value() != 200) {
        logger.error("Failed to download file: url={}, user_code={}, chat_id={}, filepath={}, status={}", uri, userCode, chatId, filepath, response.getStatusCode());
        throw new BssException("下载文件失败: status=" + response.getStatusCode().value());
      }
      FileUtils.copyToFile(response.getBody(), savePath);
      return null;
    };
    try {
      logger.trace("Download file start: url={}, user_code={}, chat_id={}, filepath={}", uri, userCode, chatId, filepath);
      HttpUtil.getRestTemplate().execute(uri, HttpMethod.GET, null, responseExtractor);
    }
    catch (HttpStatusCodeException e) {
      String body = readResponseBody(e);
      logger.error("Failed to download file: url={}, user_code={}, chat_id={}, filepath={}, status={}, response={}", uri, userCode, chatId, filepath, e.getStatusCode().value(), body);
      throw new BssException("下载文件失败: " + extractErrorMessage(uri, e, body), e);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to download file: url={}, user_code={}, chat_id={}, filepath={}", uri, userCode, chatId, filepath, e);
      throw new BssException("下载文件失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 释放沙箱
   *
   * <p>释放操作不太重要，静默忽略异常</p>
   *
   * @param userCode 用户编码
   * @param chatId 会话 ID
   */
  public void removeSandboxQuietly(String userCode, String chatId) {
    String url = "";
    try {
      Assert.hasLength(userCode, "用户编码不能为空");
      Assert.hasLength(chatId, "会话 ID 不能为空");
      url = buildApiUrl("api/whalestack/sandbox/remove");

      Map<String, Object> params = Map.of("user_code", userCode, "chat_id", chatId);
      HttpHeaders headers = buildHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      HttpEntity<?> requestEntity = new HttpEntity<>(MultiValueMap.fromSingleValue(params), headers);
      logger.trace("Remove sandbox start: url={}, user_code={}, chat_id={}", url, userCode, chatId);
      ApiResult<Void> result = HttpUtil.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ApiResult<Void>>() {
      }).getBody();
      if (result == null) {
        logger.warn("Failed to remove sandbox: url={}, user_code={}, chat_id={}", url, userCode, chatId);
      }
      else if (!result.isSuccess()) {
        logger.warn("Failed to remove sandbox: url={}, user_code={}, chat_id={}, code={}, message={}", url, userCode, chatId, result.getCode(), result.getMessage());
      }
      else {
        logger.trace("Removed sandbox success: url={}, user_code={}, chat_id={}", url, userCode, chatId);
      }
    }
    catch (HttpStatusCodeException e) {
      String body = readResponseBody(e);
      logger.error("Failed to remove sandbox: url={}, user_code={}, chat_id={}, status={}, response={}", url, userCode, chatId, e.getStatusCode().value(), body);
    }
    catch (BssException e) {
      logger.error("Failed to remove sandbox: url={}, user_code={}, chat_id={}, error={}", url, userCode, chatId, e.getMessage());
    }
    catch (Exception e) {
      logger.error("Failed to remove sandbox: url={}, user_code={}, chat_id={}", url, userCode, chatId, e);
    }
  }

  /**
   * 读取响应体
   */
  private String readResponseBody(HttpStatusCodeException e) {
    try {
      return e.getResponseBodyAsString();
    }
    catch (Exception e2) {
      logger.warn("Failed to read response body", e);
    }
    return "";
  }

  /**
   * 提取错误信息
   */
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  @SuppressWarnings("PMD.GuardLogStatement")
  private String extractErrorMessage(URI url, HttpStatusCodeException e, String body) {
    try {
      if (body.startsWith("{")) {
        JsonNode json = JsonUtil.readTree(body);
        // 有时返回 error: {"error":"sandbox not found"}
        String error = json.path("error").asText(null);
        if (StringUtils.isNotEmpty(error)) {
          return error;
        }
        // 有时返回 message: {"code":401,"message":"Unauthorized: invalid session"}
        String message = json.path("message").asText(null);
        if (StringUtils.isNotEmpty(message)) {
          return message;
        }
      }
    }
    catch (Exception e2) {
      logger.warn("Failed to extract error message: url={}, status={}, response={}", url, e.getStatusCode().value(), body);
    }
    return "status=" + e.getStatusCode().value() + ", response=" + body;
  }


  /**
   * 构造接口地址
   */
  private String buildApiUrl(String path) {
    String baseUrl = SystemParameter.AGENT_POOL_API_URL.getValueFromDb();
    Assert.hasLength(baseUrl, "未配置 Agent Pool 接口地址");
    return baseUrl.endsWith("/") ? baseUrl + path : baseUrl + "/" + path;
  }

  /**
   * 构造请求头
   */
  private HttpHeaders buildHeaders() {
    String key = SystemParameter.AGENT_POOL_API_KEY.getValueFromDb();
    Assert.hasLength(key, "未配置 Agent Pool 接口密钥");
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(key);
    return headers;
  }


  /**
   * API 响应结果
   */
  @Getter
  @Setter
  @ToString
  public static final class ApiResult<T> {
    /** 响应编码, 200 表示成功 */
    private Integer code;
    /** 响应描述 */
    private String message;
    /** 响应数据 */
    private T data;

    /**
     * 是否成功
     */
    @JsonIgnore
    public boolean isSuccess() {
      return this.code != null && this.code == 200;
    }
  }
}
