package com.iwhalecloud.bote.common.operator.impl.binary;

import com.iwhalecloud.bote.common.operator.AbstractOperatorExecutor;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;

/**
 * 枚举值操作符执行器
 *
 * <p>右操作数为 "," 分隔的多个数值，校验左操作数在这些数值中。</p>
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class EnumInOperatorExecutor extends AbstractOperatorExecutor {
  @Override
  @SuppressWarnings("java:S2259")
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    Object leftValue = condition.resolveLeftValue(paramResolver);
    // 值为空时当作不匹配
    if (ObjectUtils.isEmpty(leftValue)) {
      return false;
    }

    // 解析右操作数，应为 "," 分隔的多个值
    String rightValue = Objects.toString(condition.resolveRightValue(paramResolver), null);
    Assert.hasLength(rightValue, () -> "枚举值操作符的右操作数不能为空: " + condition);
    assert rightValue != null;
    String[] enumValues = rightValue.trim().split("\\s*,\\s*");

    // 将左操作数转为字符以方便比较
    String leftValueStr = leftValue.toString();
    return ArrayUtils.contains(enumValues, leftValueStr);
  }
}
