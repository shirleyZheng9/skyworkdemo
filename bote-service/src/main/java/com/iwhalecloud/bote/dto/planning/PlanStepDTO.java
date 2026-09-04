package com.iwhalecloud.bote.dto.planning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.PlanConsts;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.entity.planning.PlanStepEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 规划执行记录步骤 DTO
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PlanStepDTO extends PlanStepEntity {

  @Schema(description = "步骤信息")
  private List<SimpleFlowStepDTO> flowSteps;

  /**
   * 是否是自动执行
   */
  @JsonIgnore
  public boolean isAuto() {
    return BaseConsts.TRUE.equals(getIsAutoRun());
  }

  /**
   * 是否已完成
   */
  @JsonIgnore
  public boolean isCompleted() {
    return Objects.equals(PlanConsts.STATUS_SUCCESS, getStepStatus());
  }

  /**
   * 获取智能体变量
   */
  @JsonIgnore
  public Map<String, Object> getAgentParams() {
    Map<String, Object> params = new HashMap<>();
    if (StringUtils.isNotEmpty(this.getAgentParam())) {
      // 应 BSS 述求，用 agentParam 封装个性化的智能体变量
      params.put("agentParam", JsonUtil.parseJson(this.getAgentParam(), new TypeReference<Map<String, Object>>() {
      }));
    }
    return params;
  }
}
