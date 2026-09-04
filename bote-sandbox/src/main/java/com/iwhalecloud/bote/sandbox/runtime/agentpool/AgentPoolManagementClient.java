package com.iwhalecloud.bote.sandbox.runtime.agentpool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.sandbox.dto.agentpool.AgentPoolApiResult;
import com.iwhalecloud.bote.sandbox.dto.agentpool.CreateSandboxRequest;
import com.iwhalecloud.bote.sandbox.dto.agentpool.CreateSandboxResponse;
import com.iwhalecloud.bote.sandbox.dto.agentpool.SandboxDetailInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * AgentPool 管理客户端
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class AgentPoolManagementClient {
  private static final Logger logger = LoggerFactory.getLogger(AgentPoolManagementClient.class);

  /** 密钥 */
  private final String apiKey;
  /** 创建沙箱接口地址 */
  private final String createSandboxUrl;
  /** 查询沙箱详情接口地址 */
  private final String querySandboxUrl;
  /** 删除沙箱接口地址 */
  private final String removeSandboxUrl;
  /** 续期沙箱接口地址 */
  private final String renewSandboxUrl;

  public AgentPoolManagementClient(String baseUrl, @Nullable String apiKey) {
    this.apiKey = apiKey;
    this.createSandboxUrl = baseUrl + "/common_sandbox/create";
    this.querySandboxUrl = baseUrl + "/sandbox/detail";
    this.removeSandboxUrl = baseUrl + "/sandbox/remove";
    this.renewSandboxUrl = baseUrl + "/sandbox/renew";
  }

  /**
   * 创建沙箱
   */
  public CreateSandboxResponse createSandbox(CreateSandboxRequest request) {
    AgentPoolApiResult<CreateSandboxResponse> result;
    try {
      result = HttpUtil.post(createSandboxUrl, request, new ParameterizedTypeReference<>() {
      }, buildHeaders());
    }
    catch (BssException e) {
      AgentPoolApiResult<Void> errorResult = extractErrorResponse(e);
      if (errorResult != null && StringUtils.isNotEmpty(errorResult.getMessage())) {
        logger.error("Failed to create sandbox: request={}, error={}", request, errorResult.getMessage());
        e.setFailMsg("创建沙箱失败: " + errorResult.getMessage());
      }
      else {
        logger.error("Failed to create sandbox: request={}", request, e);
        e.setFailMsg("创建沙箱失败: " + e.getFailMsg());
      }
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to create sandbox: request={}", request, e);
      throw new BssException("创建沙箱失败: " + ExpUtil.getMsg(e), e);
    }

    Assert.notNull(result, "创建沙箱失败，响应为空");
    Assert.isTrue(result.isSuccess(), () -> "创建沙箱失败: " + result.getMessage());
    CreateSandboxResponse response = result.getData();
    Assert.notNull(response, "创建沙箱失败，响应数据为空");
    Assert.notNull(response.getSandboxId(), "创建沙箱失败，未返回沙箱 ID");
    Assert.hasLength(response.getExecdEndpoint(), "创建沙箱失败，未返回 execd_endpoint");
    return response;
  }

  /**
   * 获取沙箱详情
   */
  @Nullable
  public SandboxDetailInfo getSandboxDetail(String sandboxId) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("sandbox_id", sandboxId);
    AgentPoolApiResult<SandboxDetailInfo> result;
    try {
      result = HttpUtil.get(querySandboxUrl, params, new ParameterizedTypeReference<>() {
      }, buildHeaders());
    }
    catch (BssException e) {
      AgentPoolApiResult<Void> errorResult = extractErrorResponse(e);
      if (errorResult != null && StringUtils.isNotEmpty(errorResult.getMessage())) {
        // 沙箱不存在时响应编码为 400
        if (("sandbox " + sandboxId + " not found").equals(errorResult.getMessage())) {
          return null;
        }
        logger.error("Failed to query sandbox: sandboxId={}, error={}", sandboxId, errorResult.getMessage());
        e.setFailMsg("查询沙箱失败: " + errorResult.getMessage());
      }
      else {
        logger.error("Failed to query sandbox: sandboxId={}", sandboxId, e);
        e.setFailMsg("查询沙箱失败: " + e.getFailMsg());
      }
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to query sandbox: sandboxId={}", sandboxId, e);
      throw new BssException("查询沙箱失败: " + ExpUtil.getMsg(e), e);
    }

    Assert.notNull(result, "查询沙箱失败，响应为空");
    Assert.isTrue(result.isSuccess(), () -> "查询沙箱失败: " + result.getMessage());
    return result.getData();
  }

  /**
   * 删除沙箱
   */
  public void removeSandbox(String sandboxId) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.set("sandbox_id", sandboxId);
    AgentPoolApiResult<Void> result;
    try {
      result = HttpUtil.post(removeSandboxUrl, params, new ParameterizedTypeReference<>() {
      }, buildHeaders());
    }
    catch (Exception e) {
      result = extractErrorResponse(e);
      if (result == null) {
        logger.error("Failed to remove sandbox: sandboxId={}", sandboxId, e);
        return;
      }
      throw e;
    }

    if (result == null) {
      logger.error("Failed to remove sandbox, empty response: sandboxId={}", sandboxId);
    }
    else if (!result.isSuccess()) {
      logger.error("Failed to remove sandbox: sandboxId={}, error={}", sandboxId, result.getMessage());
    }
  }

  /**
   * 续期沙箱
   */
  public void renewSandbox(String sandboxId, Instant expiresAt) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.set("sandbox_id", sandboxId);
    params.set("expires_at", expiresAt.toString());
    AgentPoolApiResult<Void> result;
    try {
      result = HttpUtil.post(renewSandboxUrl, params, new ParameterizedTypeReference<>() {
      }, buildHeaders());
    }
    catch (Exception e) {
      result = extractErrorResponse(e);
      if (result == null) {
        logger.error("Failed to renew sandbox: sandboxId={}", sandboxId, e);
        return;
      }
    }

    if (result == null) {
      logger.error("Failed to renew sandbox, empty response: sandboxId={}", sandboxId);
    }
    else if (!result.isSuccess()) {
      logger.error("Failed to renew sandbox: sandboxId={}, error={}", sandboxId, result.getMessage());
    }
  }

  /**
   * 构造请求头
   */
  private HttpHeaders buildHeaders() {
    // HttpUtil 内部会修改请求头，无法安全复用
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    if (StringUtils.isNotEmpty(apiKey)) {
      headers.setBearerAuth(apiKey);
    }
    return headers;
  }

  /**
   * HTTP 请求失败时从响应体中提取错误信息
   */
  @Nullable
  private AgentPoolApiResult<Void> extractErrorResponse(Exception e) {
    if (e.getCause() instanceof HttpStatusCodeException httpStatusCodeException) {
      try {
        String responseBody = httpStatusCodeException.getResponseBodyAsString();
        if (responseBody.startsWith("{") && responseBody.contains("\"code\":")) {
          return JsonUtil.parseJson(responseBody, new TypeReference<>() {
          });
        }
      }
      catch (Exception ex) {
        // 忽略解析异常
        return null;
      }
    }
    return null;
  }
}
