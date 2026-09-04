package com.iwhalecloud.bote.dto.model.query;

import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型调测参数
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString
public class LargeModelTestParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "大模型 ID (测试只读权限的模型时使用，比如在租户中测试平台的模型，传了 ID 时不需要再传模型配置)")
  private Long modelId;
  @Schema(description = "模型分类")
  private String modelType;
  @Schema(description = "模型编码")
  private String modelCode;
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
  @Schema(description = "函数调用模式")
  private String functionCallMode;
  @Schema(description = "请求头")
  private List<HeaderItem> headers;
  @Schema(description = "协议扩展配置")
  private Map<String, Object> protocolExt;
  @Schema(description = "扩展请求参数")
  private String extReqParamsJson;
  @Schema(description = "是否支持Vision")
  private Boolean supportsVision;

  @Schema(description = "是否流式输出")
  private Boolean stream;
  @Schema(description = "客户端 ID(中断请求使用)")
  private String clientId;
  @Schema(description = "问题")
  private String question;

  @Schema(description = "文件ID列表")
  private List<Long> fileIds;
  @Schema(description = "历史对话消息列表")
  private List<TestChatMessage> history;
}
