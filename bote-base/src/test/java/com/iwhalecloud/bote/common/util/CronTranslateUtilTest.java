package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link CronTranslateUtil} 单元测试
 *
 * <p>覆盖 toChineseDescription 的空值返回空串、合法 cron 转中文描述、
 * 非法 cron 返回原始字符串分支。基于 cronutils 库实现，不需要 Spring 环境。</p>
 */
class CronTranslateUtilTest {

  @Test
  void toChineseDescription_blankCron_returnsEmpty() {
    assertThat(CronTranslateUtil.toChineseDescription("")).isEmpty();
    assertThat(CronTranslateUtil.toChineseDescription(null)).isEmpty();
    assertThat(CronTranslateUtil.toChineseDescription("   ")).isEmpty();
  }

  @Test
  void toChineseDescription_validDailyCron_returnsChineseDescription() {
    // 每天 14:30 执行
    String desc = CronTranslateUtil.toChineseDescription("0 30 14 * * ?");
    assertThat(desc).isNotEmpty();
    assertThat(desc).contains("14");
    assertThat(desc).contains("30");
  }

  @Test
  void toChineseDescription_validEveryMinuteCron_returnsChineseDescription() {
    String desc = CronTranslateUtil.toChineseDescription("0 * * * * ?");
    assertThat(desc).isNotEmpty();
  }

  @Test
  void toChineseDescription_validWorkdayCron_returnsChineseDescription() {
    // 工作日 09:00
    String desc = CronTranslateUtil.toChineseDescription("0 0 9 ? * MON-FRI");
    assertThat(desc).isNotEmpty();
  }

  @Test
  void toChineseDescription_invalidCron_returnsOriginalCron() {
    String invalidCron = "not a cron";
    String desc = CronTranslateUtil.toChineseDescription(invalidCron);
    assertThat(desc).isEqualTo(invalidCron);
  }

  @Test
  void toChineseDescription_cronWithExtraSpaces_trimmedBeforeParsing() {
    // 带前后空格的合法 cron 应能正常解析
    String desc = CronTranslateUtil.toChineseDescription("  0 30 14 * * ?  ");
    assertThat(desc).isNotEmpty();
    assertThat(desc).isNotEqualTo("  0 30 14 * * ?  ");
  }
}
