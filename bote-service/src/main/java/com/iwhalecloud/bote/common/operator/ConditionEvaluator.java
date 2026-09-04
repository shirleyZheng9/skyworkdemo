package com.iwhalecloud.bote.common.operator;

import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.function.Function;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 表达式求值器
 *
 * @author bianjp
 * @since 2024-12-24
 */
public final class ConditionEvaluator {
  private ConditionEvaluator() {
  }

  /**
   * 执行表达式
   *
   * <p>递归执行子条件</p>
   *
   * @param condition 条件
   * @param paramResolver 参数解析器
   * @return 表达式的值
   */
  public static boolean evaluate(@Nullable IfCondition condition, Function<String, Object> paramResolver) {
    // 条件为空时视为满足
    if (condition == null) {
      return true;
    }
    // AND
    if (condition.isAnd()) {
      Assert.notEmpty(condition.getChildren(), "AND 条件组不能为空");
      return executeAnd(condition, paramResolver);
    }
    // OR
    if (condition.isOr()) {
      Assert.notEmpty(condition.getChildren(), "OR 条件组不能为空");
      return executeOr(condition, paramResolver);
    }
    OperatorExecutor operatorExecutor = OperatorExecutorFactory.get(condition.getOperator());
    return operatorExecutor.execute(condition, paramResolver);
  }

  /**
   * 执行 AND 条件组
   */
  private static boolean executeAnd(IfCondition group, Function<String, Object> paramResolver) {
    for (IfCondition child : group.getChildren()) {
      boolean flag = evaluate(child, paramResolver);
      // 短路求值，任何一个子条件不满足即可退出
      if (!flag) {
        return false;
      }
    }
    return true;
  }

  /**
   * 执行 OR 条件组
   */
  private static boolean executeOr(IfCondition group, Function<String, Object> paramResolver) {
    for (IfCondition child : group.getChildren()) {
      boolean flag = evaluate(child, paramResolver);
      // 短路求值，任何一个子条件满足即可退出
      if (flag) {
        return true;
      }
    }
    return false;
  }

}
