package com.iwhalecloud.bote.dto.orchestration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

/**
 * 分支条件
 *
 * @author bianjp
 * @since 2024-08-30
 */
@Getter
@Setter
@JsonInclude(Include.NON_EMPTY)
public class IfCondition {
  /** 操作符。and, or 为特殊操作符，表示分组 */
  private String operator;
  /** 左操作数（取值表达式），必填 */
  private String left;
  /** 右操作数（取值表达式）。一元操作符可以没有，比如 isNull, isEmpty */
  private String right;
  /** 左操作数提供者，设置时代替 left。使用 Supplier 而非 Object 以兼容 null 值 */
  @JsonIgnore
  private Supplier<Object> leftSupplier;
  /** 右操作数提供者，设置时代替 right。使用 Supplier 而非 Object 以兼容 null 值 */
  @JsonIgnore
  private Supplier<Object> rightSupplier;
  /** 操作数的类型，可选，尽量设置 */
  private String type;
  /** 子条件列表，仅在 operator=and/or 时使用 */
  private List<IfCondition> children;

  /**
   * 构造 and 条件组
   */
  public static IfCondition and(IfCondition... conditions) {
    IfCondition condition = new IfCondition();
    condition.operator = "and";
    condition.children = Arrays.asList(conditions);
    return condition;
  }

  /**
   * 构造 or 条件组
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static IfCondition or(IfCondition... conditions) {
    IfCondition condition = new IfCondition();
    condition.operator = "or";
    condition.children = Arrays.asList(conditions);
    return condition;
  }

  /**
   * 是否是逻辑且
   */
  @JsonIgnore
  public boolean isAnd() {
    return "and".equalsIgnoreCase(operator);
  }

  /**
   * 是否是逻辑或
   */
  @JsonIgnore
  public boolean isOr() {
    return "or".equalsIgnoreCase(operator);
  }

  /**
   * 解析左操作数的值
   *
   * @return 左操作数的值
   */
  @Nullable
  public Object resolveLeftValue(Function<String, Object> paramResolver) {
    return leftSupplier != null ? leftSupplier.get() : paramResolver.apply(left);
  }

  /**
   * 解析右操作数的值
   *
   * @return 右操作数的值
   */
  @Nullable
  public Object resolveRightValue(Function<String, Object> paramResolver) {
    return rightSupplier != null ? rightSupplier.get() : paramResolver.apply(right);
  }

  @Override
  public String toString() {
    if ("and".equalsIgnoreCase(operator) || "or".equalsIgnoreCase(operator)) {
      if (CollectionUtils.isEmpty(children)) {
        return "";
      }
      if (children.size() == 1) {
        return children.get(0).toString();
      }
      return children.stream().map(IfCondition::toString).collect(Collectors.joining(" " + operator + " "));
    }
    if (operator != null && operator.startsWith("is")) {
      return left + " " + operator;
    }
    return left + " " + operator + " " + right;
  }
}
