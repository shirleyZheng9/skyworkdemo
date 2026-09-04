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

import com.iwhalecloud.bote.doc.common.mybatis.parser.JsqlParserSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 多表条件处理基对象，从原有的 {@link TenantLineInnerInterceptor} 拦截器中提取出来
 *
 * @author houkunlin
 * @since 3.5.2
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("rawtypes")
public abstract class BaseMultiTableInnerInterceptor extends JsqlParserSupport implements InnerInterceptor {

  protected void processSelectBody(Select selectBody, final String whereSegment) {
    if (selectBody == null) {
      return;
    }
    if (selectBody instanceof PlainSelect) {
      processPlainSelect((PlainSelect) selectBody, whereSegment);
    }
    else if (selectBody instanceof ParenthesedSelect parenthesedSelect) {
      processSelectBody(parenthesedSelect.getSelect(), whereSegment);
    }
    else if (selectBody instanceof SetOperationList operationList) {
      List<Select> selectBodyList = operationList.getSelects();
      if (selectBodyList != null && !selectBodyList.isEmpty()) {
        selectBodyList.forEach(body -> processSelectBody(body, whereSegment));
      }
    }
  }

  /**
   * delete update 语句 where 处理
   */
  protected Expression andExpression(Table table, Expression where, final String whereSegment) {
    //获得where条件表达式
    final Expression expression = buildTableExpression(table, where, whereSegment);
    if (expression == null) {
      return where;
    }
    if (where != null) {
      if (where instanceof OrExpression) {
        return new AndExpression(new Parenthesis(where), expression);
      }
      else {
        return new AndExpression(where, expression);
      }
    }
    return expression;
  }

  /**
   * 处理 PlainSelect
   */
  protected void processPlainSelect(final PlainSelect plainSelect, final String whereSegment) {
    //#3087 github
    List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
    if (CollectionUtils.isNotEmpty(selectItems)) {
      selectItems.forEach(selectItem -> processSelectItem(selectItem, whereSegment));
    }

    // 处理 where 中的子查询
    Expression where = plainSelect.getWhere();
    processWhereSubSelect(where, whereSegment);

    // 处理 fromItem
    FromItem fromItem = plainSelect.getFromItem();
    List<Table> list = processFromItem(fromItem, whereSegment);
    List<Table> mainTables = new ArrayList<>(list);

    // 处理 join
    List<Join> joins = plainSelect.getJoins();
    if (CollectionUtils.isNotEmpty(joins)) {
      processJoins(mainTables, joins, whereSegment);
    }

    // 当有 mainTable 时，进行 where 条件追加
    if (CollectionUtils.isNotEmpty(mainTables)) {
      plainSelect.setWhere(builderExpression(where, mainTables, whereSegment));
    }
  }

  private List<Table> processFromItem(FromItem fromItem, final String whereSegment) {
    // 处理括号括起来的表达式
//        while (fromItem instanceof ParenthesedFromItem) {
//            fromItem = ((ParenthesedFromItem) fromItem).getFromItem();
//        }

    List<Table> mainTables = new ArrayList<>();
    // 无 join 时的处理逻辑
    if (fromItem instanceof Table fromTable) {
      mainTables.add(fromTable);
    }
    else if (fromItem instanceof ParenthesedFromItem) {
      // SubJoin 类型则还需要添加上 where 条件
      List<Table> tables = processSubJoin((ParenthesedFromItem) fromItem, whereSegment);
      mainTables.addAll(tables);
    }
    else {
      // 处理下 fromItem
      processOtherFromItem(fromItem, whereSegment);
    }
    return mainTables;
  }

  /**
   * 处理where条件内的子查询
   * <p>
   * 支持如下:
   * <ol>
   *     <li>in</li>
   *     <li>=</li>
   *     <li>&gt;</li>
   *     <li>&lt;</li>
   *     <li>&gt;=</li>
   *     <li>&lt;=</li>
   *     <li>&lt;&gt;</li>
   *     <li>EXISTS</li>
   *     <li>NOT EXISTS</li>
   * </ol>
   * <p>
   * 前提条件:
   * 1. 子查询必须放在小括号中
   * 2. 子查询一般放在比较操作符的右边
   *
   * @param where where 条件
   */
  protected void processWhereSubSelect(Expression where, final String whereSegment) {
    if (where == null) {
      return;
    }
    if (where instanceof FromItem) {
      processOtherFromItem((FromItem) where, whereSegment);
      return;
    }
    if (where.toString().indexOf("SELECT") > 0) {
      // 有子查询
      if (where instanceof BinaryExpression expression) {
        // 比较符号 , and , or , 等等
        processWhereSubSelect(expression.getLeftExpression(), whereSegment);
        processWhereSubSelect(expression.getRightExpression(), whereSegment);
      }
      else if (where instanceof InExpression expression) {
        // in
        Expression inExpression = expression.getRightExpression();
        if (inExpression instanceof Select) {
          processSelectBody((Select) inExpression, whereSegment);
        }
      }
      else if (where instanceof ExistsExpression expression) {
        // exists
        processWhereSubSelect(expression.getRightExpression(), whereSegment);
      }
      else if (where instanceof NotExpression expression) {
        // not exists
        processWhereSubSelect(expression.getExpression(), whereSegment);
      }
      else if (where instanceof Parenthesis expression) {
        processWhereSubSelect(expression.getExpression(), whereSegment);
      }
    }
  }

