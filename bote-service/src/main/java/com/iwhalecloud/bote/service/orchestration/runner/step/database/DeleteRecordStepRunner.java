package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.DeleteRecordsStep;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

/**
 * 删除记录步骤执行器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class DeleteRecordStepRunner extends AbstractDatabaseStepRunner<DeleteRecordsStep> {

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  protected void executeSql(SceneOrchestrationContext context, DeleteRecordsStep step, String sql, Object[] params) {
    logger.debug("Executing delete SQL: sql={}, params={}", sql, params);
    int affectedRows = context.getJdbcTemplate(step.getTable().getDataSourceId()).update(sql, params);
    logger.debug("Executed delete SQL successfully: sql={}, affectedRows={}", sql, affectedRows);
    context.setStepOutput(step, ImmutableMap.of("affectedRows", affectedRows));
  }

  @Override
  protected void validateStep(DeleteRecordsStep step) {
    Assert.notEmpty(step.getWhere(), "未指定 where 条件");
  }

  @Override
  protected Pair<String, Object[]> buildSql(SceneOrchestrationContext context, DeleteRecordsStep step, Object[] params) {
    Pair<String, Object[]> whereClauseResult = buildWhereClause(step, context);
    Assert.notNull(whereClauseResult, "没有非空的 where 条件");
    String sql = "DELETE FROM " + step.getTable().getTableCode() + whereClauseResult.getLeft();
    return Pair.of(sql, whereClauseResult.getRight());
  }
}
