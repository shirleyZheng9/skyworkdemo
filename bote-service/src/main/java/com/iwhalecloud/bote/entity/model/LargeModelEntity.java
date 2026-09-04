package com.iwhalecloud.bote.entity.model;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型 Entity
 *
 * @author auto
 * @since 2024-09-20
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_library_large_model")
public class LargeModelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long modelId;
  @DiffField(name = "MODEL_CODE")
  @Schema(description = "模型编码")
  private String modelCode;
  @DiffField(name = "MODEL_NAME")
  @Schema(description = "模型编码")
  private String modelName;
  @DiffField(name = "MODEL_TYPE")
  @Schema(description = "模型类别10A大语言(llm)10B文本词嵌入(text-embedding)10C语言转文本(speech2text)10D文本转语音(TTS)10E排序(rebank)")
  private String modelType;
  @DiffField(name = "MODEL_DESC")
  @Schema(description = "模型描述")
  private String modelDesc;
  @DiffField(name = "PROTOCOL_TYPE")
  @Schema(description = "协议类型")
  private String protocolType;
  @DiffField(name = "ACCESS_URL")
  @Schema(description = "访问地址")
  private String accessUrl;
  @DiffField(name = "ACCESS_KEY")
  @Schema(description = "访问秘钥")
  private String accessKey;
  @DiffField(name = "TEMPERATURE")
  @Schema(description = "温度系数")
  private BigDecimal temperature;
  @DiffField(name = "MAX_TOKENS")
  @Schema(description = "最大令牌数")
  private Integer maxTokens;
  @DiffField(name = "CONTEXT_LENGTH")
  @Schema(description = "上下文长度")
  private Integer contextLength;
  @DiffField(name = "FUNCTION_CALL_MODE")
  @Schema(description = "函数调用模式")
  private String functionCallMode;
  @DiffField(name = "STREAMING_FUNCTION_CALL")
  @Schema(description = "是否支持流式工具调用(T/F)")
  private String streamingFunctionCall;
  @DiffField(name = "SUPPORTS_VISION")
  @Schema(description = "是否支持视觉(T/F)")
  private String supportsVision;
  @DiffField(name = "EXT_ATTR_JSON")
  @Schema(description = "不同模型可能存在其他参数属性，通过此字段存储方便扩展")
  private String extAttrJson;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "PRODUCT_TYPE")
  @Schema(description = "产品类型")
  private String productType;
  @DiffField(name = "MODEL_ICON")
  @Schema(description = "模型图标")
  private String modelIcon;
  @DiffField(name = "IS_PUBLIC")
  @Schema(description = "是否公共(T/F), 仅用于平台级模型, 非公共模型只供平台自身使用，对租户不可见")
  private String isPublic;
  @DiffField(name = "DATA_FROM")
  @Schema(description = "来源：空为租户来源，10A为通用智能体来源，此时租户id存储的是空间id")
  private String dataFrom;
  @DiffField(name = "IS_ENABLED")
  @Schema(description = "是否启用")
  private String isEnabled;
}
