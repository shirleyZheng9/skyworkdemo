package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.FlowRunLogVO;
import com.iwhalecloud.bote.dto.skill.query.FlowRunLogQueryParams;
import java.util.Date;

/**
 * 流程执行日志服务
 *
 * @author bianjp
 * @since 2025-03-05
 */
public interface IFlowRunLogService {

  /**
   * 记录复杂场景、工作流的执行日志
   */
  void addLog(OrchestrationEngineRequest request, OrchestrationEngineResponse response, Date startTime);

  /**
   * 记录简单场景的执行日志
   */
  void addLog(SceneChatParamsDTO sceneChatParams, OrchestrationEngineResponse response, Date startTime);

  /**
   * 分页查询流程执行日志
   */
  PageInfo<FlowRunLogVO> qryFlowLogPage(FlowRunLogQueryParams queryParams);

  /**
   * 查询流程执行日志详情
   */
  FlowRunLogVO getFlowLog(Long tenantId, Long logId);
}
