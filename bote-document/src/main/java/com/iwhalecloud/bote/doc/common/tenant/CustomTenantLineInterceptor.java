package com.iwhalecloud.bote.doc.common.tenant;

import com.iwhalecloud.bote.doc.common.mybatis.plugins.handler.TenantLineHandler;
import com.iwhalecloud.bote.doc.common.mybatis.plugins.inner.TenantLineInnerInterceptor;
import lombok.EqualsAndHashCode;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * 租户拦截器
 *
 * @author Aiqing
 * @since 2023/9/4
 */
@EqualsAndHashCode(callSuper = true)
public class CustomTenantLineInterceptor extends TenantLineInnerInterceptor {

  private final TenantLineHandler tenantLineHandler;

  public CustomTenantLineInterceptor(TenantLineHandler tenantLineHandler) {
    super(tenantLineHandler);
    this.tenantLineHandler = tenantLineHandler;
  }

  @Override
  public Expression buildTableExpression(Table table, Expression where, String whereSegment) {
    if (tenantLineHandler.ignoreTable(table.getName())) {
      return null;
    }
    // 判断是否已有租户过滤字段，有则不再添加
    if (where instanceof Parenthesis) {
      Expression expression = ((Parenthesis) where).getExpression();
      if (resolveAndExpressionTenantColumn(expression)) {
        return null;
      }
    }
    else if (where instanceof AndExpression && resolveAndExpressionTenantColumn(where)) {
      return null;

    }
    return new EqualsTo(getAliasColumn(table), tenantLineHandler.getTenantId());
  }

  private boolean resolveAndExpressionTenantColumn(Expression expression) {
    if (!(expression instanceof AndExpression andExpression)) {
      return false;
    }
    Expression leftExpression = andExpression.getLeftExpression();
    Expression rightExpression = andExpression.getRightExpression();

    if (resolveEqualsToTenantColumn(leftExpression)) {
      return true;
    }

    if (resolveEqualsToTenantColumn(rightExpression)) {
      return true;
    }
    return false;
  }

  private boolean resolveEqualsToTenantColumn(Expression expression) {
    if (expression instanceof EqualsTo) {
      EqualsTo equalsTo = (EqualsTo) expression;
      Expression equalsToLeftExpression = equalsTo.getLeftExpression();
      if (equalsToLeftExpression instanceof Column) {
        String columnName = ((Column) equalsToLeftExpression).getColumnName();
        return this.tenantLineHandler.getTenantIdColumn().equalsIgnoreCase(columnName);
      }
    }
    else if (expression instanceof AndExpression) {
      return resolveAndExpressionTenantColumn(expression);
    }
    else if (expression instanceof Parenthesis) {
      return resolveAndExpressionTenantColumn(((Parenthesis) expression).getExpression());
    }
    return false;
  }
}
