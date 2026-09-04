package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.dto.base.SqlParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.UpdateRecordsStep;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

/**
 * 更新记录步骤执行器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class UpdateRecordsStepRunner extends AbstractDatabaseStepRunner<UpdateRecordsStep> {

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  protected void executeSql(SceneOrchestrationContext context, UpdateRecordsStep step, String sql, Object[] params) {
    logger.debug("Executing update SQL: sql={}, params={}", sql, params);
    int affectedRows = context.getJdbcTemplate(step.getTable().getDataSourceId()).update(sql, params);
    logger.debug("Executed update SQL successfully: sql={}, affectedRows={}", sql, affectedRows);
    context.setStepOutput(step, ImmutableMap.of("affectedRows", affectedRows));
  }

  @Override
  protected void validateStep(UpdateRecordsStep step) {
    Assert.notEmpty(step.getParameters(), "未指定更新字段");
    Assert.notEmpty(step.getWhere(), "未指定 where 条件");
  }

  @Override
  protected Pair<String, Object[]> buildSql(SceneOrchestrationContext context, UpdateRecordsStep step, Object[] params) {
    Pair<String, Object[]> whereClauseResult = buildWhereClause(step, context);
    Assert.notNull(whereClauseResult, "没有非空的 where 条件");
    String whereClause = whereClauseResult.getLeft();
    StringBuilder sb = new StringBuilder(step.getParameters().size() * 20 + whereClause.length());
    sb.append("UPDATE ").append(step.getTable().getTableCode()).append(" SET ");
    for (int i = 0, count = step.getParameters().size(); i < count; i++) {
      SqlParameterSpec spec = step.getParameters().get(i);
      sb.append(spec.getName()).append(" = ?");
      if (i + 1 < count) {
        sb.append(", ");
      }
    }
    sb.append(whereClause);
    String sql = sb.toString();

    // 将 set 参数和 where 参数合并为一个数组
    Object[] whereParams = whereClauseResult.getRight();
    Object[] allParams = new Object[params.length + whereParams.length];
    System.arraycopy(params, 0, allParams, 0, params.length);
    System.arraycopy(whereParams, 0, allParams, params.length, whereParams.length);
    return Pair.of(sql, allParams);
  }
}
