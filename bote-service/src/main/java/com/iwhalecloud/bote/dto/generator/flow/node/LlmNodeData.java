package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.model.VisionConfig;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep.LlmMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 大模型节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class LlmNodeData {
  /** 大模型 ID */
  private Long modelId;
  /** 消息列表（至少要有一条，第一条必须是 system，后面的只能是 user/assistant） */
  private List<LlmMessage> messages;
  /** 用户消息内容（模板字符串，支持引用变量），非必填 */
  private String userMessage;
  /** 视觉配置，可选 */
  private VisionConfig vision;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 是否流式输出 */
  private Boolean stream;
}
