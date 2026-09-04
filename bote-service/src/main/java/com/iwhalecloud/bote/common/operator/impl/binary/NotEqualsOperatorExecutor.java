package com.iwhalecloud.bote.common.operator.impl.binary;

import org.springframework.lang.Nullable;

/**
 * 不相等操作符执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class NotEqualsOperatorExecutor extends AbstractBinaryOperatorExecutor {

  @Override
  public boolean executeBinary(@Nullable Object left, @Nullable Object right) {
    if (left == null && right == null) {
      return false;
    }
    else if (left == null || right == null) {
      return true;
    }

    return !left.equals(right);
  }

}
