package com.iwhalecloud.bote.common.operator;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bote.common.operator.impl.binary.BetweenOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.EnumInOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.EqualsOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.GreatThanOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.GreatThanOrEqualToOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.LessThanOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.LessThanOrEqualToOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.binary.NotEqualsOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.list.ListContainsAttrOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.list.ListContainsOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.list.ListNotContainsAttrOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.list.ListNotContainsOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.string.ContainsOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.string.EndsWithOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.string.NotContainsOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.string.NotEndsWithOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.string.NotStartsWithOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.string.StartsWithOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.unary.IsEmptyOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.unary.IsNotEmptyOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.unary.IsNotNullOperatorExecutor;
import com.iwhalecloud.bote.common.operator.impl.unary.IsNullOperatorExecutor;
import java.util.HashMap;
import java.util.Map;

/**
 * 运算符执行器工厂类
 *
 * @author bianjp
 * @since 2024-08-30
 */
public abstract class OperatorExecutorFactory {
  /** 执行器合集。key 为操作符 */
  private static final Map<String, OperatorExecutor> executorsMap;

  static {
    executorsMap = new HashMap<>();
    // 初始化所有操作符执行器
    executorsMap.put("=", new EqualsOperatorExecutor());
    executorsMap.put("!=", new NotEqualsOperatorExecutor());
    executorsMap.put(">", new GreatThanOperatorExecutor());
    executorsMap.put(">=", new GreatThanOrEqualToOperatorExecutor());
    executorsMap.put("<", new LessThanOperatorExecutor());
    executorsMap.put("<=", new LessThanOrEqualToOperatorExecutor());
    executorsMap.put("isNull", new IsNullOperatorExecutor());
    executorsMap.put("isNotNull", new IsNotNullOperatorExecutor());
    executorsMap.put("isEmpty", new IsEmptyOperatorExecutor());
    executorsMap.put("isNotEmpty", new IsNotEmptyOperatorExecutor());
    executorsMap.put("between", new BetweenOperatorExecutor());
    executorsMap.put("enumIn", new EnumInOperatorExecutor());
    // string
    executorsMap.put("contains", new ContainsOperatorExecutor());
    executorsMap.put("notContains", new NotContainsOperatorExecutor());
    executorsMap.put("startsWith", new StartsWithOperatorExecutor());
    executorsMap.put("notStartsWith", new NotStartsWithOperatorExecutor());
    executorsMap.put("endsWith", new EndsWithOperatorExecutor());
    executorsMap.put("notEndsWith", new NotEndsWithOperatorExecutor());
    // list
    executorsMap.put("listContains", new ListContainsOperatorExecutor());
    executorsMap.put("listNotContains", new ListNotContainsOperatorExecutor());
    executorsMap.put("listContainsAttr", new ListContainsAttrOperatorExecutor());
    executorsMap.put("listNotContainsAttr", new ListNotContainsAttrOperatorExecutor());
  }

  /**
   * 获取操作符执行器
   *
   * @param operator 操作符
   * @return 执行器
   */
  public static OperatorExecutor get(String operator) {
    OperatorExecutor executor = executorsMap.get(operator);
    if (executor == null) {
      throw new BssException("不支持的操作符: " + operator);
    }
    return executor;
  }

  /**
   * 判断是否支持操作符
   *
   * @param operator 操作符
   * @return 是否支持
   */
  public static boolean supports(String operator) {
    return executorsMap.containsKey(operator);
  }

}
