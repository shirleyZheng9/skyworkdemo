package com.iwhalecloud.bote.doc.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 特殊的日期时间格式化， 格式化规则如下
 * <p>
 * 3分钟内，显示刚刚
 * 大于三分钟，但在一小时内，显示多少分钟前
 * 大于一小时，但在一天内，显示多少小时前
 * 昨天，显示 昨天 HH:mm
 * 前天，显示 前天 HH:mm
 * 大于三天，但在7天内，显示多少天前
 * 超过7天，但在今年内，显示 MM-dd
 * 今年以前，显示  yyyy-MM-dd
 * </p>
 *
 * @author Aiqing
 */
public final class ShortTimeFormatter {

  private static final DateTimeFormatter MM_DD_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
  private static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private ShortTimeFormatter() {
    throw new IllegalStateException();
  }

  public static String formatDate(Date date) {
    LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return formatLocalDateTime(localDateTime);
  }

  public static String formatLocalDateTime(LocalDateTime inputTime) {
    LocalDateTime now = LocalDateTime.now();

    long timeDiff = ChronoUnit.MINUTES.between(inputTime, now);

    if (timeDiff <= 3) {
      return "刚刚";
    }
    else if (timeDiff <= 60) {
      return String.format("%d分钟前", timeDiff);
    }
    // 24 * 60
    else if (timeDiff <= 1440) {
      int hours = (int) timeDiff / 60;
      return String.format("%d小时前", hours);
    }
    // 48 * 60
    else if (timeDiff <= 2880) {
      LocalDateTime yesterday = now.minusDays(1);
      return String.format("昨天 %02d:%02d", yesterday.getHour(), yesterday.getMinute());
    }
    // 7 * 24 * 60
    else if (timeDiff <= 10080) {
      return String.format("%d天前", (int) timeDiff / 1440);
    }
    else if (inputTime.getYear() == now.getYear()) {
      return inputTime.format(MM_DD_FORMATTER);
    }
    else {
      return inputTime.format(YYYY_MM_DD_FORMATTER);
    }
  }
}
