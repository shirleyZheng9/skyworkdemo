package com.iwhalecloud.bote.doc.module.control.base.role;

/**
 * 权限比较接口
 */
public interface RoleComparable<T> extends Comparable<T> {

  boolean isEqualTo(T other);

  boolean isGreaterThan(T other);

  boolean isGreaterThanOrEqualTo(T other);

  boolean isLessThan(T other);

  boolean isLessThanOrEqualTo(T other);
}
