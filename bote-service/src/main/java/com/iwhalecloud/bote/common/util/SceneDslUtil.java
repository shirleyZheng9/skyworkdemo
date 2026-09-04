package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景 DSL 工具类
 *
 * <p>处理场景 DSL 对象的 JSON 序列化/反序列化。在序列化时排除 null 属性，以降低存储空间占用。</p>
 *
 * @author bianjp
 * @since 2024-08-29
 */
@SuppressWarnings({"java:S2139", "java:S1192"})
public final class SceneDslUtil {
  private static final Logger logger = LoggerFactory.getLogger(SceneDslUtil.class);

  private SceneDslUtil() {
  }

  /**
   * 从 JSON 字符串解析 DSL 对象
   *
   * @param json JSON 字符串
   * @return DSL 对象
   */
  public static SceneDslDTO parse(String json) {
    Assert.hasLength(json, "json 不能为空");
    return JsonUtil.parseJsonRequired(json, SceneDslDTO.class);
  }

  /**
   * 转为 JSON 字符串
   *
   * @param dsl DSL 对象
   * @return JSON 字符串
   */
  public static String toJson(SceneDslDTO dsl) {
    Assert.notNull(dsl, "dsl 不能为空");
    return JsonUtil.toJsonStringCompact(dsl);
  }

  /**
   * 解析节点的配置数据
   *
   * @param nodeName 节点名称
   * @param nodeJson 节点配置 JSON 字符串
   * @return 配置数据
   */
  @Nullable
  public static JsonNode parseNodeJson(String nodeName, @Nullable String nodeJson) {
    // 服务节点可能尚未配置，不要报错。也有一些特殊节点确实没有配置数据，比如条件的 else 分支节点
    if (StringUtils.isEmpty(nodeJson)) {
      return null;
    }
    try {
      JsonNode json = JsonUtil.readTree(nodeJson);
      if (json.isNull()) {
        return null;
      }
      return json;
    }
    catch (RuntimeException e) {
      logger.error("Failed to parse node data: nodeName={}, nodeJson={}", nodeName, nodeJson, e);
      throw new BssException(String.format("解析节点【%s】配置失败: %s", nodeName, e.getMessage()), e);
    }
  }

  /**
   * 解析 JSON 配置的单个属性
   *
   * @param json JSON 对象
   * @param attr 属性名称
   * @param clazz 属性类型
   * @return 属性值。可能为 null
   */
  @Nullable
  public static <T> T parseJsonAttr(@Nullable JsonNode json, String attr, Class<T> clazz) {
    if (json == null) {
      return null;
    }
    JsonNode attrNode = json.get(attr);
    if (attrNode == null || attrNode.isNull()) {
      return null;
    }
    return JsonUtil.convert(attrNode, clazz);
  }

  /**
   * 解析 JSON 配置的单个属性
   *
   * @param json JSON 对象
   * @param attr 属性名称
   * @param typeReference 属性类型
   * @return 属性值。可能为 null
   */
  @Nullable
  public static <T> T parseJsonAttr(@Nullable JsonNode json, String attr, TypeReference<T> typeReference) {
    if (json == null) {
      return null;
    }
    JsonNode attrNode = json.get(attr);
    if (attrNode == null || attrNode.isNull()) {
      return null;
    }
    return JsonUtil.convert(attrNode, typeReference);
  }
}
