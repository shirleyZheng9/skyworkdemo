package com.iwhalecloud.bote.doc.common.mybatis.toolkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.doc.common.mybatis.exception.MybatisPlusException;
import org.junit.jupiter.api.Test;

/**
 * {@link ClassUtils} 单元测试。
 *
 * <p>覆盖 isBoolean、isProxy（null/普通类 false；cglib 代理 true 分支需真实代理类，按可测性跳过）、
 * getUserClass 两重载（null 抛异常/非代理返回自身）、newInstance（按 Class 与按类名、无无参构造抛
 * mpe）、toClassConfident（有效/无效/带 ClassLoader）、getPackageName（按 Class/按名、无包名返空）、
 * getDefaultClassLoader 非空。</p>
 */
class ClassUtilsTest {

  @Test
  void isBoolean_recognizesBooleanTypes() {
    assertThat(ClassUtils.isBoolean(boolean.class)).isTrue();
    assertThat(ClassUtils.isBoolean(Boolean.class)).isTrue();
    assertThat(ClassUtils.isBoolean(int.class)).isFalse();
    assertThat(ClassUtils.isBoolean(String.class)).isFalse();
  }

  @Test
  void isProxy_nullOrNormalClass_returnsFalse() {
    assertThat(ClassUtils.isProxy(null)).isFalse();
    assertThat(ClassUtils.isProxy(String.class)).isFalse();
    assertThat(ClassUtils.isProxy(ClassUtilsTest.class)).isFalse();
  }

  @Test
  void getUserClass_byClass_null_throwsIllegalArgument() {
    assertThatThrownBy(() -> ClassUtils.getUserClass((Class<?>) null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getUserClass_byClass_nonProxy_returnsSelf() {
    assertThat(ClassUtils.getUserClass(String.class)).isSameAs(String.class);
  }

  @Test
  void getUserClass_byObject_null_throwsIllegalArgument() {
    assertThatThrownBy(() -> ClassUtils.getUserClass((Object) null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getUserClass_byObject_returnsUserClass() {
    assertThat(ClassUtils.getUserClass("hello")).isEqualTo(String.class);
  }

  @Test
  void newInstance_byClass_valid_returnsInstance() {
    assertThat(ClassUtils.newInstance(String.class)).isInstanceOf(String.class);
  }

  @Test
  void newInstance_byClass_noNoArgCtor_throwsMpe() {
    assertThatThrownBy(() -> ClassUtils.newInstance(Integer.class))
      .isInstanceOf(MybatisPlusException.class)
      .hasMessageContaining("无参的构造方法");
  }

  @Test
  void newInstance_byName_valid_returnsInstance() {
    String instance = ClassUtils.newInstance("java.lang.String");
    assertThat(instance).isInstanceOf(String.class);
  }

  @Test
  void newInstance_byName_invalid_throwsMpe() {
    assertThatThrownBy(() -> ClassUtils.newInstance("no.such.Class123"))
      .isInstanceOf(MybatisPlusException.class);
  }

  @Test
  void toClassConfident_valid_returnsClass() {
    assertThat(ClassUtils.toClassConfident("java.lang.String")).isEqualTo(String.class);
  }

  @Test
  void toClassConfident_invalid_throwsMpe() {
    assertThatThrownBy(() -> ClassUtils.toClassConfident("no.such.Class123"))
      .isInstanceOf(MybatisPlusException.class)
      .hasMessageContaining("找不到指定的class");
  }

  @Test
  void toClassConfident_withClassLoader_valid() {
    assertThat(ClassUtils.toClassConfident("java.lang.String", getClass().getClassLoader()))
      .isEqualTo(String.class);
  }

  @Test
  void getPackageName_byClass_null_throwsIllegalArgument() {
    assertThatThrownBy(() -> ClassUtils.getPackageName((Class<?>) null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getPackageName_byClass_returnsPackage() {
    assertThat(ClassUtils.getPackageName(String.class)).isEqualTo("java.lang");
  }

  @Test
  void getPackageName_byName_null_throwsIllegalArgument() {
    assertThatThrownBy(() -> ClassUtils.getPackageName((String) null))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getPackageName_byName_withDot_returnsPackage() {
    assertThat(ClassUtils.getPackageName("java.lang.String")).isEqualTo("java.lang");
  }

  @Test
  void getPackageName_byName_withoutDot_returnsEmpty() {
    assertThat(ClassUtils.getPackageName("NoPackage")).isEmpty();
  }

  @Test
  void getDefaultClassLoader_returnsNonNull() {
    org.junit.jupiter.api.Assertions.assertNotNull(ClassUtils.getDefaultClassLoader());
  }
}
