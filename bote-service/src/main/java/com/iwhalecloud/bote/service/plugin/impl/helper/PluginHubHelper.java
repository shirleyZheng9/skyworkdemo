package com.iwhalecloud.bote.service.plugin.impl.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.plugin.PluginAuthDTO;
import com.iwhalecloud.bote.dto.plugin.SimplePluginAuthParam;
import com.iwhalecloud.bote.dto.plugin.params.PluginAuthQueryParams;
import com.iwhalecloud.bote.dto.plugin.request.QueryCatalogRequest;
import com.iwhalecloud.bote.dto.plugin.request.QueryPluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.TestPluginToolRequest;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 插件市场 API 调用辅助类
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Component
@RequiredArgsConstructor
public class PluginHubHelper {
  private static final Logger logger = LoggerFactory.getLogger(PluginHubHelper.class);

  // @formatter:off
  private static final String queryCatalogTreeApiUrl = "/plugin/manager/catalog/queryCatalogTree";
  private static final String queryAuthPluginPageApiUrl = "/plugin/manager/plugin/queryAuthPluginPage";
  private static final String getPluginDefinitionApiUrl = "/plugin/manager/plugin/getPluginDefinition";
  private static final String getPluginToolsApiUrl = "/plugin/manager/plugin/getPluginTools";
  private static final String callPluginToolApiUrl = "/plugin/manager/plugin/callPluginTool";
  private static final String createPortalUserApiUrl = "/plugin/manager/portalUser/createPortalUser";
  private static final String subscribePluginApiUrl = "/plugin/manager/pluginSubscribe/subscribeWithinAuth";
  private static final String unsubscribePluginApiUrl = "/plugin/manager/pluginSubscribe/unsubscribe";
  private static final String getPluginInfoListUrl = "/plugin/manager/plugin/getPluginInfoList";
  private static final String queryPluginIconUrl = "/plugin/manager/plugin/queryPluginIcon";
  private static final String countAuthPluginByCatalogApiUrl = "/plugin/manager/plugin/countAuthPluginByCatalog";

  /** 插件认证信息 */
  private static final String getPluginAuthJsonApiUrl = "/plugin/manager/pluginAuthJson/getPluginAuthJson";
  private static final String savePluginAuthJsonApiUrl = "/plugin/manager/pluginAuthJson/savePluginAuthJson";
  private static final String queryPluginAuthJsonListApiUrl = "/plugin/manager/pluginAuthJson/queryPluginAuthJsonList";
  // @formatter:on

  private final TenantSettingInfoCache tenantSettingInfoCache;

