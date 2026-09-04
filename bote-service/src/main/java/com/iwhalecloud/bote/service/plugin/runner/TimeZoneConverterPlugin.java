package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.TimeZoneConverterPluginParams;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 时区转换工具插件
 *
 * @author lizuyin
 * @since 2025-11-18
 */
@Component
public class TimeZoneConverterPlugin extends AbstractPlugin<TimeZoneConverterPluginParams> {

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public TimeZoneConverterPlugin() {
    super(TimeZoneConverterPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_TIME_ZONE_CONVERTER;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fromTimezone",
      "原始时区（可选，字符串类型），格式为UTC、UTC+1、UTC-1、UTC+08:00等，不填则使用当前环境的默认时区", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("time",
      "需要转换的时间（可选，字符串类型），不填则默认使用fromTimezone代表时区的当前时间，只支持yyyy-MM-dd HH:mm:ss",
      AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("toTimezone",
      "目标时区（可选，字符串类型），格式为UTC、UTC+1、UTC-1、UTC+08:00等，不填则使用当前环境的默认时区", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(
      ParameterSpec.newProperty("result", "转换后的时间字符串（yyyy-MM-dd HH:mm:ss格式）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(TimeZoneConverterPluginParams params) {
    // fromTimezone 是可选参数，如果提供了则进行格式校验
    if (params.getFromTimezone() != null && StringUtils.isNotBlank(params.getFromTimezone())) {
      String fromTimezoneStr = params.getFromTimezone().trim();
      ZoneId parsedFromZoneId = parseTimeZone(fromTimezoneStr);
      if (parsedFromZoneId == null) {
        throw new IllegalArgumentException(
          "参数 fromTimezone 格式错误，支持的格式为：UTC、UTC+1、UTC-1、UTC+08、UTC+08:00、UTC-05:30等，或标准时区名称如Asia/Shanghai、America/New_York等");
      }
    }

    // time 是可选参数，如果提供了则进行格式校验
    if (params.getTime() != null && StringUtils.isNotBlank(params.getTime())) {
      String timeStr = params.getTime().trim();
      try {
        // 解析时间字符串以验证格式，解析成功则格式正确
        @SuppressWarnings("unused")
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr, TIME_FORMATTER);
      }
      catch (DateTimeParseException e) {
        throw new IllegalArgumentException("参数 time 格式错误，只支持 yyyy-MM-dd HH:mm:ss 格式: " + e.getMessage(), e);
      }
    }

    // toTimezone 是可选参数，如果提供了则进行格式校验
    if (params.getToTimezone() != null && StringUtils.isNotBlank(params.getToTimezone())) {
      String toTimezoneStr = params.getToTimezone().trim();
      ZoneId parsedToZoneId = parseTimeZone(toTimezoneStr);
      if (parsedToZoneId == null) {
        throw new IllegalArgumentException(
          "参数 toTimezone 格式错误，支持的格式为：UTC、UTC+1、UTC-1、UTC+08、UTC+08:00、UTC-05:30等，或标准时区名称如Asia/Shanghai、America/New_York等");
      }
    }
  }

  @Override
  public Object doRun(TimeZoneConverterPluginParams pluginParams) {
    try {
      // 步骤1：解析源时区
      ZoneId fromZoneId = resolveFromZoneId(pluginParams.getFromTimezone());

      // 步骤2：解析目标时区
      ZoneId toZoneId = resolveToZoneId(pluginParams.getToTimezone());

      // 步骤3：构建源时区的ZonedDateTime
      Map<String, Object> zonedDateTimeResult = buildZonedDateTime(pluginParams.getTime(), fromZoneId);
      if (zonedDateTimeResult.containsKey("success") && !Boolean.TRUE.equals(zonedDateTimeResult.get("success"))) {
        return zonedDateTimeResult;
      }
      ZonedDateTime zonedDateTime = (ZonedDateTime) zonedDateTimeResult.get("zonedDateTime");

      // 步骤4：转换到目标时区
      ZonedDateTime targetZonedDateTime = convertTimeZone(zonedDateTime, toZoneId);

      // 步骤5：格式化结果
      return formatResult(targetZonedDateTime);

    }
    catch (IllegalArgumentException e) {
      // 参数格式错误，返回错误结果
      return createErrorResult(e.getMessage());
    }
    catch (Exception e) {
      logger.error("时区时间转换时发生异常", e);
      return createErrorResult("时区时间转换时发生异常: " + e.getMessage());
    }
  }

  /**
   * 解析源时区
   *
   * @param fromTimezone 源时区字符串，可为空
   * @return ZoneId对象，解析失败抛出IllegalArgumentException
   */
  private ZoneId resolveFromZoneId(String fromTimezone) {
    if (fromTimezone == null || StringUtils.isBlank(fromTimezone)) {
      return ZoneId.systemDefault();
    }
    String fromTimezoneStr = fromTimezone.trim();
    ZoneId parsedFromZoneId = parseTimeZone(fromTimezoneStr);
    if (parsedFromZoneId == null) {
      throw new IllegalArgumentException(
        "参数 fromTimezone 格式错误，支持的格式为：UTC、UTC+1、UTC-1、UTC+08、UTC+08:00、UTC-05:30等，或标准时区名称如Asia/Shanghai、America/New_York等");
    }
    return parsedFromZoneId;
  }

  /**
   * 解析目标时区
   *
   * @param toTimezone 目标时区字符串，可为空
   * @return ZoneId对象，解析失败抛出IllegalArgumentException
   */
  private ZoneId resolveToZoneId(String toTimezone) {
    if (toTimezone == null || StringUtils.isBlank(toTimezone)) {
      return ZoneId.systemDefault();
    }
    String toTimezoneStr = toTimezone.trim();
    ZoneId parsedToZoneId = parseTimeZone(toTimezoneStr);
    if (parsedToZoneId == null) {
      throw new IllegalArgumentException(
        "参数 toTimezone 格式错误，支持的格式为：UTC、UTC+1、UTC-1、UTC+08、UTC+08:00、UTC-05:30等，或标准时区名称如Asia/Shanghai、America/New_York等");
    }
    return parsedToZoneId;
  }

  /**
   * 构建ZonedDateTime对象
   *
   * @param time 时间字符串，可为空
   * @param fromZoneId 源时区
   * @return 包含success和zonedDateTime的Map，如果失败success为false
   */
  private Map<String, Object> buildZonedDateTime(String time, ZoneId fromZoneId) {
    Map<String, Object> result = new HashMap<>();
    if (time == null || StringUtils.isBlank(time)) {
      // time为空，使用fromTimezone时区的当前时间
      ZonedDateTime zonedDateTime = ZonedDateTime.now(fromZoneId);
      result.put("success", true);
      result.put("zonedDateTime", zonedDateTime);
      return result;
    }

    // 解析时间字符串
    String timeStr = time.trim();
    try {
      LocalDateTime localDateTime = LocalDateTime.parse(timeStr, TIME_FORMATTER);
      // 将LocalDateTime与源时区结合，创建ZonedDateTime
      ZonedDateTime zonedDateTime = localDateTime.atZone(fromZoneId);
      result.put("success", true);
      result.put("zonedDateTime", zonedDateTime);
      return result;
    }
    catch (DateTimeParseException e) {
      return createErrorResult("参数 time 格式错误，只支持 yyyy-MM-dd HH:mm:ss 格式: " + e.getMessage());
    }
    catch (Exception e) {
      return createErrorResult("解析时间时发生异常: " + e.getMessage());
    }
  }

  /**
   * 转换时区
   *
   * @param zonedDateTime 源时区的ZonedDateTime
   * @param toZoneId 目标时区
   * @return 转换后的ZonedDateTime
   */
  private ZonedDateTime convertTimeZone(ZonedDateTime zonedDateTime, ZoneId toZoneId) {
    return zonedDateTime.withZoneSameInstant(toZoneId);
  }

  /**
   * 格式化结果为字符串
   *
   * @param zonedDateTime 要格式化的ZonedDateTime
   * @return 格式化后的结果Map，如果失败返回错误结果
   */
  private Map<String, Object> formatResult(ZonedDateTime zonedDateTime) {
    try {
      String result = zonedDateTime.format(TIME_FORMATTER);
      return createSuccessResult(result);
    }
    catch (Exception e) {
      return createErrorResult("格式化时间时发生异常: " + e.getMessage());
    }
  }

  /**
   * 解析时区字符串，转换为ZoneId
   *
   * @param timeZoneStr 时区字符串，如UTC、UTC+1、UTC-1、UTC+08:00等
   * @return ZoneId对象，解析失败返回null
   */
  private ZoneId parseTimeZone(String timeZoneStr) {
    try {
      // 转换为大写，便于处理
      String upperStr = timeZoneStr.toUpperCase();
      // 依次尝试不同的解析方式
      ZoneId zoneId = parseUtcZero(upperStr);
      if (zoneId != null) {
        return zoneId;
      }
      zoneId = parseUtcOffsetHoursMinutes(upperStr);
      if (zoneId != null) {
        return zoneId;
      }
      zoneId = parseUtcOffsetHours(upperStr);
      if (zoneId != null) {
        return zoneId;
      }
      // 最后尝试解析标准时区名称
      return parseStandardTimeZone(timeZoneStr);
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * 解析UTC+/-N格式（如UTC+1、UTC-5）
   *
   * @param upperStr 大写的时区字符串
   * @return ZoneId对象，如果格式不匹配或无效则返回null
   */
  private ZoneId parseUtcOffsetHours(String upperStr) {
    Pattern pattern = Pattern.compile("^UTC([+-])(\\d+)$");
    java.util.regex.Matcher matcher = pattern.matcher(upperStr);
    if (matcher.matches()) {
      String sign = matcher.group(1);
      int hours = Integer.parseInt(matcher.group(2));
      if (hours >= 0 && hours <= 23) {
        String offsetStr = String.format("UTC%s%02d:00", sign, hours);
        return ZoneId.of(offsetStr);
      }
    }
    return null;
  }

  /**
   * 解析UTC零时区（UTC、UTC+0、UTC-0、UTC+00:00、UTC-00:00）
   *
   * @param upperStr 大写的时区字符串
   * @return ZoneId对象，如果不是UTC零时区则返回null
   */
  private ZoneId parseUtcZero(String upperStr) {
    if ("UTC".equals(upperStr) || "UTC+0".equals(upperStr) || "UTC-0".equals(upperStr) || "UTC+00:00".equals(upperStr)
      || "UTC-00:00".equals(upperStr)) {
      return ZoneId.of("UTC");
    }
    return null;
  }

  /**
   * 解析标准时区名称（如Asia/Shanghai、America/New_York等）
   *
   * @param timeZoneStr 时区字符串
   * @return ZoneId对象，如果解析失败则返回null
   */
  private ZoneId parseStandardTimeZone(String timeZoneStr) {
    try {
      return ZoneId.of(timeZoneStr);
    }
    catch (Exception e) {
      // 如果直接解析失败，返回null
      return null;
    }
  }

  /**
   * 解析UTC+/-N:MM格式（如UTC+08:00、UTC-05:30）
   *
   * @param upperStr 大写的时区字符串
   * @return ZoneId对象，如果格式不匹配或无效则返回null
   */
  private ZoneId parseUtcOffsetHoursMinutes(String upperStr) {
    Pattern pattern = Pattern.compile("^UTC([+-])(\\d{1,2}):(\\d{2})$");
    java.util.regex.Matcher matcher = pattern.matcher(upperStr);
    if (matcher.matches()) {
      String sign = matcher.group(1);
      int hours = Integer.parseInt(matcher.group(2));
      int minutes = Integer.parseInt(matcher.group(3));
      if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
        String offsetStr = String.format("UTC%s%02d:%02d", sign, hours, minutes);
        return ZoneId.of(offsetStr);
      }
    }
    return null;
  }

  /**
   * 创建错误结果
   */
  private Map<String, Object> createErrorResult(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    response.put("result", null);
    return response;
  }

  /**
   * 创建成功结果
   */
  private Map<String, Object> createSuccessResult(String result) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "");
    response.put("result", result);
    return response;
  }
}

