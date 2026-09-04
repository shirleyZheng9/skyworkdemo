package com.iwhalecloud.bote.common.operator.impl.unary;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;

/**
 * 为空操作符执行器
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class IsEmptyOperatorExecutor extends AbstractUnaryOperatorExecutor {
  @Override
  protected boolean executeUnary(@Nullable Object operand) {
    return ObjectUtils.isEmpty(operand);
  }
}
