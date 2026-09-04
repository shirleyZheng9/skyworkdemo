package com.iwhalecloud.bote.service.plugin.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.PluginHubCache;
import com.iwhalecloud.bote.cache.PluginHubMcpClientCache;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.ToolInputType;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams.PluginToolParams;
import com.iwhalecloud.bote.dto.plugin.SimplePluginAuthParam;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition.PluginGatewayDTO;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition.PluginToolDTO;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bote.mapper.plugin.PluginManageMapper;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.dto.request.CallToolRequest;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import com.iwhalecloud.bote.service.plugin.IPlugin;
import com.iwhalecloud.bote.service.plugin.IPluginEngine;
import com.iwhalecloud.bote.service.plugin.PluginFactory;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginCallHelper;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginParameterHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.LogUtil;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 插件执行引擎
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class PluginEngine implements IPluginEngine {

  private static final Logger logger = LoggerFactory.getLogger(PluginEngine.class);

  // @formatter:off
  private final PluginManageMapper pluginManageMapper;
  private final PluginHubCache pluginHubCache;
  private final PluginHubMcpClientCache mcpClientCache;
  // @formatter:on

  @Override
  public Object execute(PluginExecuteParams params) {
    if (BooleanUtils.isTrue(params.getIsPluginHub())) {
      return doExecute(params);
    }
    else {
      PluginDTO plugin = pluginManageMapper.getPlugin(params.getTenantId(), params.getPluginId());
      if (plugin == null) {
        throw new BssException("插件不存在, pluginId=" + params.getPluginId());
      }
      if (!PluginConsts.PLUGIN_STATUS_ENABLE.equals(plugin.getPluginStatus())) {
        throw new BssException("插件未启用, pluginId=" + params.getPluginId());
      }
      IPlugin runner = PluginFactory.getPlugin(plugin.getPluginCode());
      params.setNeedModel(plugin.getNeedModel());
      return runner.execute(params);
    }
  }

  /**
   * 对接插件市场
   */
  @SuppressWarnings("unchecked")
  private Object doExecute(PluginExecuteParams params) {
    Long tenantId = params.getTenantId();
    Long pluginId = params.getPluginId();
    PluginDefinition plugin = pluginHubCache.get(tenantId, pluginId);
    Assert.notNull(plugin, () -> "查询不到有效插件定义，pluginId=" + pluginId);
    PluginGatewayDTO gateway = plugin.getGateway();
    Assert.notNull(gateway, () -> "查询不到有效网关定义，pluginId=" + pluginId);

    PluginToolSpec tool = IterableUtils.find(plugin.getTools(), p -> params.getToolName().equals(p.getName()));
    Assert.notNull(tool, () -> "查询不到有效的工具，pluginId=" + pluginId + ", toolName=" + params.getToolName());

    String baseUrl = gateway.getUrl();
    String serverUrl = StringUtils.stripEnd(plugin.getRelativePath(), "/");
    if (PluginConsts.GATEWAY_TYPE_HIGRESS.equals(gateway.getGatewayType())) {
      // Higress 类型，需要调整服务地址
      baseUrl = gateway.getHigressRuntimeUrl();
    }
    if (PluginConsts.PLUGIN_TYPE_MCP.equals(plugin.getPluginType())) {
      // 执行 MCP 服务
      McpClient mcpClient = mcpClientCache.getMcpClient(params.getTenantId(), pluginId);
      // 工具调用、tool call 不同模式下，入参存在偏差
      Map<String, Object> body = params.getParams().containsKey("body")
        ? (Map<String, Object>) MapUtils.getMap(params.getParams(), "body")
        : params.getParams();
      CallToolResult result = mcpClient.callTool(new CallToolRequest(params.getToolName(), body));
      return JsonUtil.convert(result, new TypeReference<Map<String, Object>>() {
      });
    }
    else {
      if (params.getIsToolCall()) {
        // tool call 模式下，特殊参数格式内容，进行值处理
        PluginParameterHelper.resolveParameterValue(tool, params);
      }

      // 请求内容格式
      MediaType mediaType = MediaType.APPLICATION_JSON;
      if (tool.getInput() != null && ToolInputType.MULTIPART == tool.getInput().getBodyType()) {
        mediaType = MediaType.MULTIPART_FORM_DATA;
        // 调整 body 参数，file 改为对应的文件流，map 调整为 MultiValueMap
        PluginParameterHelper.buildMultipartBody(tool, params);
      }

      // 执行工具集服务
      if (PluginConsts.DISCOVERY_TYPE_SDK.equals(plugin.getDiscoveryToolType())) {
        // 自动发现服务，按照规则构造请求地址
        serverUrl = serverUrl + "/" + tool.getName();
        // 认证参数，统一加 x-plugin-config- 前缀
        for (SimplePluginAuthParam authParam : CollectionUtils.emptyIfNull(plugin.getAuthParams())) {
          if (!authParam.getCode().toLowerCase().startsWith("x-plugin-config-")) {
            authParam.setCode(("x-plugin-config-" + authParam.getCode()).toUpperCase());
          }
        }
        return callSdkApi(baseUrl, serverUrl, plugin.getAuthParams(), params.getParams(), mediaType);
      }
      else {
        PluginToolDTO api = IterableUtils.find(plugin.getApiTools(), p -> params.getToolName().equals(p.getToolName()));
        Assert.notNull(api, () -> "查询不到有效的工具，pluginId=" + pluginId + ", toolName=" + params.getToolName());
        return callApi(gateway, api, plugin.getAuthParams(), params.getParams());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Object callSdkApi(String baseUrl, String serverUrl, List<SimplePluginAuthParam> authParams, Map<String, Object> toolParams,
    MediaType mediaType) {
    Map<String, Object> header = (Map<String, Object>) MapUtils.getMap(toolParams, "header");
    Map<String, Object> path = (Map<String, Object>) MapUtils.getMap(toolParams, "path");
    // 请求方法，约定都是 POST
    HttpMethod method = HttpMethod.valueOf("POST");
    // 请求地址
    URI uri = PluginCallHelper.buildUrl(baseUrl, serverUrl, header, path);
    // 请求头
    HttpHeaders headers = PluginCallHelper.buildHeaders(header);
    headers.setContentType(mediaType);
    for (SimplePluginAuthParam authParam : CollectionUtils.emptyIfNull(authParams)) {
      if (!"header".equals(authParam.getType())) {
        continue;
      }
      headers.set(authParam.getCode(), Objects.toString(authParam.getValue(), ""));
    }
    // 请求体
    Object body = toolParams.get("body");
    BodyBuilder builder = RequestEntity.method(method, uri).headers(headers);
    RequestEntity<?> requestEntity = body == null ? builder.build() : builder.body(body);
    return doCallTool(requestEntity);
  }

  private Object callApi(PluginGatewayDTO gateway, PluginToolDTO tool, List<SimplePluginAuthParam> authParams, Map<String, Object> toolParams) {
    // 转换入参结构
    PluginToolParams convertedParams = PluginCallHelper.convertRequestParams(tool, toolParams);

    // 补充头部认证参数
    Map<String, Object> headers = convertedParams.getHeader();
    for (SimplePluginAuthParam authParam : CollectionUtils.emptyIfNull(authParams)) {
      if (!"header".equals(authParam.getType())) {
        continue;
      }
      if (MapUtils.isEmpty(headers)) {
        headers = new HashMap<>();
      }
      headers.put(authParam.getCode(), Objects.toString(authParam.getValue(), ""));
    }
    convertedParams.setHeader(headers);

    // 构造请求
    RequestEntity<?> requestEntity = PluginCallHelper.buildRequest(gateway, tool, convertedParams);
    Object data = doCallTool(requestEntity);
    // 转换出参结构
    if (data != null && tool.getResponse() != null) {
      data = ParamConverterUtil.convert("", tool.getResponse(), data);
    }
    return data;
  }

  private Object doCallTool(RequestEntity<?> requestEntity) {
    HttpMethod method = requestEntity.getMethod();
    URI uri = requestEntity.getUrl();
    logger.debug("Request start: {} {} data={}", method, uri, LogUtil.limitLength(requestEntity.getBody()));
    // 调用接口
    Object data;
    ResponseEntity<Object> responseEntity;
    try {
      responseEntity = HttpUtil.getRestTemplate().exchange(requestEntity, Object.class);
      data = responseEntity.getBody();
      logger.debug("Request end: {} url={}, status={}, response={}", method, uri, responseEntity.getStatusCode().value(), LogUtil.limitLength(data));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: {} url={}, status={}", method, uri, e.getStatusCode());
      throw new BssException("HTTP 请求失败: url=" + uri + ", error=" + e.getStatusCode(), e);
    }
    catch (Exception e) {
      logger.error("Request failed: {} url={}", method, uri, e);
      throw new BssException("HTTP 请求失败: url=" + uri + ", error=" + ExpUtil.getMsg(e), e);
    }
    return data;
  }
}
