package com.iwhalecloud.bote.dto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型简单信息
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimpleLargeModelDTO {
  @Schema(description = "大模型 ID")
  private Long modelId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "模型编码")
  private String modelCode;
  @Schema(description = "模型名称")
  private String modelName;
  @Schema(description = "模型类别")
  private String modelType;
  @Schema(description = "模型描述")
  private String modelDesc;
  @Schema(description = "模型图标")
  private String modelIcon;
  @Schema(description = "协议类型")
  private String protocolType;
  @Schema(description = "访问地址")
  private String accessUrl;
  @Schema(description = "访问密钥")
  private String accessKey;
  @Schema(description = "温度系数")
  private BigDecimal temperature;
  @Schema(description = "最大令牌数")
  private Integer maxTokens;
  @Schema(description = "上下文长度")
  private Integer contextLength;
  @Schema(description = "函数调用模式")
  private String functionCallMode;
  @Schema(description = "是否支持流式工具调用(T/F)")
  private String streamingFunctionCall;
  @Schema(description = "是否支持视觉(T/F)")
  private String supportsVision;
  @Schema(description = "扩展参数 JSON 字符串")
  private String extAttrJson;
  @Schema(description = "扩展配置")
  private LargeModelExtConfigDTO extConfig;
  @Schema(description = "创建人名称")
  private String creatorName;


  /**
   * 转为模型配置信息对象
   */
  public ModelConfigInfoDTO toModelConfigInfo() {
    return ModelConfigInfoDTO.builder()
      .modelId(modelId)
      .tenantId(tenantId)
      .modelName(modelName)
      .modelCode(modelCode)
      .protocolType(protocolType)
      .accessUrl(accessUrl)
      .build();
  }
}
