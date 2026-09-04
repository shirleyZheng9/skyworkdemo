package com.iwhalecloud.bote.beyond;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.BeyondProperties;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogItem;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondResourcePublishData;
import com.iwhalecloud.bote.dto.beyond.BeyondResourcePublishRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 百应 API 客户端
 *
 * @author bianjp
 * @since 2025-07-18
 */
@Component
@ConditionalOnBooleanProperty("beyond.enabled")
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class BeyondApiClient {
  private static final Logger logger = LoggerFactory.getLogger(BeyondApiClient.class);

  private final BeyondAuthHelper beyondAuthHelper;
  private final BeyondProperties beyondProperties;

  /**
   * 下载百应的文件
   *
   * @param url 下载地址
   * @return 临时文件。不确定文件大小，因此使用临时文件。注意调用方必须确保删除临时文件
   */
  public File downloadFile(String url) {
    Assert.hasLength(url, "文件下载地址不能为空");
    //noinspection HttpUrlsUsage
    Assert.isTrue(url.startsWith("http://") || url.startsWith("https://"), "文件下载地址不合法");
    File file;
    boolean success = false;
    try {
      file = Files.createTempFile("beyond-file-", ".tmp").toFile();
    }
    catch (IOException e) {
      throw new BssException("创建临时文件失败: " + ExpUtil.getMsg(e), e);
    }
    try {
      HttpUtil.getRestTemplate().execute(url, HttpMethod.GET,
        beyondAuthHelper::addTokenHeader,
        response -> {
          FileUtils.copyInputStreamToFile(response.getBody(), file);
          return null;
        });
      success = true;
      return file;
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to download beyond file: url={}, staus={}, response={}", url, e.getStatusCode().value(), responseBody);
      throw new BssException("下载百应文件失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to download beyond file: url={}", url, e);
      throw new BssException("下载百应文件失败: " + ExpUtil.getMsg(e), e);
    }
    finally {
      // 失败时删除临时文件
      if (!success) {
        FileUtils.deleteQuietly(file);
      }
    }
  }

  /**
   * 查询目录树信息
   *
   * @param request 目录检索请求参数
   * @return 目录树响应数据
   */
  @Nullable
  public BeyondResponse<List<BeyondCatalogItem>> queryCatalogTree(BeyondCatalogTreeRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    String apiUrl = beyondProperties.getCatalogTreeApiUrl();
    Assert.notNull(apiUrl, () -> "非法的百应接口地址: " + beyondProperties.getCatalogTreeApiUrl());
    try {
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      return HttpUtil.post(apiUrl, request, new ParameterizedTypeReference<BeyondResponse<List<BeyondCatalogItem>>>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to query beyond catalog tree: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(), responseBody);
      throw new BssException("查询百应目录列表失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to query beyond catalog tree: url={}", apiUrl, e);
      throw new BssException("查询百应目录列表失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 查询组织树信息
   *
   * @param request 组织检索请求参数
   * @return 组织树响应数据
   */
  @Nullable
  public BeyondResponse<List<BeyondOrgItem>> getOrgTree(BeyondOrgTreeRequest request) {
    String apiUrl = beyondProperties.getOrgTreeApiUrl();
    Assert.notNull(apiUrl, () -> "非法的百应接口地址: " + beyondProperties.getOrgTreeApiUrl());
    try {
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      return HttpUtil.post(apiUrl, request, new ParameterizedTypeReference<BeyondResponse<List<BeyondOrgItem>>>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to get beyond org tree: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(), responseBody);
      throw new BssException("查询百应组织树失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to get beyond org tree: url={}", apiUrl, e);
      throw new BssException("查询百应组织树失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 查询组织管理员信息
   *
   * @param request 组织管理员检索请求参数
   * @return 组织管理员响应数据
   */
  @Nullable
  public BeyondResponse<List<BeyondOrgAdminItem>> getOrgAdmin(BeyondOrgAdminRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getOrgId(), "组织ID不能为空");
    String apiUrl = beyondProperties.getOrgAdminApiUrl();
    Assert.notNull(apiUrl, () -> "非法的百应接口地址: " + beyondProperties.getOrgAdminApiUrl());
    try {
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      return HttpUtil.post(apiUrl, request, new ParameterizedTypeReference<BeyondResponse<List<BeyondOrgAdminItem>>>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to get beyond org admin: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(), responseBody);
      throw new BssException("查询百应组织管理员失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to get beyond org admin: url={}", apiUrl, e);
      throw new BssException("查询百应组织管理员失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 发布资源到百应平台
   *
   * @param request 资源发布请求参数
   * @return 资源发布响应数据
   */
  @Nullable
  public BeyondResponse<List<BeyondResourcePublishData>> publishResource(BeyondResourcePublishRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    String apiUrl = beyondProperties.getResourcePublishApiUrl();
    Assert.notNull(apiUrl, () -> "非法的百应接口地址: " + beyondProperties.getResourcePublishApiUrl());
    try {
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      return HttpUtil.post(apiUrl, request, new ParameterizedTypeReference<BeyondResponse<List<BeyondResourcePublishData>>>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to publish beyond resource: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(), responseBody);
      throw new BssException("发布百应资源失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to publish beyond resource: url={}", apiUrl, e);
      throw new BssException("发布百应资源失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 发布数字员工到百应平台
   *
   * @param request 资源发布请求参数
   * @return 资源发布响应数据
   */
  @Nullable
  public BeyondResponse<List<BeyondResourcePublishData>> publishEmployee(BeyondResourcePublishRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    String apiUrl = beyondProperties.getPublishEmployeeApiUrl();
    Assert.notNull(apiUrl, () -> "非法的百应接口地址: " + beyondProperties.getPublishEmployeeApiUrl());
    try {
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      return HttpUtil.post(apiUrl, request, new ParameterizedTypeReference<>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to publish beyond Employee: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(), responseBody);
      throw new BssException("发布百应数字员工失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to publish beyond resource: url={}", apiUrl, e);
      throw new BssException("发布百应数字员工失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 获取组织详情信息
   *
   * @param request 组织详情请求参数
   * @return 组织详情响应数据
   */
  @Nullable
  public BeyondResponse<BeyondOrgDetailItem> getOrgDetail(BeyondOrgDetailRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getOrgId(), "组织ID不能为空");
    String apiUrl = beyondProperties.getOrgDetailApiUrl();
    Assert.notNull(apiUrl, () -> "非法的百应接口地址: " + beyondProperties.getOrgDetailApiUrl());
    try {
      HttpHeaders headers = beyondAuthHelper.buildHttpHeaders();
      return HttpUtil.post(apiUrl, request, new ParameterizedTypeReference<>() {
      }, headers);
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      String errorMsg = extractErrorMsg(responseBody, e);
      logger.error("Failed to get beyond org detail: url={}, status={}, response={}", apiUrl, e.getStatusCode().value(), responseBody);
      throw new BssException("获取百应组织详情失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to get beyond org detail: url={}", apiUrl, e);
      throw new BssException("获取百应组织详情失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 从响应体提取错误信息
   */
  private String extractErrorMsg(String responseBody, HttpStatusCodeException httpStatusCodeException) {
    if (Strings.CS.startsWith(responseBody, "{")) {
      try {
        JsonNode node = JsonUtil.getObjectMapper().readTree(responseBody);
        String msg = node.path("msg").asText("");
        if (StringUtils.isEmpty(msg)) {
          msg = node.path("resultMsg").asText("");
        }
        if (StringUtils.isNotEmpty(msg)) {
          return msg;
        }
      }
      catch (JsonProcessingException e) {
        // 忽略解析失败
      }
    }
    // 提取不到时使用 HTTP 状态码作为错误信息
    return httpStatusCodeException.getStatusCode().toString();
  }
}
