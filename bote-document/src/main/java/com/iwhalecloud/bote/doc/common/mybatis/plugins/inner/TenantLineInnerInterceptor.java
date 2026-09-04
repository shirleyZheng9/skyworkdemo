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
package com.iwhalecloud.bote.doc.common.mybatis.plugins.inner;

import com.iwhalecloud.bote.doc.common.mybatis.parser.OracleInsertAllHandler;
import com.iwhalecloud.bote.doc.common.mybatis.plugins.handler.TenantLineHandler;
import com.iwhalecloud.bote.doc.common.mybatis.plugins.helper.InterceptorIgnoreHelper;
import com.iwhalecloud.bote.doc.common.mybatis.toolkit.ClassUtils;
import com.iwhalecloud.bote.doc.common.mybatis.toolkit.ExceptionUtils;
import com.iwhalecloud.bote.doc.common.mybatis.toolkit.PluginUtils;
import com.iwhalecloud.bote.doc.common.mybatis.toolkit.PropertyMapper;
import com.iwhalecloud.bote.doc.common.mybatis.toolkit.StringPool;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @author hubin
 * @since 3.4.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("rawtypes")
public class TenantLineInnerInterceptor extends BaseMultiTableInnerInterceptor implements InnerInterceptor {

  private TenantLineHandler tenantLineHandler;

