package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.LarkAuthUtil;
import com.iwhalecloud.bote.dto.plugin.lark.LarkResultDTO;
import com.iwhalecloud.bote.dto.plugin.params.AbstractLarkPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 飞书插件抽象类
 *
 * @author qian.sisheng
 * @since 2025-08-23
 */
public abstract class AbstractLarkPlugin<T extends AbstractLarkPluginParams> extends AbstractPlugin<T> {

  /** 获取 appToken 的正则表达式 */
  private final Pattern APP_TOKEN_PATTERN = Pattern.compile(".*/(base|wiki)/([^/?]+).*");
  /** 获取 tableId 的正则表达式 */
  private final Pattern TABLE_ID_PATTERN = Pattern.compile(".*[?&]table=([^&]+).*");
  /** 获取知识空间节点信息 url */
  private static final String GET_NODE_URL = "https://open.feishu.cn/open-apis/wiki/v2/spaces/get_node";

  public AbstractLarkPlugin(Class<T> clazz) {
    super(clazz);
  }

  /**
   * 飞书 API 调用
   */
  protected <R> Object executeLarkApiCall(LarkApiCall<R> apiCall, T params, TypeReference<?> responseType) {
    // 获取访问令牌
    String userAccessToken = LarkAuthUtil.getCurrentUserAccessToken(params.getAppId(), params.getAppSecret());
    // 未授权或授权已过期，返回授权链接
    if (StringUtils.isEmpty(userAccessToken)) {
      return LarkAuthUtil.createAuthUrl(params.getAppId(), params.getAppSecret(), params.getPluginName());
    }
    // 构建请求头
    HttpHeaders headers = LarkAuthUtil.buildHeaders(userAccessToken);
    // 执行具体API调用
    R response = apiCall.execute(headers);
    if (response == null) {
      throw new BssException("飞书API执行异常: 响应为空");
    }
    // 如果指定了响应类型，则进行转换
    if (responseType != null) {
      return snakeCaseMapper.convertValue(response, responseType);
    }
    return response;
  }

  /**
   * 获取 appToken，支持从url解析 appToken
   */
  protected String getAppToken(T params) {
    Assert.isTrue(StringUtils.isNotEmpty(params.getUrl()) || StringUtils.isNotEmpty(params.getAppToken()), "appToken和url不能同时为空");
    if (StringUtils.isNotEmpty(params.getAppToken())) {
      return params.getAppToken();
    }
    // 文件夹中的多维表格，从url中解析appToken, 如 https://xxx/base/appToken/xxx
    Matcher matcher = APP_TOKEN_PATTERN.matcher(params.getUrl());
    String token = null;
    if (matcher.matches()) {
      token = matcher.group(2);
    }
    if (params.getUrl().contains("/base/")) {
      return token;
    }
    // 知识库下的多维表格, 需要从知识空间节点中获取
    if (params.getUrl().contains("/wiki/")) {
      return getKnowledgeTableToken(params, token);
    }
    throw new BssException("无效的url, 请检查url是否完整");
  }

  /**
   * 获取知识库下的多维表格的 appToken
   */
  @SuppressWarnings("unchecked")
  private String getKnowledgeTableToken(T params, String token) {
    LarkResultDTO<Map<String, Object>> result = (LarkResultDTO<Map<String, Object>>) executeLarkApiCall(headers -> {
      MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
      requestParams.add("token", token);
      requestParams.add("object_type", "wiki");
      return HttpUtil.get(GET_NODE_URL, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
      }, headers);
    }, params, new TypeReference<LarkResultDTO<Map<String, Object>>>() {
    });
    if (!"0".equals(result.getCode()) || result.getData() == null) {
      throw new BssException("获取知识空间节点信息失败: " + result.getMsg());
    }
    Map<String, Object> node = (Map<String, Object>) MapUtils.getMap(result.getData(), "node");
    return MapUtils.getString(node, "obj_token");
  }

  /**
   * 获取 tableId，支持从url解析 tableId
   */
  protected String getTableId(T params) {
    Assert.isTrue(StringUtils.isNotEmpty(params.getUrl()) || StringUtils.isNotEmpty(params.getTableId()), "tableId和url不能同时为空");
    if (StringUtils.isNotEmpty(params.getTableId())) {
      return params.getTableId();
    }
    // 从url中解析tableId, 如 https://xxx/base/appToken/xxx?table=xxx
    Matcher matcher = TABLE_ID_PATTERN.matcher(params.getUrl());
    if (matcher.matches()) {
      return matcher.group(1);
    }
    throw new BssException("无效的url, 请检查url是否完整");
  }

  /**
   * 飞书API调用接口
   */
  @FunctionalInterface
  protected interface LarkApiCall<R> {
    /**
     * 执行API调用
     *
     * @param headers 请求头
     * @return API调用结果
     */
    R execute(HttpHeaders headers);
  }
}
