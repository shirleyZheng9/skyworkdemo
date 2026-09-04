package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 智能体 步骤
 *
 * @author chen.linfa
 * @since 2025-07-01
 */
@Getter
@Setter
public class SceneStep extends AbstractStep {
  /** 智能体 ID */
  private Long sceneId;
  /** 消息内容 */
  private String messageContent;
  /** 入参 */
  private Map<String, Object> params;
  /** 计划步骤 ID */
  private Long stepId;
  /** BSS 需求，智能体初始的上下文参数 */
  private Map<String, Object> contextParams;

  public SceneStep() {
    super(StepType.SCENE);
  }
}
