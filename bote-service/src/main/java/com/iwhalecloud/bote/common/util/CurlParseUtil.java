package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.dto.skill.CurlParamsDTO;
import com.iwhalecloud.bote.dto.skill.CurlParseResultDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * Curl命令解析工具类 支持解析CMD格式和Bash格式的curl命令
 *
 * @author qian.sisheng
 * @since 2025-09-16
 */
public final class CurlParseUtil {

  private static final Logger logger = LoggerFactory.getLogger(CurlParseUtil.class);
  /** URL正则表达式 */
  private static final Pattern URL_PATTERN = Pattern.compile("(?:curl\\s+)?(?:-[^\\s]*\\s+)*['\"]?(https?://[^\\s'\"]+)['\"]?");
  /** 请求方法正则表达式 */
  private static final Pattern METHOD_PATTERN = Pattern.compile("(?:-X|--request)\\s+['\"]?([A-Z]+)['\"]?");
  /** header正则表达式 */
  private static final Pattern HEADER_PATTERN = Pattern.compile("(-H|--header|-b)\\s+['\"]?([^'\"]+)['\"]?");

  /** body参数正则表达式 */
  private static final Pattern DATA_PATTERN = Pattern.compile("(?:^|\\s)(?:--data-raw|--data)\\s+([\"'])(.*?)(?<!\\\\)\\1|(?:(?:^|\\s)(?:--data-raw|--data)\\s+(\\S+)(?=\\s|$))");
  /** 表单参数正则表达式 */
  private static final Pattern FORM_PATTERN = Pattern.compile("(?:-F|--form)\\s+([\"'])((?:[^\"'\\\\]|\\\\.)*?)\\1");
  /** JSON参数正则表达式 */
  private static final Pattern JSON_PATTERN = Pattern.compile("--json\\s+([\"'])((?:[^\"'\\\\]|\\\\.)*?)\\1");

  /** CMD命令行继续符正则表达式 */
  private static final Pattern CMD_LINE_CONTINUE = Pattern.compile("\\^\\s*\\r?\\n\\s*");
  /** Bash命令行继续符正则表达式 */
  private static final Pattern BASH_LINE_CONTINUE = Pattern.compile("\\\\\\s*\\r?\\n\\s*");
  /** 非JSON表单参数正则表达式 */
  private static final Pattern NON_JSON_FORM_PATTERN = Pattern.compile("Content-Disposition:\\s*form-data;\\s*name=\\\\*\"([^\"]*)\\\\*\"");
  /** 通用请求头 */
  private static final List<String> COMMON_HEADERS = List.of("Accept", "Accept-Encoding", "Accept-Language", "Cache-Control", "Connection",
    "Content-Length", "Content-Type", "Host", "Origin", "Pragma", "Referer", "Sec-Fetch-Dest", "Sec-Fetch-Mode", "Sec-Fetch-Site", "Sec-Fetch-User",
    "User-Agent", "sec-ch-ua", "sec-ch-ua-mobile", "sec-ch-ua-platform");

  private CurlParseUtil() {

  }

  /**
   * 解析curl命令
   */
  public static CurlParseResultDTO parseCurl(CurlParamsDTO curlParams) {
    String curlCommand = curlParams.getCurlCommand();
    if (StringUtils.isBlank(curlCommand)) {
      throw new IllegalArgumentException("Curl命令不能为空");
    }
    try {
      logger.debug("开始解析curl命令: {}", curlCommand);
      // 替换特殊字符
      String cleanedCommand = cleanCommand(curlCommand);
      CurlParseResultDTO result = new CurlParseResultDTO();
      // 解析 url
      parseUrl(cleanedCommand, result);
      // 解析请求方法
      parseMethod(cleanedCommand, result);
      // 解析请求头
      parseHeaders(cleanedCommand, result, curlParams.getIgnoreCommonHeaders());
      // 解析请求体
      parseBody(cleanedCommand, result);
      return result;
    }
    catch (Exception e) {
      logger.error("解析curl命令失败: {}", curlCommand, e);
      throw new BssException("解析curl命令失败: " + e.getMessage(), e);
    }
  }

