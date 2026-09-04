/*
 * Copyright (c) 2011-2024, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iwhalecloud.bote.doc.common.mybatis.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Oracle INSERT ALL 语法处理器 支持自动注入租户ID字段到 Oracle 的 INSERT ALL 批量插入语句中
 *
 * @author Aiqing
 * @since 2025-01-16
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class OracleInsertAllHandler {

  private static final Logger logger = LoggerFactory.getLogger(OracleInsertAllHandler.class);

  private OracleInsertAllHandler() {

  }
  /**
   * Oracle INSERT ALL 语法检测模式
   */
  private static final Pattern ORACLE_INSERT_ALL_PATTERN = Pattern
    .compile("(?i)INSERT\\s+ALL\\s+.*?INTO.*?SELECT.*?FROM\\s+dual", Pattern.DOTALL);

  /**
   * 匹配单个 INTO 子句的模式 捕获组：1-表名, 2-字段列表, 3-值列表
   */
  private static final Pattern INTO_CLAUSE_PATTERN = Pattern
    .compile("(?i)INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)", Pattern.DOTALL);

  /**
   * 检测SQL是否为Oracle INSERT ALL语法
   *
   * @param sql SQL语句
   * @return true-是INSERT ALL语法
   */
  public static boolean isOracleInsertAll(String sql) {
    if (sql == null || sql.trim().isEmpty()) {
      return false;
    }
    return ORACLE_INSERT_ALL_PATTERN.matcher(sql).find();
  }

  /**
   * 为 Oracle INSERT ALL 语句注入租户ID
   *
   * @param sql SQL语句
   * @param tenantIdColumn 租户ID字段名
   * @param tenantIdValue 租户ID值（通常是占位符，如 "0" 或 "#{tenantId}"）
   * @return 注入租户ID后的SQL
   */
  public static String injectTenantId(String sql, String tenantIdColumn, String tenantIdValue) {
    if (!isOracleInsertAll(sql)) {
      return sql;
    }

    try {
      StringBuilder result = new StringBuilder();
      Matcher matcher = INTO_CLAUSE_PATTERN.matcher(sql);
      int lastEnd = 0;
      boolean modified = false;

      // 处理INSERT ALL之前的部分
      int insertAllPos = sql.toUpperCase().indexOf("INSERT ALL");
      if (insertAllPos >= 0) {
        result.append(sql, 0, insertAllPos + 10); // "INSERT ALL"
        lastEnd = insertAllPos + 10;
      }

      // 处理每个 INTO 子句
      while (matcher.find()) {
        // 添加 INTO 之前的内容（空白字符等）
        result.append(sql, lastEnd, matcher.start());

        String tableName = matcher.group(1);
        String columns = matcher.group(2);
        String values = matcher.group(3);

        // 检查是否已包含租户ID字段
        if (!containsTenantIdColumn(columns, tenantIdColumn)) {
          // 注入租户ID字段和值
          String newColumns = columns.trim() + ",\n      " + tenantIdColumn;
          String newValues = values.trim() + ",\n      " + tenantIdValue;

          result.append("INTO ").append(tableName).append(" (\n      ").append(newColumns)
            .append("\n      ) VALUES (\n      ").append(newValues).append("\n      )");

          modified = true;
          logger.debug("Injected tenant_id into Oracle INSERT ALL for table: {}", tableName);
        }
        else {
          // 已包含租户ID，保持原样
          result.append(matcher.group(0));
        }

        lastEnd = matcher.end();
      }

      // 添加剩余部分（SELECT ... FROM dual）
      result.append(sql.substring(lastEnd));

      if (modified) {
        String finalSql = result.toString();
        logger.debug("Oracle INSERT ALL SQL after tenant injection:\n{}", finalSql);
        return finalSql;
      }

      return sql;
    }
    catch (Exception e) {
      logger.warn("Failed to inject tenant_id into Oracle INSERT ALL, return original SQL. Error: {}", e.getMessage());
      return sql;
    }
  }

  /**
   * 检查字段列表中是否已包含租户ID字段
   *
   * @param columns 字段列表
   * @param tenantIdColumn 租户ID字段名
   * @return true-已包含
   */
  private static boolean containsTenantIdColumn(String columns, String tenantIdColumn) {
    if (columns == null || tenantIdColumn == null) {
      return false;
    }
    // 分割字段列表，检查是否包含租户ID字段
    String[] columnArray = columns.split(",");
    for (String column : columnArray) {
      if (column.trim().equalsIgnoreCase(tenantIdColumn)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 从SQL中提取租户ID值的占位符 根据实际的参数绑定情况，可能是固定值或占位符
   *
   * @param sql 原始SQL
   * @return 租户ID值
   */
  public static String extractTenantIdPlaceholder(String sql) {
    // 尝试从现有的字段值中推断占位符格式
    // 如果使用 ? 占位符，返回 ?
    // 如果使用 #{} 占位符，返回 #{dto.tenantId}
    if (sql.contains("?")) {
      return "?";
    }
    else if (sql.contains("#{")) {
      // 尝试从现有的参数中推断租户ID的参数格式
      Pattern paramPattern = Pattern.compile("#\\{(\\w+\\.)?(\\w+)\\}");
      Matcher matcher = paramPattern.matcher(sql);
      if (matcher.find()) {
        String prefix = matcher.group(1);
        if (prefix != null) {
          return "#{" + prefix + "tenantId}";
        }
      }
      return "#{tenantId}";
    }
    // 默认使用 ? 占位符
    return "?";
  }
}
