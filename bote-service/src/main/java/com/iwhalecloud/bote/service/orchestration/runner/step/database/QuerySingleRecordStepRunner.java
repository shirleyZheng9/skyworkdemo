package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.iwhalecloud.bote.common.jdbc.LimitRowsResultSetExtractor;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.QuerySingleRecordStep;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 查询单条记录步骤执行器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class QuerySingleRecordStepRunner extends AbstractDatabaseStepRunner<QuerySingleRecordStep> {

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void executeSql(SceneOrchestrationContext context, QuerySingleRecordStep step, String sql, Object[] params) {
    logger.debug("Executing single-select SQL: sql={}, params={}", sql, params);
    List<String> columnNames = step.getTable().getColumns().stream().map(SimpleDataTableColumnDTO::getColumnCode).toList();
    LimitRowsResultSetExtractor resultSetExtractor = newResultSetExtractor(columnNames);
    List<Map<String, Object>> list = context.getJdbcTemplate(step.getTable().getDataSourceId()).query(sql, resultSetExtractor, params);
    list = ListUtils.emptyIfNull(list);
    logger.debug("Executed single-select SQL successfully: sql={}, count={}", sql, list.size());
    Map<String, Object> result = list.isEmpty() ? null : list.getFirst();
    context.setStepOutput(step, Collections.singletonMap("result", result));
  }

  @Override
  protected void validateStep(QuerySingleRecordStep step) {
    // 查询不需要校验
  }

  @Override
  protected Pair<String, Object[]> buildSql(SceneOrchestrationContext context, QuerySingleRecordStep step, Object[] params) {
    return buildSelectSql(context, step, 1);
  }
}
