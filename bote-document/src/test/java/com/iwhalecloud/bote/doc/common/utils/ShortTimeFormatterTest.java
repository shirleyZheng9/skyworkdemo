package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.junit.jupiter.api.Test;

/**
 * {@link ShortTimeFormatter} 单元测试。
 *
 * <p>用 now-相对时间输入覆盖各时间差分支；"昨天" 分支因依赖方法内部 now() 的时分，仅校验前缀与
 * HH:mm 格式以避开分钟边界抖动。各用例时间差均远离分支边界，确保连跑稳定。</p>
 */
class ShortTimeFormatterTest {

  private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");
  private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Test
  void format_withinThreeMinutes_returns刚刚() {
    assertThat(ShortTimeFormatter.formatLocalDateTime(LocalDateTime.now())).isEqualTo("刚刚");
  }

  @Test
  void format_withinOneHour_returnsMinutesAgo() {
    LocalDateTime input = LocalDateTime.now().minusMinutes(5);
    assertThat(ShortTimeFormatter.formatLocalDateTime(input)).isEqualTo("5分钟前");
  }

  @Test
  void format_withinOneDay_returnsHoursAgo() {
    LocalDateTime input = LocalDateTime.now().minusMinutes(120);
    assertThat(ShortTimeFormatter.formatLocalDateTime(input)).isEqualTo("2小时前");
  }

  @Test
  void format_yesterday_returnsYesterdayTime() {
    // 25 小时前命中"昨天"分支；方法返回 now.minusDays(1) 的 HH:mm，此处仅校验前缀与两位时分格式
    LocalDateTime input = LocalDateTime.now().minusMinutes(1500);
    assertThat(ShortTimeFormatter.formatLocalDateTime(input))
      .startsWith("昨天 ")
      .matches("昨天 \\d{2}:\\d{2}");
  }

  @Test
  void format_withinSevenDays_returnsDaysAgo() {
    LocalDateTime input = LocalDateTime.now().minusMinutes(3000);
    assertThat(ShortTimeFormatter.formatLocalDateTime(input)).isEqualTo("2天前");
  }

  @Test
  void format_overSevenDaysSameYear_returnsMMdd() {
    LocalDateTime input = LocalDateTime.now().minusDays(8);
    // 8 天前绝大多数日期与本年同月；跨年时方法走 yyyy-MM-dd 分支，自适应断言保持确定性
    String expected = input.getYear() == LocalDateTime.now().getYear()
      ? input.format(MM_DD) : input.format(YYYY_MM_DD);
    assertThat(ShortTimeFormatter.formatLocalDateTime(input)).isEqualTo(expected);
  }

  @Test
  void format_previousYear_returnsYyyyMMdd() {
    LocalDateTime input = LocalDateTime.now().minusYears(1);
    assertThat(ShortTimeFormatter.formatLocalDateTime(input)).isEqualTo(input.format(YYYY_MM_DD));
  }

  @Test
  void formatDate_delegatesToFormatLocalDateTime() {
    LocalDateTime input = LocalDateTime.now().minusMinutes(5);
    Date date = Date.from(input.atZone(ZoneId.systemDefault()).toInstant());
    assertThat(ShortTimeFormatter.formatDate(date)).isEqualTo("5分钟前");
  }
}
