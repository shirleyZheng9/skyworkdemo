package com.iwhalecloud.bote.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.job.AbstractSimpleJob;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 调用工作流定时任务
 *
 * @author qian.sisheng
 * @since 2025-11-07
 */
public class CallFlowServiceJob extends AbstractSimpleJob {

  public CallFlowServiceJob() {
    super("调用工作流定时任务");
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void doExecute(ShardingContext shardingContext) {
    String jobParameter = shardingContext.getJobParameter();
    if (StringUtils.isEmpty(jobParameter)) {
      throw new BssException("作业参数不能为空");
    }
    OrchestrationEngineRequest request = JsonUtil.parseJson(jobParameter, new TypeReference<>() {
    });
    if (request == null) {
      throw new BssException("作业参数格式错误");
    }
    Long flowId = request.getFlowId();
    if (flowId == null) {
      throw new BssException("流程ID不能为空");
    }
    logger.debug("Start call flow service job: flowId={}, params={}", flowId, request.getParameters());
    SpringUtil.getBean(IOrchestrationEngine.class).run(request);
  }
}
