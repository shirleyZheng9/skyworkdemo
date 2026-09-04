package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流执行状态
 *
 * @author bianjp
 * @since 2025-06-17
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class FlowExecutionStateDTO {
  /** 租户 ID */
  private Long tenantId;
  /** 流程 ID */
  private Long flowId;
  /** 创建时间 */
  private String createTime;
  /** 更新时间 */
  private String updateTime;
  /** 执行状态 */
  private FlowExecutionStatus status;
  /** 错误信息 */
  private String error;

  /**
   * 工作流执行状态
   */
  public enum FlowExecutionStatus {
    /** 运行中 */
    @JsonProperty("running")
    RUNNING,
    /** 成功 */
    @JsonProperty("success")
    SUCCESS,
    /** 失败 */
    @JsonProperty("failed")
    FAILED,
  }
}
