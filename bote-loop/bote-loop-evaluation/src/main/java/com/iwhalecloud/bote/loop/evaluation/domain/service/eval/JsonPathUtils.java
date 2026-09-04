package com.iwhalecloud.bote.loop.evaluation.domain.service.eval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON工具类，仅使用Jackson的ObjectMapper实现
 * 提供JSON序列化、反序列化和简单字段提取功能
 */
public final class JsonPathUtils {

  private JsonPathUtils() {
    // 工具类，禁止实例化
  }
  // 初始化Jackson ObjectMapper（用于JSON处理）
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * 将对象序列化为JSON字符串
   *
   * @param obj 要序列化的对象
   * @return JSON字符串
   * @throws JsonProcessingException 序列化失败时抛出
   */
  public static String toJsonString(Object obj) throws JsonProcessingException {
    if (obj == null) {
      return "null";
    }
    return OBJECT_MAPPER.writeValueAsString(obj);
  }

  /**
   * 将JSON字符串反序列化为指定类型的对象
   *
   * @param json JSON字符串
   * @param clazz 目标类型
   * @param <T> 目标类型泛型
   * @return 反序列化后的对象
   * @throws JsonProcessingException 反序列化失败时抛出
   */
  public static <T> T fromJsonString(String json, Class<T> clazz) throws JsonProcessingException {
    if (json == null || json.isEmpty()) {
      return null;
    }
    return OBJECT_MAPPER.readValue(json, clazz);
  }

  /**
   * 解析JSON字符串为JsonNode，用于手动处理JSON结构
   *
   * @param json JSON字符串
   * @return JsonNode对象
   * @throws JsonProcessingException 解析失败时抛出
   */
  public static JsonNode parseJson(String json) throws JsonProcessingException {
    if (json == null || json.isEmpty()) {
      return null;
    }
    return OBJECT_MAPPER.readTree(json);
  }

  /**
   * 通过简单的字段路径从JSON中获取数据
   * 支持的路径格式："user.name"、"items[0].id"等
   * 不支持复杂的JSONPath语法
   *
   * @param json JSON字符串
   * @param path 字段路径
   * @return 提取的数据
   * @throws JsonProcessingException 解析失败时抛出
   */
  public static Object getByPath(String json, String path) throws JsonProcessingException {
    if (path == null || path.isEmpty()) {
      return json;
    }

    JsonNode rootNode = parseJson(json);
    if (rootNode == null) {
      return null;
    }

    return traverseJsonPath(rootNode, path);
  }

  private static Object traverseJsonPath(JsonNode rootNode, String path) {
    JsonNode currentNode = rootNode;
    String[] pathSegments = splitPath(path);

    for (String segment : pathSegments) {
      if (currentNode == null || currentNode.isMissingNode()) {
        return null;
      }

      currentNode = processPathSegment(currentNode, segment);
      if (currentNode == null) {
        return null;
      }
    }

    return convertJsonNodeToObject(currentNode);
  }

  private static JsonNode processPathSegment(JsonNode currentNode, String segment) {
    if (isArrayIndex(segment)) {
      return processArrayIndex(currentNode, segment);
    } else {
      return processObjectField(currentNode, segment);
    }
  }

  private static boolean isArrayIndex(String segment) {
    return segment.startsWith("[") && segment.endsWith("]");
  }

