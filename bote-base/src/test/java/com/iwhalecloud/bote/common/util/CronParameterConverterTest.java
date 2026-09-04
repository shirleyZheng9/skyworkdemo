package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iwhalecloud.bote.dto.job.cron.JobCronParameterDTO;
import com.iwhalecloud.bote.dto.job.cron.JobCronParameterDTO.TimePayload;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link CronParameterConverter} 单元测试
 *
 * <p>覆盖 toCron 全部调度类型（DAILY/WEEKLY/MONTHLY/WORKDAY/ONCE/CRON）及其校验异常，
 * fromCron 全部模式匹配分支及 CRON 兜底，以及 toCron -> fromCron 往返一致性。</p>
 */
class CronParameterConverterTest {

  // ==================== 辅助方法 ====================

  private static TimePayload time(int hour, int minute) {
    TimePayload t = new TimePayload();
    t.setHour(hour);
    t.setMinute(minute);
    return t;
  }

  private static JobCronParameterDTO bean(String type) {
    JobCronParameterDTO bean = new JobCronParameterDTO();
    bean.setType(type);
    return bean;
  }

  // ==================== toCron ====================

  @Test
  void toCron_daily_returnsCorrectCron() {
    JobCronParameterDTO b = bean("DAILY");
    b.setTime(time(14, 30));
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 30 14 * * ?");
  }

  @Test
  void toCron_weekly_returnsCorrectCron() {
    JobCronParameterDTO b = bean("WEEKLY");
    b.setTime(time(14, 30));
    b.setWeekdays(List.of(1, 3, 5)); // Mon, Wed, Fri (ISO)
    // ISO 1->Quartz 2, 3->4, 5->6
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 30 14 ? * 2,4,6");
  }

  @Test
  void toCron_weeklySingleDay_returnsCorrectCron() {
    JobCronParameterDTO b = bean("WEEKLY");
    b.setTime(time(9, 0));
    b.setWeekdays(List.of(7)); // Sunday (ISO) -> Quartz 1
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 0 9 ? * 1");
  }

  @Test
  void toCron_monthly_returnsCorrectCron() {
    JobCronParameterDTO b = bean("MONTHLY");
    b.setTime(time(14, 30));
    b.setDaysOfMonth(List.of(15, 10, 1)); // 会被排序去重
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 30 14 1,10,15 * ?");
  }

  @Test
  void toCron_workday_returnsCorrectCron() {
    JobCronParameterDTO b = bean("WORKDAY");
    b.setTime(time(9, 0));
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 0 9 ? * MON-FRI");
  }

  @Test
  void toCron_once_returnsCorrectCron() {
    JobCronParameterDTO b = bean("ONCE");
    b.setTime(time(14, 30));
    b.setDate("2026-07-14");
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 30 14 14 7 ? 2026");
  }

  @Test
  void toCron_cronType_returnsTrimmedExpression() {
    JobCronParameterDTO b = bean("CRON");
    b.setCronExpression("  0 0 12 * * ?  ");
    assertThat(CronParameterConverter.toCron(b)).isEqualTo("0 0 12 * * ?");
  }

  @Test
  void toCron_nullBean_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.toCron(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("不能为空");
  }

