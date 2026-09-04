package com.iwhalecloud.bote.common.operator.impl.list;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 列表包含操作符
 *
 * <p>判断条件: 列表中存在某个元素等于给定值，适用于基本类型列表和字符串列表</p>
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class ListContainsOperatorExecutor extends AbstractListOperatorExecutor {

  @Override
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    // 解析列表
    List<Object> list = parseList(condition.getLeft(), condition.resolveLeftValue(paramResolver));
    // 列表为空时不需要检查右操作数
    if (list.isEmpty()) {
      return false;
    }

    // 解析右操作数
    Object rightValue = condition.resolveRightValue(paramResolver);
    // 转换类型，确保与列表元素的类型相同
    rightValue = convertRightValueType(condition, list, rightValue);

    return list.contains(rightValue);
  }

  /**
   * 转换右操作数的类型
   */
  @Nullable
  private Object convertRightValueType(IfCondition condition, List<Object> list, @Nullable Object rightValue) {
    if (rightValue == null) {
      return null;
    }
    // 优先使用配置的类型
    if (StringUtils.isNotEmpty(condition.getType())) {
      return AttrDataType.convert(condition.getRight(), condition.getType(), rightValue);
    }
    // 检查列表元素的类型。不使用 detectDataType 机制，那样可能需要转换所有列表元素的类型，比较麻烦
    Class<?> listItemType = list.stream().filter(Objects::nonNull).findFirst().map(Object::getClass).orElse(null);
    if (listItemType != null && !listItemType.equals(rightValue.getClass())) {
      // 只处理简单类型
      if (isSimpleType(listItemType)) {
        rightValue = conversionService.convert(rightValue, listItemType);
      }
    }
    return rightValue;
  }
}
