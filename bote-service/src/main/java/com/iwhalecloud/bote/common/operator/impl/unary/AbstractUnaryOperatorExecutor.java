package com.iwhalecloud.bote.common.operator.impl.unary;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.operator.AbstractOperatorExecutor;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.function.Function;
import org.springframework.lang.Nullable;

/**
 * 单目运算符执行器抽象类
 *
 * @author bianjp
 * @since 2024-08-30
 */
public abstract class AbstractUnaryOperatorExecutor extends AbstractOperatorExecutor {

  @Override
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    Object leftValue = condition.resolveLeftValue(paramResolver);
    leftValue = AttrDataType.convert(condition.getLeft(), condition.getType(), leftValue);
    return executeUnary(leftValue);
  }

  /**
   * 执行单目运行符
   *
   * @param operand 操作数
   * @return 执行结果
   */
  protected abstract boolean executeUnary(@Nullable Object operand);
}
