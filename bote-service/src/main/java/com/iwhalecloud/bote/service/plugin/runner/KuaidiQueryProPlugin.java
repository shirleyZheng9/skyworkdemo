package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.KuaidiQueryProPluginParams;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 查快递Pro插件（智能识别快递公司）
 *
 * @author lizuyin
 * @since 2025-11-21
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class KuaidiQueryProPlugin extends AbstractPlugin<KuaidiQueryProPluginParams> {

  /** 智能识别API地址 */
  private static final String BAIDIYUN_QUERY_API = "/poll/query.do";
  private static final String BAIDIYUN_COM_API = "/autonumber/auto";
  public KuaidiQueryProPlugin() {
    super(KuaidiQueryProPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_KUAIDI_QUERY_PRO;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("customer", "快递100客户编码", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("key", "密钥", AttrDataType.STRING));
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
  public void validateParams(KuaidiQueryProPluginParams params) {
    Assert.notNull(params, "参数不能为空");
    Assert.hasText(params.getCustomer(), "参数 customer 不能为空");
    Assert.hasText(params.getKey(), "参数 key 不能为空");
    Assert.hasText(params.getNum(), "参数 num 不能为空");
  }

  @Override
  public Object doRun(KuaidiQueryProPluginParams params) {
    try {
      // 第一步：调用智能识别API获取快递公司编码
      String comCode = identifyExpressCompany(params.getNum(), params.getKey());
      if (StringUtils.isBlank(comCode)) {
        return createErrorResult("无法识别快递公司，请检查单号是否正确");
      }

      // 第二步：使用识别到的快递公司编码进行查询
      return queryExpressInfo(params, comCode);

    } catch (Exception e) {
      logger.error("查快递Pro插件执行异常", e);
      return createErrorResult("查快递Pro插件执行异常: " + e.getMessage());
    }
  }

  /**
   * 智能识别快递公司
   *
   * @param num 快递单号
   * @param key 密钥
   * @return 快递公司编码，如果识别失败返回null
   */
  private String identifyExpressCompany(String num, String key) {
    try {
      // 构建请求参数
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("num", num);
      params.add("key", key);

      // 创建请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

      // 调用智能识别API
      String json = HttpUtil.get(SystemParameter.BAIDIYUN_API_URL.getValueFromEnv() + BAIDIYUN_COM_API, params, new ParameterizedTypeReference<String>() {
      }, headers);
      if (json == null) {
        logger.warn("智能识别API响应为空: num={}", num);
        return null;
      }

      // 检查是否是错误响应
      if (json.contains("\"returnCode\"")) {
        AutoNumberErrorResponse errorResponse = JsonUtil.parseJson(json, AutoNumberErrorResponse.class);
        if (errorResponse != null && errorResponse.getReturnCode() != null) {
          String errorMsg = getErrorMessage(errorResponse.getReturnCode());
          logger.warn("智能识别API返回错误: num={}, returnCode={}, message={}", num, errorResponse.getReturnCode(), errorMsg);
          throw new RuntimeException(errorMsg);
        }
      }

      // 解析响应数组
      List<AutoNumberResponse> responses = JsonUtil.parseJson(json, new TypeReference<List<AutoNumberResponse>>() {
      });
      if (responses == null || responses.isEmpty()) {
        logger.warn("智能识别API返回空结果: num={}", num);
        return null;
      }

      // 返回第一个结果的快递公司编码
      String comCode = responses.get(0).getComCode();
      logger.debug("智能识别成功: num={}, comCode={}", num, comCode);
      return comCode;

    } catch (Exception e) {
      logger.error("智能识别快递公司异常: num={}", num, e);
      throw new RuntimeException("智能识别快递公司失败: " + e.getMessage(), e);
    }
  }

  /**
   * 查询快递信息
   *
   * @param params 插件参数
   * @param comCode 快递公司编码
   * @return 查询结果
   */
  private Object queryExpressInfo(KuaidiQueryProPluginParams params, String comCode) {
    try {
      // 构建请求参数
      Map<String, String> paramMap = buildParamMap(params, comCode);
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
      String json = HttpUtil.post(SystemParameter.BAIDIYUN_API_URL.getValueFromEnv() + BAIDIYUN_QUERY_API, queryParams, null, new ParameterizedTypeReference<String>() {
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
        String errorMsg = StringUtils.isNotBlank(response.getMessage())
          ? response.getMessage()
          : "快递100 API调用失败";
        return createErrorResult(errorMsg);
      }

      // 返回成功结果
      return createSuccessResult(response);

    } catch (Exception e) {
      logger.error("查询快递信息异常: num={}, comCode={}", params.getNum(), comCode, e);
      return createErrorResult("查询快递信息异常: " + e.getMessage());
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
   * 构建请求参数Map
   *
   * @param params 插件参数
   * @param comCode 快递公司编码
   * @return 请求参数Map
   */
  private Map<String, String> buildParamMap(KuaidiQueryProPluginParams params, String comCode) {
    Map<String, String> paramMap = new HashMap<>();
    paramMap.put("com", comCode);
    paramMap.put("num", params.getNum());
    paramMap.put("phone", StringUtils.isNotBlank(params.getPhone()) ? params.getPhone() : "");
    paramMap.put("from", "");
    paramMap.put("to", "");
    paramMap.put("show", "0");
    paramMap.put("resultv2", "0");
    paramMap.put("order", "desc");
    return paramMap;
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
    result.put("result", null);
    result.put("message", message);
    return result;
  }

  /**
   * 获取错误消息
   *
   * @param returnCode 错误代码
   * @return 错误消息
   */
  private String getErrorMessage(String returnCode) {
    switch (returnCode) {
      case "601":
        return "KEY已过期，未开通智能单号识别接口";
      case "701":
        return "KEY缺失，请求时需要带上key";
      case "201":
        return "不是有效的快递单号，请检查单号是否正确";
      default:
        return "智能识别失败，错误代码: " + returnCode;
    }
  }

  //TODO: 待搬迁至单独的插件应用后, 再将DTO拆分成单独的类

  /**
   * 智能识别API响应DTO
   */
  @Setter
  @Getter
  public static final class AutoNumberResponse {
    /** 单号的长度 */
    private Integer lengthPre;
    /** 快递公司对应的编码 */
    private String comCode;
    /** 快递公司名称 */
    private String name;
  }

  /**
   * 智能识别API错误响应DTO
   */
  @Setter
  @Getter
  public static final class AutoNumberErrorResponse {
    /** 返回代码 */
    private String returnCode;
    /** 消息 */
    private String message;
    /** 结果 */
    private Boolean result;
  }

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

