package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.iwhalecloud.bote.common.jdbc.LimitRowsResultSetExtractor;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.QueryRecordsStep;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 查询多条记录步骤执行器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class QueryRecordsStepRunner extends AbstractDatabaseStepRunner<QueryRecordsStep> {

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void executeSql(SceneOrchestrationContext context, QueryRecordsStep step, String sql, Object[] params) {
    logger.debug("Executing select SQL: sql={}, params={}", sql, params);
    List<String> columnNames = step.getTable().getColumns().stream().map(SimpleDataTableColumnDTO::getColumnCode).toList();
    LimitRowsResultSetExtractor resultSetExtractor = newResultSetExtractor(columnNames);
    List<Map<String, Object>> list = context.getJdbcTemplate(step.getTable().getDataSourceId()).query(sql, resultSetExtractor, params);
    list = ListUtils.emptyIfNull(list);
    logger.debug("Executed select SQL successfully: sql={}, count={}", sql, list.size());
    context.setStepOutput(step, Collections.singletonMap("result", list));
  }

  @Override
  protected void validateStep(QueryRecordsStep step) {
    // 查询不需要校验
  }

  @Override
  protected Pair<String, Object[]> buildSql(SceneOrchestrationContext context, QueryRecordsStep step, Object[] params) {
    return buildSelectSql(context, step, step.getLimit());
  }
}
