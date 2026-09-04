package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 调用智能体步骤（将 ISceneChatService#run 封装为工具时的步骤描述）
 * <p>用于自主规划智能体中配置「智能体类型」技能，规划模型通过工具调用子智能体。</p>
 *
 * @author chen.linfa
 * @since 2026-03-16
 */
@Getter
@Setter
public class InvokeSceneStep extends AbstractStep {
  /** 被调用的智能体（场景）ID */
  private Long sceneId;
  /** 被调用的智能体名称，用于工具描述 */
  private String sceneName;

  public InvokeSceneStep() {
    super(StepType.INVOKE_SCENE);
  }
}
