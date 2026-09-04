package com.iwhalecloud.bote.common.sql.parse;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.DcPublicUtil;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 查询 SQL 解析辅助类
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
public final class QuerySqlParseUtil {
  private QuerySqlParseUtil() {
  }

  /**
   * 转义查询 SQL
   * <p>1. 界面配置的查询 SQL，按照 mybatis 风格处理，存在一系列个性化述求，统一在此转义 SQL </p>
   *
   * @param sql 原始查询 SQL
   * @param databaseType 数据库类型
   * @return 转义后 SQL
   */
  public static String escapeSql(String sql, Map<String, String> sqlFragments, String databaseType) {
    // 第一步，转义公共、引用 SQL 片段
    sql = escapeSqlFragment(sql, sqlFragments, databaseType);

    // 第二步，查询条件中的 < 符号需要转义，处理前，需要先把 mybatis 特殊标签中的 < 符号例外
    String[] labels = SystemParameter.SQL_PARSE_LABEL.getValueFromEnv().split(",");
    for (String label : labels) {
      sql = sql.replace("<" + label, ":" + label);
      sql = sql.replace("</" + label, ":/" + label);
    }
    sql = sql.replace("<", "&lt;");
    for (String label : labels) {
      sql = sql.replace(":" + label, "<" + label);
      sql = sql.replace(":/" + label, "</" + label);
    }

    // 第三步，mysql 数据库，移除字段的 ` 符号
    sql = sql.replace("`", "");

    // 第四步，<if> <when> 标签中的常量等值表达式，需要统一补充 .toString()，避免解析异常或者运行时表达式计算偏差
    sql = escapeTestCondition(sql, "<if");
    sql = escapeTestCondition(sql, "<when");

    // 最后，移除末尾的 ; 符号，避免报错
    sql = StringUtils.stripEnd(sql.trim(), ";");

    return sql;
  }

  /**
   * 转义特殊符号，用于 SQL like 查询
   * <p>1. 避免输入 \ 等符号导致 SQL查询异常 </p>
   * <p>2. 避免输入 % _ 等符号出现查询结果不正确 </p>
   * <p>3. 避免 SQL 注入 </p>
   *
   * @param content like 查询入参
   * @return 转义后入参
   */
  public static String escapeSqlForLike(String content) {
    if (StringUtils.isEmpty(content)) {
      return content;
    }
    List<String> keywords = Arrays.asList("(", "{", "\\", "^", "$", "*", "}", ")", "?", "+", ".", "|", "%", "_");
    for (String key : keywords) {
      if (keywords.contains(key)) {
        content = content.replace(key, "\\" + key);
      }
    }
    return content;
  }

  /**
   * 转义 SQL 中的公共片段，方便适配不同数据库
   * <p>1. 公共片段维护在静态词典 SQL_FRAGMENT </p>
   * <p>2. 示例 SQL 片段，<include refid="SqlFragments.func_now"/> </p>
   * <p>3. default 默认数据库类型为 postgresql </p>
   * <p>4. SQL 中的引用片段, 示例 <include refid="userColumn"/> </p>
   *
   * @param sql 查询 SQL * @param sqlFragments SQL 片段
   * @param databaseType 数据库类型
   * @return 转义后 SQL
   */
  public static String escapeSqlFragment(String sql, Map<String, String> sqlFragments, String databaseType) {
    String type = databaseType.toLowerCase();
    Map<String, List<DcPublicEntity>> group = CollectionUtils.emptyIfNull(DcPublicUtil.getList(DcPublicUtil.SQL_FRAGMENT)).stream()
      .collect(Collectors.groupingBy(DcPublicEntity::getPcode));
    List<DcPublicEntity> list = new ArrayList<>();
    for (Entry<String, List<DcPublicEntity>> entry : group.entrySet()) {
      DcPublicEntity dto = IterableUtils.find(CollectionUtils.emptyIfNull(entry.getValue()), p -> type.equalsIgnoreCase(p.getCodea()));
      if (dto != null) {
        list.add(dto);
      }
      else {
        // 没有特殊声明，采用默认实现
        dto = IterableUtils.find(CollectionUtils.emptyIfNull(entry.getValue()), p -> "default".equalsIgnoreCase(p.getCodea()));
        if (dto != null) {
          list.add(dto);
        }
      }
    }
    // 处理公共 SQL 片段
    String sqlFragment = "<include refid=\"SqlFragments.%s\"/>";
    for (DcPublicEntity dcPublic : list) {
      // oracle 版本 codeb 定义为大字段，空值时，replace API 使用会抛出异常
      sql = sql.replace(String.format(sqlFragment, dcPublic.getPcode()), StringUtils.isEmpty(dcPublic.getCodeb()) ? "" : dcPublic.getCodeb());
    }

    // 处理引用的 SQL 片段
    if (!sql.contains("<include")) {
      return sql;
    }
    sqlFragment = "<include refid=\"%s\"/>";
    for (Entry<String, String> fragment : MapUtils.emptyIfNull(sqlFragments).entrySet()) {
      sql = sql.replace(String.format(sqlFragment, fragment.getKey()), fragment.getValue());
    }
    return sql;
  }

