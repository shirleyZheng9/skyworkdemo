package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.GaodeGeocodePluginParams;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地理编码插件 调用高德API查询地址所在坐标
 *
 * @author lizuyin
 * @since 2025-11-19
 */
@Component
public class GaodeGeocodePlugin extends AbstractPlugin<GaodeGeocodePluginParams> {

  public GaodeGeocodePlugin() {
    super(GaodeGeocodePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_GAODE_GEOCODE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("key", "高德API密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("address", "地址", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "坐标位置（格式：经度,纬度）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(GaodeGeocodePluginParams params) {
    Assert.notNull(params, "参数不能为空");
    Assert.hasText(params.getKey(), "参数 key 不能为空");
    Assert.hasText(params.getAddress(), "参数 address 不能为空");
  }

  @Override
  public Object doRun(GaodeGeocodePluginParams params) {
    try {
      // 获取高德API地址
      String baseUrl = getApiBaseUrl();
      if (baseUrl == null) {
        return createErrorResult("未配置高德API地址，请配置系统参数");
      }

      // 构建请求URI
      URI uri = buildRequestUri(baseUrl, params);

      // 创建HTTP请求实体
      HttpEntity<?> requestEntity = createHttpEntity();

      // 调用高德API
      String json = callGaodeApi(uri, requestEntity);
      if (json == null) {
        return createErrorResult("高德API响应为空");
      }

      // 解析响应
      GaodeGeocodeResponse response = parseResponse(json);
      if (response == null) {
        return createErrorResult("高德API响应解析失败");
      }

      // 验证响应状态
      String validationError = validateResponse(response);
      if (validationError != null) {
        return createErrorResult(validationError);
      }

      // 提取位置信息
      String location = extractLocation(response);
      if (location == null) {
        return createErrorResult("地址坐标数据为空");
      }

      // 返回成功结果
      return createSuccessResult(location);

    }
    catch (Exception e) {
      logger.error("高德地理编码插件执行异常", e);
      return createErrorResult("高德地理编码插件执行异常: " + e.getMessage());
    }
  }

  /**
   * 获取高德API地址
   *
   * @return API地址，如果未配置则返回null
   */
  private String getApiBaseUrl() {
    String baseUrl = SystemParameter.GAODE_API_URL.getValueFromEnv();
    return StringUtils.isNotBlank(baseUrl) ? baseUrl : null;
  }

  /**
   * 构建请求URI
   *
   * @param baseUrl API基础地址
   * @param params 插件参数
   * @return 构建好的URI对象
   */
  private URI buildRequestUri(String baseUrl, GaodeGeocodePluginParams params) {
    return UriComponentsBuilder.fromUriString(baseUrl)
      .queryParam("address", params.getAddress())
      .queryParam("key", params.getKey())
      .encode()
      .build()
      .toUri();
  }

  /**
   * 创建HTTP请求实体
   *
   * @return HTTP请求实体
   */
  private HttpEntity<?> createHttpEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    return new HttpEntity<>(headers);
  }

  /**
   * 调用高德API
   *
   * @param uri 请求URI
   * @param requestEntity HTTP请求实体
   * @return API响应的JSON字符串，如果响应为空则返回null
   */
  private String callGaodeApi(URI uri, HttpEntity<?> requestEntity) {
    ResponseEntity<String> responseEntity = HttpUtil.getRestTemplate()
      .exchange(uri, HttpMethod.GET, requestEntity, String.class);
    String json = responseEntity.getBody();
    return StringUtils.isNotBlank(json) ? json : null;
  }

  /**
   * 解析JSON响应
   *
   * @param json JSON字符串
   * @return 解析后的响应对象，如果解析失败则返回null
   */
  private GaodeGeocodeResponse parseResponse(String json) {
    return JsonUtil.parseJson(json, GaodeGeocodeResponse.class);
  }

  /**
   * 验证响应状态
   *
   * @param response API响应对象
   * @return 如果验证失败返回错误消息，否则返回null
   */
  private String validateResponse(GaodeGeocodeResponse response) {
    // 检查响应状态
    if (!"1".equals(response.getStatus())) {
      return StringUtils.isNotBlank(response.getInfo()) ? response.getInfo() : "高德API调用失败";
    }

    // 检查地理编码列表是否为空
    if (response.getGeocodes() == null || response.getGeocodes().isEmpty()) {
      return "未找到匹配的地址坐标";
    }

    return null;
  }

  /**
   * 提取位置信息
   *
   * @param response API响应对象
   * @return 位置信息（格式：经度,纬度），如果提取失败则返回null
   */
  private String extractLocation(GaodeGeocodeResponse response) {
    GeocodeItem firstItem = response.getGeocodes().get(0);
    if (firstItem == null || StringUtils.isBlank(firstItem.getLocation())) {
      return null;
    }
    return firstItem.getLocation();
  }

  /**
   * 创建成功结果
   *
   * @param location 位置信息（格式：经度,纬度）
   * @return 成功结果Map
   */
  private Map<String, Object> createSuccessResult(String location) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "");
    result.put("result", location);
    return result;
  }

  /**
   * 创建错误结果
   */
  private Map<String, Object> createErrorResult(String message) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", false);
    result.put("message", message);
    result.put("result", null);
    return result;
  }

  /**
   * 高德地理编码API响应DTO
   */
  @Setter
  @Getter
  private static final class GaodeGeocodeResponse {
    /**
     * 返回结果数量
     */
    private String count;

    /**
     * 返回状态值
     */
    private String status;

    /**
     * 返回状态说明
     */
    private String info;

    /**
     * 返回状态说明码
     */
    private String infocode;

    /**
     * 地理编码信息列表
     */
    private List<GeocodeItem> geocodes;
  }

  /**
   * 地理编码信息项
   */
  @Setter
  @Getter
  private static final class GeocodeItem {
    /**
     * 国家
     */
    private String country;

    /**
     * 格式化地址
     */
    @JsonProperty("formatted_address")
    private String formattedAddress;

    /**
     * 城市
     */
    private String city;

    /**
     * 区域编码
     */
    private String adcode;

    /**
     * 级别
     */
    private String level;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市编码
     */
    private String citycode;

    /**
     * 区县
     */
    private String district;

    /**
     * 坐标位置（格式：经度,纬度）
     */
    private String location;
  }
}

