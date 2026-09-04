package com.iwhalecloud.bote.adapter.dify.config;

import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Dify大模型配置
 *
 * @author qian.sisheng
 * @since 2025-10-17
 */
@Getter
@Setter
@ToString
@Builder
public class DifyLlmProperties {
  /** 模型配置信息 */
  private ModelConfigInfoDTO modelConfig;
  /** chatFlow API 密钥 */
  private String chatFlowSecret;
}
