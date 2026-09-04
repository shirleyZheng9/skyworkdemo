package com.iwhalecloud.bote.service.skill.impl.helper;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Column;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SqlTypeEnum;
import com.iwhalecloud.bote.common.sql.parse.DruidSqlParseUtil;
import com.iwhalecloud.bote.common.sql.parse.SqlScriptParseUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.skill.ParseSqlNodeResult;
import com.iwhalecloud.bote.dto.skill.SqlOperaParams;
import com.iwhalecloud.bote.dto.skill.ParseSqlResult;
import com.iwhalecloud.bote.dto.skill.SqlScriptGenerateInfo;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * SQL 解析 - 辅助工具
 *
 * @author chen.linfa
 * @since 2024-09-21
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ParseSqlHelper {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** 匹配 select * 正则表达式 */
  private final Pattern SELECT_ASTERISK_PATTERN = Pattern.compile("^\\s*select.*\\*.*from", Pattern.DOTALL);

  private final IDataSourceProviderService dataSourceProviderService;

  public ParseSqlResult parseSql(SqlOperaParams sqlOperaParams) {
    // 数据库类型
    String databaseType = dataSourceProviderService.getDataSourceDialect(sqlOperaParams.getTenantId(), sqlOperaParams.getDataSourceId());
    // 校验 SQL 的合法性，并移除动态节点语句
    ParseSqlNodeResult parseSqlNodeResult;
    try {
      // $ 符号转换为 #
      String sql = sqlOperaParams.getSql().replace("$", "#");
      sql = sql.replace("`", "");
      // 结尾追加换行符，修复问题：SQL 最后一行是注释代码时，出现解析异常，分页查询异常
      sql = sql + "\n";
      sqlOperaParams.setSql(sql);
      // 解析 SQL 片段
      parseSqlNodeResult = SqlScriptParseUtil.parseSqlNode(sqlOperaParams.getSql(), Collections.emptyMap(), databaseType);
    }
    catch (BssException e) {
      logger.error("Failed to parse mybatis sql. sql={}", sqlOperaParams.getSql(), e);
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to parse mybatis sql. sql={}", sqlOperaParams.getSql(), e);
      throw BaseErrorConstant.PARSE_SQL_FAILED.toException(e);
    }

    ParseSqlResult result = new ParseSqlResult();
    // 根据 SQL 获取出入参列表
    extractParam(sqlOperaParams, parseSqlNodeResult, databaseType, result);
    return result;
  }
  /**
   * 根据 SQL 获取出入参列表
   *
   * @param sqlOperaParams sql解析配置参数
   * @param parseSqlNodeResult 解析参数（sql、if节点中的入参集合）
   */
  private void extractParam(SqlOperaParams sqlOperaParams, ParseSqlNodeResult parseSqlNodeResult, String databaseType, ParseSqlResult result) {
    List<ParameterSpec> inputParams = new ArrayList<>();
    List<ParameterSpec> outputParams = new ArrayList<>();

    DataSource dataSource = dataSourceProviderService.getDataSource(sqlOperaParams.getTenantId(), sqlOperaParams.getDataSourceId());
    try {
      SqlScriptGenerateInfo sqlScriptInfo = DruidSqlParseUtil.buildSqlScriptGenerateInfo(parseSqlNodeResult.getResolvedSql(), databaseType);
      Assert.notNull(sqlScriptInfo, "");
      // 组装 sql 解析参数
      extraKeys(sqlScriptInfo.getInputItemList(), inputParams, true);
      // 添加if test节点中的入参
      appendTestNodeParams(inputParams, parseSqlNodeResult);
      inputParams = distinct(inputParams);
      // 根据是否为 select * 的情况处理出参列表
      String resolvedSql = parseSqlNodeResult.getResolvedSql();
      if (SELECT_ASTERISK_PATTERN.matcher(resolvedSql.toLowerCase()).find()) {
        List<Map<String, String>> itemList = DruidSqlParseUtil.builtSelectItemList(resolvedSql, databaseType, dataSource);
        if (!itemList.isEmpty()) {
          extraKeys(itemList, outputParams, false);
        }
        else {
          extraKeys(sqlScriptInfo.getSelectItemList(), outputParams, false);
        }
      }
      else {
        extraKeys(sqlScriptInfo.getSelectItemList(), outputParams, false);
      }
      // 获取数据库字段类型
      getParameterTypeFromDb(dataSource, sqlScriptInfo, inputParams, outputParams);

      // 转 json 设置出入参
      buildParams(sqlOperaParams, result, inputParams, outputParams);
      //设置使用的表信息
      result.setLowTableNameSet(sqlScriptInfo.getLowerTableNameSet());
    }
    catch (BssException e) {
      logger.error("Failed to extract sql params. sql={}", sqlOperaParams.getSql(), e);
      throw e;
    }
    catch (Exception e) {
      logger.error("Failed to extract sql params. sql={}", sqlOperaParams.getSql(), e);
      throw BaseErrorConstant.PARSE_SQL_FAILED.toException(e);
    }
  }

  private void buildParams(SqlOperaParams sqlOperaParams, ParseSqlResult result, List<ParameterSpec> inputParams, List<ParameterSpec> outputParams) {
    if (BaseConsts.SQL_RESULT_TYPE_VALUE.equals(sqlOperaParams.getScriptResultType())) {
      result.setResponseObj(JsonUtil.toJsonString(outputParams));
    }
    else if (BaseConsts.SQL_RESULT_TYPE_LIST.equals(sqlOperaParams.getScriptResultType())) {
      ParameterSpec parameterSpecObject = ParameterSpec.newObject("object", "对象", outputParams);
      result.setResponseObj(JsonUtil.toJsonString(ParameterSpec.newList("list", "数组", parameterSpecObject)));
    }
    else if (BaseConsts.SQL_RESULT_TYPE_PAGE.equals(sqlOperaParams.getScriptResultType())) {
      List<ParameterSpec> children = new ArrayList<>();
      ParameterSpec pageNum = ParameterSpec.newProperty("pageNum", "页码", AttrDataType.INTEGER);
      ParameterSpec pageSize = ParameterSpec.newProperty("pageSize", "每页数量", AttrDataType.INTEGER);
      children.add(ParameterSpec.newProperty("total", "总数量", AttrDataType.INTEGER));
      children.add(pageNum);
      children.add(pageSize);
      children.add(ParameterSpec.newProperty("pages", "当前页数", AttrDataType.INTEGER));
      children.add(ParameterSpec.newList("list", "列表", ParameterSpec.newObject("object", "对象", outputParams)));
      result.setResponseObj(JsonUtil.toJsonString(ParameterSpec.newRoot(children)));
      inputParams.add(pageNum);
      inputParams.add(pageSize);
    }
    else {
      result.setResponseObj(JsonUtil.toJsonString(ParameterSpec.newRoot(outputParams)));
    }
    result.setRequestObj(JsonUtil.toJsonString(ParameterSpec.newRoot(inputParams)));
  }

  private List<ParameterSpec> distinct(List<ParameterSpec> inputParams) {
    List<ParameterSpec> specs = new ArrayList<>();
    List<String> exists = new ArrayList<>();
    for (ParameterSpec spec : inputParams) {
      if (exists.contains(spec.getName())) {
        continue;
      }
      specs.add(spec);
      exists.add(spec.getName());
    }
    return specs;
  }

  private void extraKeys(List<Map<String, String>> itemList, List<ParameterSpec> parameterSpecs, boolean isInput) {
    itemList.forEach(itemMap -> {
      String columnName = MapUtils.getString(itemMap, "columnName");
      String alias = MapUtils.getString(itemMap, "alias", columnName);
      String paramName = MapUtils.getString(itemMap, "paramName");
      String isList = MapUtils.getString(itemMap, "isList");
      // @formatter:off
      ParameterSpec parameterSpec;
      if (isInput) {
        if (BaseConsts.TRUE.equals(isList)) {
          parameterSpec = ParameterSpec.newList(paramName, paramName, ParameterSpec.newProperty(columnName, columnName, null));
        }
        else {
          parameterSpec = ParameterSpec.newProperty(paramName, paramName, null);
        }
      }
      else {
        parameterSpec = ParameterSpec.newProperty(alias, paramName, null);
      }
      // @formatter:on
      parameterSpecs.add(parameterSpec);
    });

  }

  private void appendTestNodeParams(List<ParameterSpec> inputParams, ParseSqlNodeResult parseSqlNodeResult) {
    if (CollectionUtils.isEmpty(parseSqlNodeResult.getParamNames())) {
      return;
    }
    // 添加if节点中的入参
    for (String code : parseSqlNodeResult.getParamNames()) {
      inputParams.add(ParameterSpec.newProperty(code, code, AttrDataType.STRING));
    }
  }

  /**
   * 获取数据库字段类型
   */
  private void getParameterTypeFromDb(DataSource dataSource, SqlScriptGenerateInfo sqlScriptInfo, List<ParameterSpec> inputParams,
    List<ParameterSpec> outputParams) {
    try {
      for (String rawTableName : sqlScriptInfo.getLowerTableNameSet()) {
        int dotIndex = rawTableName.lastIndexOf(".");
        String schema = "";
        String tableName = rawTableName;
        if (dotIndex != -1) {
          // 截取 schema
          schema = rawTableName.substring(0, dotIndex);
          tableName = rawTableName.substring(dotIndex + 1);
        }
        Table table = DatabaseInspector.inspectTable(dataSource, schema, tableName);
        Assert.notNull(table, "表不存在");
        detectColumnInfo(table, inputParams);
        detectColumnInfo(table, outputParams);
      }
    }
    catch (Exception e) {
      // 探测失败，不往上抛出异常，避免影响主流程
      logger.error("Failed to inspect table. table={}", sqlScriptInfo.getLowerTableNameSet(), e);
    }
    // 如果探测失败，默认设置字段为字符串类型
    inputParams.stream().filter(p -> p.getType() == null).forEach(p -> p.setType(AttrDataType.STRING));
    outputParams.stream().filter(p -> p.getType() == null).forEach(p -> p.setType(AttrDataType.STRING));
  }

  /**
   * 设置字段类型、中文描述
   *
   * @param table 表结构信息
   * @param parameterSpecs 参数节点
   */
  private void detectColumnInfo(Table table, List<ParameterSpec> parameterSpecs) {
    for (ParameterSpec parameterSpec : parameterSpecs) {
      List<ParameterSpec> children = parameterSpec.getChildren();
      if (CollectionUtils.isNotEmpty(children)) {
        detectColumnInfo(table, children);
      }
      Column column = table.getColumn(parameterSpec.getName());
      if (column == null) {
        continue;
      }
      String sqlTypeName = JdbcUtils.resolveTypeName(column.getDataType());
      parameterSpec.setType(SqlTypeEnum.getAttrDataTypeByTypeName(sqlTypeName));
      if (StringUtils.isNotEmpty(column.getRemarks())) {
        parameterSpec.setDescription(column.getRemarks());
      }
    }
  }
}
