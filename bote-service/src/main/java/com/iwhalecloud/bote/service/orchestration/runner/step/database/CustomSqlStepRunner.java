package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.sql.parse.SqlScriptParseUtil;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.database.CustomSqlStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * 数据库工具步骤执行器
 *
 * @author chen.linfa
 * @since 2025-11-28
 */
public class CustomSqlStepRunner extends AbstractStepRunner<CustomSqlStep> {

  private static final IDataSourceProviderService providerService = SpringUtil.getBean(IDataSourceProviderService.class);

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, CustomSqlStep step, String toolCallId, Map<String, Object> toolArguments,
    Optional<OrchestrationStepRunLog> log) {
    log.ifPresent(l -> {
      Map<String, Object> input = new LinkedHashMap<>();
      input.put("parameters", toolArguments);
      l.setInput(input);
    });
    String sql = MapUtils.getString(toolArguments, "sql");
    String sqlType = getSqlCommandType(sql);
    Pair<Boolean, String> result = validate(step, sql, sqlType, sceneChatParams.getTenantId());
    if (!result.getKey()) {
      return result.getRight();
    }
    // 执行 SQL
    try {
      DataSource dataSource = providerService.getDataSource(sceneChatParams.getTenantId(), step.getDataSourceId());
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      if (BaseConsts.DATABASE_QUERY.equals(sqlType)) {
        List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(sql);
        if (CollectionUtils.isEmpty(queryResult)) {
          return queryResult;
        }
        // 过滤掉平台预置的数据渠道字段
        return queryResult.stream()
          .map(row -> {
            Map<String, Object> filteredRow = new LinkedHashMap<>(row);
            filteredRow.remove(BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL);
            return filteredRow;
          })
          .collect(Collectors.toList());
      }
      else {
        int total = jdbcTemplate.update(sql);
        return total + " row affected";
      }
    }
    catch (Exception e) {
      logger.error("Failed to execute sql script", e);
      return "Failed to execute sql script." + e.getMessage();
    }
  }

  private String getSqlCommandType(String sql) {
    sql = sql.trim().toLowerCase();
    String type = "";
    if (sql.startsWith(BaseConsts.DATABASE_INSERT)) {
      type = BaseConsts.DATABASE_INSERT;
    }
    else if (sql.startsWith(BaseConsts.DATABASE_UPDATE)) {
      type = BaseConsts.DATABASE_UPDATE;
    }
    else if (sql.startsWith(BaseConsts.DATABASE_DELETE)) {
      type = BaseConsts.DATABASE_DELETE;
    }
    else if (sql.startsWith("select")) {
      type = BaseConsts.DATABASE_QUERY;
    }
    return type;
  }

  private Pair<Boolean, String> validate(CustomSqlStep step, String sql, String sqlType, Long tenantId) {
    // 校验必填入参
    Assert.hasText(sql, "入参 SQL 不能为空");
    if (StringUtils.isEmpty(sqlType) || (CollectionUtils.isNotEmpty(step.getAllowed()) && !step.getAllowed().contains(sqlType))) {
      // 限制了数据库操作权限，跳出
      return Pair.of(false, "the tool to be called is not available");
    }

    // 验证 SQL 合法性
    String dataType = providerService.getDataSourceType(tenantId, step.getDataSourceId()).name().toLowerCase();
    try {
      SqlScriptParseUtil.getSqlSource(sql, dataType);
    }
    catch (Exception e) {
      logger.error("Failed to parse sql script", e);
      return Pair.of(false, "the sql is invalid");
    }
    return Pair.of(true, null);
  }

}
