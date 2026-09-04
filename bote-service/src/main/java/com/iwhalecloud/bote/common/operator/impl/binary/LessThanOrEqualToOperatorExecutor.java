package com.iwhalecloud.bote.common.operator.impl.binary;

import org.springframework.lang.Nullable;

/**
 * 小于等于操作符执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class LessThanOrEqualToOperatorExecutor extends AbstractBinaryOperatorExecutor {

  @SuppressWarnings("unchecked")
  @Override
  public boolean executeBinary(@Nullable Object left, @Nullable Object right) {
    if (left == null && right == null) {
      return true;
    }
    else if (left == null || right == null) {
      return false;
    }

    if (left instanceof Comparable && right instanceof Comparable) {
      return ((Comparable<Object>) left).compareTo(right) <= 0;
    }
    return false;
  }

}
