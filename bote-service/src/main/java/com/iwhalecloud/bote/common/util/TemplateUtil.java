package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.lang.Nullable;

/**
 * 字符串模板工具类
 *
 * @author bianjp
 * @since 2025-03-18
 */
public final class TemplateUtil {
  /** 模板参数匹配模式（提示词模式智能体的提示词中引用技能时包含 URL 编码后的技能名称，里面可能包含 "%-!~*'()" 等字符) */
  private static final Pattern TEMPLATE_PARAM_PATTERN = Pattern.compile("\\$\\{([\\w._\\[\\]$%\\-!~*'()]+)}");

  private TemplateUtil() {
  }


  /**
   * 解析模板
   *
   * @param template 模板内容
   * @param paramResolver 参数值解析器，参数为模板中引用的参数名称（或表达式）
   * @return 解析后的文本
   */
  public static String resolveTemplate(String template, Function<String, Object> paramResolver) {
    // 未引用参数时直接返回
    if (!template.contains("${")) {
      return template;
    }
    // 替换模板中的参数
    Matcher matcher = TEMPLATE_PARAM_PATTERN.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String key = matcher.group(1);
      Object value = paramResolver.apply(key);
      // 将参数转为字符串类型
      String valueStr = convertParamToString(value);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(valueStr));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * 解析模板，返回片段列表
   *
   * @param template 模板内容
   * @param paramResolver 参数值解析器，参数为模板中引用的参数名称（或表达式）
   * @return 模板片段列表，元素类型为字符串或 SseInvoker
   */
  public static List<Object> parseTemplate(String template, Function<String, Object> paramResolver) {
    // 未引用参数时直接返回
    if (!template.contains("${")) {
      return Collections.singletonList(template);
    }
    List<Object> fragments = new ArrayList<>();
    // 替换模板中的参数
    Matcher matcher = TEMPLATE_PARAM_PATTERN.matcher(template);
    // 拆分模板，将每个文本片段、参数片段分别添加到 fragments 中
    int lastPos = 0;
    while (matcher.find()) {
      if (matcher.start() > lastPos) {
        fragments.add(template.substring(lastPos, matcher.start()));
      }
      lastPos = matcher.end();
      String key = matcher.group(1);
      Object value = paramResolver.apply(key);
      // 特殊处理 SseInvoker, 留给调用方处理
      if (value instanceof SseInvoker) {
        fragments.add(value);
      }
      else {
        fragments.add(convertParamToString(value));
      }
    }
    if (lastPos < template.length()) {
      fragments.add(template.substring(lastPos));
    }
    return fragments;
  }

  /**
   * 将参数转为字符串类型
   */
  private static String convertParamToString(@Nullable Object value) {
    // 特殊处理日期时间类型。日期类型 LocalDate 不需要处理，默认就会转为 yyyy-MM-dd 格式
    if (value instanceof Date) {
      return DateUtil.format((Date) value);
    }
    // 列表、对象转为 JSON 格式（toString 的效果不太好）
    if (value instanceof Collection || value instanceof Map) {
      return JsonUtil.toJsonString(value);
    }
    // 使用 toString
    return value != null ? value.toString() : "";
  }

}
