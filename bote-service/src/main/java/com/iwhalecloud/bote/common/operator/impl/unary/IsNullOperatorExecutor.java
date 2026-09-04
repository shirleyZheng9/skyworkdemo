package com.iwhalecloud.bote.common.operator.impl.unary;

import org.springframework.lang.Nullable;

/**
 * 为 null 操作符执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class IsNullOperatorExecutor extends AbstractUnaryOperatorExecutor {
  @Override
  protected boolean executeUnary(@Nullable Object operand) {
    return operand == null;
  }
}
