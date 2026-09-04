package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.SqlStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.engine.SqlSkillEngine;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * SQL 步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class SqlStepRunner extends AbstractStepRunner<SqlStep> {
  private final SqlSkillEngine sqlSkillEngine = SpringUtil.getBean(SqlSkillEngine.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, SqlStep step) {
    Map<String, Object> params = buildRequestParametersToMap(step.getParameters());
    context.setStepInputLog(params);
    context.setStepOutput(step, executeSql(context.getTenantId(), step.getSqlId(), params));
  }

  @Override
  @Nullable
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, SqlStep step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    // 留给 SQL 引擎转换入参
    log.ifPresent(l -> l.setInput(toolArguments));
    return executeSql(sceneChatParams.getTenantId(), step.getSqlId(), toolArguments);
  }

  /**
   * 执行 SQL
   */
  @Nullable
  private Object executeSql(Long tenantId, Long sqlId, @Nullable Map<String, Object> params) {
    Assert.notNull(sqlId, "sqlId 不能为空");
    return sqlSkillEngine.execute(tenantId, sqlId, params);
  }

}
