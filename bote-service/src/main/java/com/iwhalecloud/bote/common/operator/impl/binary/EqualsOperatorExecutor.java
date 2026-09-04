package com.iwhalecloud.bote.common.operator.impl.binary;

import org.springframework.lang.Nullable;

/**
 * 相同操作符执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class EqualsOperatorExecutor extends AbstractBinaryOperatorExecutor {

  @Override
  public boolean executeBinary(@Nullable Object left, @Nullable Object right) {
    if (left == null && right == null) {
      return true;
    }
    else if (left == null || right == null) {
      return false;
    }

    return left.equals(right);
  }

}
