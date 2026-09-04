package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.GetTimeZonePluginParams;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 获取指定时区时间插件 支持用户输入指定的时区，以获取该时区的当前时间
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Component
public class GetTimeZonePlugin extends AbstractPlugin<GetTimeZonePluginParams> {

  public GetTimeZonePlugin() {
    super(GetTimeZonePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_GET_TIME_ZONE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("timeZone",
      "时区标识（可选，字符串类型），格式为UTC、UTC+1、UTC-1、UTC+08:00等，不填则使用当前环境的默认时区", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "时间字符串（ISO 8601格式）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(GetTimeZonePluginParams params) {
    // timeZone 是可选参数，如果提供了则进行格式校验
    if (params.getTimeZone() != null && StringUtils.isNotBlank(params.getTimeZone())) {
      String timeZoneStr = params.getTimeZone().trim();
      ZoneId parsedZoneId = parseTimeZone(timeZoneStr);
      if (parsedZoneId == null) {
        throw new IllegalArgumentException(
          "参数 timeZone 格式错误，支持的格式为：UTC、UTC+1、UTC-1、UTC+08、UTC+08:00、UTC-05:30等，或标准时区名称如Asia/Shanghai、America/New_York等");
      }
    }
  }

  @Override
  public Object doRun(GetTimeZonePluginParams pluginParams) {
    try {
      // 参数处理：timeZone为null或空字符串时，使用系统默认时区
      ZoneId zoneId;

      if (pluginParams.getTimeZone() == null || StringUtils.isBlank(pluginParams.getTimeZone())) {
        // 使用系统默认时区
        zoneId = ZoneId.systemDefault();
      }
      else {
        // 解析时区格式
        String timeZoneStr = pluginParams.getTimeZone().trim();
        ZoneId parsedZoneId = parseTimeZone(timeZoneStr);

        if (parsedZoneId == null) {
          return createErrorResult(
            "参数 timeZone 格式错误，支持的格式为：UTC、UTC+1、UTC-1、UTC+08、UTC+08:00、UTC-05:30等，或标准时区名称如Asia/Shanghai、America/New_York等");
        }

        zoneId = parsedZoneId;
      }

      // 获取指定时区的当前时间
      ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);

      // 格式化为ISO 8601格式
      DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
      String timeString = zonedDateTime.format(formatter);

      // 返回成功结果
      return createSuccessResult(timeString);

    }
    catch (Exception e) {
      logger.error("获取时区时间时发生异常", e);
      return createErrorResult("获取时区时间时发生异常: " + e.getMessage());
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
      zoneId = parseUtcOffsetHours(upperStr);
      if (zoneId != null) {
        return zoneId;
      }
      zoneId = parseUtcOffsetHoursMinutes(upperStr);
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
   * 创建成功结果
   */
  private Map<String, Object> createSuccessResult(String result) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "");
    response.put("result", result);
    return response;
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
}

