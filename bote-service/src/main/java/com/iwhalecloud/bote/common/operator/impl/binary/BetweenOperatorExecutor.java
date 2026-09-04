package com.iwhalecloud.bote.common.operator.impl.binary;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.operator.AbstractOperatorExecutor;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.Assert;

/**
 * between 操作符执行器
 *
 * <p>右操作数为 "~" 分隔的两个数值，校验左操作数在这两个数值之间（包含起始值、结束值）。</p>
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class BetweenOperatorExecutor extends AbstractOperatorExecutor {

  @SuppressWarnings({"unchecked", "java:S2259"})
  @Override
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    Object leftValue = condition.resolveLeftValue(paramResolver);
    // 值为空时当作不匹配
    if (ObjectUtils.isEmpty(leftValue)) {
      return false;
    }

    // 解析右操作数，应为 "~" 分隔的两部分
    String rightValue = Objects.toString(condition.resolveRightValue(paramResolver), null);
    Assert.hasLength(rightValue, () -> "between 操作符的右操作数不能为空: " + condition);
    String[] pieces = StringUtils.split(StringUtils.trim(rightValue), "~", 2);
    Assert.isTrue(pieces != null && pieces.length == 2, () -> "between 操作符的右操作数不合法，应为 ~ 分隔的两部分: " + condition);
    Assert.isTrue(StringUtils.isNotEmpty(pieces[0]), () -> "between 操作符的右操作数不合法，起始值不能为空: " + condition);
    Assert.isTrue(StringUtils.isNotEmpty(pieces[1]), () -> "between 操作符的右操作数不合法，结束值不能为空: " + condition);

    // 确定数据类型。前端未指定时根据左操作数的类型判断
    AttrDataType dataType = AttrDataType.ofCode(condition.getType());
    if (dataType == null) {
      // 优先当作数字
      if (leftValue instanceof Number ||
        (NumberUtils.isCreatable(leftValue.toString()) && NumberUtils.isCreatable(pieces[0]) && NumberUtils.isCreatable(pieces[1]))) {
        dataType = AttrDataType.NUMBER;
      }
      else {
        dataType = AttrDataType.STRING;
      }
    }

    // 转换数据类型
    Comparable<Object> convertedLeftValue = (Comparable<Object>) dataType.convert(condition.getLeft(), leftValue);
    Object rangeStart = dataType.convert(null, pieces[0]);
    Object rangeEnd = dataType.convert(null, pieces[1]);
    assert convertedLeftValue != null;
    assert rangeStart != null;
    assert rangeEnd != null;

    // 比较
    return convertedLeftValue.compareTo(rangeStart) >= 0 && convertedLeftValue.compareTo(rangeEnd) <= 0;
  }

}
