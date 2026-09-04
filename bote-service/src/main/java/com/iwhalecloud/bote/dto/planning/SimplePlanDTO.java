package com.iwhalecloud.bote.dto.planning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.chat.vo.SessionMsgVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;

/**
 * LLM 生成的计划，用于与大模型交互
 *
 * @author chen.linfa
 * @since 2025-05-16
 */
@Getter
@Setter
@ToString
public class SimplePlanDTO {
  /** 计划 ID */
  private Long planId;
  /** 租户 ID */
  private Long tenantId;
  /** 应用 ID */
  private Long botId;
  /** 会话 ID */
  private Long sessionId;
  /** 用户的原始请求 */
  private String userMessage;
  /** 用户的入参 */
  private Map<String, Object> userParams;
  /** 状态，用于渲染历史会话 */
  private Integer status;
  /** 步骤列表 */
  private List<SimplePlanStepDTO> steps;
  /** 标识生成计划后自动发起执行 */
  private Boolean isAuto;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SimplePlanStepDTO {
    /** 智能体 ID */
    private Long agentId;
    /** 智能体名称 */
    private String agentName;
    /** 智能体入参 */
    private String agentRequest;
    /** 智能体变量 */
    private Map<String, Object> agentParam;
    /** 流程步骤 */
    private List<SimpleFlowStepDTO> flowSteps;
    /** 对话消息，用于渲染历史会话 */
    private List<SessionMsgVO> messages;
    /** 执行状态，用于渲染历史会话 */
    private Integer stepStatus;

    /** 步骤 ID */
    private String stepId;

    /**
     * 获取智能体变量
     */
    @JsonIgnore
    public String getStepParams() {
      if (MapUtils.isNotEmpty(agentParam)) {
        // 应 BSS 述求，用 agentParam 封装个性化的智能体变量
        Map<String, Object> params = new HashMap<>();
        params.put("agentParam", agentParam);
        return JsonUtil.toJsonString(params);
      }
      return null;
    }
  }
}