  /**
   * 替换特殊字符
   */
  private static String cleanCommand(String command) {
    String cleanedCommand = command.trim();
    logger.debug("替换前curl命令: {}", cleanedCommand);
    // 处理行继续符（支持CMD和Bash）
    cleanedCommand = CMD_LINE_CONTINUE.matcher(cleanedCommand).replaceAll(" ");
    cleanedCommand = BASH_LINE_CONTINUE.matcher(cleanedCommand).replaceAll(" ");
    // 转义处理
    cleanedCommand = cleanedCommand.replaceAll("\\^", "");
    // 移除多余空格
    cleanedCommand = cleanedCommand.replaceAll("\\s+", " ");
    logger.debug("替换后的curl命令: {}", cleanedCommand);
    return cleanedCommand;
  }

  /**
   * 解析 URL
   */
  private static void parseUrl(String command, CurlParseResultDTO result) {
    Matcher matcher = URL_PATTERN.matcher(command);
    if (matcher.find()) {
      String url = matcher.group(1);
      // 解析查询参数
      try {
        URI uri = new URI(url);
        String baseUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
        result.setRelativePath(baseUrl);
        // 提取query参数
        if (uri.getQuery() != null) {
          Map<String, String> queryParams = parseQueryString(uri.getQuery());
          result.setQueryJson(JsonUtil.toJsonString(ParamSpecUtil.buildParamSpec(queryParams)));
        }
      }
      catch (URISyntaxException e) {
        logger.error("URL解析失败: {}", url, e);
        throw new BssException("URL解析失败: " + e.getMessage(), e);
      }
    }
  }

  /**
   * 解析 HTTP方法
   */
  private static void parseMethod(String command, CurlParseResultDTO result) {
    Matcher matcher = METHOD_PATTERN.matcher(command);
    if (matcher.find()) {
      result.setReqMethod(matcher.group(1));
    }
    else {
      // 根据是否有数据参数推断方法
      if (hasDataParameters(command)) {
        result.setReqMethod("POST");
      }
      else {
        result.setReqMethod("GET");
      }
    }
  }

  /**
   * 解析请求头
   */
  private static void parseHeaders(String command, CurlParseResultDTO result, boolean ignoreCommonHeaders) {
    Map<String, String> headers = new HashMap<>();
    Matcher matcher = HEADER_PATTERN.matcher(command);
    while (matcher.find()) {
      String type = matcher.group(1);
      String headerLine = matcher.group(2);
      parseHeaderLine(headerLine, headers, type, ignoreCommonHeaders);
    }
    result.setHeaderJson(JsonUtil.toJsonString(ParamSpecUtil.buildParamSpec(headers)));
  }

  /**
   * 解析请求体
   */
  private static void parseBody(String command, CurlParseResultDTO result) {
    try {
      Pair<String, String> pair = extractBodyContent(command);
      if (pair != null && StringUtils.isNotEmpty(pair.getLeft())) {
        processBodyContent(pair.getLeft(), pair.getRight(), result);
      }
    }
    catch (Exception e) {
      logger.error("body解析失败: command={}", command, e);
    }
  }