  private static JsonNode processArrayIndex(JsonNode currentNode, String segment) {
    if (!currentNode.isArray()) {
      return null;
    }

    try {
      int index = Integer.parseInt(segment.substring(1, segment.length() - 1));
      ArrayNode arrayNode = (ArrayNode) currentNode;
      if (index >= 0 && index < arrayNode.size()) {
        return arrayNode.get(index);
      } else {
        return null;
      }
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static JsonNode processObjectField(JsonNode currentNode, String segment) {
    if (currentNode.isObject()) {
      return currentNode.get(segment);
    } else {
      return null;
    }
  }

  /**
   * 将JsonNode转换为Java对象
   *
   * @param node JsonNode对象
   * @return 对应的Java对象
   */
  private static Object convertJsonNodeToObject(JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return null;
    }
    if (node.isTextual()) {
      return node.textValue();
    }
    if (node.isNumber()) {
      return node.numberValue();
    }
    if (node.isBoolean()) {
      return node.booleanValue();
    }
    if (node.isArray()) {
      List<Object> list = new ArrayList<>();
      for (JsonNode element : node) {
        list.add(convertJsonNodeToObject(element));
      }
      return list;
    }
    if (node.isObject()) {
      try {
        return OBJECT_MAPPER.treeToValue(node, Object.class);
      }
      catch (JsonProcessingException e) {
        return node.toString();
      }
    }
    return node.toString();
  }

  /**
   * 将路径分割为片段，处理数组索引
   *
   * @param path 字段路径
   * @return 分割后的路径片段
   */
  private static String[] splitPath(String path) {
    List<String> segments = new ArrayList<>();
    StringBuilder currentSegment = new StringBuilder();
    boolean inBracket = false;

    for (char c : path.toCharArray()) {
      if (c == '.' && !inBracket) {
        if (currentSegment.length() > 0) {
          segments.add(currentSegment.toString());
          currentSegment.setLength(0);
        }
      }
      else if (c == '[') {
        inBracket = true;
        if (currentSegment.length() > 0) {
          segments.add(currentSegment.toString());
          currentSegment.setLength(0);
        }
        currentSegment.append(c);
      }
      else if (c == ']') {
        inBracket = false;
        currentSegment.append(c);
        segments.add(currentSegment.toString());
        currentSegment.setLength(0);
      }
      else {
        currentSegment.append(c);
      }
    }

    if (currentSegment.length() > 0) {
      segments.add(currentSegment.toString());
    }

    return segments.toArray(new String[0]);
  }

  /**
   * 通过路径从JSON中获取数据，并转换为字符串
   *
   * @param json JSON字符串
   * @param path 字段路径
   * @return 转换后的字符串
   * @throws JsonProcessingException 处理失败时抛出
   */
  public static String getStringByPath(String json, String path) throws JsonProcessingException {
    Object result = getByPath(json, path);
    if (result == null) {
      return "";
    }
    return convertToString(result);
  }

  /**
   * 将任意对象转换为字符串
   *
   * @param obj 要转换的对象
   * @return 转换后的字符串
   * @throws JsonProcessingException 复杂对象序列化失败时抛出
   */
  public static String convertToString(Object obj) throws JsonProcessingException {
    if (obj == null) {
      return "";
    }
    if (obj instanceof String) {
      return (String) obj;
    }
    if (obj instanceof Number || obj instanceof Boolean) {
      return obj.toString();
    }
    // 对于复杂对象，序列化为JSON字符串
    return toJsonString(obj);
  }

  /**
   * 提取路径的第一级字段名
   *
   * @param path 字段路径
   * @return 第一级字段名
   */
  public static String getFirstPathField(String path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("路径为空");
    }

    // 移除可能的$符号
    String processedPath = path.startsWith("$") ? path.substring(1) : path;

    // 跳过前导的.
    if (processedPath.startsWith(".")) {
      processedPath = processedPath.substring(1);
    }

    // 分割路径获取第一个片段
    String[] segments = splitPath(processedPath);
    if (segments.length == 0) {
      throw new IllegalArgumentException("无效的路径格式");
    }

    String firstSegment = segments[0];
    // 如果第一个片段是数组索引，查看下一个片段
    if (firstSegment.startsWith("[") && firstSegment.endsWith("]")) {
      if (segments.length > 1) {
        return segments[1];
      }
      else {
        throw new IllegalArgumentException("路径格式错误");
      }
    }

    return firstSegment;
  }

  /**
   * 计算路径的层级数量
   *
   * @param path 字段路径
   * @return 层级数量
   */
  public static int getPathLevel(String path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("路径为空");
    }

    // 移除可能的$符号
    String processedPath = path.startsWith("$") ? path.substring(1) : path;

    // 分割路径并计算有效层级
    String[] segments = splitPath(processedPath);
    int level = 0;

    for (String segment : segments) {
      // 跳过空片段
      if (segment.isEmpty()) {
        continue;
      }
      // 数组索引不计为单独层级，而是当前层级的一部分
      if (!(segment.startsWith("[") && segment.endsWith("]"))) {
        level++;
      }
    }

    return level;
  }

  /**
   * 删除路径的第一级字段，保留其余层级
   *
   * @param path 字段路径
   * @return 处理后的路径
   */
  public static String removeFirstPathLevel(String path) {
    validatePath(path);
    String processedPath = preprocessPath(path);
    String[] segments = splitPath(processedPath);

    if (segments.length == 0) {
      return "";
    }

    int skipCount = determineSkipCount(segments);
    return buildRemainingPath(segments, skipCount);
  }

  private static void validatePath(String path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("路径为空");
    }
  }

  private static String preprocessPath(String path) {
    String processedPath = removeDollarSign(path);
    return removeLeadingDots(processedPath);
  }

  private static String removeDollarSign(String path) {
    return path.startsWith("$") ? path.substring(1) : path;
  }

  private static String removeLeadingDots(String path) {
    while (path.startsWith(".")) {
      path = path.substring(1);
    }
    return path;
  }

  private static int determineSkipCount(String[] segments) {
    int skip = 1;
    if (isArrayIndex(segments[0])) {
      if (segments.length == 1) {
        return segments.length; // 返回空字符串
      }
      skip = 2;
    }
    return skip;
  }

  private static String buildRemainingPath(String[] segments, int skipCount) {
    if (skipCount >= segments.length) {
      return "";
    }

    StringBuilder result = new StringBuilder();
    for (int i = skipCount; i < segments.length; i++) {
      appendSegmentWithSeparator(result, segments, i, skipCount);
    }
    return result.toString();
  }

  private static void appendSegmentWithSeparator(StringBuilder result, String[] segments, int index, int skipCount) {
    if (index > skipCount && !isArrayIndex(segments[index])) {
      result.append(".");
    }
    result.append(segments[index]);
  }
}
