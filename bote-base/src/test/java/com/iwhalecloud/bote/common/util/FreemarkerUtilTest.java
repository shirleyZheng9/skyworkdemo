package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@link FreemarkerUtil} 单元测试
 *
 * <p>覆盖 process 正常模板渲染、空模板校验抛异常、非法模板语法抛 BssException。
 * 私有方法 buildFreemarkerConfiguration 通过静态初始化器间接覆盖。</p>
 */
class FreemarkerUtilTest {

  @Test
  void process_validTemplateWithVariables_returnsRenderedString() {
    String template = "Hello, ${name}! You are ${age} years old.";
    Map<String, Object> variables = Map.of("name", "Alice", "age", 30);

    String result = FreemarkerUtil.process(template, variables);

    assertThat(result).isEqualTo("Hello, Alice! You are 30 years old.");
  }

  @Test
  void process_validTemplateNoVariables_returnsLiteral() {
    String template = "Hello, World!";

    String result = FreemarkerUtil.process(template, null);

    assertThat(result).isEqualTo("Hello, World!");
  }

  @Test
  void process_emptyTemplate_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> FreemarkerUtil.process("", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void process_nullTemplate_throwsIllegalArgumentException() {
    assertThatThrownBy(() -> FreemarkerUtil.process(null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void process_invalidTemplateSyntax_throwsBssException() {
    String badTemplate = "Hello, ${name!";  // 未闭合的标签

    assertThatThrownBy(() -> FreemarkerUtil.process(badTemplate, Map.of("name", "Alice")))
        .isInstanceOf(BssException.class);
  }

  @Test
  void process_missingVariable_throwsBssException() {
    String template = "Hello, ${missing}";

    assertThatThrownBy(() -> FreemarkerUtil.process(template, Map.of()))
        .isInstanceOf(BssException.class);
  }

  @Test
  void process_numberFormat_noFormatting() {
    // Configuration 设置了 numberFormat="#", 所以数字不会被千分位格式化
    String template = "Value: ${value}";
    Map<String, Object> variables = Map.of("value", 1000000);

    String result = FreemarkerUtil.process(template, variables);

    assertThat(result).isEqualTo("Value: 1000000");
  }
}