  /**
   * 提取请求体内容和类型
   */
  @Nullable
  private static Pair<String, String> extractBodyContent(String command) {
    String body;

    // 1. 提取JSON格式数据
    if (JSON_PATTERN.matcher(command).find()) {
      Matcher matcher = JSON_PATTERN.matcher(command);
      return Pair.of(matcher.find() ? matcher.group(1) : "", MediaType.APPLICATION_JSON_VALUE);
    }

    // 2. 提取表单数据
    if (FORM_PATTERN.matcher(command).find()) {
      body = extractFormData(command);
      return Pair.of(body, MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    // 3. 提取非JSON格式的表单数据
    body = extractNonJsonFormData(command);
    if (StringUtils.isNotEmpty(body)) {
      return Pair.of(body, MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    // 4. 提取data-raw数据
    if (DATA_PATTERN.matcher(command).find()) {
      return parseDataRaw(command);
    }
    return null;
  }

  private static Pair<String, String> parseDataRaw(String command) {
    String body = "";
    Matcher matcher = DATA_PATTERN.matcher(command);
    if (matcher.find()) {
      // 如果匹配到引号包围的数据
      if (matcher.group(2) != null) {
        body = matcher.group(2);
      }
      // 如果匹配到无引号的数据
      else if (matcher.group(3) != null) {
        body = matcher.group(3);
      }
    }
    if (isJsonString(body)) {
      // cmd含有转义符，bash的Json数据不含有转义符，通过JsonUtil.parseJson判断
      Object object = JsonUtil.parseJson(body, Object.class);
      if (object == null) {
        body = StringEscapeUtils.unescapeJson(body);
      }
      return Pair.of(body, MediaType.APPLICATION_JSON_VALUE);
    }
    if (body.contains("=") && !body.trim().startsWith("{")) {
      return Pair.of(JsonUtil.toJsonString(parseQueryString(body)), MediaType.MULTIPART_FORM_DATA_VALUE);
    }
    return Pair.of(body, MediaType.APPLICATION_JSON_VALUE);
  }

  /**
   * 提取非JSON格式的表单数据
   */
  private static String extractNonJsonFormData(String command) {
    Map<String, String> formData = new HashMap<>();
    Matcher nonJsonMatcher = NON_JSON_FORM_PATTERN.matcher(command);
    while (nonJsonMatcher.find()) {
      formData.put(nonJsonMatcher.group(1), "");
    }
    return MapUtils.isNotEmpty(formData) ? JsonUtil.toJsonString(formData) : "";
  }

  /**
   * 处理body内容并设置到结果对象
   */
  private static void processBodyContent(String body, String bodyType, CurlParseResultDTO result) {
    try {
      Object parsedBody = JsonUtil.parseJson(body, Object.class);
      if (parsedBody != null) {
        result.setBodyJson(JsonUtil.toJsonString(ParamSpecUtil.buildParamSpec(parsedBody)));
        result.setBodyType(StringUtils.defaultIfEmpty(bodyType, MediaType.APPLICATION_JSON_VALUE));
      }
    }
    catch (Exception e) {
      logger.error("body内容处理失败: body={}, type={}", body, bodyType, e);
    }
  }

  /**
   * 提取 form-data 数据
   */
  private static String extractFormData(String command) {
    Map<String, Object> formData = new HashMap<>();
    Matcher matcher = FORM_PATTERN.matcher(command);

    while (matcher.find()) {
      String formField = matcher.group(2);
      String[] parts = formField.split("=", 2);
      if (parts.length == 2) {
        formData.put(parts[0].trim(), parts[1].trim());
      }
    }
    return MapUtils.isEmpty(formData) ? "" : JsonUtil.toJsonString(formData);
  }

  /**
   * 检查是否有数据参数
   */
  private static boolean hasDataParameters(String command) {
    return DATA_PATTERN.matcher(command).find() || FORM_PATTERN.matcher(command).find() || JSON_PATTERN.matcher(command).find();
  }

  /**
   * 解析查询字符串
   */
  private static Map<String, String> parseQueryString(String query) {
    Map<String, String> params = new HashMap<>();
    if (StringUtils.isNotBlank(query)) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=", 2);
        if (keyValue.length == 2) {
          params.put(keyValue[0], keyValue[1]);
        }
      }
    }
    return params;
  }

  /**
   * 解析请求头行
   */
  private static void parseHeaderLine(String headerLine, Map<String, String> headers, String type, boolean ignoreCommonHeaders) {
    //  Cookie 以 -b 开头，特殊处理
    if ("-b".equals(type) && !ignoreCommonHeaders && !COMMON_HEADERS.contains("Cookie")) {
      headers.put("Cookie", headerLine);
      return;
    }
    int colonIndex = headerLine.indexOf(':');
    if (colonIndex > 0) {
      String key = headerLine.substring(0, colonIndex).trim();
      String value = headerLine.substring(colonIndex + 1).trim();
      // 忽略常见的请求头
      if (ignoreCommonHeaders && COMMON_HEADERS.contains(key)) {
        return;
      }
      headers.put(key, value);
    }
  }

  /**
   * 判断是否为JSON字符串
   */
  private static boolean isJsonString(@Nullable String str) {
    if (StringUtils.isEmpty(str)) {
      return false;
    }
    str = str.trim();
    return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
  }
}