  @Test
  void toCron_emptyType_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.toCron(bean("")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toCron_nullType_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.toCron(bean(null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toCron_unsupportedType_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.toCron(bean("HOURLY")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("不支持的调度类型");
  }

  @Test
  void toCron_dailyWithoutTime_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.toCron(bean("DAILY")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("time");
  }

  @Test
  void toCron_weeklyWithoutWeekdays_throwsIllegalArgument() {
    JobCronParameterDTO b = bean("WEEKLY");
    b.setTime(time(9, 0));
    assertThatThrownBy(() -> CronParameterConverter.toCron(b))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("weekdays");
  }

  @Test
  void toCron_weeklyInvalidWeekday_throwsIllegalArgument() {
    JobCronParameterDTO b = bean("WEEKLY");
    b.setTime(time(9, 0));
    b.setWeekdays(List.of(0));
    assertThatThrownBy(() -> CronParameterConverter.toCron(b))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toCron_monthlyWithoutDays_throwsIllegalArgument() {
    JobCronParameterDTO b = bean("MONTHLY");
    b.setTime(time(9, 0));
    assertThatThrownBy(() -> CronParameterConverter.toCron(b))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("daysOfMonth");
  }

  @Test
  void toCron_monthlyInvalidDay_throwsIllegalArgument() {
    JobCronParameterDTO b = bean("MONTHLY");
    b.setTime(time(9, 0));
    b.setDaysOfMonth(List.of(32));
    assertThatThrownBy(() -> CronParameterConverter.toCron(b))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toCron_onceWithoutDate_throwsIllegalArgument() {
    JobCronParameterDTO b = bean("ONCE");
    b.setTime(time(9, 0));
    assertThatThrownBy(() -> CronParameterConverter.toCron(b))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("date");
  }

  @Test
  void toCron_cronWithoutExpression_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.toCron(bean("CRON")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cronExpression");
  }

  // ==================== fromCron ====================

  @Test
  void fromCron_dailyPattern_returnsDailyBean() {
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 30 14 * * ?");
    assertThat(b.getType()).isEqualTo("DAILY");
    assertThat(b.getTime().getHour()).isEqualTo(14);
    assertThat(b.getTime().getMinute()).isEqualTo(30);
  }

  @Test
  void fromCron_workdayPattern_returnsWorkdayBean() {
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 0 9 ? * MON-FRI");
    assertThat(b.getType()).isEqualTo("WORKDAY");
    assertThat(b.getTime().getHour()).isEqualTo(9);
  }

  @Test
  void fromCron_weeklyPattern_returnsWeeklyBean() {
    // Quartz 2,4,6 -> ISO 1,3,5
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 30 14 ? * 2,4,6");
    assertThat(b.getType()).isEqualTo("WEEKLY");
    assertThat(b.getWeekdays()).containsExactly(1, 3, 5);
  }

  @Test
  void fromCron_monthlyPattern_returnsMonthlyBean() {
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 30 14 1,15 * ?");
    assertThat(b.getType()).isEqualTo("MONTHLY");
    assertThat(b.getDaysOfMonth()).containsExactly(1, 15);
  }

  @Test
  void fromCron_oncePattern_returnsOnceBean() {
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 30 14 14 7 ? 2026");
    assertThat(b.getType()).isEqualTo("ONCE");
    assertThat(b.getDate()).isEqualTo("2026-07-14");
    assertThat(b.getTime().getHour()).isEqualTo(14);
  }

  @Test
  void fromCron_unknownPattern_returnsCronBean() {
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 0 12 1W * ?");
    assertThat(b.getType()).isEqualTo("CRON");
    assertThat(b.getCronExpression()).isEqualTo("0 0 12 1W * ?");
  }

  @Test
  void fromCron_emptyCron_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.fromCron(""))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fromCron_nullCron_throwsIllegalArgument() {
    assertThatThrownBy(() -> CronParameterConverter.fromCron(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fromCron_setsSchemaVersion() {
    JobCronParameterDTO b = CronParameterConverter.fromCron("0 30 14 * * ?");
    assertThat(b.getSchemaVersion()).isEqualTo(1);
  }

  // ==================== 往返测试 ====================

  @Test
  void roundTrip_daily() {
    JobCronParameterDTO original = bean("DAILY");
    original.setTime(time(14, 30));
    String cron = CronParameterConverter.toCron(original);
    JobCronParameterDTO parsed = CronParameterConverter.fromCron(cron);
    assertThat(parsed.getType()).isEqualTo("DAILY");
    assertThat(parsed.getTime().getHour()).isEqualTo(14);
    assertThat(parsed.getTime().getMinute()).isEqualTo(30);
  }

  @Test
  void roundTrip_weekly() {
    JobCronParameterDTO original = bean("WEEKLY");
    original.setTime(time(14, 30));
    original.setWeekdays(List.of(1, 3, 5));
    String cron = CronParameterConverter.toCron(original);
    JobCronParameterDTO parsed = CronParameterConverter.fromCron(cron);
    assertThat(parsed.getType()).isEqualTo("WEEKLY");
    assertThat(parsed.getWeekdays()).containsExactly(1, 3, 5);
  }

  @Test
  void roundTrip_monthly() {
    JobCronParameterDTO original = bean("MONTHLY");
    original.setTime(time(14, 30));
    original.setDaysOfMonth(List.of(1, 15));
    String cron = CronParameterConverter.toCron(original);
    JobCronParameterDTO parsed = CronParameterConverter.fromCron(cron);
    assertThat(parsed.getType()).isEqualTo("MONTHLY");
    assertThat(parsed.getDaysOfMonth()).containsExactly(1, 15);
  }

  @Test
  void roundTrip_workday() {
    JobCronParameterDTO original = bean("WORKDAY");
    original.setTime(time(9, 0));
    String cron = CronParameterConverter.toCron(original);
    JobCronParameterDTO parsed = CronParameterConverter.fromCron(cron);
    assertThat(parsed.getType()).isEqualTo("WORKDAY");
    assertThat(parsed.getTime().getHour()).isEqualTo(9);
  }

  @Test
  void roundTrip_once() {
    JobCronParameterDTO original = bean("ONCE");
    original.setTime(time(14, 30));
    original.setDate("2026-01-15");
    String cron = CronParameterConverter.toCron(original);
    JobCronParameterDTO parsed = CronParameterConverter.fromCron(cron);
    assertThat(parsed.getType()).isEqualTo("ONCE");
    assertThat(parsed.getDate()).isEqualTo("2026-01-15");
  }
}
