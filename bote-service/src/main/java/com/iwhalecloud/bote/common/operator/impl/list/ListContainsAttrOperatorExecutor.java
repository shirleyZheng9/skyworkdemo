package com.iwhalecloud.bote.common.operator.impl.list;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.ParamUtil;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

/**
 * 列表包含属性操作符
 *
 * <p>判断条件: 列表中存在某个元素的指定属性等于给定值，适用于对象列表</p>
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class ListContainsAttrOperatorExecutor extends AbstractListOperatorExecutor {
  @Override
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    // 解析列表
    List<Object> list = parseList(condition.getLeft(), condition.resolveLeftValue(paramResolver));
    // 列表为空时不需要检查右操作数
    if (list.isEmpty()) {
      return false;
    }

    // 解析右操作数
    String[] pieces = parseRightOperand(condition);
    String attrName = pieces[0];
    Object expectedAttrValue;
    if (pieces.length > 1) {
      expectedAttrValue = AttrDataType.convert(pieces[1], condition.getType(), paramResolver.apply(pieces[1]));
    }
    else {
      expectedAttrValue = null;
    }

    // 如果期望值为 null, 直接检查是否存在属性值为 null 的元素
    if (expectedAttrValue == null) {
      return checkListContainsAttrValueNull(list, attrName);
    }
    return checkListContainsAttrValue(list, attrName, expectedAttrValue);
  }

  /**
   * 解析右操作数
   */
  private String[] parseRightOperand(IfCondition condition) {
    // 值为 "::" 分隔的两部分，左边为属性名称，右边为期望属性值的表达式
    String[] pieces = StringUtils.split(condition.getRight(), "::", 2);
    if (pieces == null || (pieces.length != 2 && pieces.length != 1)) {
      throw new BssException(String.format("列表包含属性操作符的右操作数配置错误: left=%s, right=%s", condition.getLeft(), condition.getRight()));
    }
    return pieces;
  }

  /**
   * 检查列表中是否存在属性值为空的元素
   */
  private boolean checkListContainsAttrValueNull(List<Object> list, String attrName) {
    for (Object o : list) {
      Object value = ParamUtil.getNestedProperty(o, attrName);
      if (value == null) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查是否存在属性值等于期望值的元素
   */
  private boolean checkListContainsAttrValue(List<Object> list, String attrName, Object expectedAttrValue) {
    // 比较前需要转换类型，避免类型不一致导致判断结果不符合预期
    Class<?> targetType = expectedAttrValue.getClass();
    // 只对简单类型做转换，兼容期望值为对象的情况
    boolean isSimpleType = isSimpleType(targetType);
    for (Object o : list) {
      Object value = ParamUtil.getNestedProperty(o, attrName);
      if (value != null) {
        if (value.getClass() != targetType && isSimpleType) {
          value = conversionService.convert(value, targetType);
        }
        if (expectedAttrValue.equals(value)) {
          return true;
        }
      }
    }
    return false;
  }

}
