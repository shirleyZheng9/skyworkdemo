package com.iwhalecloud.bote.service.orchestration.runner.step.database;

import com.iwhalecloud.bote.cache.DataTableCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.datatype.AttrValueTypeConverter;
import com.iwhalecloud.bote.common.enums.ParamsMatchType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.jdbc.LimitRowsResultSetExtractor;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.base.SqlParameterSpec;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.AbstractDatabaseStep;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.consts.DatabaseType;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 数据库相关的步骤执行器抽象类
 * <p>查询类操作输出查询结果，更新类操作输出 affectedRows</p>
 *
 * @author bianjp
 * @since 2025-11-25
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractDatabaseStepRunner<T extends AbstractDatabaseStep> extends AbstractStepRunner<T> {

  private final DataTableCache tableCache = SpringUtil.getBean(DataTableCache.class);
  /** 属性值类型转换器 */
  private final AttrValueTypeConverter converter = SpringUtil.getBean(AttrValueTypeConverter.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, T step) {
    prepare(context, step);
    // 解析参数
    Object[] params = parseParameters(step.getParameters());
    // 校验必填参数
    validateRequiredSqlParams(step.getParameters(), params);
    // 构造 SQL
    Pair<String, Object[]> generateResult = buildSql(context, step, params);
    context.setStepInputLog(generateResult.getRight());
    context.addStepLog("SQL: %s, params: %s", generateResult.getLeft(), JsonUtil.toJsonString(generateResult.getRight()));
    executeSql(context, step, generateResult.getLeft(), generateResult.getRight());
  }

  /**
   * 校验步骤配置
   *
   * @param step 步骤
   */
  protected abstract void validateStep(T step);

  /**
   * 构造 SQL 语句
   *
   * @param context 上下文
   * @param step 步骤
   * @param params 参数数组
   * @return 左边为 SQL, 右边为参数数组
   */
  protected abstract Pair<String, Object[]> buildSql(SceneOrchestrationContext context, T step, Object[] params);

  /**
   * 执行 SQL 语句
   *
   * @param context 上下文
   * @param step 步骤
   * @param sql SQL 语句
   * @param params 参数列表
   */
  protected abstract void executeSql(SceneOrchestrationContext context, T step, String sql, Object[] params);

  /**
   * 构造限制最大返回行数的查询结果集提取器
   */
  protected LimitRowsResultSetExtractor newResultSetExtractor(@Nullable List<String> columnNames) {
    return new LimitRowsResultSetExtractor(SystemParameter.SQL_QUERY_RECORD_LIMIT.getRequiredIntegerValueFromDb(), columnNames);
  }

  /**
   * 构造查询语句
   */
  protected Pair<String, Object[]> buildSelectSql(SceneOrchestrationContext context, T step, @Nullable Integer limit) {
    Pair<String, Object[]> whereClauseResult = buildWhereClause(step, context);
    SimpleDataTableDTO table = step.getTable();
    StringBuilder sb = new StringBuilder("SELECT ").append(getTableColumnCodes(table)).append(" FROM ").append(table.getTableCode());
    // 查询单条记录步骤没有非空的 where 条件，不查询出结果
    if (whereClauseResult == null && StepType.QUERY_SINGLE_RECORD.equals(step.getType())) {
      limit = 0;
    }
    if (whereClauseResult != null) {
      sb.append(whereClauseResult.getLeft());
    }
    String tmpSql = sb.toString();
    if (limit != null) {
      tmpSql = addLimitClause(context, step, tmpSql, limit);
    }
    return Pair.of(tmpSql, whereClauseResult != null ? whereClauseResult.getRight() : new Object[0]);
  }

  /**
   * 提取表字段编码列表，过滤必要的字段
   */
  private String getTableColumnCodes(SimpleDataTableDTO table) {
    // 过滤表中预置的平台数据渠道字段
    List<String> columnCodeList = table.getColumns().stream()
      .map(SimpleDataTableColumnDTO::getColumnCode)
      .filter(columnCode -> !BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equals(columnCode))
      .toList();
    return String.join(", ", columnCodeList);
  }

  /**
   * 构造 where 子句
   *
   * @param step 步骤
   * @return 左边为 where 子句，右边为实际应使用的参数值数组（有些条件不需要参数值，比如 IS NULL）
   */
  @Nullable
  protected Pair<String, Object[]> buildWhereClause(T step, SceneOrchestrationContext context) {
    List<SqlParameterSpec> specs = step.getWhere();
    if (CollectionUtils.isEmpty(specs)) {
      return null;
    }
    List<Object> params = new ArrayList<>(specs.size());
    StringBuilder sb = new StringBuilder(specs.size() * 30);
    sb.append(" WHERE 1=1");
    int initialSize = sb.length();
    for (SqlParameterSpec spec : specs) {
      appendCondition(step, spec, sb, params);
    }
    if (sb.length() == initialSize) {
      return null;
    }

    // 添加数据渠道条件
    appendDataTunnelCondition(context, step.getTable(), sb, params);

    // 删除 WHERE 后面的 " 1=1 AND"
    sb.delete(6, 14);
    return Pair.of(sb.toString(), params.toArray(new Object[0]));
  }

  /**
   * 准备工作
   */
  private void prepare(SceneOrchestrationContext context, T step) {
    // 校验
    SimpleDataTableDTO table = tableCache.get(context.getTenantId(), step.getTableId());
    Assert.notNull(table, () -> "表定义不存在，tableId=" + step.getTableId());
    validateStep(step);
    // 初始化数据源
    context.initDataSource(table.getDataSourceId());
    step.setTable(table);
  }

  /**
   * 解析参数值
   */
  private Object[] parseParameters(@Nullable List<SqlParameterSpec> parameters) {
    if (CollectionUtils.isEmpty(parameters)) {
      return new Object[0];
    }

    List<Object> result = new ArrayList<>(parameters.size());
    for (SqlParameterSpec spec : parameters) {
      Object value = parseParamValue(spec);
      result.add(value);
    }

    return result.toArray(new Object[0]);
  }

  /**
   * 校验必填参数
   *
   * @param specs 参数规格列表
   * @param params 参数值列表
   */
  private void validateRequiredSqlParams(List<SqlParameterSpec> specs, Object[] params) {
    if (CollectionUtils.isNotEmpty(specs)) {
      for (int i = 0; i < specs.size(); i++) {
        SqlParameterSpec spec = specs.get(i);
        if (Boolean.TRUE.equals(spec.getRequired())) {
          Assert.notNull(params[i], () -> "缺少必填入参：" + spec.getName());
        }
      }
    }
  }

  /**
   * 添加 limit 子句
   */
  private String addLimitClause(SceneOrchestrationContext context, T step, String tmpSql, @Nullable Integer limit) {
    // Oracle 需要使用 ROWNUM
    DatabaseType dataSourceType = context.getDataSourceType(step.getTable().getDataSourceId());
    if (dataSourceType.isOracle()) {
      // 别名加上时间戳以避免冲突
      return "SELECT * FROM (" + tmpSql + ") tmp_" + System.currentTimeMillis() + " WHERE ROWNUM <= " + limit;
    }
    return tmpSql + " limit " + limit;
  }

  /**
   * 追加查询条件
   */
  @Nullable
  private void appendCondition(T step, SqlParameterSpec spec, StringBuilder sb, List<Object> params) {
    ParamsMatchType matchType = ParamsMatchType.findMatchType(spec.getOperator());
    String name = spec.getName();
    if (matchType == ParamsMatchType.IS_NULL) {
      sb.append(" AND ").append(name).append(" IS NULL");
    }
    else if (matchType == ParamsMatchType.NOT_NULL) {
      sb.append(" AND ").append(name).append(" IS NOT NULL");
    }
    else if (matchType == ParamsMatchType.IN || matchType == ParamsMatchType.NOT_IN) {
      appendInOperatorCondition(step, spec, sb, params, matchType);
    }
    else if (matchType.isArithmeticCompare() || matchType.isFuzzyMatching()) {
      appendBinaryOperatorCondition(step, spec, sb, params, matchType);
    }
    else if (matchType.isBetween()) {
      appendBetweenCondition(step, spec, sb, params, matchType);
    }
    else {
      throw new BssException("不支持的查询条件匹配类型: " + matchType.getCode());
    }
  }

  /**
   * 追加 IN/NOT IN 条件
   */
  @SuppressWarnings("unchecked")
  private void appendInOperatorCondition(T step, SqlParameterSpec spec, StringBuilder sb, List<Object> params, ParamsMatchType matchType) {
    Collection<Object> values;
    // 如果是字符串常量，当作逗号分隔的列表
    if (StringUtils.isNotEmpty(spec.getValue()) && !spec.getValue().startsWith("$.")) {
      values = Arrays.stream(spec.getValue().trim().split("\\s*,\\s*")).filter(StringUtils::isNotEmpty)
        .map(v -> convertValueType(spec.getName(), v, spec.getDataType())).collect(Collectors.toList());
    }
    // 赋值表达式
    else {
      Object value = parseParamValue(spec);
      if (value == null) {
        values = Collections.emptyList();
      }
      else {
        Assert.isTrue(value instanceof Collection, () -> "条件 " + spec.getName() + " 的参数值必须是列表");
        values = (Collection<Object>) value;
      }
    }
    // 校验必填参数
    if (values.isEmpty() && shouldIgnoreCondition(step, spec)) {
      return;
    }
    String operator = matchType == ParamsMatchType.IN ? " IN " : " NOT IN ";
    sb.append(" AND ").append(spec.getName()).append(operator).append("(").append(StringUtils.repeat("?", ",", values.size())).append(")");
    params.addAll(values);
  }

  /**
   * 追加二元操作符的查询条件
   */
  private void appendBinaryOperatorCondition(T step, SqlParameterSpec spec, StringBuilder sb, List<Object> params, ParamsMatchType matchType) {
    Object value = parseParamValue(spec);
    if (value == null && shouldIgnoreCondition(step, spec)) {
      return;
    }
    params.add(value);
    if (matchType.isArithmeticCompare()) {
      sb.append(" AND ").append(spec.getName()).append(" ").append(matchType.getCode()).append(" ?");
    }
    else {
      sb.append(" AND ").append(spec.getName()).append(buildFuzzyOperator(matchType));
    }
  }

  /**
   * 追加范围查询条件
   */
  private void appendBetweenCondition(T step, SqlParameterSpec spec, StringBuilder sb, List<Object> params, ParamsMatchType matchType) {
    String[] pieces = StringUtils.split(spec.getValue(), "~");
    if (pieces == null || pieces.length != 2) {
      throw new BssException("非法的范围查询条件输入值配置: field=" + spec.getName() + ", expression=" + spec.getValue());
    }
    Object leftValue = convertValueType(pieces[0], SceneParamUtil.getParamValue(pieces[0]), spec.getDataType());
    Object rightValue = convertValueType(pieces[1], SceneParamUtil.getParamValue(pieces[1]), spec.getDataType());
    // 校验必填参数
    if ((leftValue == null || rightValue == null) && shouldIgnoreCondition(step, spec)) {
      return;
    }
    params.add(leftValue);
    params.add(rightValue);
    sb.append(" AND (").append(spec.getName());
    if (matchType == ParamsMatchType.BETWEEN) {
      sb.append(" >= ? ").append(" AND ").append(spec.getName()).append(" <= ? ");
    }
    else {
      sb.append(" < ? ").append(" OR ").append(spec.getName()).append(" > ? ");
    }
    sb.append(")");
  }

  /**
   * 添加数据渠道条件
   */
  private void appendDataTunnelCondition(SceneOrchestrationContext context, SimpleDataTableDTO table, StringBuilder sb,
                                         List<Object> params) {
    // 检查是否包含平台预置的数据渠道字段
    boolean match = table.getColumns().stream()
      .anyMatch(o -> BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL.equals(o.getColumnCode()));
    if (match) {
      // 通过是否测试场景构建数据渠道过滤条件
      String value = context.getRequest().getDebug() ? BaseConsts.DATA_TUNNEL_TEST : BaseConsts.DATA_TUNNEL_OFFICIAL;
      sb.append(" AND ").append(BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL).append(" = ?");
      params.add(value);
    }
  }

  /**
   * 解析参数值
   */
  @Nullable
  private Object parseParamValue(SqlParameterSpec spec) {
    Object value;
    // 优先取明确指定的值
    if (StringUtils.isNotEmpty(spec.getValue())) {
      value = SceneParamUtil.getParamValue(spec.getValue());
      // 值为列表但数据类型为基本类型时，做个特殊处理，保持列表类型（否则 Spring 的 CollectionToObjectConverter 会将列表转为其第一个元素），以方便自定义 SQL 查询节点中使用 IN 条件（解析 SQL 时无法正确识别到参数应为列表）
      // 比如 select id, name from customer where id in (:ids), NamedParameterJdbcTemplate 识别到 ids 参数为列表类型时会自动展开为多个参数。如果 ids 参数值变成列表的第一个元素值则无法实现 IN 查询效果
      // org.springframework.jdbc.core.namedparam.NamedParameterUtils#substituteNamedParameters
      // FIXME
      if (value instanceof Collection && StringUtils.isNotEmpty(spec.getDataType())) {
        value = ((Collection<?>) value).stream().map(v -> convertValueType(spec.getName(), v, spec.getDataType())).collect(Collectors.toList());
      }
      else {
        value = convertValueType(spec.getName(), value, spec.getDataType());
      }
    }
    else {
      value = null;
    }
    return value;
  }

  /**
   * 转换属性值类型
   *
   * @param propertyPath 属性路径
   * @param value 原始值
   * @param targetType 目标类型
   * @return 转换后的值
   */
  @Nullable
  private Object convertValueType(@Nullable String propertyPath, @Nullable Object value, @Nullable String targetType) {
    if (value == null) {
      return null;
    }
    return converter.convertValue(propertyPath, value, targetType);
  }

  /**
   * 检查是否应该忽略条件
   */
  private boolean shouldIgnoreCondition(T step, SqlParameterSpec spec) {
    // 校验必填参数
    validateRequiredSqlParam(step, spec);
    // 输入值为空且非必填时忽略查询条件，避免执行失败
    if (!Boolean.TRUE.equals(spec.getRequired())) {
      logger.debug("Ignore where condition as param is null: step={}, field={}, operator={}", step.getName(), spec.getName(), spec.getOperator());
      return true;
    }
    return false;
  }

  /**
   * 校验必填 SQL 参数
   *
   * @param step 步骤
   * @param spec 参数规格
   */
  private void validateRequiredSqlParam(T step, SqlParameterSpec spec) {
    if (Boolean.TRUE.equals(spec.getRequired())) {
      throw new BssException("步骤【" + step.getName() + "】缺少必填入参：" + spec.getName());
    }
  }

  /**
   * 构造模糊匹配表达式
   */
  private static String buildFuzzyOperator(ParamsMatchType matchType) {
    String operatorClause;
    switch (matchType) {
      case CONTAINS:
        operatorClause = " LIKE CONCAT(CONCAT('%', ?),'%')";
        break;
      case NOT_CONTAINS:
        operatorClause = " NOT LIKE CONCAT(CONCAT('%', ?),'%')";
        break;
      case STARTS_WITH:
        operatorClause = " LIKE CONCAT(?, '%')";
        break;
      case ENDS_WITH:
        operatorClause = " LIKE CONCAT('%', ?)";
        break;
      default:
        throw new BssException("不支持的查询条件匹配类型: " + matchType.getCode());
    }
    return operatorClause;
  }
}
