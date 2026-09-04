package com.iwhalecloud.bote.dto.planning.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 对话过程，计划相关参数
 *
 * @author chen.linfa
 * @since 2025-05-16
 */
@Getter
@Setter
@ToString
public class PlanParams {
  @Schema(description = "计划 ID")
  private Long planId;

  @Schema(description = "是否中断计划")
  private Boolean interrupted;

  @Schema(description = "用户的原始请求")
  private String userMessage;

  @Schema(description = "确认要执行的计划内容")
  private List<SimplePlanStepDTO> steps;

  @Schema(description = "通过应用的规划智策略生成计划，初始化的智能体列表")
  private List<SimplePlanStepDTO> busiInfos;

  @Schema(description = "标识是否通过应用的规划智策略生成计划")
  private Boolean planable;

  @Schema(description = "标识生成计划后自动发起执行")
  private Boolean isAuto;

  /**
   * 判断是否执行计划
   */
  @JsonIgnore
  public boolean processPlan() {
    return BooleanUtils.isTrue(interrupted) || CollectionUtils.isNotEmpty(steps);
  }

  /**
   * 判断是否通过规划生成计划
   */
  @JsonIgnore
  public boolean recognizePlan() {
    return CollectionUtils.isNotEmpty(busiInfos) || BooleanUtils.isTrue(planable);
  }
}
