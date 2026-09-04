package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 参数规格
 *
 * <p>用于描述服务的入参结构、出参结构、参数赋值配置等。</p>
 *
 * @author bianjp
 * @since 2024-08-09
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ParameterSpec {
  /** 参数名称（英文，一般应是合法的 Java 变量名称） */
  private String name;
  /** 参数描述 */
  private String description;
  /** 类型（为空时等同于 {@link AttrDataType#ANY}） */
  private AttrDataType type;
  /** 是否必填 */
  private Boolean required;
  /** 默认值 */
  private String defaultValue;
  /** 赋值 */
  private String value;
  /** 子节点列表，表示对象的属性，或者列表的元素。列表只能有一个元素 */
  private List<ParameterSpec> children;
  /** 是否对模型可见，默认为 true */
  private Boolean modelVisible;
  /** 节点唯一标识 */
  private String key;
  /** 父节点标识 */
  private String parentKey;
  /** 静态数据编码 */
  private String attrCode;
  /** 参数格式，用于定义一些特殊参数，例如模型（llm_model）文件系统（file_info） */
  private String format;

  /**
   * 是否是对象
   */
  @JsonIgnore
  public boolean isObject() {
    return type == AttrDataType.OBJECT;
  }

  /**
   * 是否是列表
   */
  @JsonIgnore
  public boolean isList() {
    return type == AttrDataType.ARRAY;
  }

  /**
   * 是否是属性
   */
  @JsonIgnore
  public boolean isProperty() {
    return type != AttrDataType.OBJECT && type != AttrDataType.ARRAY;
  }

  /**
   * 是否有子节点
   */
  @JsonIgnore
  public boolean hasChildren() {
    return CollectionUtils.isNotEmpty(children);
  }

  /**
   * 是否有子节点赋值
   */
  @JsonIgnore
  public boolean hasChildrenAssignment() {
    if (CollectionUtils.isEmpty(children)) {
      return false;
    }
    // 检查子节点是否赋值
    if (children.stream().anyMatch(p -> StringUtils.isNotEmpty(p.getValue()) || StringUtils.isNotEmpty(p.getDefaultValue()))) {
      return true;
    }
    // 递归检查子节点的子节点是否赋值
    return children.stream().anyMatch(p -> p.isObject() && p.hasChildrenAssignment());
  }

  /**
   * 是否是空对象
   */
  @JsonIgnore
  public boolean isEmptyObject() {
    return isObject() && CollectionUtils.isEmpty(children);
  }

  /**
   * 获取数组的元素结构
   */
  @JsonIgnore
  public ParameterSpec getArrayElement() {
    return CollectionUtils.isNotEmpty(children) ? children.get(0) : null;
  }

  /**
   * 构造根节点
   */
  public static ParameterSpec newRoot() {
    return newObject(BaseConsts.PARAMETER_NODE_ROOT, BaseConsts.PARAMETER_NODE_ROOT_DESCRIPTION, null);
  }

  /**
   * 构造根节点
   */
  public static ParameterSpec newRoot(@Nullable List<ParameterSpec> children) {
    return newObject(BaseConsts.PARAMETER_NODE_ROOT, BaseConsts.PARAMETER_NODE_ROOT_DESCRIPTION, children);
  }

  /**
   * 构造对象
   */
  public static ParameterSpec newObject(String name, String description, @Nullable List<ParameterSpec> children) {
    return ParameterSpec.builder().name(name).description(description).type(AttrDataType.OBJECT).children(children).build();
  }

  /**
   * 构造列表
   */
  public static ParameterSpec newList(String name, String description, @Nullable ParameterSpec element) {
    List<ParameterSpec> children = element == null ? null : Collections.singletonList(element);
    return ParameterSpec.builder().name(name).description(description).type(AttrDataType.ARRAY).children(children).build();
  }

  /**
   * 构造属性
   */
  public static ParameterSpec newProperty(String name, String description, @Nullable AttrDataType type) {
    return ParameterSpec.builder().name(name).description(description).type(type).build();
  }

}