  /**
   * 转义 SQL 中的 test 条件表达式
   * <p>1. if, when 标签中的常量等值表达式，需要统一补充 .toString()，避免解析异常或者运行时表达式计算偏差</p>
   * <p>2. 标准示例：&lt;if test="flag != null and flag == '1'.toString()"&gt; </p>
   *
   * @param sql 查询 SQL
   * @return 转义后 SQL
   */
  private static String escapeTestCondition(String sql, String label) {
    StringBuilder str = new StringBuilder();
    if (sql.contains(label)) {
      String[] fragments = sql.split(label);
      for (int i = 0; i < fragments.length; i++) {
        String fragment = fragments[i];
        if (i == 0) {
          str.append(fragment);
          continue;
        }
        // 提取 test 表达式
        String testStr = fragment.substring(fragment.indexOf("\"") + 1);
        testStr = testStr.substring(0, testStr.indexOf("\""));

        fragment = fragment.replace(testStr, parseTestCondition(testStr, "=="));
        fragment = fragment.replace(testStr, parseTestCondition(testStr, "!="));
        str.append(label).append(fragment);
      }
    }
    return StringUtils.isEmpty(str.toString()) ? sql : str.toString();
  }

  /**
   * 处理 test 表达式中的 == 和 != 两边值
   */
  private static String parseTestCondition(String testStr, String sign) {
    if (!testStr.contains(sign)) {
      return testStr;
    }
    StringBuilder fragment = new StringBuilder();
    String[] condition = testStr.trim().split("\\s*" + sign + "\\s*");
    for (int i = 0; i < condition.length; i++) {
      // 条件因子
      String factor;
      // 一个 test 语句，可能存在多个 == 或 !=
      if (i == 0) {
        // 首个片段，字符串最后一个空格后边的值，属于因子
        int lastIndex = condition[i].lastIndexOf(" ");
        if (-1 != lastIndex) {
          factor = condition[i].substring(lastIndex).trim();
          fragment.append(condition[i], 0, lastIndex).append(" ").append(fillToStringForFactor(factor));
        }
        else {
          fragment.append(fillToStringForFactor(condition[i]));
        }
      }
      else if (i == condition.length - 1) {
        // 最后一个片段，字符串首个空格前边的值，属于因子
        fragment.append(" ").append(sign).append(" ");
        if (!condition[i].contains(" ")) {
          factor = condition[i];
          fragment.append(fillToStringForFactor(factor));
        }
        else {
          factor = condition[i].substring(0, condition[i].indexOf(" ")).trim();
          fragment.append(fillToStringForFactor(factor));
          fragment.append(condition[i].substring(condition[i].indexOf(" ")));
        }
      }
      else {
        // 中间片段，字符串的两边都属于因子
        // 左边因子
        fragment.append(" ").append(sign).append(" ");
        factor = condition[i].substring(0, condition[i].indexOf(" ")).trim();
        fragment.append(fillToStringForFactor(factor));

        fragment.append(condition[i], condition[i].indexOf(" "), condition[i].lastIndexOf(" "));

        // 右边因子
        factor = condition[i].substring(condition[i].lastIndexOf(" ")).trim();
        fragment.append(" ").append(fillToStringForFactor(factor));
      }
    }
    return fragment.toString();
  }

  /**
   * test 表达式中的 == 或 != 的常量因子，追加 .toString()
   */
  private static String fillToStringForFactor(String factor) {
    String template = "'%s'.toString()";
    if (StringUtils.isNumeric(factor)) {
      // 纯数字因子，需要调整为 'XXX'.toString()
      return String.format(template, factor);
    }
    else if (factor.contains("'") && !factor.contains(".toString()") && !factor.endsWith(")")) {
      // 字符串类型的常量因子，且不带 .toString()，并且因子末尾不包含空格
      return factor + ".toString()";
    }
    return factor;
  }

  /**
   * 解析 SQL 片段中的 refid 属性值
   *
   * @param sql 查询 SQL
   * @return refid 属性值
   */
  public static List<String> parseIncludeRefid(String sql) {
    if (!sql.contains("<include")) {
      return Collections.emptyList();
    }
    String[] fragments = sql.split("<include");
    List<String> refids = new ArrayList<>();
    for (int i = 0; i < fragments.length; i++) {
      String fragment = fragments[i];
      if (i == 0) {
        continue;
      }
      // 提取 refid 属性值
      String refid = fragment.substring(fragment.indexOf("\"") + 1);
      refid = refid.substring(0, refid.indexOf("\""));
      refids.add(refid.trim());
    }
    return refids;
  }

}
