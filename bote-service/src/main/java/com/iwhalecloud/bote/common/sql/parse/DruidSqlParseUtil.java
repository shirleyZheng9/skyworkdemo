package com.iwhalecloud.bote.common.sql.parse;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.util.JdbcUtils;
import com.google.common.base.CaseFormat;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.sql.visitor.MysqlSelectStmtVisitor;
import com.iwhalecloud.bote.common.sql.visitor.OracleSelectStmtVisitor;
import com.iwhalecloud.bote.common.sql.visitor.PostgresqlSelectStmtVisitor;
import com.iwhalecloud.bote.common.util.DatabaseUtil;
import com.iwhalecloud.bote.dto.skill.SqlScriptGenerateInfo;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Column;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * SQL解析工具类
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
public final class DruidSqlParseUtil {
  private DruidSqlParseUtil() {
  }

  private static final String EXPR = "expr";

  private static final String PARAM_NAME = "paramName";

  private static final String COLUMN_NAME = "columnName";

  private static final String ALIAS = "alias";

  private static final String IS_LIST = "isList";

  /**
   * 字段名下划线转驼峰
   *
   * @param columnName 字段名
   * @return 驼峰字段名
   */
  public static String convertColumnName(String columnName) {
    return columnName.contains("_") ? StringUtils.uncapitalize(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName)) : columnName;
  }

  /**
   * 构建SQL脚本生成信息
   *
   * @param scriptSql SQL脚本
   * @param databaseType 数据库类型
   * @return 加工后的SQL脚本生成信息
   */
  @Nullable
  public static SqlScriptGenerateInfo buildSqlScriptGenerateInfo(String scriptSql, String databaseType) {
    databaseType = DatabaseUtil.getDatabaseDialect(databaseType, databaseType);
    SQLStatement sqlStatement = SQLUtils.parseSingleStatement(scriptSql, databaseType);
    if (sqlStatement instanceof SQLSelectStatement) {
      if (JdbcUtils.isOracleDbType(databaseType)) {
        OracleSelectStmtVisitor oracleVisitor = new OracleSelectStmtVisitor();
        sqlStatement.accept(oracleVisitor);
        return oracleVisitor.getGenerateInfo();
      }
      else if (JdbcUtils.isMysqlDbType(databaseType)) {
        MysqlSelectStmtVisitor mysqlVisitor = new MysqlSelectStmtVisitor();
        sqlStatement.accept(mysqlVisitor);
        return mysqlVisitor.getGenerateInfo();
      }
      else if (JdbcUtils.isPgsqlDbType(databaseType)) {
        PostgresqlSelectStmtVisitor postgresqlVisitor = new PostgresqlSelectStmtVisitor();
        sqlStatement.accept(postgresqlVisitor);
        return postgresqlVisitor.getGenerateInfo();
      }
      else {
        throw BaseErrorConstant.UNSUPPORTED_DATABASE_TYPE.toException(databaseType);
      }
    }
    return null;
  }

  /**
   * 访问查询语句
   *
   * @param selectStatement 查询语句
   * @param info SQL脚本生成信息
   */
  public static void visitSqlSelectStatement(SQLSelectStatement selectStatement, SqlScriptGenerateInfo info) {
    // 处理查询参数列表
    SQLSelectQuery query = selectStatement.getSelect().getQuery();
    // 全量查询表达式
    String fullQuerySql = null;
    if (query instanceof SQLSelectQueryBlock) {
      fullQuerySql = processSelectQueryBlock((SQLSelectQueryBlock) query, selectStatement.getDbType().name(), info);
    }
    else if (query instanceof SQLUnionQuery) {
      for (SQLSelectQuery relation : ((SQLUnionQuery) query).getRelations()) {
        if (relation instanceof SQLSelectQueryBlock) {
          fillSelectItemList((SQLSelectQueryBlock) relation, info);
        }
      }
      fullQuerySql = SQLUtils.toSQLString(query, selectStatement.getDbType());
    }
    // 美化排版
    if (StringUtils.isNotEmpty(fullQuerySql)) {
      fullQuerySql = fullQuerySql.replace("\n", "\n\t\t");
      info.setFullQuerySql(fullQuerySql);
    }
  }

  /**
   * 处理 SelectQueryBlock，提取出入参及其表达式
   *
   * @return 全量查询语句
   */
  private static String processSelectQueryBlock(SQLSelectQueryBlock queryBlock, String dbType, SqlScriptGenerateInfo info) {
    // 提取 SELECT 参数列表
    fillSelectItemList(queryBlock, info);
    // 处理 FROM 表达式
    SQLTableSource fromExpr = queryBlock.getFrom();
    if (fromExpr != null) {
      String fromExprStr = SQLUtils.toSQLString(fromExpr, dbType);
      info.setTableExpr(fromExprStr.replace("\n", "\n\t"));
    }
    // 截取全量查询表达式
    String queryStr = SQLUtils.toSQLString(queryBlock, dbType);
    // 处理 ORDER BY 表达式
    SQLOrderBy orderBy = queryBlock.getOrderBy();
    if (orderBy != null) {
      String orderByStr = SQLUtils.toSQLString(queryBlock.getOrderBy(), dbType);
      info.setOrderBy(orderByStr);
      queryStr = queryStr.substring(0, queryStr.indexOf(orderByStr));
    }
    SQLSelectGroupByClause groupBy = queryBlock.getGroupBy();
    if (groupBy != null) {
      // 处理 GROUP BY 表达式
      String groupByStr = SQLUtils.toSQLString(queryBlock.getGroupBy(), dbType);
      info.setGroupBy(groupByStr);
      queryStr = queryStr.substring(0, queryStr.indexOf(groupByStr));
    }
    // 处理 WHERE 表达式
    SQLExpr where = queryBlock.getWhere();
    if (where != null) {
      String whereStr = SQLUtils.toSQLString(queryBlock.getWhere(), dbType);
      String[] conditions = whereStr.split("\n");
      for (String condition : conditions) {
        Map<String, String> conditionMap = new HashMap<>(4);
        conditionMap.put(EXPR, condition.replace("\t", "").replace("AND ", ""));
        info.getConditionList().add(conditionMap);
      }
      queryStr = queryStr.substring(0, queryStr.indexOf(whereStr) - 7);
    }
    return queryStr;
  }

  /**
   * 组装查询参数列表
   */
  private static void fillSelectItemList(SQLSelectQueryBlock selectQueryBlock, SqlScriptGenerateInfo info) {
    selectQueryBlock.getSelectList().forEach(selectItem -> {
      Map<String, String> selectItemMap = new HashMap<>(8);
      String selectItemStr = SQLUtils.toSQLString(selectItem, selectQueryBlock.getDbType());
      selectItemMap.put(EXPR, selectItemStr.replace("\n", ""));
      SQLExpr expr = selectItem.getExpr();
      String columnName = "";
      if (expr instanceof SQLIdentifierExpr) {
        columnName = ((SQLIdentifierExpr) expr).getName();
      }
      else if (expr instanceof SQLPropertyExpr) {
        columnName = ((SQLPropertyExpr) expr).getName();
      }
      selectItemMap.put(COLUMN_NAME, columnName);
      String alias = selectItem.getAlias();
      if (StringUtils.isNotEmpty(alias)) {
        // 避免 UNION 语法重复添加字段
        if (info.getColumnMapGroups().get(alias) == null) {
          info.getColumnMapGroups().put(alias, selectItemMap);
          selectItemMap.put(PARAM_NAME, convertColumnName(alias));
          selectItemMap.put(ALIAS, alias);
          info.getSelectItemList().add(selectItemMap);
        }
      }
      else {
        if (info.getColumnMapGroups().get(columnName) == null) {
          info.getColumnMapGroups().put(columnName, selectItemMap);
          selectItemMap.put(PARAM_NAME, convertColumnName(columnName));
          selectItemMap.put(ALIAS, columnName);
          info.getSelectItemList().add(selectItemMap);
        }
      }
    });
  }

  /**
   * 处理变量引用表达式
   */
  public static void visitSqlVariantRefExpr(SQLVariantRefExpr variantRefExpr, SqlScriptGenerateInfo info) {
    Map<String, String> inputItem = new HashMap<>(4);
    buildInputItemList(variantRefExpr, inputItem, info);
    for (Map<String, String> condition : info.getConditionList()) {
      if (Objects.equals(condition.get(EXPR), inputItem.get(EXPR))) {
        condition.put(COLUMN_NAME, inputItem.get(COLUMN_NAME));
        condition.put(PARAM_NAME, inputItem.get(PARAM_NAME));
      }
    }
  }

  /**
   * 构建入参名列表，并填充到解析结果中
   *
   * @param variantRefExpr 变量表达式
   */
  private static void buildInputItemList(SQLVariantRefExpr variantRefExpr, Map<String, String> inputItem, SqlScriptGenerateInfo info) {
    String paramExpr = variantRefExpr.toString();
    String paramName = "";
    if (paramExpr.startsWith(":")) {
      paramName = paramExpr.substring(1);
    }
    else if (paramExpr.startsWith("#")) {
      // 截取 {xxx} 中的内容作为变量名
      paramName = paramExpr.substring(paramExpr.indexOf("{") + 1, paramExpr.lastIndexOf("}"));
    }
    inputItem.put(PARAM_NAME, paramName);
    inputItem.put(IS_LIST, BaseConsts.FALSE);
    // 以下对表字段名进行提取
    if (variantRefExpr.getParent() instanceof SQLBinaryOpExpr) {
      processSqlBinaryOpExpr(variantRefExpr, inputItem);
      inputItem.put(EXPR, variantRefExpr.getParent().toString());
    }
    // 适用于表达式使用了函数的情况
    else if (variantRefExpr.getParent() instanceof SQLMethodInvokeExpr) {
      processSqlMethodInvokeExpr(variantRefExpr, inputItem);
      inputItem.put(EXPR, variantRefExpr.getParent().getParent().toString());
    }
    else if (variantRefExpr.getParent() instanceof SQLInListExpr) {
      SQLExpr expr = ((SQLInListExpr) variantRefExpr.getParent()).getExpr();
      String exprStr = expr.toString();
      String targetExprStr = exprStr + " IN (" + paramExpr + ")";
      String replaceExprStr = exprStr + " IN (${" + paramName + "})";
      for (Map<String, String> condition : info.getConditionList()) {
        if (Objects.equals(condition.get(EXPR), targetExprStr)) {
          condition.put(EXPR, replaceExprStr);
          break;
        }
      }
      inputItem.put(COLUMN_NAME, exprStr);
      inputItem.put(EXPR, replaceExprStr);
      inputItem.put(IS_LIST, BaseConsts.TRUE);
    }
    info.getInputItemList().add(inputItem);
  }

  private static void processSqlBinaryOpExpr(SQLVariantRefExpr variantRefExpr, Map<String, String> inputItem) {
    // 适用于表达式形如 a.xxx = #{xxx} 的情况
    if (((SQLBinaryOpExpr) variantRefExpr.getParent()).getLeft() instanceof SQLPropertyExpr) {
      inputItem.put(COLUMN_NAME, ((SQLPropertyExpr) ((SQLBinaryOpExpr) variantRefExpr.getParent()).getLeft()).getName());
    }
    // 适用于表达式形如 xxx = #{xxx} 的情况
    else {
      inputItem.put(COLUMN_NAME, ((SQLBinaryOpExpr) variantRefExpr.getParent()).getLeft().toString());
    }
  }

  private static void processSqlMethodInvokeExpr(SQLVariantRefExpr variantRefExpr, Map<String, String> inputItem) {
    SQLObject grandparent = variantRefExpr.getParent().getParent();
    if (grandparent instanceof SQLBinaryOpExpr) {
      SQLExpr left = ((SQLBinaryOpExpr) grandparent).getLeft();
      // 适用于表达式运算符左边没有使用函数的情况，如 xxx = lower(#{xxx})
      if (left instanceof SQLIdentifierExpr) {
        inputItem.put(COLUMN_NAME, ((SQLIdentifierExpr) left).getName());
      }
      // 适用于表达式运算符左边没有使用函数的情况，如 a.xxx = lower(#{xxx})
      else if (left instanceof SQLPropertyExpr) {
        inputItem.put(COLUMN_NAME, ((SQLPropertyExpr) left).getName());
      }
      // 适用于表达式运算符两边都使用函数的情况，lower(xxx) = lower(#{xxx})
      else if (left instanceof SQLMethodInvokeExpr) {
        ((SQLMethodInvokeExpr) left).getArguments().forEach(expr -> {
          if (expr instanceof SQLIdentifierExpr) {
            inputItem.put(COLUMN_NAME, ((SQLIdentifierExpr) expr).getName());
          }
        });
      }
    }
  }

  /**
   * 构建 select * 情况下的出参字段列表
   */
  public static List<Map<String, String>> builtSelectItemList(String scriptSql, String databaseType, DataSource dataSource) {
    try {
      String sql = processAsteriskSelectSql(scriptSql, databaseType, dataSource);
      if (!Objects.equals(sql, scriptSql)) {
        SqlScriptGenerateInfo generateInfo = buildSqlScriptGenerateInfo(sql, databaseType);
        return generateInfo != null ? generateInfo.getSelectItemList() : Collections.emptyList();
      }
      else {
        return Collections.emptyList();
      }
    }
    catch (Exception e) {
      return Collections.emptyList();
    }
  }

  /**
   * 处理含有 select * 的 sql，将 * 展开替换成具体的字段，需要考虑多种情况
   * <li>1. from 语句中的表来自于嵌套的 select 语句；</li>
   * <li>2. 多表之间的连表查询，所以可能会出现 select a.*, b.* 的操作</li>
   * <li>3. 支持处理 select *, b.* 这种确实表别名的混合情况 </li>
   *
   * @param scriptSql 含有 select * 的脚本
   * @param databaseType 数据类型
   * @param dataSource 数据源
   * @return 替换后的sql脚本
   */
  public static String processAsteriskSelectSql(String scriptSql, String databaseType, DataSource dataSource) {
    // 预处理SQL: 移除行首注释
    String parseSql = scriptSql.toLowerCase().replaceAll("(?m)^\\s*(--|#).*?$", " ");
    parseSql = parseSql.replace("\n", " ").replace("\t", " ");
    // 解析SQL
    //databaseType = LcdpDbUtil.getDatabaseDialect(databaseType, databaseType);
    SQLStatement sqlStatement = SQLUtils.parseSingleStatement(parseSql, databaseType);
    if (sqlStatement instanceof SQLSelectStatement) {
      SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
      SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlSelectStatement.getSelect().getQuery();
      // 检查判断是否符合处理条件
      if (checkAsteriskToken(query.getSelectList(), databaseType)) {
        // 提取 select 字段列表
        List<String> columnList = getTableColumnList(parseSql, databaseType, dataSource);
        String remainSql = parseSql.substring(parseSql.indexOf(" from "));
        return "select " + String.join(", ", columnList) + remainSql;
      }
    }
    return scriptSql;
  }

  /**
   * 检查是否符合 select * 的情况
   */
  private static boolean checkAsteriskToken(List<SQLSelectItem> itemList, String databaseType) {
    boolean checkoutResult = false;
    boolean containSubSelect = false;
    for (SQLSelectItem item : itemList) {
      String column = item.getExpr().toString();
      // 符合条件的情况
      if ("*".equals(column) || column.contains(".*")) {
        checkoutResult = true;
      }
      // 过滤查询字段包含子查询的情况
      try {
        SQLStatement sqlStatement = SQLUtils.parseSingleStatement(column, databaseType);
        if (sqlStatement instanceof SQLSelectStatement) {
          containSubSelect = true;
          break;
        }
      }
      catch (Exception e) {
        // 忽略异常
      }
    }
    return !containSubSelect && checkoutResult;
  }

  /**
   * 获取表的全部字段列表，包括临时表
   */
  private static List<String> getTableColumnList(String scriptSql, String databaseType, DataSource dataSource) {
    List<String> columnList = new ArrayList<>();
    SQLStatement sqlStatement = SQLUtils.parseSingleStatement(scriptSql, databaseType);
    if (sqlStatement instanceof SQLSelectStatement) {
      // 提取 select 字段列表
      SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
      SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlSelectStatement.getSelect().getQuery();
      List<String> selectList = convertSelectColumn(query.getSelectList());
      Map<String, List<String>> columnMap = null;
      // 解析并展开 * 字段，展开后的字段列表做为一个 list 元素
      for (String item : selectList) {
        if (!item.contains("*")) {
          columnList.add(item);
        }
        else {
          // 从 from 语句中提取 [表名-对应表字段列表] 的 map
          if (null == columnMap) {
            columnMap = buildColumnMap(query.getFrom().toString(), databaseType, dataSource);
          }
          String tableName = "";
          String prefix = "";
          if ("*".equals(item)) {
            tableName = "mainTable";
          }
          else if (item.contains(".*") && item.length() > 2) {
            // 处理 a.* 的情况，不处理只有 ".*" 这种不符合语法的情况
            tableName = item.substring(0, item.indexOf(".*"));
            prefix = item.substring(0, item.indexOf("*"));
          }
          // 提取字段列表，做空判断
          List<String> allColumnList = columnMap.get(tableName);
          if (allColumnList == null) {
            columnList.add(item);
          }
          else {
            allColumnList = expandColumn(columnMap.get(tableName), prefix);
            columnList.addAll(allColumnList);
          }
        }
      }
    }
    return columnList;
  }

  /**
   * 从 from 语句中构建 [表名-表对应字段列表] 的 Map
   * <pl>mainTable 表示主表，是为了处理裸 "*" 的情况</pl>
   */
  private static Map<String, List<String>> buildColumnMap(String fromSql, String databaseType, DataSource dataSource) {
    Map<String, List<String>> columnMap = new HashMap<>(8);
    // 处理多表连表查询的情况
    if (fromSql.toLowerCase().contains(" join ")) {
      // 切割多表查询语句
      List<String> joinList = splitJoinSql(fromSql);
      Map<String, List<String>> mainTable = new HashMap<>(8);
      // 解析 join 后面的表信息，可能也是嵌套查询、嵌套连表查询等，所以需要递归解析
      for (int i = 0; i < joinList.size(); i++) {
        String joinSql = joinList.get(i);
        Map<String, List<String>> map = buildColumnMap(joinSql, databaseType, dataSource);
        columnMap.putAll(map);
        if (i == 0) {
          // 紧跟 from 后面的第一个表属于主表
          mainTable.put("mainTable", map.get("mainTable"));
        }
      }
      columnMap.putAll(mainTable);
    }
    // 处理内嵌查询
    else if (fromSql.startsWith("(") && fromSql.contains(")")) {
      // 获取包含")"的表别名，然后再将其移除，通过这种方式避免数组越界
      String aliaTableName = fromSql.substring(fromSql.lastIndexOf(")")).trim();
      if (aliaTableName.length() > 1) {
        aliaTableName = aliaTableName.substring(1).trim();
        String selectSql = fromSql.substring(fromSql.indexOf("(") + 1, fromSql.lastIndexOf(")"));
        List<String> tableColumnList = getTableColumnList(selectSql, databaseType, dataSource);
        columnMap.put(aliaTableName, tableColumnList);
        columnMap.put("mainTable", tableColumnList);
      }
    }
    else {
      // 处理普通单表查询："[tableName, tableName alias, tableName as alias]"
      List<String> fromList = Arrays.stream(fromSql.split(" ")).collect(Collectors.toList());
      if (fromList.size() <= 3) {
        String tableName = fromList.get(0);
        Table table = DatabaseInspector.inspectTable(dataSource, tableName);
        if (table != null) {
          List<String> columns = table.getColumns().stream().map(Column::getName).collect(Collectors.toList());
          if (fromList.size() > 1) {
            // 有表别名就添加对应的别名
            columnMap.put(fromList.get(fromList.size() - 1), columns);
          }
          columnMap.put("mainTable", columns);
        }
      }
    }
    return columnMap;
  }

  /**
   * 将 * 展开成具体字段列表，支持前缀添加和替换，比如 a.*
   */
  private static List<String> expandColumn(List<String> columns, String prefix) {
    List<String> result = new ArrayList<>();
    for (String column : columns) {
      if (!column.contains(".")) {
        result.add(prefix + column);
      }
      else {
        result.add(prefix + column.substring(column.indexOf(".") + 1));
      }
    }
    return result;
  }

  /**
   * 将 select 的原生字段列表转换成字符串列表
   */
  private static List<String> convertSelectColumn(List<SQLSelectItem> itemList) {
    List<String> list = new ArrayList<>();
    for (SQLSelectItem item : itemList) {
      // 优先选用 alias 值，没有则使用目标字段值
      String alias = item.getAlias();
      if (StringUtils.isNotEmpty(alias)) {
        list.add(alias);
      }
      else {
        SQLExpr expr = item.getExpr();
        list.add(expr.toString());
      }
    }
    return list;
  }

  /**
   * 切割 join 多表查询与语句，移除 join 关键字
   */
  private static List<String> splitJoinSql(String scriptSql) {
    List<String> joinList = Arrays.stream(scriptSql.toLowerCase().split(" join ")).map(String::new)
      .collect(Collectors.toList());
    List<String> keyList = Arrays.asList(" left", " right", " inner", " outer", " full");
    List<String> resultList = new ArrayList<>();
    for (String sql : joinList) {
      String processedSql = sql;
      // 移除与 join 相关的关键字
      for (String key : keyList) {
        int keyIndex = processedSql.lastIndexOf(key);
        if (-1 != keyIndex && key.equals(processedSql.substring(keyIndex))) {
          processedSql = processedSql.substring(0, keyIndex);
        }
      }
      // 移除 on 条件表达式
      int index = processedSql.indexOf(" on ");
      if (-1 != index) {
        processedSql = processedSql.substring(0, index).trim();
      }
      resultList.add(processedSql);
    }
    return resultList;
  }

}
