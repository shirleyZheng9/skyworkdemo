package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link SkillCodeUtils} 单元测试
 *
 * <p>覆盖 toSafeId（null、特殊字符替换、合法输入保持不变）和
 * nameToSlug（特殊字符替换、连续分隔符折叠、首尾去除、小写转换、中文保留）。</p>
 */
class SkillCodeUtilsTest {

  // ==================== toSafeId ====================

  @Test
  void toSafeId_null_returnsEmpty() {
    assertThat(SkillCodeUtils.toSafeId(null)).isEmpty();
  }

  @Test
  void toSafeId_specialChars_replacedWithUnderscore() {
    assertThat(SkillCodeUtils.toSafeId("hello world!@#")).isEqualTo("hello_world___");
  }

  @Test
  void toSafeId_safeChars_unchanged() {
    assertThat(SkillCodeUtils.toSafeId("abc.123-456_789")).isEqualTo("abc.123-456_789");
  }

  @Test
  void toSafeId_chineseChars_replacedWithUnderscore() {
    assertThat(SkillCodeUtils.toSafeId("技能code")).isEqualTo("__code");
  }

  @Test
  void toSafeId_emptyString_returnsEmpty() {
    assertThat(SkillCodeUtils.toSafeId("")).isEmpty();
  }

  // ==================== nameToSlug ====================

  @Test
  void nameToSlug_specialChars_replacedAndFolded() {
    assertThat(SkillCodeUtils.nameToSlug("hello! world@#")).isEqualTo("hello-world");
  }

  @Test
  void nameToSlug_consecutiveSeparators_folded() {
    assertThat(SkillCodeUtils.nameToSlug("a---b")).isEqualTo("a-b");
  }

  @Test
  void nameToSlug_leadingTrailingSeparators_trimmed() {
    assertThat(SkillCodeUtils.nameToSlug("---abc---")).isEqualTo("abc");
  }

  @Test
  void nameToSlug_uppercase_convertedToLowercase() {
    assertThat(SkillCodeUtils.nameToSlug("HelloWorld")).isEqualTo("helloworld");
  }

  @Test
  void nameToSlug_chineseChars_preserved() {
    assertThat(SkillCodeUtils.nameToSlug("技能 abc")).isEqualTo("技能-abc");
  }

  @Test
  void nameToSlug_mixedChars_correctSlug() {
    assertThat(SkillCodeUtils.nameToSlug("My Skill #1!")).isEqualTo("my-skill-1");
  }

  @Test
  void nameToSlug_dotsAndUnderscores_preserved() {
    assertThat(SkillCodeUtils.nameToSlug("v1.2_test")).isEqualTo("v1.2_test");
  }
}
