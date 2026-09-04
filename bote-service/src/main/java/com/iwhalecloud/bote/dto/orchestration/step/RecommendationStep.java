package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 推荐步骤
 *
 * @author bianjp
 * @since 2025-07-21
 */
@Getter
@Setter
public class RecommendationStep extends AbstractStep {
  /** 推荐类型(scene: 智能体, flow: 工作流, page: 页面, question: 问题) */
  private String recommendationType;
  /** 智能体 ID 列表(仅用于 type=scene) */
  private List<Long> sceneIds;
  /** 工作流 ID 列表(仅用于 type=flow) */
  private List<Long> flowIds;
  /** 页面 ID 列表(仅用于 type=page) */
  private List<Long> pageIds;
  /** 问题列表(仅用于 type=question) */
  private List<RecommendationQuestionItem> questions;

  public RecommendationStep() {
    super(StepType.RECOMMENDATION);
  }

  /**
   * 推荐问题项
   */
  @Getter
  @Setter
  @ToString
  public static class RecommendationQuestionItem {
    /** 问题 */
    private String text;
    /** 展示文本，可选，默认使用 text */
    private String displayText;
  }
}
