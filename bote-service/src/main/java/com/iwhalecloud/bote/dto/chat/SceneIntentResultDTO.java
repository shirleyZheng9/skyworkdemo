package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景意图
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
public class SceneIntentResultDTO {
  /** 场景 ID */
  private Long sceneId;
  /** 上下文 ID */
  private String contextId;
  /** 场景名称 */
  private String sceneName;
  /** 问题 */
  private String question;

  /** 生成的计划 */
  private SimplePlanDTO plan;

  /** 应用 ID */
  private Long botId;
  /** 应用名称 */
  private String botName;
  /** 应用图标 */
  private String botIcon;

  /** 标记意图识别命中的新智能体 */
  private Boolean hit;

  public SceneIntentResultDTO() {
  }

  public SceneIntentResultDTO(Long sceneId, String sceneName) {
    this.sceneId = sceneId;
    this.sceneName = sceneName;
  }

  /**
   * 作为对话事件内容，简化参数结果
   *
   * @see com.iwhalecloud.bote.common.consts.ChatMessageType#CONFIRM_PLAN
   */
  @JsonIgnore
  public SimplePlanDTO wrapPlan() {
    plan.setStatus(PlanConsts.STATUS_NOT_STARTED);
    for (SimplePlanStepDTO step : plan.getSteps()) {
      step.setStepStatus(PlanConsts.STATUS_NOT_STARTED);
      step.setStepId(Sequences.PLAN_RECORD_STEP_ID.next() + "");
    }
    return plan;
  }
}
