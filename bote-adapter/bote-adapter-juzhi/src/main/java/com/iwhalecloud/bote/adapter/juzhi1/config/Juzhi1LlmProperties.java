package com.iwhalecloud.bote.adapter.juzhi1.config;

import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 一级聚智大模型配置
 *
 * @author bianjp
 * @since 2025-05-17
 */
@Getter
@Setter
@ToString
@Builder
public class Juzhi1LlmProperties {
  /** 模型配置信息 */
  private ModelConfigInfoDTO modelConfig;
  /** 能力编码 */
  private String funcCode;
  /** 地市 */
  private String city;
  /** 应用方名称 */
  private String application;
  /** 模型 */
  private String model;
  /** 采样温度 */
  private Double temperature;
  /** 上下文长度 */
  private Integer contextLength;
}