  @Override
  public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
    // 租户拦截器不对查询操作进行条件拼接
    // 所有 SELECT 查询均不自动添加 tenant_id 条件
    // 需要在 Mapper XML 中手动添加 tenant_id 条件
  }

  @Override
  public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
    PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
    MappedStatement ms = mpSh.mappedStatement();
    SqlCommandType sct = ms.getSqlCommandType();
    if (sct == SqlCommandType.INSERT || sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
      if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) {
        return;
      }
      PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
      mpBs.sql(parserMulti(mpBs.sql(), null));
    }
  }

  @Override
  public void setProperties(Properties properties) {
    PropertyMapper.newInstance(properties)
      .whenNotBlank("tenantLineHandler", ClassUtils::newInstance, this::setTenantLineHandler);
  }

  @SuppressWarnings({"unchecked", "PMD.LooseCoupling"})
  private static void processExpressions(ExpressionList<Expression> expressions, Expression tenantId) {
    if (CollectionUtils.isNotEmpty(expressions)) {
      //fix github issue 4998 jsqlparse 4.5 批量insert ItemsList不是MultiExpressionList 了，需要特殊处理
      int len = expressions.size();
      for (int i = 0; i < len; i++) {
        Expression expression = expressions.get(i);
        if (expression instanceof Parenthesis) {
          ExpressionList rowConstructor = new RowConstructor<>()
            .withExpressions(new ExpressionList<>(((Parenthesis) expression).getExpression(), tenantId));
          expressions.set(i, rowConstructor);
        }
        else if (expression instanceof ParenthesedExpressionList) {
          ((ParenthesedExpressionList) expression).addExpression(tenantId);
        }
        else {
          expressions.add(tenantId);
        }
      }
    }
    else {
      expressions.add(tenantId);
    }
  }

  @SuppressWarnings("all")
  @Override
  protected void processInsert(Insert insert, int index, String sql, Object obj) {
    if (tenantLineHandler.ignoreTable(insert.getTable().getName())) {
      // 过滤退出执行
      return;
    }
    List<Column> columns = insert.getColumns();
    if (CollectionUtils.isEmpty(columns)) {
      // 针对不给列名的insert 不处理
      return;
    }
    String tenantIdColumn = tenantLineHandler.getTenantIdColumn();
    if (tenantLineHandler.ignoreInsert(columns, tenantIdColumn)) {
      // 针对已给出租户列的insert 不处理
      return;
    }
    columns.add(new Column(tenantIdColumn));
    Expression tenantId = tenantLineHandler.getTenantId();
    // fixed gitee pulls/141 duplicate update
    List<UpdateSet> duplicateUpdateColumns = insert.getDuplicateUpdateSets();
    if (CollectionUtils.isNotEmpty(duplicateUpdateColumns)) {
      duplicateUpdateColumns.add(new UpdateSet(new Column(tenantIdColumn), tenantId));
    }

    Select select = insert.getSelect();
    if (select instanceof PlainSelect) { //fix github issue 4998  修复升级到4.5版本的问题
      this.processInsertSelect(select, (String) obj);
    }
    else if (insert.getValues() != null) {
      // fixed github pull/295
      Values values = insert.getValues();
      @SuppressWarnings("unchecked")
      ExpressionList<Expression> expressions = (ExpressionList<Expression>) values.getExpressions();
      if (expressions instanceof ParenthesedExpressionList) {
        expressions.addExpression(tenantId);
      }
      else {
        processExpressions(expressions, tenantId);
      }
    }
    else {
      throw ExceptionUtils.mpe("Failed to process multiple-table update, please exclude the tableName or statementId");
    }
  }

  /**
   * delete 语句处理
   */
  @Override
  protected void processDelete(Delete delete, int index, String sql, Object obj) {
    if (tenantLineHandler.ignoreTable(delete.getTable().getName())) {
      // 过滤退出执行
      return;
    }
    delete.setWhere(this.andExpression(delete.getTable(), delete.getWhere(), (String) obj));
  }

  /**
   * update 语句处理
   */
  @Override
  protected void processUpdate(Update update, int index, String sql, Object obj) {
    final Table table = update.getTable();
    if (tenantLineHandler.ignoreTable(table.getName())) {
      // 过滤退出执行
      return;
    }
    List<UpdateSet> sets = update.getUpdateSets();
    if (!CollectionUtils.isEmpty(sets)) {
      sets.forEach(us -> us.getValues().forEach(ex -> {
        if (ex instanceof Select) {
          processSelectBody((Select) ex, (String) obj);
        }
      }));
    }
    update.setWhere(this.andExpression(table, update.getWhere(), (String) obj));
  }

  @Override
  protected void processSelect(Select select, int index, String sql, Object obj) {
    // 租户拦截器不处理 SELECT 查询
    // 此方法不会被调用，因为 beforeQuery 已直接返回
  }

  /**
   * 处理 insert into select
   * <p>
   * 进入这里表示需要 insert 的表启用了多租户,则 select 的表都启动了
   *
   * @param selectBody SelectBody
   */
  protected void processInsertSelect(Select selectBody, final String whereSegment) {
    if (selectBody instanceof PlainSelect) {
      PlainSelect plainSelect = (PlainSelect) selectBody;
      FromItem fromItem = plainSelect.getFromItem();
      if (fromItem instanceof Table) {
        // fixed gitee pulls/141 duplicate update
        processPlainSelect(plainSelect, whereSegment);
        appendSelectItem(plainSelect.getSelectItems());
      }
      else if (fromItem instanceof Select) {
        Select subSelect = (Select) fromItem;
        appendSelectItem(plainSelect.getSelectItems());
        processInsertSelect(subSelect, whereSegment);
      }
    }
    else if (selectBody instanceof ParenthesedSelect) {
      ParenthesedSelect parenthesedSelect = (ParenthesedSelect) selectBody;
      processInsertSelect(parenthesedSelect.getSelect(), whereSegment);

    }
  }

  /**
   * 追加 SelectItem
   *
   * @param selectItems SelectItem
   */
  protected void appendSelectItem(List<SelectItem<?>> selectItems) {
    if (CollectionUtils.isEmpty(selectItems)) {
      return;
    }
    if (selectItems.size() == 1) {
      SelectItem item = selectItems.get(0);
      Expression expression = item.getExpression();
      if (expression instanceof AllColumns) {
        return;
      }
    }
    selectItems.add(new SelectItem<>(new Column(tenantLineHandler.getTenantIdColumn())));
  }

  /**
   * 租户字段别名设置
   * <p>tenantId 或 tableAlias.tenantId</p>
   *
   * @param table 表对象
   * @return 字段
   */
  protected Column getAliasColumn(Table table) {
    StringBuilder column = new StringBuilder();
    // 该起别名就要起别名,禁止修改此处逻辑
    if (table.getAlias() != null) {
      column.append(table.getAlias().getName()).append(StringPool.DOT);
    }
    column.append(tenantLineHandler.getTenantIdColumn());
    return new Column(column.toString());
  }

  /**
   * 处理 Oracle INSERT ALL 语法，自动注入租户ID
   *
   * @param sql SQL语句
   * @param obj 参数对象
   * @return 注入租户ID后的SQL
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected String processOracleInsertAll(String sql, Object obj) {
    if (logger.isDebugEnabled()) {
      logger.debug("Processing Oracle INSERT ALL with tenant injection");
    }

    try {
      // 获取租户ID字段名
      String tenantIdColumn = tenantLineHandler.getTenantIdColumn();

      // 获取租户ID值
      // 根据SQL中的占位符格式来确定租户ID的表示方式
      String tenantIdValue = OracleInsertAllHandler.extractTenantIdPlaceholder(sql);

      // 如果无法从SQL中推断，使用默认的租户ID表达式
      if ("?".equals(tenantIdValue)) {
        // 对于 ? 占位符，需要使用实际的租户ID值
        Expression tenantId = tenantLineHandler.getTenantId();
        tenantIdValue = tenantId.toString();
      }

      // 使用 OracleInsertAllHandler 注入租户ID
      String processedSql = OracleInsertAllHandler.injectTenantId(sql, tenantIdColumn, tenantIdValue);

      if (logger.isDebugEnabled()) {
        logger.debug("Oracle INSERT ALL processed successfully with tenant_id injection");
      }

      return processedSql;
    }
    catch (Exception e) {
      logger.warn("Failed to inject tenant_id into Oracle INSERT ALL, return original SQL. Error: {}", e.getMessage());
      return sql;
    }
  }

  /**
   * 构建租户条件表达式
   *
   * @param table 表对象
   * @param where 当前where条件
   * @param whereSegment 所属Mapper对象全路径（在原租户拦截器功能中，这个参数并不需要参与相关判断）
   * @return 租户条件表达式
   * @see BaseMultiTableInnerInterceptor#buildTableExpression(Table, Expression, String)
   */
  @Override
  public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) {
    if (tenantLineHandler.ignoreTable(table.getName())) {
      return null;
    }
    return new EqualsTo(getAliasColumn(table), tenantLineHandler.getTenantId());
  }
}
