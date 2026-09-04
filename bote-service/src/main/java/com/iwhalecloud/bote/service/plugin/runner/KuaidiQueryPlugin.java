package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.KuaidiQueryPluginParams;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查快递插件
 *
 * @author lizuyin
 * @since 2025-11-21
 */
@Component
public class KuaidiQueryPlugin extends AbstractPlugin<KuaidiQueryPluginParams> {

  private static final String BAIDIYUN_QUERY_API = "/poll/query.do";

  public KuaidiQueryPlugin() {
    super(KuaidiQueryPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_KUAIDI_QUERY;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("customer", "快递100客户编码", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("key", "密钥", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("com", "快递公司编码", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("num", "快递单号", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("phone", "收件人或寄件人手机号（可选）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "查询结果", AttrDataType.OBJECT));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(KuaidiQueryPluginParams params) {
    Assert.notNull(params, "参数不能为空");
    Assert.hasText(params.getCustomer(), "参数 customer 不能为空");
    Assert.hasText(params.getKey(), "参数 key 不能为空");
    Assert.hasText(params.getCom(), "参数 com 不能为空");
    Assert.hasText(params.getNum(), "参数 num 不能为空");
  }

  @Override
  public Object doRun(KuaidiQueryPluginParams params) {
    try {
      // 构建请求参数
      Map<String, String> paramMap = buildParamMap(params);
      String paramJson = JsonUtil.toJsonString(paramMap);

      // 计算签名
      String sign = generateSign(paramJson, params.getKey(), params.getCustomer());

      // 构建查询参数
      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("customer", params.getCustomer());
      queryParams.put("sign", sign);
      queryParams.put("param", paramJson);

      // 创建请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

      // 调用快递100 API
      String json = HttpUtil.post(SystemParameter.BAIDIYUN_API_URL.getValueFromEnv() + BAIDIYUN_QUERY_API, queryParams,
        null, new ParameterizedTypeReference<String>() {
        }, headers);

      if (json == null) {
        return createErrorResult("快递100 API响应为空");
      }

      // 解析响应
      KuaidiQueryResponse response = parseResponse(json);
      if (response == null) {
        return createErrorResult("快递100 API响应解析失败");
      }

      // 检查响应状态
      if (!"200".equals(response.getStatus())) {
        String errorMsg = StringUtils.isNotBlank(response.getMessage()) ? response.getMessage() : "快递100 API调用失败";
        return createErrorResult(errorMsg);
      }

      // 返回成功结果
      return createSuccessResult(response);

    }
    catch (Exception e) {
      logger.error("查快递插件执行异常", e);
      return createErrorResult("查快递插件执行异常: " + e.getMessage());
    }
  }



  /**
   * 解析JSON响应
   *
   * @param json JSON字符串
   * @return 解析后的响应对象，如果解析失败则返回null
   */
  private KuaidiQueryResponse parseResponse(String json) {
    return JsonUtil.parseJson(json, KuaidiQueryResponse.class);
  }

  /**
   * 生成快递100签名
   *
   * @param param 参数JSON字符串
   * @param key 密钥
   * @param customer 客户编码
   * @return 签名（大写MD5）
   */
  private String generateSign(String param, String key, String customer) {
    String raw = param + key + customer;
    return DigestUtils.md5Hex(raw).toUpperCase();
  }

  /**
   * 构建请求参数Map
   *
   * @param params 插件参数
   * @return 请求参数Map
   */
  private Map<String, String> buildParamMap(KuaidiQueryPluginParams params) {
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("com", params.getCom());
    paramMap.put("num", params.getNum());
    paramMap.put("phone", StringUtils.isNotBlank(params.getPhone()) ? params.getPhone() : "");
    paramMap.put("from", "");
    paramMap.put("to", "");
    paramMap.put("resultv2", "0");
    paramMap.put("show", "0");
    paramMap.put("order", "desc");
    return paramMap;
  }

  /**
   * 创建成功结果
   *
   * @param response API响应对象
   * @return 成功结果Map
   */
  private Map<String, Object> createSuccessResult(KuaidiQueryResponse response) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "");
    result.put("result", response);
    return result;
  }

  /**
   * 创建错误结果
   *
   * @param message 错误消息
   * @return 错误结果Map
   */
  private Map<String, Object> createErrorResult(String message) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", false);
    result.put("message", message);
    result.put("result", null);
    return result;
  }

  //TODO: 待搬迁至单独的插件应用后, 再将DTO拆分成单独的类

  /**
   * 快递100查询响应DTO
   */
  @Setter
  @Getter
  public static final class KuaidiQueryResponse {
    /** 消息 */
    private String message;
    /** 快递单号 */
    private String nu;
    /** 是否签收 */
    private String ischeck;
    /** 快递状态 */
    private String condition;
    /** 快递公司编码 */
    private String com;
    /** 状态码 */
    private String status;
    /** 快递状态 */
    private String state;
    /** 物流信息列表 */
    private List<TrackingData> data;
  }

  /**
   * 物流信息项
   */
  @Setter
  @Getter
  public static final class TrackingData {
    /** 时间 */
    private String time;
    /** 格式化时间 */
    private String ftime;
    /** 物流信息 */
    private String context;
  }
}

