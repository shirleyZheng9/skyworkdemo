package com.iwhalecloud.bote.service.plugin.impl.helper;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams.PluginToolParams;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition.PluginGatewayDTO;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition.PluginToolDTO;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 执行工具辅助类
 *
 * @author chen.linfa
 * @since 2025-12-23
 */
public final class PluginCallHelper {

  private PluginCallHelper() {
  }

  /**
   * 转换请求参数
   */
  public static PluginToolParams convertRequestParams(PluginToolDTO tool, Map<String, Object> params) {
    if (tool.getRequest() == null || !tool.getRequest().hasChildren()) {
      return new PluginToolParams();
    }
    PluginToolParams convertedParams = new PluginToolParams();
    for (ParameterSpec child : tool.getRequest().getChildren()) {
      // path, header, query 必须是对象，body 可以是任意类型
      if ("path".equals(child.getName())) {
        convertedParams.setPath(ParamConverterUtil.convertObject(child.getName(), child, params.get("path")));
      }
      else if ("header".equals(child.getName())) {
        convertedParams.setHeader(ParamConverterUtil.convertObject(child.getName(), child, params.get("header")));
      }
      else if ("query".equals(child.getName())) {
        convertedParams.setQuery(ParamConverterUtil.convertObject(child.getName(), child, params.get("query")));
      }
      else if ("body".equals(child.getName())) {
        convertedParams.setBody(ParamConverterUtil.convert(child.getName(), child, params.get("body")));
      }
    }
    return convertedParams;
  }

  /**
   * 构造请求
   */
  public static RequestEntity<?> buildRequest(PluginGatewayDTO gateway, PluginToolDTO tool, PluginToolParams params) {
    Assert.hasLength(gateway.getUrl(), () -> "插件关联的网关未配置地址: pluginId=" + tool.getPluginId());

    // 请求方法
    HttpMethod method = HttpMethod.valueOf(tool.getReqMethod().toUpperCase());
    // 请求地址
    URI uri = buildUrl(gateway.getUrl(), tool.getRelativePath(), params.getPath(), params.getQuery());
    // 请求头
    HttpHeaders headers = buildHeaders(params.getHeader());
    // 请求体
    Object body = method == HttpMethod.GET ? null : params.getBody();
    if (MediaType.MULTIPART_FORM_DATA_VALUE.equals(tool.getBodyType()) && body != null) {
      body = PluginParameterHelper.buildMultipartBody(body);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    }
    // 非 GET 请求且有请求体时默认设置为 JSON 格式
    if (body != null && headers.getContentType() == null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }

    BodyBuilder builder = RequestEntity.method(method, uri).headers(headers);
    return body == null ? builder.build() : builder.body(body);
  }

  /**
   * 构造请求头
   */
  public static HttpHeaders buildHeaders(@Nullable Map<String, Object> headerParams) {
    HttpHeaders headers = new HttpHeaders();
    // 添加 API 技能配置的请求头
    if (MapUtils.isNotEmpty(headerParams)) {
      for (Entry<String, Object> entry : headerParams.entrySet()) {
        headers.set(entry.getKey(), Objects.toString(entry.getValue(), ""));
      }
    }

    // 优先接收 JSON 格式
    if (headers.getAccept().isEmpty()) {
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
    }
    return headers;
  }

  /**
   * 构造请求地址，添加路径参数、URL 参数
   */
  public static URI buildUrl(String gatewayUrl, @Nullable String relativePath, @Nullable Map<String, Object> pathParams,
    @Nullable Map<String, Object> queryParams) {
    String url;
    if (StringUtils.isNotEmpty(relativePath)) {
      // 如果 relativePath 是完整的地址，不再拼上网关地址
      //noinspection HttpUrlsUsage
      if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
        url = relativePath;
      }
      else {
        url = StringUtils.stripEnd(gatewayUrl, "/") + "/" + StringUtils.stripStart(relativePath, "/");
      }
    }
    else {
      url = gatewayUrl;
    }
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
    // 解码存量的 query 参数，避免重复转义
    HttpUtil.decodeQueryParams(url, builder);
    if (MapUtils.isNotEmpty(queryParams)) {
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        Object value = entry.getValue();
        // 忽略值为 null 的参数
        if (value == null) {
          continue;
        }
        // 数组
        if (value instanceof Collection) {
          builder.queryParam(entry.getKey(), (Collection<?>) value);
        }
        else {
          builder.queryParam(entry.getKey(), value);
        }
      }
    }
    // query 参数需要转义
    builder.encode();
    if (MapUtils.isNotEmpty(pathParams)) {
      return builder.build(pathParams);
    }
    return builder.build().toUri();
  }

}
