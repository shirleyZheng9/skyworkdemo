package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorObject;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 流程执行日志
 *
 * @author bianjp
 * @since 2025-03-05
 */
@Getter
@Setter
@ToString
public class FlowRunLogDTO implements DisruptorObject {
  /** 日志 ID */
  private Long logId;
  /** 对象类型(scene/flow) */
  private String objType;
  /** 对象 ID (场景 ID 或工作流 ID) */
  private Long objId;
  /** 状态 */
  private String logStatus;
  /** 开始时间 */
  private Date startTime;
  /** 耗时(ms) */
  private Integer timeSpent;
  /** 入参 */
  private String input;
  /** 出参 */
  private String outputLog;
  /** 节点日志 */
  private String stepLog;
  /** 失败信息 */
  private String failMsg;
  /** 异常堆栈 */
  private String failStack;
  /** 事务 ID (对应一轮对话，一问一答) */
  private Long transactionId;
  /** 上下文 ID */
  private String contextId;
  /** 租户 ID */
  private Long tenantId;
  /** 用户 ID */
  private Long userId;

  public FlowRunLogDTO(Long tenantId, Long sceneId, Long flowId) {
    this.tenantId = tenantId;
    if (sceneId != null) {
      this.objType = "scene";
      this.objId = sceneId;
    }
    else {
      this.objType = "flow";
      this.objId = flowId;
    }
  }

  /**
   * 构造复杂场景、工作流的执行日志
   */
  public FlowRunLogDTO(OrchestrationEngineRequest request) {
    this(request.getTenantId(), request.getSceneId(), request.getFlowId());
    setInput(request.getMessageContent(), request.getFileIds(), request.getParameters(), request.getContextParams());
    this.transactionId = request.getTransactionId();
    this.contextId = request.getContextId();
  }

  /**
   * 构造简单场景的执行日志
   */
  public FlowRunLogDTO(SceneChatParamsDTO sceneChatParams) {
    this(sceneChatParams.getTenantId(), sceneChatParams.getSceneId(), sceneChatParams.getFlowId());
    setInput(sceneChatParams.getMessageContent(), sceneChatParams.getFileIds(), sceneChatParams.getParams(), sceneChatParams.getContextParams());
    this.transactionId = sceneChatParams.getTransactionId();
  }

  /**
   * 设置入参
   */
  private void setInput(String message, List<Long> fileIds, Map<String, Object> params, Map<String, Object> contextParams) {
    Map<String, Object> map = new HashMap<>();
    if (StringUtils.isNotEmpty(message)) {
      map.put("message", message);
    }
    if (CollectionUtils.isNotEmpty(fileIds)) {
      map.put("fileIds", fileIds);
    }
    if (MapUtils.isNotEmpty(params)) {
      map.put("params", params);
    }
    if (MapUtils.isNotEmpty(contextParams)) {
      map.put("contextParams", contextParams);
    }
    this.input = JsonUtil.toJsonString(map);
  }

  /**
   * 设置出参
   */
  public void setOutput(Object output) {
    if (output != null) {
      this.outputLog = JsonUtil.toJsonString(output);
    }
  }

  /**
   * 设置节点日志
   */
  public void setStepLog(List<OrchestrationStepRunLog> stepLogs) {
    if (CollectionUtils.isNotEmpty(stepLogs)) {
      this.stepLog = JsonUtil.toJsonString(stepLogs);
    }
  }
}
