package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.SqlParameterSpec;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.InsertRecordStep;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;

/**
 * 插入记录步骤执行器
 *
 * @author bianjp
 * @since 2020-11-02
 */
public class InsertRecordStepRunner extends AbstractDatabaseStepRunner<InsertRecordStep> {

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  protected void executeSql(SceneOrchestrationContext context, InsertRecordStep step, String sql, Object[] params) {
    logger.debug("Executing insert SQL: sql={}, params={}", sql, params);
    int affectedRows = context.getJdbcTemplate(step.getTable().getDataSourceId()).update(sql, params);
    logger.debug("Executed insert SQL successfully: sql={}, affectedRows={}", sql, affectedRows);

    Map<String, Object> result = new HashMap<>(4);
    result.put("affectedRows", affectedRows);
    // 找出主键字段的值 TODO
    //    String primaryKey = step.getPrimaryKey();
    //    if (StringUtils.isNotEmpty(primaryKey)) {
    //      int index = IterableUtils.indexOf(step.getParameters(), p -> primaryKey.equalsIgnoreCase(p.getName()));
    //      if (index != -1) {
    //        Object id = params[index];
    //        result.put("id", id);
    //      }
    //    }
    context.setStepOutput(step, result);
  }

  @Override
  protected void validateStep(InsertRecordStep step) {
    Assert.notEmpty(step.getParameters(), "未指定插入字段");
  }

  @Override
  protected Pair<String, Object[]> buildSql(SceneOrchestrationContext context, InsertRecordStep step, Object[] params) {
    SimpleDataTableDTO table = step.getTable();
    // 检查是否包含平台预置的数据渠道字段
    boolean match = table.getColumns().stream()
      .anyMatch(o -> BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equals(o.getColumnCode()));
    // 组装赋值字段名
    String columnNames = step.getParameters().stream().map(SqlParameterSpec::getName).collect(Collectors.joining(", "));
    if (match) {
      columnNames = columnNames + ", " + BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL;
    }
    // 组装sql预编译占位符
    String placeholders = StringUtils.repeat("?", ", ", step.getParameters().size());
    if (match) {
      placeholders += ", ?";
    }
    String sql = "INSERT INTO " + table.getTableCode() + "(" + columnNames + ") VALUES (" + placeholders + ")";
    // 补充数据渠道参数值
    if (match) {
      List<Object> list = new ArrayList<>(Arrays.asList(params));
      list.add(context.getRequest().getDebug() ? BaseConsts.DATA_TUNNEL_TEST : BaseConsts.DATA_TUNNEL_OFFICIAL);
      params = list.toArray();
    }
    return Pair.of(sql, params);
  }

}