  protected void processSelectItem(SelectItem selectItem, final String whereSegment) {
    Expression expression = selectItem.getExpression();
    if (expression instanceof Select) {
      processSelectBody((Select) expression, whereSegment);
    }
    else if (expression instanceof Function) {
      processFunction((Function) expression, whereSegment);
    }
    else if (expression instanceof ExistsExpression existsExpression) {
      processSelectBody((Select) existsExpression.getRightExpression(), whereSegment);
    }
  }

  /**
   * 处理函数
   * <p>支持: 1. select fun(args..) 2. select fun1(fun2(args..),args..)<p>
   * <p> fixed gitee pulls/141</p>
   */
  @SuppressWarnings("PMD.LooseCoupling")
  protected void processFunction(Function function, final String whereSegment) {
    ExpressionList<?> parameters = function.getParameters();
    if (parameters != null) {
      parameters.forEach(expression -> {
        if (expression instanceof Select) {
          processSelectBody((Select) expression, whereSegment);
        }
        else if (expression instanceof Function) {
          processFunction((Function) expression, whereSegment);
        }
        else if (expression instanceof EqualsTo) {
          if (((EqualsTo) expression).getLeftExpression() instanceof Select) {
            processSelectBody((Select) ((EqualsTo) expression).getLeftExpression(), whereSegment);
          }
          if (((EqualsTo) expression).getRightExpression() instanceof Select) {
            processSelectBody((Select) ((EqualsTo) expression).getRightExpression(), whereSegment);
          }
        }
      });
    }
  }

  /**
   * 处理子查询等
   */
  protected void processOtherFromItem(FromItem fromItem, final String whereSegment) {
    // 去除括号
//        while (fromItem instanceof ParenthesisFromItem) {
//            fromItem = ((ParenthesisFromItem) fromItem).getFromItem();
//        }

    if (fromItem instanceof ParenthesedSelect subSelect) {
      processSelectBody(subSelect, whereSegment);
    }
    else if (fromItem instanceof ParenthesedFromItem) {
      logger.debug("Perform a subQuery, if you do not give us feedback");
    }
  }

  /**
   * 处理 sub join
   *
   * @param subJoin subJoin
   * @return Table subJoin 中的主表
   */
  private List<Table> processSubJoin(ParenthesedFromItem subJoin, final String whereSegment) {
    List<Table> mainTables = new ArrayList<>();
    while (subJoin.getJoins() == null && subJoin.getFromItem() instanceof ParenthesedFromItem) {
      subJoin = (ParenthesedFromItem) subJoin.getFromItem();
    }
    if (subJoin.getJoins() != null) {
      List<Table> list = processFromItem(subJoin.getFromItem(), whereSegment);
      mainTables.addAll(list);
      processJoins(mainTables, subJoin.getJoins(), whereSegment);
    }
    return mainTables;
  }

  /**
   * 处理 joins
   *
   * @param mainTables 可以为 null
   * @param joins join 集合
   */
  private void processJoins(List<Table> mainTables, List<Join> joins, final String whereSegment) {
    JoinContext context = initializeJoinContext(mainTables);
    Deque<List<Table>> onTableDeque = new LinkedList<>();

    for (Join join : joins) {
      processSingleJoin(join, context, onTableDeque, whereSegment);
    }
  }

  /**
   * 初始化 Join 上下文
   */
  private JoinContext initializeJoinContext(List<Table> mainTables) {
    JoinContext context = new JoinContext();
    context.mainTables = mainTables;
    if (mainTables.size() == 1) {
      context.mainTable = mainTables.get(0);
      context.leftTable = context.mainTable;
    }
    return context;
  }

  /**
   * 处理单个 Join
   */
  private void processSingleJoin(Join join, JoinContext context, Deque<List<Table>> onTableDeque, final String whereSegment) {
    FromItem joinItem = join.getRightItem();
    List<Table> joinTables = extractJoinTables(joinItem, whereSegment);

    if (joinTables == null) {
      processOtherFromItem(joinItem, whereSegment);
      context.leftTable = null;
      return;
    }

    if (join.isSimple()) {
      handleSimpleJoin(joinTables, context);
      return;
    }

    Table joinTable = joinTables.getFirst();
    List<Table> onTables = determineOnTables(join, joinTable, context);
    updateMainTables(context);
    processOnExpressions(join, onTables, onTableDeque, whereSegment);
    context.leftTable = joinTable;
  }

  /**
   * 提取 Join 表列表
   */
  private List<Table> extractJoinTables(FromItem joinItem, final String whereSegment) {
    if (joinItem instanceof Table) {
      List<Table> joinTables = new ArrayList<>();
      joinTables.add((Table) joinItem);
      return joinTables;
    }
    else if (joinItem instanceof ParenthesedFromItem) {
      return processSubJoin((ParenthesedFromItem) joinItem, whereSegment);
    }
    return null;
  }