  /**
   * 查询指定类型的目录数据
   */
  public List<CatalogDTO> queryCatalogTree(QueryCatalogRequest request) {
    Map<String, Object> params = new HashMap<>(4);
    params.put("catalogType", request.getCatalogType());
    params.put("catalogName", request.getCatalogName());
    params.put("catalogId", request.getCatalogId());
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(request.getTenantId(), requestBody, HttpMethod.POST, queryCatalogTreeApiUrl, "queryCatalogTree", "查询目录");
    if (res.isEmpty()) {
      return List.of();
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 新增门户开发者，返回密钥
   */
  public String createPortalUser(String portalCode, String userName, String realName) {
    Map<String, Object> params = new HashMap<>(4);
    params.put("portalCode", portalCode);
    params.put("userName", userName);
    params.put("realName", realName);
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(null, requestBody, HttpMethod.POST, createPortalUserApiUrl, "createPortalUser", "新增门户开发者");
    if (res.isEmpty()) {
      return null;
    }
    return res.path("apiKey").asText(null);
  }

  /**
   * 查询已授权的插件列表（分页）
   */
  public PageInfo<PluginDefinition> queryAuthPluginPage(QueryPluginRequest request) {
    Map<String, Object> params = new HashMap<>(8);
    params.put("searchContent", request.getSearchContent());
    params.put("catalogItemId", request.getCatalogItemId());
    params.put("pluginType", request.getPluginType());
    params.put("pluginSubType", request.getPluginSubType());
    params.put("subscribeStatus", request.getSubscribeStatus());
    params.put("pageNum", request.getPageNum());
    params.put("pageSize", request.getPageSize());
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(request.getTenantId(), requestBody, HttpMethod.POST, queryAuthPluginPageApiUrl, "queryAuthPluginPage",
      "查询已授权的插件列表");
    if (res.isEmpty()) {
      PageInfo<PluginDefinition> pageInfo = new PageInfo<>();
      pageInfo.setPageNum(request.getPageNum());
      pageInfo.setPageSize(request.getPageSize());
      return pageInfo;
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 根据插件分类统计插件数量
   */
  public Map<String, Long> countAuthPluginByCatalog(QueryPluginRequest request) {
    Map<String, Object> params = new HashMap<>(8);
    params.put("searchContent", request.getSearchContent());
    params.put("pluginType", request.getPluginType());
    params.put("pluginSubType", request.getPluginSubType());
    params.put("subscribeStatus", request.getSubscribeStatus());
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(request.getTenantId(), requestBody, HttpMethod.POST, countAuthPluginByCatalogApiUrl, "countAuthPluginByCatalog",
      "根据插件分类统计插件数量");
    if (res.isEmpty()) {
      return Map.of();
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 查询插件定义
   */
  public PluginDefinition getPluginDefinition(Long tenantId, Long pluginId, @Nullable Boolean includeTools) {
    // 请求体
    Map<String, Object> params = new HashMap<>(8);
    params.put("pluginId", pluginId);
    params.put("includeTools", includeTools);
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    // 执行请求调用
    JsonNode res = callApi(tenantId, requestBody, HttpMethod.POST, getPluginDefinitionApiUrl, "getPluginDefinition", "查询插件定义");
    if (res.isEmpty()) {
      return null;
    }
    return JsonUtil.convert(res, PluginDefinition.class);
  }

  /**
   * 批量查询插件信息列表
   */
  public List<PluginDefinition> getPluginInfoList(Long tenantId, List<Long> pluginIds) {
    if (pluginIds.isEmpty()) {
      return Collections.emptyList();
    }
    // 请求体
    Map<String, Object> params = new HashMap<>(8);
    params.put("pluginIds", pluginIds);
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    // 执行请求调用
    JsonNode res = callApi(tenantId, requestBody, HttpMethod.POST, getPluginInfoListUrl, "getPluginInfoList", "查询插件信息列表");
    if (res.isEmpty()) {
      return List.of();
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 查询插件图标
   */
  public String queryPluginIcon(Long tenantId, Long pluginId) {
    String url = queryPluginIconUrl + "?pluginId=" + pluginId;
    JsonNode res = callApi(tenantId, null, HttpMethod.GET, url, "queryPluginIcon", "查询插件图标");
    if (res.isEmpty()) {
      return null;
    }
    return res.path("pluginIcon").asText(null);
  }

  /**
   * 查询插件的工具列表
   */
  public List<PluginToolSpec> getPluginTools(Long tenantId, Long pluginId) {
    // 请求体
    Map<String, Object> params = new HashMap<>(8);
    params.put("pluginId", pluginId);
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    // 执行请求调用
    JsonNode res = callApi(tenantId, requestBody, HttpMethod.POST, getPluginToolsApiUrl, "getPluginTools", "查询插件的工具列表");
    if (res.isEmpty()) {
      return List.of();
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 调用插件工具
   */
  public Object callPluginTool(TestPluginToolRequest request) {
    Map<String, Object> params = new HashMap<>(8);
    params.put("pluginId", request.getPluginId());
    params.put("toolName", request.getToolName());
    params.put("toolParams", request.getToolParams());
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(request.getTenantId(), requestBody, HttpMethod.POST, callPluginToolApiUrl, "callPluginTool", "调用插件工具");
    if (res.isEmpty()) {
      return List.of();
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 订阅插件
   */
  public void subscribePlugin(Long tenantId, Long pluginId, List<SimplePluginAuthParam> authParams) {
    Map<String, Object> params = new HashMap<>(8);
    params.put("pluginId", pluginId);
    params.put("authParams", authParams);
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    callApi(tenantId, requestBody, HttpMethod.POST, subscribePluginApiUrl, "subscribeWithinAuth", "订阅插件");
  }

  /**
   * 取消订阅插件
   */
  public void unsubscribePlugin(Long tenantId, Long pluginId) {
    String url = unsubscribePluginApiUrl + "?pluginId=" + pluginId;
    callApi(tenantId, null, HttpMethod.GET, url, "unsubscribe", "取消订阅插件");
  }

  /**
   * 查询单个插件认证信息
   */
  public PluginAuthDTO getPluginAuthJson(Long tenantId, Long authId) {
    String url = getPluginAuthJsonApiUrl + "?authId=" + authId;
    JsonNode res = callApi(tenantId, null, HttpMethod.GET, url, "getPluginAuthJson", "查询单个插件认证信息");
    if (res.isEmpty()) {
      return null;
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 保存插件认证信息
   */
  public PluginAuthDTO savePluginAuthJson(PluginAuthDTO auth) {
    Map<String, Object> params = new HashMap<>(8);
    params.put("authId", auth.getAuthId());
    params.put("pluginId", auth.getPluginId());
    params.put("params", auth.getParams());
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(auth.getTenantId(), requestBody, HttpMethod.POST, savePluginAuthJsonApiUrl, "savePluginAuthJson", "保存插件认证信息");
    if (res.isEmpty()) {
      return null;
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  /**
   * 查询插件认证信息
   */
  public List<PluginAuthDTO> queryPluginAuthJsonList(PluginAuthQueryParams queryParams) {
    Map<String, Object> params = new HashMap<>(4);
    params.put("searchContent", queryParams.getSearchContent());
    JsonNode requestBody = JsonUtil.convert(params, JsonNode.class);
    JsonNode res = callApi(queryParams.getTenantId(), requestBody, HttpMethod.POST, queryPluginAuthJsonListApiUrl, "queryPluginAuthJsonList", "查询插件认证信息");
    if (res.isEmpty()) {
      return null;
    }
    return JsonUtil.convert(res, new TypeReference<>() {
    });
  }

  private JsonNode callApi(Long tenantId, JsonNode requestBody, HttpMethod method, String apiUrl, String operationCode, String operationText) {
    Assert.isTrue(SystemParameter.PLUGIN_ENABLED.getBooleanValueFromDb(), "未启用插件市场功能，请联系系统管理员");

    HttpEntity<?> requestEntity = buildRequestEntity(tenantId, requestBody);
    JsonNode res;
    try {
      String url = getUrl(apiUrl);
      res = HttpUtil.getRestTemplate().exchange(url, method, requestEntity, JsonNode.class).getBody();
    }
    catch (Exception e) {
      logger.error("Failed to {}: request={}", operationCode, requestBody, e);
      throw new BssException("插件市场" + operationText + "失败: " + ExpUtil.getMsg(e), e);
    }

    if (res == null) {
      logger.error("Failed to {}, empty response: request={}", operationCode, requestBody);
      throw new BssException("插件市场" + operationText + "失败，响应为空");
    }
    if (!"0".equals(res.path("resultCode").asText())) {
      logger.error("Failed to {}: request={}, response={}", operationCode, requestBody, res);
      throw new BssException("插件市场" + operationText + "失败: " + res.path("resultMsg").asText(""));
    }
    return res.path("resultObject");
  }

  private String getUrl(String apiUrl) {
    String url = SystemParameter.PLUGIN_API_URL.getValueFromDb();
    Assert.hasText(url, "未配置插件市场 API 地址");
    url = StringUtils.stripEnd(url, "/");
    return url + apiUrl;
  }

  /**
   * 构造带有令牌信息的请求头部
   */
  private HttpHeaders buildHeader(@Nullable Long tenantId) {
    HttpHeaders headers = new HttpHeaders();
    String apikey;
    if (tenantId != null) {
      apikey = tenantSettingInfoCache.getPluginApiKey(tenantId);
    }
    else {
      apikey = SystemParameter.PLUGIN_API_KEY.getValueFromDb();
    }
    Assert.hasText(apikey, "未配置插件市场的 API 密钥");
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apikey);
    return headers;
  }

  /**
   * 构造请求对象
   */
  private HttpEntity<?> buildRequestEntity(@Nullable Long tenantId, @Nullable Object body) {
    HttpHeaders headers = buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
      return new HttpEntity<>(body, headers);
    }
    return new HttpEntity<>(headers);
  }
}
