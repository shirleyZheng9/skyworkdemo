package com.iwhalecloud.bote.generator.flow.context;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流转换器上下文
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class FlowConverterContext {
  /** 租户 ID */
  private final Long tenantId;
  /** 工作流出参（仅用于任务型工作流） */
  private ParameterSpec response;
  /** 工作流出参的各级节点的 key 映射，key 为属性路径, value 为属性的 key 值 */
  private Map<String, String> responseKeyMap;
}
