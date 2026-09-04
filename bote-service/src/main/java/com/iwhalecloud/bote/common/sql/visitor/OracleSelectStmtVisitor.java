package com.iwhalecloud.bote.common.sql.visitor;

import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectTableReference;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
import com.iwhalecloud.bote.common.sql.parse.DruidSqlParseUtil;
import com.iwhalecloud.bote.dto.skill.SqlScriptGenerateInfo;

/**
 * Oracle 查询语句访问器
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
public class OracleSelectStmtVisitor extends OracleASTVisitorAdapter {

  private final SqlScriptGenerateInfo sqlScriptGenerateInfo = new SqlScriptGenerateInfo();

  /**
   * 获取生成信息
   *
   * @return SQL脚本生成信息
   */
  public SqlScriptGenerateInfo getGenerateInfo() {
    return sqlScriptGenerateInfo;
  }

  @Override
  public boolean visit(SQLSelectStatement selectStatement) {
    DruidSqlParseUtil.visitSqlSelectStatement(selectStatement, sqlScriptGenerateInfo);
    return true;
  }

  /**
   * 获取 FROM 表达式
   */
  @Override
  public boolean visit(SQLExprTableSource exprTableSource) {
    sqlScriptGenerateInfo.getLowerTableNameSet().add(exprTableSource.getExpr().toString().toLowerCase());
    return true;
  }

  /**
   * 获取 Oracle FROM 表达式
   */
  @Override
  public boolean visit(OracleSelectTableReference tableReference) {
    sqlScriptGenerateInfo.getLowerTableNameSet().add(tableReference.getExpr().toString().toLowerCase());
    return true;
  }

  /**
   * 处理变量引用表达式
   */
  @Override
  public boolean visit(SQLVariantRefExpr variantRefExpr) {
    DruidSqlParseUtil.visitSqlVariantRefExpr(variantRefExpr, sqlScriptGenerateInfo);
    return true;
  }

}
