package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.TimeFormatConverterPluginParams;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 时间格式转换插件
 * <p>将时间从一种格式转换为另一种格式，支持自定义格式字符串</p>
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Component
public class TimeFormatConverterPlugin extends AbstractPlugin<TimeFormatConverterPluginParams> {

  /**
   * 默认时间格式
   */
  private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public TimeFormatConverterPlugin() {
    super(TimeFormatConverterPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_TIME_FORMAT_CONVERTER;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("time", "需要转换的时间（可选，字符串类型），不填则默认使用当前环境时区的时间",
      AttrDataType.STRING));
    children.add(
      ParameterSpec.newProperty("toFormat", "目标格式（可选，字符串类型），不填则默认使用yyyy-MM-dd HH:mm:ss格式",
        AttrDataType.STRING));
    children.add(
      ParameterSpec.newProperty("fromFormat", "原始格式（可选，字符串类型），不填则默认使用yyyy-MM-dd HH:mm:ss格式",
        AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "转换后的时间字符串", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(TimeFormatConverterPluginParams params) {
    // 如果提供了 fromFormat，验证格式字符串的有效性
    if (StringUtils.isNotBlank(params.getFromFormat())) {
      try {
        DateTimeFormatter.ofPattern(params.getFromFormat().trim());
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("参数 fromFormat 格式错误: " + e.getMessage(), e);
      }
    }

    // 如果提供了 toFormat，验证格式字符串的有效性
    if (StringUtils.isNotBlank(params.getToFormat())) {
      try {
        DateTimeFormatter.ofPattern(params.getToFormat().trim());
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("参数 toFormat 格式错误: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public Object doRun(TimeFormatConverterPluginParams pluginParams) {
    Map<String, Object> result = new HashMap<>();
    try {
      // 处理默认值：fromFormat
      String fromFormatValue = DEFAULT_TIME_FORMAT;
      if (StringUtils.isNotBlank(pluginParams.getFromFormat())) {
        fromFormatValue = pluginParams.getFromFormat().trim();
      }

      // 处理默认值：toFormat
      String toFormatValue = DEFAULT_TIME_FORMAT;
      if (StringUtils.isNotBlank(pluginParams.getToFormat())) {
        toFormatValue = pluginParams.getToFormat().trim();
      }

      // 创建格式化器
      DateTimeFormatter fromFormatter = DateTimeFormatter.ofPattern(fromFormatValue);
      DateTimeFormatter toFormatter = DateTimeFormatter.ofPattern(toFormatValue);

      // 处理 time 参数
      LocalDateTime localDateTime;
      if (StringUtils.isBlank(pluginParams.getTime())) {
        // time 为空，使用当前系统时间
        localDateTime = LocalDateTime.now();
      }
      else {
        // 解析 time 字符串
        String timeStr = pluginParams.getTime().trim();
        try {
          localDateTime = LocalDateTime.parse(timeStr, fromFormatter);
        }
        catch (DateTimeParseException e) {
          result.put("success", false);
          result.put("message", "参数 time 无法按照 fromFormat 格式解析: " + e.getMessage());
          result.put("result", null);
          return result;
        }
      }

      // 格式化为目标格式
      String convertedTime = localDateTime.format(toFormatter);

      // 返回成功结果
      result.put("success", true);
      result.put("message", "");
      result.put("result", convertedTime);
      return result;
    }
    catch (IllegalArgumentException e) {
      // 格式字符串错误
      result.put("success", false);
      result.put("message", "格式字符串错误: " + e.getMessage());
      result.put("result", null);
      return result;
    }
    catch (Exception e) {
      // 其他异常
      logger.error("时间格式转换时发生异常", e);
      result.put("success", false);
      result.put("message", "时间格式转换时发生异常: " + e.getMessage());
      result.put("result", null);
      return result;
    }
  }
}

