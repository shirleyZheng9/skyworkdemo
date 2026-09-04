package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.LargeModelExtConfigDTO;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.model.query.LargeModelTestParams;
import com.iwhalecloud.bote.llm.client.LlmClientFactory;
import com.iwhalecloud.bote.llm.client.config.EmbeddingProperties;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.util.ModelValidationUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 模型配置工具类
 *
 * @author bianjp
 * @since 2025-05-12
 */
public final class ModelConfigUtil {
  private ModelConfigUtil() {
  }


  /**
   * 校验模型配置
   *
   * @param model 模型配置
   */
  public static void validate(LargeModelDTO model) {
    if (ModelConsts.MODEL_TYPE_LLM.equals(model.getModelType())) {
      Assert.hasLength(model.getProtocolType(), "协议类型不能为空");
      LlmClientFactory factory = ModelClientUtil.getFactory(model.getProtocolType());
      factory.validate(buildLlmProperties(model));
    }
    else if (ModelConsts.MODEL_TYPE_EMBEDDING.equals(model.getModelType())) {
      ModelValidationUtil.validateUrl(model.getAccessUrl(), "接口地址");
      Assert.hasLength(model.getModelCode(), "模型名称不能为空");
    }
  }

  /**
   * 构造大语言模型配置
   *
   * @param model 模型配置
   * @return 大语言模型配置
   */
  public static LlmProperties buildLlmProperties(SimpleLargeModelDTO model) {
    parseExtConfig(model);
    LargeModelExtConfigDTO extConfig = model.getExtConfig();
    return LlmProperties.builder()
      .modelConfig(model.toModelConfigInfo())
      .url(model.getAccessUrl())
      .apiKey(model.getAccessKey())
      .model(model.getModelCode())
      .temperature(model.getTemperature())
      .maxTokens(model.getMaxTokens())
      .contextLength(model.getContextLength())
      .functionCallMode(model.getFunctionCallMode())
      .streamingFunctionCall(BaseConsts.TRUE.equals(model.getStreamingFunctionCall()))
      .supportsVision(BaseConsts.TRUE.equals(model.getSupportsVision()))
      .headers(extConfig != null ? extConfig.getHeaders() : null)
      .ext(extConfig != null ? extConfig.getProtocolExt() : null)
      .extReqParams(parseExtReqParams(extConfig != null ? extConfig.getExtReqParamsJson() : null))
      .build();
  }

  /**
   * 构造大语言模型配置
   *
   * @param model 模型配置
   * @return 大语言模型配置
   */
  private static LlmProperties buildLlmProperties(LargeModelDTO model) {
    return LlmProperties.builder()
      .url(model.getAccessUrl())
      .apiKey(model.getAccessKey())
      .model(model.getModelCode())
      .temperature(model.getTemperature())
      .maxTokens(model.getMaxTokens())
      .contextLength(model.getContextLength())
      .functionCallMode(model.getFunctionCallMode())
      .streamingFunctionCall(BaseConsts.TRUE.equals(model.getStreamingFunctionCall()))
      .supportsVision(BaseConsts.TRUE.equals(model.getSupportsVision()))
      .headers(model.getHeaders())
      .ext(model.getProtocolExt())
      .extReqParams(parseExtReqParams(model.getExtReqParamsJson()))
      .build();
  }

  /**
   * 构造模型测试的大语言模型配置
   *
   * @param params 模型测试参数
   * @return 大语言模型配置
   */
  public static LlmProperties buildLlmProperties(LargeModelTestParams params) {
    return LlmProperties.builder()
      .url(params.getAccessUrl())
      .apiKey(params.getAccessKey())
      .model(params.getModelCode())
      .temperature(params.getTemperature())
      .maxTokens(params.getMaxTokens())
      .functionCallMode(params.getFunctionCallMode())
      .headers(params.getHeaders())
      .ext(params.getProtocolExt())
      .extReqParams(parseExtReqParams(params.getExtReqParamsJson()))
      .build();
  }

  /**
   * 构造嵌入模型配置
   *
   * @param model 模型配置
   * @return 嵌入模型配置
   */
  public static EmbeddingProperties buildEmbeddingProperties(SimpleLargeModelDTO model) {
    parseExtConfig(model);
    LargeModelExtConfigDTO extConfig = model.getExtConfig();
    return EmbeddingProperties.builder()
      .url(model.getAccessUrl())
      .apiKey(model.getAccessKey())
      .model(model.getModelCode())
      .headers(extConfig != null ? extConfig.getHeaders() : null)
      .build();
  }

  /**
   * 构造模型测试的嵌入模型配置
   *
   * @param params 模型测试参数
   * @return 嵌入模型配置
   */
  public static EmbeddingProperties buildEmbeddingProperties(LargeModelTestParams params) {
    return EmbeddingProperties.builder()
      .url(params.getAccessUrl())
      .apiKey(params.getAccessKey())
      .model(params.getModelCode())
      .headers(params.getHeaders())
      .build();
  }

  /**
   * 解析扩展请求参数
   */
  @Nullable
  private static Map<String, Object> parseExtReqParams(@Nullable String extReqParamsJson) {
    String json = StringUtils.trimToEmpty(extReqParamsJson);
    if (json.isEmpty()) {
      return null;
    }
    return JsonUtil.parseJsonRequired(json, new TypeReference<Map<String, Object>>() {
    });
  }

  /**
   * 解析扩展配置
   */
  private static void parseExtConfig(SimpleLargeModelDTO model) {
    if (StringUtils.isNotEmpty(model.getExtAttrJson())) {
      LargeModelExtConfigDTO extConfig = JsonUtil.parseJsonRequired(model.getExtAttrJson(), LargeModelExtConfigDTO.class);
      model.setExtConfig(extConfig);
    }
    model.setExtAttrJson(null);
  }

  /**
   * 解析扩展配置，返回模型配置给前端时使用
   */
  public static void parseExtConfig(LargeModelDTO model) {
    if (StringUtils.isNotEmpty(model.getExtAttrJson())) {
      LargeModelExtConfigDTO extConfig = JsonUtil.parseJsonRequired(model.getExtAttrJson(), LargeModelExtConfigDTO.class);
      model.setHeaders(extConfig.getHeaders());
      model.setProtocolExt(extConfig.getProtocolExt());
      model.setExtReqParamsJson(extConfig.getExtReqParamsJson());
    }
    model.setExtAttrJson(null);
  }

  /**
   * 保存扩展配置，保存前端传递的模型配置时使用
   */
  public static void saveExtConfig(LargeModelDTO model) {
    if (CollectionUtils.isNotEmpty(model.getHeaders()) || MapUtils.isNotEmpty(model.getProtocolExt()) || StringUtils.isNotEmpty(model.getExtReqParamsJson())) {
      LargeModelExtConfigDTO extConfig = new LargeModelExtConfigDTO();
      extConfig.setHeaders(CollectionUtils.isNotEmpty(model.getHeaders()) ? model.getHeaders() : null);
      extConfig.setProtocolExt(MapUtils.isNotEmpty(model.getProtocolExt()) ? model.getProtocolExt() : null);
      extConfig.setExtReqParamsJson(model.getExtReqParamsJson());
      model.setExtAttrJson(JsonUtil.toJsonStringCompact(extConfig));
    }
    else {
      model.setExtAttrJson(null);
    }
  }
}