  /**
   * 处理简单 Join
   */
  private void handleSimpleJoin(List<Table> joinTables, JoinContext context) {
    // 隐式内连接，直接添加到主表列表
    context.mainTables.addAll(joinTables);
  }

  /**
   * 确定 ON 表列表
   */
  private List<Table> determineOnTables(Join join, Table joinTable, JoinContext context) {
    if (join.isRight()) {
      return handleRightJoin(joinTable, context);
    }
    else if (join.isInner()) {
      return handleInnerJoin(joinTable, context);
    }
    else {
      return Collections.singletonList(joinTable);
    }
  }

  /**
   * 处理右连接
   */
  private List<Table> handleRightJoin(Table joinTable, JoinContext context) {
    context.mainTable = joinTable;
    context.mainTables.clear();
    return context.leftTable != null ? Collections.singletonList(context.leftTable) : null;
  }

  /**
   * 处理内连接
   */
  private List<Table> handleInnerJoin(Table joinTable, JoinContext context) {
    List<Table> onTables;
    if (context.mainTable == null) {
      onTables = Collections.singletonList(joinTable);
    }
    else {
      onTables = Arrays.asList(context.mainTable, joinTable);
    }
    context.mainTable = null;
    context.mainTables.clear();
    return onTables;
  }

  /**
   * 更新主表列表
   */
  private void updateMainTables(JoinContext context) {
    if (context.mainTable != null && !context.mainTables.contains(context.mainTable)) {
      context.mainTables.add(context.mainTable);
    }
  }

  /**
   * 处理 ON 表达式
   */
  private void processOnExpressions(Join join, List<Table> onTables, Deque<List<Table>> onTableDeque, final String whereSegment) {
    Collection<Expression> originOnExpressions = join.getOnExpressions();

    if (originOnExpressions.size() == 1 && onTables != null) {
      processSingleOnExpression(join, originOnExpressions, onTables, whereSegment);
    }
    else if (originOnExpressions.size() > 1) {
      processMultipleOnExpressions(join, originOnExpressions, onTableDeque, onTables, whereSegment);
    }
  }

  /**
   * 处理单个 ON 表达式
   */
  private void processSingleOnExpression(Join join, Collection<Expression> originOnExpressions,
                                         List<Table> onTables, final String whereSegment) {
    List<Expression> onExpressions = new LinkedList<>();
    onExpressions.add(builderExpression(originOnExpressions.iterator().next(), onTables, whereSegment));
    join.setOnExpressions(onExpressions);
  }

  /**
   * 处理多个 ON 表达式
   */
  private void processMultipleOnExpressions(Join join, Collection<Expression> originOnExpressions,
                                            Deque<List<Table>> onTableDeque, List<Table> onTables,
                                            final String whereSegment) {
    onTableDeque.push(onTables);

    Collection<Expression> onExpressions = new LinkedList<>();
    for (Expression originOnExpression : originOnExpressions) {
      List<Table> currentTableList = onTableDeque.poll();
      if (CollectionUtils.isEmpty(currentTableList)) {
        onExpressions.add(originOnExpression);
      }
      else {
        onExpressions.add(builderExpression(originOnExpression, currentTableList, whereSegment));
      }
    }
    join.setOnExpressions(onExpressions);
  }

  /**
   * 处理条件
   */
  protected Expression builderExpression(Expression currentExpression, List<Table> tables, final String whereSegment) {
    // 没有表需要处理直接返回
    if (CollectionUtils.isEmpty(tables)) {
      return currentExpression;
    }
    // 构造每张表的条件
    List<Expression> expressions = tables.stream()
      .map(item -> buildTableExpression(item, currentExpression, whereSegment))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    // 没有表需要处理直接返回
    if (CollectionUtils.isEmpty(expressions)) {
      return currentExpression;
    }

    // 注入的表达式
    Expression injectExpression = expressions.get(0);
    // 如果有多表，则用 and 连接
    if (expressions.size() > 1) {
      for (int i = 1; i < expressions.size(); i++) {
        injectExpression = new AndExpression(injectExpression, expressions.get(i));
      }
    }

    if (currentExpression == null) {
      return injectExpression;
    }
    if (currentExpression instanceof OrExpression) {
      return new AndExpression(new Parenthesis(currentExpression), injectExpression);
    }
    else {
      return new AndExpression(currentExpression, injectExpression);
    }
  }

  /**
   * 构建数据库表的查询条件
   *
   * @param table 表对象
   * @param where 当前where条件
   * @param whereSegment 所属Mapper对象全路径
   * @return 需要拼接的新条件（不会覆盖原有的where条件，只会在原有条件上再加条件），为 null 则不加入新的条件
   */
  public abstract Expression buildTableExpression(Table table, Expression where, String whereSegment);

  /**
   * Join 上下文类
   */
  private static final class JoinContext {
    Table mainTable;
    Table leftTable;
    List<Table> mainTables;
  }
}
