package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.dto.job.cron.JobCronParameterDTO;
import com.iwhalecloud.bote.dto.job.cron.JobCronParameterDTO.TimePayload;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.util.Assert;

/**
 * {@link JobCronParameterDTO} 与 Quartz 6 段 cron 表达式的双向转换工具。
 *
 * @author lizuyin
 * @since 2026-04-01
 */
public final class CronParameterConverter {

  /** ISO weekday (1=Mon..7=Sun) → Quartz weekday (SUN=1..SAT=7) */
  private static final int[] ISO_TO_QUARTZ = {0, 2, 3, 4, 5, 6, 7, 1};
  /** Quartz weekday (1=SUN..7=SAT) → ISO weekday (1=Mon..7=Sun) */
  private static final int[] QUARTZ_TO_ISO = {0, 7, 1, 2, 3, 4, 5, 6};

  private static final String TYPE_DAILY = "DAILY";
  private static final String TYPE_WEEKLY = "WEEKLY";
  private static final String TYPE_MONTHLY = "MONTHLY";
  private static final String TYPE_WORKDAY = "WORKDAY";
  private static final String TYPE_ONCE = "ONCE";
  private static final String TYPE_CRON = "CRON";

  private static final int SCHEMA_VERSION = 1;

  private CronParameterConverter() {
  }

  // ==================== toCron ====================

  /**
   * 将结构化调度参数转为 6 段 Quartz cron 表达式。
   */
  public static String toCron(JobCronParameterDTO bean) {
    Assert.notNull(bean, "JobCronParameterBean 不能为空");
    Assert.hasText(bean.getType(), "调度类型不能为空");

    return switch (bean.getType()) {
      case TYPE_DAILY -> dailyToCron(bean);
      case TYPE_WEEKLY -> weeklyToCron(bean);
      case TYPE_MONTHLY -> monthlyToCron(bean);
      case TYPE_WORKDAY -> workdayToCron(bean);
      case TYPE_ONCE -> onceToCron(bean);
      case TYPE_CRON -> {
        Assert.hasText(bean.getCronExpression(), "CRON 类型必须提供 cronExpression");
        yield bean.getCronExpression().trim();
      }
      default -> throw new IllegalArgumentException("不支持的调度类型: " + bean.getType());
    };
  }

  private static String dailyToCron(JobCronParameterDTO bean) {
    TimePayload t = requireTime(bean);
    return String.format("0 %d %d * * ?", t.getMinute(), t.getHour());
  }

  private static String weeklyToCron(JobCronParameterDTO bean) {
    TimePayload t = requireTime(bean);
    List<Integer> weekdays = bean.getWeekdays();
    Assert.notEmpty(weekdays, "WEEKLY 类型必须提供 weekdays");
    Assert.isTrue(weekdays.stream().allMatch(d -> d >= 1 && d <= 7), "weekdays 必须在 1(周一) ~ 7(周日) 之间");
    String dow = weekdays.stream().map(iso -> String.valueOf(ISO_TO_QUARTZ[iso])).collect(Collectors.joining(","));
    return String.format("0 %d %d ? * %s", t.getMinute(), t.getHour(), dow);
  }

  private static String monthlyToCron(JobCronParameterDTO bean) {
    TimePayload t = requireTime(bean);
    List<Integer> days = bean.getDaysOfMonth();
    Assert.notEmpty(days, "MONTHLY 类型必须提供 daysOfMonth");
    Assert.isTrue(days.stream().allMatch(d -> d >= 1 && d <= 31), "daysOfMonth 必须在 1 ~ 31 之间");
    String dom = days.stream().distinct().sorted().map(String::valueOf).collect(Collectors.joining(","));
    return String.format("0 %d %d %s * ?", t.getMinute(), t.getHour(), dom);
  }

  private static String workdayToCron(JobCronParameterDTO bean) {
    TimePayload t = requireTime(bean);
    return String.format("0 %d %d ? * MON-FRI", t.getMinute(), t.getHour());
  }

