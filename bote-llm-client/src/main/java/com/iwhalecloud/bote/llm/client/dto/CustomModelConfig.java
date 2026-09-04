package com.iwhalecloud.bote.llm.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 自定义模型配置
 *
 * @author wangtingyun
 * @since 2026-01-20
 */
@Getter
@Setter
@ToString
public class CustomModelConfig {
  /** 否启用温度系数配置 */
  private Boolean temperatureEnabled;
  /** 温度系数 */
  private Double temperature;
  /** 否启用最大令牌数配置 */
  private Boolean maxTokensEnabled;
  /** 最大令牌数 */
  private Integer maxTokens;
}
