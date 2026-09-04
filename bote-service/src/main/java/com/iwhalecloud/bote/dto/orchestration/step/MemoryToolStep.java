package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 长期记忆工具步骤
 *
 * <p>运行时动态注入，持有 {@link ToolCallback} 引用以便直接调用 {@link com.iwhalecloud.bote.agent.tools.MemoryTools}
 * 中的记忆检索、读取、编辑、写入工具。</p>
 *
 * @author wangtingyun
 * @since 2026-04-22
 */
@Getter
@Setter
public class MemoryToolStep extends AbstractStep {

  private final ToolCallback toolCallback;

  public MemoryToolStep(ToolCallback toolCallback) {
    super(StepType.MEMORY_TOOL);
    this.toolCallback = toolCallback;
  }
}
