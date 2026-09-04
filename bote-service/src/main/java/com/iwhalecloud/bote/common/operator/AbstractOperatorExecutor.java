package com.iwhalecloud.bote.common.operator;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 运算符执行器抽象类
 *
 * @author bianjp
 * @since 2024-08-30
 */
public abstract class AbstractOperatorExecutor implements OperatorExecutor {

  /**
   * 检测数据类型
   */
  @Nullable
  protected AttrDataType detectDataType(@Nullable Object leftValue, @Nullable Object rightValue) {
    AttrDataType dataType;
    // 相同类型不进行指定
    if (isSameType(leftValue, rightValue)) {
      dataType = null;
    }
    else if (isAnyDate(leftValue, rightValue)) {
      dataType = AttrDataType.DATE;
    }
    else if (isAnyFloat(leftValue, rightValue)) {
      dataType = AttrDataType.NUMBER;
    }
    else if (isAnyInteger(leftValue, rightValue)) {
      dataType = AttrDataType.INTEGER;
    }
    else if (isAnyBoolean(leftValue, rightValue)) {
      dataType = AttrDataType.BOOLEAN;
    }
    else if (isAnyMap(leftValue, rightValue)) {
      dataType = AttrDataType.OBJECT;
    }
    else if (isAnyList(leftValue, rightValue)) {
      dataType = AttrDataType.ARRAY;
    }
    else {
      dataType = AttrDataType.STRING;
    }
    return dataType;
  }

  /**
   * 判断两个值类型是否相同
   */
  private boolean isSameType(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue != null && rightValue != null && leftValue.getClass() == rightValue.getClass();
  }

  /**
   * 判断值是否为日期类型
   */
  private boolean isAnyDate(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue instanceof Date || rightValue instanceof Date;
  }

  /**
   * 判断值是否为整型
   */
  private boolean isAnyInteger(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue instanceof Long || rightValue instanceof Long || leftValue instanceof Integer || rightValue instanceof Integer;
  }

  /**
   * 判断值是否为浮点型
   */
  private boolean isAnyFloat(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue instanceof Double || rightValue instanceof Double || leftValue instanceof Float || rightValue instanceof Float
      || leftValue instanceof BigDecimal || rightValue instanceof BigDecimal;
  }

  /**
   * 判断值是否为布尔类型
   */
  private boolean isAnyBoolean(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue instanceof Boolean || rightValue instanceof Boolean;
  }

  /**
   * 判断值是否为对象类型
   */
  private boolean isAnyMap(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue instanceof Map || rightValue instanceof Map;
  }

  /**
   * 判断值是否为列表类型
   */
  private boolean isAnyList(@Nullable Object leftValue, @Nullable Object rightValue) {
    return leftValue instanceof List || rightValue instanceof List;
  }
}