  private static String onceToCron(JobCronParameterDTO bean) {
    TimePayload t = requireTime(bean);
    Assert.hasText(bean.getDate(), "ONCE 类型必须提供 date");
    LocalDate d = LocalDate.parse(bean.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
    int year = d.getYear();
    return String.format("0 %d %d %d %d ? %d", t.getMinute(), t.getHour(), d.getDayOfMonth(), d.getMonthValue(),
      year);
  }

  private static TimePayload requireTime(JobCronParameterDTO bean) {
    Assert.notNull(bean.getTime(), "必须提供 time（hour, minute）");
    return bean.getTime();
  }

  // ==================== fromCron ====================

  /**
   * 匹配 daily: 0 M H * * ?
   */
  private static final Pattern PAT_DAILY = Pattern.compile("0 (\\d+) (\\d+) \\* \\* \\?");

  /**
   * 匹配 workday: 0 M H ? * MON-FRI
   */
  private static final Pattern PAT_WORKDAY = Pattern.compile("0 (\\d+) (\\d+) \\? \\* MON-FRI");

  /**
   * 匹配 weekly: 0 M H ? * dow,dow... (数字形式)
   */
  private static final Pattern PAT_WEEKLY = Pattern.compile("0 (\\d+) (\\d+) \\? \\* ([1-7](?:,[1-7])*)");

  /**
   * 匹配 monthly: 0 M H D * ? 或 0 M H D,D,... * ?
   */
  private static final Pattern PAT_MONTHLY = Pattern.compile("0 (\\d+) (\\d+) ((?:\\d+)(?:,\\d+)*) \\* \\?");

  /**
   * 匹配 once: 0 M H D Mon ? YYYY（仅单年份；年份范围如 YYYY-YYYY 属于多次执行，归为 CRON）
   */
  private static final Pattern PAT_ONCE = Pattern.compile("0 (\\d+) (\\d+) (\\d+) (\\d+) \\? (\\d{4})");

  /**
   * 尝试将 6 段 Quartz cron 表达式解析为结构化调度参数。 无法匹配时归为 CRON 类型。
   */
  public static JobCronParameterDTO fromCron(String cron) {
    Assert.hasText(cron, "cron 表达式不能为空");
    String c = cron.trim();
    JobCronParameterDTO bean = new JobCronParameterDTO();
    bean.setSchemaVersion(SCHEMA_VERSION);

    Matcher m;

    // 先匹配 ONCE（含年份），再匹配 6 段类型
    m = PAT_ONCE.matcher(c);
    if (m.matches()) {
      bean.setType(TYPE_ONCE);
      bean.setTime(time(m.group(2), m.group(1)));
      int year = Integer.parseInt(m.group(5));
      int month = Integer.parseInt(m.group(4));
      int day = Integer.parseInt(m.group(3));
      bean.setDate(LocalDate.of(year, month, day).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    else {
      // 顺序：先匹配特殊的 WORKDAY，再匹配 WEEKLY/MONTHLY/DAILY，最后兜底 CRON
      m = PAT_WORKDAY.matcher(c);
      if (m.matches()) {
        bean.setType(TYPE_WORKDAY);
        bean.setTime(time(m.group(2), m.group(1)));
      }
      else {
        m = PAT_WEEKLY.matcher(c);
        if (m.matches()) {
          bean.setType(TYPE_WEEKLY);
          bean.setTime(time(m.group(2), m.group(1)));
          bean.setWeekdays(Arrays.stream(m.group(3).split(",")).map(s -> QUARTZ_TO_ISO[Integer.parseInt(s)]).sorted()
            .collect(Collectors.toList()));
        }
        else {
          m = PAT_MONTHLY.matcher(c);
          if (m.matches()) {
            bean.setType(TYPE_MONTHLY);
            bean.setTime(time(m.group(2), m.group(1)));
            bean.setDaysOfMonth(
              Arrays.stream(m.group(3).split(",")).map(Integer::parseInt).sorted().collect(Collectors.toList()));
          }
          else {
            m = PAT_DAILY.matcher(c);
            if (m.matches()) {
              bean.setType(TYPE_DAILY);
              bean.setTime(time(m.group(2), m.group(1)));
            }
            else {
              bean.setType(TYPE_CRON);
              bean.setCronExpression(c);
            }
          }
        }
      }
    }
    return bean;
  }

  private static TimePayload time(String hour, String minute) {
    TimePayload t = new TimePayload();
    t.setHour(Integer.parseInt(hour));
    t.setMinute(Integer.parseInt(minute));
    return t;
  }
}
