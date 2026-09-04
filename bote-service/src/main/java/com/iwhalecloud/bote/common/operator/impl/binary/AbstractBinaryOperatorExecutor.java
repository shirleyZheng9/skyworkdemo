package com.iwhalecloud.bote.common.operator.impl.binary;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.operator.AbstractOperatorExecutor;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.function.Function;
import org.springframework.lang.Nullable;

/**
 * 双目运算符执行器抽象类
 *
 * @author bianjp
 * @since 2024-08-30
 */
public abstract class AbstractBinaryOperatorExecutor extends AbstractOperatorExecutor {

  @Override
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    Object leftValue = condition.resolveLeftValue(paramResolver);
    Object rightValue = condition.resolveRightValue(paramResolver);
    AttrDataType dataType = AttrDataType.ofCode(condition.getType());
    if (dataType == null) {
      // 目前前端配置条件表达式时未指定操作数的数据类型，尝试自动探测类型
      dataType = detectDataType(leftValue, rightValue);
    }
    if (dataType != null) {
      leftValue = dataType.convert(condition.getLeft(), leftValue);
      rightValue = dataType.convert(condition.getRight(), rightValue);
    }
    return executeBinary(leftValue, rightValue);
  }

  /**
   * 执行双目运算符
   *
   * <p>注意对 null 的处理逻辑</p>
   *
   * @param left 左操作数
   * @param right 右操作数
   * @return 执行结果
   */
  protected abstract boolean executeBinary(@Nullable Object left, @Nullable Object right);

}
