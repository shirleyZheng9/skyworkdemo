package com.iwhalecloud.bote.adapter.juzhi2.config;

import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 二级聚智大模型配置
 *
 * @author bianjp
 * @since 2025-05-08
 */
@Getter
@Setter
@ToString
@Builder
public class Juzhi2LlmProperties {
  /** 模型配置信息 */
  private ModelConfigInfoDTO modelConfig;
  /** 智能体编码 */
  private String assistantCode;
  /** 开始节点 ID */
  private String startNodeId;
  /** 上下文长度 */
  private Integer contextLength;
}
