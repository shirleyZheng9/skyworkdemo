package com.iwhalecloud.bote.doc.common.validate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link EnumValueValidate} 单元测试。
 *
 * <p>经反射读取测试字段上的 @EnumValue 注解实例传入 initialize，覆盖 enumClass/values/两者皆无
 * 三种初始化、null 走 allowNull、枚举值按 name 匹配、ignoreCase、集合/数组多选与 allowMultiple
 * 拦截、非枚举值走 toString。</p>
 */
class EnumValueValidateTest {

  @EnumValue(enumClass = Color.class)
  static String fEnumClass;

  @EnumValue(values = {"a", "b", "c"})
  static String fValues;

  @EnumValue
  static String fNeither;

  @EnumValue(values = {"Red", "Green"}, ignoreCase = true)
  static String fIgnoreCase;

  @EnumValue(enumClass = Color.class, allowMultiple = true)
  static String fMulti;

  @EnumValue(enumClass = Color.class, allowNull = false)
  static String fNoNull;

  enum Color {
    RED, GREEN, BLUE
  }

  private EnumValue annotation(String fieldName) throws Exception {
    Field f = EnumValueValidateTest.class.getDeclaredField(fieldName);
    return f.getAnnotation(EnumValue.class);
  }

  private EnumValueValidate validator(String fieldName) throws Exception {
    EnumValueValidate v = new EnumValueValidate();
    v.initialize(annotation(fieldName));
    return v;
  }

  @Test
  void initialize_enumClass_populatesFromEnumNames() throws Exception {
    EnumValueValidate v = validator("fEnumClass");
    assertThat(v.isValid("RED", null)).isTrue();
    assertThat(v.isValid("GREEN", null)).isTrue();
    assertThat(v.isValid("YELLOW", null)).isFalse();
  }

  @Test
  void initialize_values_populatesFromValues() throws Exception {
    EnumValueValidate v = validator("fValues");
    assertThat(v.isValid("a", null)).isTrue();
    assertThat(v.isValid("x", null)).isFalse();
  }

  @Test
  void initialize_neitherEnumClassNorValues_throwsIllegalArgument() {
    EnumValueValidate v = new EnumValueValidate();
    assertThatThrownBy(() -> v.initialize(annotation("fNeither")))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("enumClass或values");
  }

  @Test
  void isValid_null_returnsAllowNull() throws Exception {
    assertThat(validator("fEnumClass").isValid(null, null)).isTrue();
    assertThat(validator("fNoNull").isValid(null, null)).isFalse();
  }

  @Test
  void isValid_enumValue_matchesByName() throws Exception {
    EnumValueValidate v = validator("fEnumClass");
    assertThat(v.isValid(Color.RED, null)).isTrue();
    assertThat(v.isValid(Color.GREEN, null)).isTrue();
  }

  @Test
  void isValid_ignoreCase_matchesCaseInsensitive() throws Exception {
    EnumValueValidate v = validator("fIgnoreCase");
    assertThat(v.isValid("red", null)).isTrue();
    assertThat(v.isValid("RED", null)).isTrue();
    assertThat(v.isValid("blue", null)).isFalse();
  }

  @Test
  void isValid_collection_allowMultipleFalse_returnsFalse() throws Exception {
    EnumValueValidate v = validator("fEnumClass");
    assertThat(v.isValid(List.of("RED"), null)).isFalse();
  }

  @Test
  void isValid_collection_allowMultipleTrue_allValidOrNot() throws Exception {
    EnumValueValidate v = validator("fMulti");
    assertThat(v.isValid(List.of("RED", "GREEN"), null)).isTrue();
    assertThat(v.isValid(List.of("RED", "YELLOW"), null)).isFalse();
  }

  @Test
  void isValid_array_allowMultipleTrue_allValidOrNot() throws Exception {
    EnumValueValidate v = validator("fMulti");
    assertThat(v.isValid(new String[]{"RED", "GREEN"}, null)).isTrue();
    assertThat(v.isValid(new String[]{"RED", "YELLOW"}, null)).isFalse();
  }

  @Test
  void isValid_array_allowMultipleFalse_returnsFalse() throws Exception {
    EnumValueValidate v = validator("fEnumClass");
    assertThat(v.isValid(new String[]{"RED"}, null)).isFalse();
  }

  @Test
  void isValid_nonEnumValue_usesToString() throws Exception {
    EnumValueValidate v = validator("fEnumClass");
    assertThat(v.isValid(123, null)).isFalse();
  }
}
