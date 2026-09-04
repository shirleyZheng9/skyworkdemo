package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.convert.ConversionService;

/**
 * {@link AttrDataType} 单元测试
 *
 * <p>覆盖 ofCode 的空/匹配/大小写不敏感/未知分支、convert 实例方法的 null/同类型短路/STRING 日期格式化/
 * ConversionService 转换成功与异常包装（含 BssException cause 与 propertyPath 有无分支）、
 * 静态 convert 重载，以及 conversionService 懒加载（为空时经 SpringUtil.getBean 获取）。</p>
 *
 * <p>conversionService 为私有静态字段，BeforeEach 通过反射置为受控 mock 以隔离测试；懒加载用例临时置空
 * 并 mockStatic(SpringUtil)。</p>
 */
class AttrDataTypeTest {

  private ConversionService cs;

  @BeforeEach
  void setUp() throws Exception {
    cs = mock(ConversionService.class);
    setConversionService(cs);
  }

  // ==================== ofCode ====================

  @Test
  void ofCode_knownCode_returnsType() {
    assertThat(AttrDataType.ofCode("string")).isSameAs(AttrDataType.STRING);
    assertThat(AttrDataType.ofCode("integer")).isSameAs(AttrDataType.INTEGER);
  }

  @Test
  void ofCode_caseInsensitive() {
    assertThat(AttrDataType.ofCode("STRING")).isSameAs(AttrDataType.STRING);
    assertThat(AttrDataType.ofCode("Integer")).isSameAs(AttrDataType.INTEGER);
  }

  @Test
  void ofCode_empty_returnsNull() {
    // StringUtils.isEmpty 不 trim，仅 null/"" 返回 null
    assertThat(AttrDataType.ofCode(null)).isNull();
    assertThat(AttrDataType.ofCode("")).isNull();
  }

  @Test
  void ofCode_unknown_throwsBssException() {
    assertThatThrownBy(() -> AttrDataType.ofCode("xyz"))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("未知的属性类型")
        .hasMessageContaining("xyz");
    // 纯空白不被 isEmpty 视为空，同样落入未知分支
    assertThatThrownBy(() -> AttrDataType.ofCode("  "))
        .isInstanceOf(BssException.class);
  }

  @Test
  void getCode_returnsCode() {
    assertThat(AttrDataType.STRING.getCode()).isEqualTo("string");
    assertThat(AttrDataType.BOOLEAN.getCode()).isEqualTo("boolean");
  }

  // ==================== convert 实例方法 ====================

  @Test
  void convert_nullValue_returnsNull() {
    assertThat(AttrDataType.STRING.convert("p", null)).isNull();
  }

  @Test
  void convert_valueAlreadyCorrectType_shortCircuits() {
    // String 值已是 String 类型，直接返回，不调用 conversionService
    assertThat(AttrDataType.STRING.convert("p", "abc")).isEqualTo("abc");
    assertThat(AttrDataType.INTEGER.convert("p", 5L)).isEqualTo(5L);
  }

  @Test
  void convert_stringFromDate_formats() {
    Object result = AttrDataType.STRING.convert("p", new Date());
    assertThat(result).isInstanceOf(String.class);
  }

  @Test
  void convert_stringFromLocalDate_formats() {
    Object result = AttrDataType.STRING.convert("p", LocalDate.now());
    assertThat(result).isInstanceOf(String.class);
  }

  @Test
  void convert_conversionServiceConverts_returnsResult() {
    when(cs.convert(123, String.class)).thenReturn("123");
    assertThat(AttrDataType.STRING.convert("p", 123)).isEqualTo("123");
  }

  @Test
  void convert_conversionServiceThrows_nonBssCause_wrapsWithPropertyPath() {
    when(cs.convert(123, String.class)).thenThrow(new RuntimeException("boom"));

    assertThatThrownBy(() -> AttrDataType.STRING.convert("attr.x", 123))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("属性值类型转换失败")
        .hasMessageContaining("attr=attr.x");
  }

  @Test
  void convert_conversionServiceThrows_bssCause_omitsPropertyPath() {
    when(cs.convert(123, String.class)).thenThrow(new RuntimeException(new BssException("inner-err")));

    assertThatThrownBy(() -> AttrDataType.STRING.convert(null, 123))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("属性值类型转换失败")
        .satisfies(e -> assertThat(((BssException) e).getMessage()).doesNotContain("attr="));
  }

  @Test
  void convert_conversionServiceLazyInit_getsBeanFromSpring() throws Exception {
    // 置空静态字段，触发懒加载
    setConversionService(null);
    try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
      ConversionService beanCs = mock(ConversionService.class);
      spring.when(() -> SpringUtil.getBean(ConversionService.class)).thenReturn(beanCs);
      when(beanCs.convert(123, String.class)).thenReturn("123");

      assertThat(AttrDataType.STRING.convert("p", 123)).isEqualTo("123");
      spring.verify(() -> SpringUtil.getBean(ConversionService.class));
    }
  }

  // ==================== 静态 convert 重载 ====================

  @Test
  void convert_staticDataTypeNull_returnsValue() {
    assertThat(AttrDataType.convert("p", (AttrDataType) null, "v")).isEqualTo("v");
  }

  @Test
  void convert_staticValueNull_returnsValue() {
    assertThat(AttrDataType.convert("p", AttrDataType.STRING, null)).isNull();
  }

  @Test
  void convert_staticDelegatesToInstance() {
    assertThat(AttrDataType.convert("p", AttrDataType.STRING, "abc")).isEqualTo("abc");
  }

  @Test
  void convert_staticStringCode_resolvesViaOfCode() {
    assertThat(AttrDataType.convert("p", "string", "abc")).isEqualTo("abc");
    // code 为空 -> ofCode 返回 null -> dataType null -> 原样返回
    assertThat(AttrDataType.convert("p", (String) null, "v")).isEqualTo("v");
  }

  // ==================== 辅助 ====================

  private static void setConversionService(ConversionService value) throws Exception {
    Field f = AttrDataType.class.getDeclaredField("conversionService");
    f.setAccessible(true);
    f.set(null, value);
  }
}
