package com.iwhalecloud.bote.service.orchestration;

import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;

/**
 * 场景编排引擎
 *
 * @author bianjp
 * @since 2024-08-29
 */
public interface IOrchestrationEngine {

  /**
   * 执行服务
   *
   * @param request 场景编排执行请求
   * @return 场景编排执行响应
   */
  OrchestrationEngineResponse run(OrchestrationEngineRequest request);

}
