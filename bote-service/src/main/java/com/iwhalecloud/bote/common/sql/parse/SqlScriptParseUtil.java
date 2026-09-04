package com.iwhalecloud.bote.common.sql.parse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.ParamUtil;
import com.iwhalecloud.bote.dto.skill.ParseSqlNodeResult;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.scripting.xmltags.WhereSqlNode;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * @author qian.sisheng
 * @since 2024/8/8
 */
public final class SqlScriptParseUtil {
  private SqlScriptParseUtil() {

  }

  private static final Logger logger = LoggerFactory.getLogger(SqlScriptParseUtil.class);

  private static final Configuration CONFIGURATION = new Configuration();

  /**
   * 解析 SQL
   *
   * @param sql SQL 语句，可以包含 MyBatis 标签
   * @param params 参数
   * @param databaseType 数据库类型
   * @return 解析结果，left 为 SQL 语句，right 为参数数组
   */
  public static Pair<String, Object[]> parse(String sql, Map<String, Object> params, String databaseType) {
    SqlSource sqlSource = getSqlSource(sql, databaseType);
    BoundSql boundSql = sqlSource.getBoundSql(params);
    return Pair.of(boundSql.getSql(), parseParameters(boundSql).toArray());
  }

  /**
   * 提取 SQL 引用的参数值列表
   */
  @SuppressWarnings("unchecked")
  public static List<Object> parseParameters(BoundSql boundSql) {
    List<Object> parameters = new ArrayList<>();
    Map<String, Object> paramMap = boundSql.getParameterObject() instanceof Map ? (Map<String, Object>) boundSql.getParameterObject() : null;
    for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
      Object value = null;
      if (paramMap != null) {
        value = extractParamValue(paramMap, parameterMapping.getProperty());
      }
      if (value == null) {
        value = boundSql.getAdditionalParameter(parameterMapping.getProperty());
        if (value == null) {
          Object parameter = boundSql.getAdditionalParameter("_parameter");
          if (parameter instanceof Map) {
            value = ((Map<String, Object>) parameter).get(parameterMapping.getProperty());
          }
        }
      }
      parameters.add(value);
    }
    return parameters;
  }

  /**
   * 提取参数值
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private static Object extractParamValue(Map<String, Object> paramMap, String paramName) {
    Object value = paramMap.get(paramName);
    if (value == null && paramName.contains(".")) {
      if (StringUtils.countMatches(paramName, '.') == 1) {
        String[] keys = StringUtils.split(paramName, '.');
        Object obj = paramMap.get(keys[0]);
        if (obj != null) {
          Map<String, Object> map = obj instanceof Map ? (Map<String, Object>) obj : JsonUtil.convert(obj, new TypeReference<Map<String, Object>>() {
          });
          value = MapUtils.getObject(map, keys[1]);
        }
      }
      else {
        value = ParamUtil.getNestedProperty(paramMap, paramName);
      }
    }
    return value;
  }

  public static SqlSource getSqlSource(String sql, String databaseType) {
    // 转义查询 SQL
    sql = QuerySqlParseUtil.escapeSql(sql, Collections.emptyMap(), databaseType);

    String xml = "<select>" + sql + "</select>";
    XPathParser parser = new XPathParser(xml);
    List<XNode> xNodes = parser.evalNodes("select|insert|update|delete");
    if (CollectionUtils.isEmpty(xNodes)) {
      throw BaseErrorConstant.GET_SQL_SOURCE_ERROR.toException();
    }
    if (xNodes.size() > 1) {
      throw BaseErrorConstant.SQL_COMPLEX_ERROR.toException();
    }
    LanguageDriver langDriver = CONFIGURATION.getLanguageDriver(null);
    XNode node = xNodes.get(0);
    return langDriver.createSqlSource(CONFIGURATION, node, null);
  }

  /**
   * 移除所有条件分支
   *
   * @param sql 原始 SQL
   * @param databaseType 数据库类型
   * @return 结果
   */
  public static ParseSqlNodeResult parseSqlNode(String sql, Map<String, String> sqlFragments, String databaseType) {
    ParseSqlNodeResult result = new ParseSqlNodeResult();
    StringBuilder sb = new StringBuilder();
    Set<String> inputParams = new HashSet<>();
    SqlSource sqlSource = getSqlSource(sql, databaseType);
    if (sqlSource instanceof DynamicSqlSource) {
      DynamicSqlSource dy = (DynamicSqlSource) sqlSource;
      try {
        Object object = FieldUtils.readDeclaredField(dy, "rootSqlNode", true);
        if (object instanceof MixedSqlNode) {
          getStaticText((MixedSqlNode) object, sb, inputParams);
        }
      }
      catch (IllegalAccessException e) {
        logger.error("Failed to parseSqlNode", e);
      }
      result.setResolvedSql(sb.toString());
    }
    else {
      parseOrderByNodeParam(sql, inputParams);
      result.setResolvedSql(QuerySqlParseUtil.escapeSqlFragment(sql, sqlFragments, databaseType));
    }
    result.setParamNames(inputParams.isEmpty() ? Collections.emptyList() : new ArrayList<>(inputParams));
    return result;
  }

  private static void getStaticText(MixedSqlNode mixedSqlNode, StringBuilder sb, Set<String> inputParams) {
    try {
      Object object = FieldUtils.readDeclaredField(mixedSqlNode, "contents", true);
      if (object instanceof List) {
        for (Object node : (List<?>) object) {
          if (node instanceof WhereSqlNode) {
            parseWhereSqlNode(node, sb, inputParams);
          }
          else if (node instanceof StaticTextSqlNode) {
            parseStaticTextSqlNode(node, sb, inputParams);
          }
          else if (node instanceof ForEachSqlNode) {
            parseForEachSqlNode(node, sb);
          }
          else if (node instanceof ChooseSqlNode) {
            parseChooseSqlNode(node, sb, inputParams);
          }
          else if (node instanceof IfSqlNode) {
            parseIfSqlNode(node, sb, inputParams);
          }
        }
      }
    }
    catch (Exception e) {
      logger.error("Failed to getStaticText", e);
    }
  }

  private static void parseWhereSqlNode(Object node, StringBuilder sb, Set<String> inputParams) {
    try {
      Object object = FieldUtils.readField(node, "contents", true);
      if (object instanceof MixedSqlNode) {
        sb.append(" where ");
        getStaticText((MixedSqlNode) object, sb, inputParams);
      }
    }
    catch (Exception e) {
      logger.error("Failed to parse WhereSqlNode", e);
    }
  }

  private static void parseStaticTextSqlNode(Object node, StringBuilder sb, Set<String> inputParams) {
    try {
      String text = FieldUtils.readDeclaredField(node, "text", true).toString();
      if (org.springframework.util.StringUtils.trimAllWhitespace(text).toLowerCase().startsWith("orderby")) {
        // 排序片段可能存在动态参数
        parseOrderByNodeParam(text, inputParams);
      }
      else {
        sb.append(text);
      }
    }
    catch (Exception e) {
      logger.error("Failed to parse StaticTextSqlNode", e);
    }
  }

  private static void parseForEachSqlNode(Object node, StringBuilder sb) {
    try {
      String expression = FieldUtils.readDeclaredField(node, "collectionExpression", true).toString();
      expression = expression.contains(".") ? expression.substring(0, expression.indexOf(".")) : expression;
      sb.append("#{").append(expression).append("}");
    }
    catch (Exception e) {
      logger.error("Failed to parse ForEachSqlNode", e);
    }
  }

  private static void parseChooseSqlNode(Object node, StringBuilder sb, Set<String> inputParams) {
    try {
      // 解析 otherwise 分支条件
      Object object = FieldUtils.readDeclaredField(node, "defaultSqlNode", true);
      if (object instanceof MixedSqlNode) {
        getStaticText((MixedSqlNode) object, sb, inputParams);
      }
      // 解析 when 分支条件
      object = FieldUtils.readDeclaredField(node, "ifSqlNodes", true);
      if (object instanceof List) {
        for (Object obj : (List<?>) object) {
          parseTestNodeParam(FieldUtils.readDeclaredField(obj, "test", true), inputParams);
        }
      }
    }
    catch (Exception e) {
      logger.error("Failed to parse ChooseSqlNode", e);
    }
  }

  private static void parseIfSqlNode(Object node, StringBuilder sb, Set<String> inputParams) {
    try {
      Object object = FieldUtils.readDeclaredField(node, "contents", true);
      if (object instanceof MixedSqlNode) {
        getStaticText((MixedSqlNode) object, sb, inputParams);
      }
      // 解析if test中的参数
      parseTestNodeParam(FieldUtils.readDeclaredField(node, "test", true), inputParams);
    }
    catch (Exception e) {
      logger.error("Failed to parse IfSqlNode", e);
    }
  }

  /**
   * 解析 if test 中的参数
   *
   * @param obj test节点字符串
   * @param inputParams 入参
   */
  private static void parseTestNodeParam(Object obj, Set<String> inputParams) {
    if (inputParams == null || ObjectUtils.isEmpty(obj)) {
      return;
    }
    String test = (String) obj;
    // 替换字符串中所有括号、分割逻辑运算符和关系运算符
    String[] fields = test.replaceAll("[)]|[(]", " ").split("\\b(not|or|and)\\b|!=|==|<|<=|>|>=");
    // 获取字段名称
    for (String field : fields) {
      String param = field.trim();
      // 排除数字、null、字符串、函数
      if (StringUtils.isNumericSpace(param) || "null".equals(param) || param.startsWith("'") || param.contains(".")) {
        continue;
      }
      // 异常字段
      if (param.contains(" ")) {
        continue;
      }
      // 去除 !号 支持boolean类型
      inputParams.add(param.replaceAll("!", ""));
    }
  }

  private static void parseOrderByNodeParam(String text, Set<String> inputParams) {
    if (text.indexOf("{") < 1) {
      return;
    }
    String[] fields = org.springframework.util.StringUtils.trimAllWhitespace(text).split("[{}]");
    for (int i = 0; i < fields.length; i++) {
      if (i % 2 != 0) {
        inputParams.add(fields[i]);
      }
    }
  }
}
