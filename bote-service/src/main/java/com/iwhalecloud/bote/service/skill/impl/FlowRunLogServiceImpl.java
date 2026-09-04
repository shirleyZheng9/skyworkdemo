package com.iwhalecloud.bote.service.skill.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.FlowRunLogVO;
import com.iwhalecloud.bote.dto.skill.query.FlowRunLogQueryParams;
import com.iwhalecloud.bote.entity.skill.FlowRunLogDTO;
import com.iwhalecloud.bote.mapper.skill.FlowRunLogMapper;
import com.iwhalecloud.bote.service.skill.IFlowRunLogService;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 流程执行日志服务
 *
 * @author bianjp
 * @since 2025-03-05
 */
@Service
@RequiredArgsConstructor
public class FlowRunLogServiceImpl implements IFlowRunLogService {
  private static final Logger logger = LoggerFactory.getLogger(FlowRunLogServiceImpl.class);

  private final FlowRunLogMapper flowTraceLogMapper;

  @Override
  public void addLog(OrchestrationEngineRequest request, OrchestrationEngineResponse response, Date startTime) {
    try {
      FlowRunLogDTO log = new FlowRunLogDTO(request);
      doAddLog(response, startTime, log);
    }
    catch (Exception e) {
      logger.warn("Failed to add flow run log", e);
    }
  }

  @Override
  public void addLog(SceneChatParamsDTO sceneChatParams, OrchestrationEngineResponse response, Date startTime) {
    try {
      FlowRunLogDTO log = new FlowRunLogDTO(sceneChatParams);
      doAddLog(response, startTime, log);
    }
    catch (Exception e) {
      logger.warn("Failed to add flow run log", e);
    }
  }

  /**
   * 异步记录日志
   */
  private void doAddLog(OrchestrationEngineResponse response, Date startTime, FlowRunLogDTO log) {
    long logId = IDUtils.nextId();
    response.setLogId(logId);
    log.setLogId(logId);
    log.setStartTime(startTime);
    log.setTimeSpent(response.getTimeSpent().intValue());
    log.setUserId(SessionUtil.getOptionalUserId());
    log.setStepLog(response.getStepLogs());
    if (Boolean.TRUE.equals(response.getSuccess())) {
      log.setLogStatus(BaseConsts.STATE_SUCCESS);
      if (Boolean.TRUE.equals(response.getChatflow())) {
        log.setOutput(response.getReplies());
      }
      else {
        log.setOutput(response.getOutput());
      }
    }
    else {
      log.setLogStatus(BaseConsts.STATE_FAIL);
      log.setFailMsg(StringUtils.left(response.getFailMsg(), 200));
      log.setFailStack(response.getFailStack());
    }

    DisruptorUtil.getInstance().produce(log);
  }

  @Override
  public PageInfo<FlowRunLogVO> qryFlowLogPage(FlowRunLogQueryParams queryParams) {
    //noinspection resource
    return flowTraceLogMapper.selectLogPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public FlowRunLogVO getFlowLog(Long tenantId, Long logId) {
    // tenantId 用于校验权限
    FlowRunLogVO log = flowTraceLogMapper.selectLogById(logId, tenantId);
    Assert.notNull(log, () -> "流程日志不存在: logId=" + logId);
    // 解析日志
    parseFlowLog(log, tenantId);
    // 填充子流程日志
    if (CollectionUtils.isNotEmpty(log.getStepLogs())) {
      fillSubFlowLogs(log.getStepLogs(), tenantId,  log);
    }
    return log;
  }

  /**
   * 填充子流程日志
   */
  private void fillSubFlowLogs(List<OrchestrationStepRunLog> stepLogs, Long tenantId, FlowRunLogVO log) {
    // 找出子流程节点日志列表，以便批量查询数据库
    List<OrchestrationStepRunLog> workflowStepLogs = new ArrayList<>();
    findSubFlowSteps(stepLogs, workflowStepLogs, log);
    if (workflowStepLogs.isEmpty()) {
      return;
    }
    // 批量查询数据库
    List<Long> logIds = workflowStepLogs.stream().map(OrchestrationStepRunLog::getInnerServiceLogId).distinct().collect(Collectors.toList());
    List<FlowRunLogVO> subFlowLogs = flowTraceLogMapper.selectStepLogsByIds(logIds);
    Map<Long, List<OrchestrationStepRunLog>> stepLogsMap = new HashMap<>();
    for (FlowRunLogVO subFlowLog : subFlowLogs) {
      parseFlowLog(subFlowLog, tenantId);
      if (CollectionUtils.isNotEmpty(subFlowLog.getStepLogs())) {
        // 递归填充子流程日志
        fillSubFlowLogs(subFlowLog.getStepLogs(), tenantId, subFlowLog);
        stepLogsMap.put(subFlowLog.getLogId(), subFlowLog.getStepLogs());
      }
    }
    for (OrchestrationStepRunLog stepLog : workflowStepLogs) {
      stepLog.setInnerServiceLogs(stepLogsMap.get(stepLog.getInnerServiceLogId()));
    }
  }

  /**
   * 找出子流程节点日志列表
   */
  private void findSubFlowSteps(List<OrchestrationStepRunLog> stepLogs, List<OrchestrationStepRunLog> workflowStepLogs, FlowRunLogVO log) {
    for (OrchestrationStepRunLog stepLog : stepLogs) {
      // 节点填充工作流和智能体ID，提供前端跳转使用
      if ("flow".equals(log.getObjType())) {
        stepLog.setFlowId(log.getObjId());
      }
      else {
        stepLog.setSceneId(log.getObjId());
      }
      stepLog.setTenantId(log.getTenantId());
      if (stepLog.getInnerServiceLogId() != null) {
        workflowStepLogs.add(stepLog);
      }
      else if (CollectionUtils.isNotEmpty(stepLog.getParallelBranchesLogs())) {
        // 递归处理并行分支日志
        for (List<OrchestrationStepRunLog> branchLogs : stepLog.getParallelBranchesLogs()) {
          if (CollectionUtils.isNotEmpty(branchLogs)) {
            findSubFlowSteps(branchLogs, workflowStepLogs, log);
          }
        }
      }
    }
  }

  /**
   * 解析流程执行日志中的 JSON 字符串
   *
   * <p>后端解析更方便前端使用，也避免了 JSON 中包含大整数时前端解析失真的问题。</p>
   */
  private void parseFlowLog(FlowRunLogVO log, Long tenantId) {
    if (StringUtils.isNotEmpty(log.getInputJson())) {
      log.setInput(JsonUtil.parseJsonRequired(log.getInputJson(), new TypeReference<Map<String, Object>>() {
      }));
    }
    if (StringUtils.isNotEmpty(log.getOutputJson())) {
      log.setOutput(JsonUtil.parseJsonRequired(log.getOutputJson(), Object.class));
    }
    if (StringUtils.isNotEmpty(log.getStepLogJson())) {
      log.setStepLogs(JsonUtil.parseJsonRequired(log.getStepLogJson(), new TypeReference<List<OrchestrationStepRunLog>>() {
      }));
    }
    // 填充工作流和智能体ID，提供前端跳转使用
    if ("flow".equals(log.getObjType())) {
      log.setFlowId(log.getObjId());
    }
    else {
      log.setSceneId(log.getObjId());
    }
    log.setTenantId(tenantId);
    log.setInputJson(null);
    log.setOutputJson(null);
    log.setStepLogJson(null);
  }

}
