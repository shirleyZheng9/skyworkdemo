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

import com.iwhalecloud.bote.doc.common.mybatis.toolkit.ExceptionUtils;
import com.iwhalecloud.bote.doc.common.mybatis.toolkit.StringPool;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://github.com/JSQLParser/JSqlParser
 *
 * @author miemie
 * @since 2020-06-22
 */
public abstract class JsqlParserSupport {

  protected final Logger logger = LoggerFactory.getLogger(JsqlParserSupport.class);

  /**
   * 检测SQL是否为JSQLParser不支持的特殊语法
   * 注意：Oracle INSERT ALL 已经被专门处理，不在此列
   *
   * @param sql SQL语句
   * @return true-不支持的语法，false-支持的语法
   */
  protected boolean isUnsupportedSyntax(String sql) {
    if (sql == null || sql.trim().isEmpty()) {
      return false;
    }
    // 这里可以添加其他不支持的数据库特定语法的检测
    // Oracle INSERT ALL 已经在 processOracleInsertAll 中处理，不需要在这里检测
    return false;
  }

  @SuppressWarnings("PMD.PreserveStackTrace")
  public String parserSingle(String sql, Object obj) {
    if (logger.isDebugEnabled()) {
      logger.debug("original SQL: " + sql);
    }

    // 检测是否为不支持的特殊语法
    if (isUnsupportedSyntax(sql)) {
      logger.warn("Detected unsupported SQL syntax (e.g., Oracle INSERT ALL), skip parsing and return original SQL");
      return sql;
    }

    try {
      Statement statement = JsqlParserGlobal.parse(sql);
      return processParser(statement, 0, sql, obj);
    }
    catch (JSQLParserException e) {
      throw ExceptionUtils.mpe("Failed to process, Error SQL: %s", e.getCause(), sql);
    }
  }

  @SuppressWarnings({"PMD.PreserveStackTrace", "PMD.LooseCoupling"})
  public String parserMulti(String sql, Object obj) {
    if (logger.isDebugEnabled()) {
      logger.debug("original SQL: " + sql);
    }

    // 检测是否为 Oracle INSERT ALL 语法，如果是则使用专门的处理器
    if (OracleInsertAllHandler.isOracleInsertAll(sql)) {
      return processOracleInsertAll(sql, obj);
    }

    // 检测是否为其他不支持的特殊语法
    if (isUnsupportedSyntax(sql)) {
      logger.warn("Detected unsupported SQL syntax, skip parsing and return original SQL");
      return sql;
    }

    try {
      // fixed github pull/295
      StringBuilder sb = new StringBuilder();
      Statements statements = JsqlParserGlobal.parseStatements(sql);
      int i = 0;
      for (Statement statement : statements) { //NOPMD - suppressed CloseResource - 不需要关闭
        if (i > 0) {
          sb.append(StringPool.SEMICOLON);
        }
        sb.append(processParser(statement, i, sql, obj));
        i++;
      }
      return sb.toString();
    }
    catch (JSQLParserException e) {
      throw ExceptionUtils.mpe("Failed to process, Error SQL: %s", e.getCause(), sql);
    }
  }

  /**
   * 处理 Oracle INSERT ALL 语法
   * 子类可以重写此方法来实现自定义的处理逻辑（如注入租户ID）
   *
   * @param sql SQL语句
   * @param obj 参数对象
   * @return 处理后的SQL
   */
  protected String processOracleInsertAll(String sql, Object obj) {
    // 默认实现：直接返回原始SQL
    // 子类（如 TenantLineInnerInterceptor）可以重写此方法来注入租户ID
    logger.debug("Detected Oracle INSERT ALL syntax, use default handler (return original SQL)");
    return sql;
  }

  /**
   * 执行 SQL 解析
   *
   * @param statement JsqlParser Statement
   * @return sql
   */
  protected String processParser(Statement statement, int index, String sql, Object obj) {
    if (logger.isDebugEnabled()) {
      logger.debug("SQL to parse, SQL: " + sql);
    }
    if (statement instanceof Insert) {
      this.processInsert((Insert) statement, index, sql, obj);
    }
    else if (statement instanceof Select) {
      this.processSelect((Select) statement, index, sql, obj);
    }
    else if (statement instanceof Update) {
      this.processUpdate((Update) statement, index, sql, obj);
    }
    else if (statement instanceof Delete) {
      this.processDelete((Delete) statement, index, sql, obj);
    }
    sql = statement.toString();
    if (logger.isDebugEnabled()) {
      logger.debug("parse the finished SQL: " + sql);
    }
    return sql;
  }

  /**
   * 新增
   */
  protected void processInsert(Insert insert, int index, String sql, Object obj) {
    throw new UnsupportedOperationException();
  }

  /**
   * 删除
   */
  protected void processDelete(Delete delete, int index, String sql, Object obj) {
    throw new UnsupportedOperationException();
  }

  /**
   * 更新
   */
  protected void processUpdate(Update update, int index, String sql, Object obj) {
    throw new UnsupportedOperationException();
  }

  /**
   * 查询
   */
  protected void processSelect(Select select, int index, String sql, Object obj) {
    throw new UnsupportedOperationException();
  }
}
