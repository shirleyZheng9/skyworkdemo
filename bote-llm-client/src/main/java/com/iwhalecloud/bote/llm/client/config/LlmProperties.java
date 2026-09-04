package com.iwhalecloud.bote.llm.client.config;

import com.iwhalecloud.bote.llm.client.consts.FunctionCallMode;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 大语言模型配置
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LlmProperties extends AbstractModelProperties {
  /** 采样温度，取值范围为 [0, 2], 默认为 1。更高的值会让输出更加随机，更低的值会让输出更加聚焦和明确 */
  private Double temperature;
  /** 允许生成的最大 token 数量 */
  private Integer maxTokens;
  /** 上下文长度 */
  private Integer contextLength;
  /** 函数调用模式 */
  private FunctionCallMode functionCallMode;
  /** 是否支持流式工具调用 */
  private Boolean streamingFunctionCall;
  /** 是否支持视觉 */
  private Boolean supportsVision;
  /** 扩展请求参数（放在请求体中） */
  private Map<String, Object> extReqParams;
  /** 扩展配置 */
  private Map<String, Object> ext;

  public LlmProperties() {
  }

  // -@cs[ParameterNumber] This method is only used for builder
  @Builder
  public LlmProperties(ModelConfigInfoDTO modelConfig, String url, String apiKey, String model, Double temperature, Integer maxTokens, Integer contextLength,
                       FunctionCallMode functionCallMode, Boolean streamingFunctionCall, Boolean supportsVision, List<HeaderItem> headers, Map<String, Object> ext,
                       Map<String, Object> extReqParams) {
    super(modelConfig, url, apiKey, model, headers);
    this.temperature = temperature;
    this.maxTokens = maxTokens;
    this.contextLength = contextLength;
    this.functionCallMode = functionCallMode;
    this.streamingFunctionCall = functionCallMode != FunctionCallMode.NONE && Boolean.TRUE.equals(streamingFunctionCall);
    this.supportsVision = supportsVision;
    this.ext = ext;
    this.extReqParams = extReqParams;
  }

  /**
   * 大语言模型配置构造器
   */
  public static class LlmPropertiesBuilder {
    public LlmPropertiesBuilder temperature(@Nullable Double temperature) {
      this.temperature = temperature;
      return this;
    }

    public LlmPropertiesBuilder temperature(@Nullable BigDecimal temperature) {
      this.temperature = temperature != null ? temperature.doubleValue() : null;
      return this;
    }

    public LlmPropertiesBuilder functionCallMode(@Nullable FunctionCallMode functionCallMode) {
      this.functionCallMode = functionCallMode;
      return this;
    }

    public LlmPropertiesBuilder functionCallMode(@Nullable String functionCallMode) {
      this.functionCallMode = FunctionCallMode.ofMode(functionCallMode);
      return this;
    }
  }

}
