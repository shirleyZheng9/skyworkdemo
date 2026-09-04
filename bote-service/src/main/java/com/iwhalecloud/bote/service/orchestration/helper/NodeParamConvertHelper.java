package com.iwhalecloud.bote.service.orchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.JsonSchemaUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.consts.ResponseFormatType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 节点参数转换工具
 *
 * @author bianjp
 * @since 2025-08-04
 */
public final class NodeParamConvertHelper {
  private NodeParamConvertHelper() {
  }

  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);
  private static final ModelClientCache modelClientCache = SpringUtil.getBean(ModelClientCache.class);

  /**
   * 参数转为表格格式（即将参数值设置为参数规格的 value）
   *
   * @param params 参数
   * @param specs 参数规格
   * @return 参数规格列表
   */
  public static List<ParameterSpec> convertParamsToTable(@Nullable Map<String, Object> params, List<ParameterSpec> specs) {
    if (MapUtils.isEmpty(params)) {
      return specs;
    }
    for (ParameterSpec spec : specs) {
      convertJsonToTable(spec.getName(), spec, params.get(spec.getName()));
    }
    return specs;
  }

  /**
   * 转换参数值为表格形式
   */
  @SuppressWarnings("unchecked")
  private static void convertJsonToTable(String propertyPath, ParameterSpec spec, @Nullable Object value) {
    if (ObjectUtils.isEmpty(value)) {
      return;
    }
    // 对象
    if (spec.isObject()) {
      Assert.isTrue(value instanceof Map, () -> "参数 " + propertyPath + " 必须是对象");
      Map<String, Object> map = (Map<String, Object>) value;
      // 有属性时，只能给属性赋值
      if (spec.hasChildren()) {
        for (ParameterSpec child : spec.getChildren()) {
          convertJsonToTable(propertyPath + "." + child.getName(), child, map.get(child.getName()));
        }
      }
      // 没有属性时，支持给对象赋值 JSON 字符串
      else {
        spec.setValue(JsonUtil.toJsonString(value));
      }
    }
    // 列表，只支持直接赋值 JSON 字符串，不支持给元素赋值
    else if (spec.isList()) {
      Assert.isTrue(value instanceof List, () -> "参数 " + propertyPath + " 必须是数组");
      spec.setValue(JsonUtil.toJsonString(value));
    }
    // 属性
    else {
      spec.setValue(value.toString());
    }
  }

  /**
   * 参数规格转为 JSON 格式
   *
   * @param specs 参数规格
   * @param useDefaultValue 是否生成默认值
   * @return 参数的 JSON 形式
   */
  public static Map<String, Object> convertParamsToJson(List<ParameterSpec> specs, boolean useDefaultValue) {
    if (CollectionUtils.isEmpty(specs)) {
      return Collections.emptyMap();
    }
    Map<String, Object> params = new LinkedHashMap<>();
    for (ParameterSpec spec : specs) {
      params.put(spec.getName(), convertSpecToJson(spec.getName(), spec, useDefaultValue));
    }
    return params;
  }

  /**
   * 转换参数规格为 JSON 形式
   */
  @Nullable
  private static Object convertSpecToJson(String propertyPath, ParameterSpec spec, boolean useDefaultValue) {
    // 对象
    if (spec.isObject()) {
      return convertObjectSpecToJson(propertyPath, spec, useDefaultValue);
    }
    // 列表，只支持直接赋值 JSON 字符串，不支持给元素赋值
    else if (spec.isList()) {
      String value = StringUtils.trimToEmpty(spec.getValue());
      List<Object> list;
      if (value.startsWith("[") && value.endsWith("]")) {
        try {
          list = JsonUtil.getObjectMapper().readValue(value, new TypeReference<List<Object>>() {
          });
        }
        catch (Exception e) {
          throw new RuntimeException(String.format("列表转换失败：attr=%s，value=%s", propertyPath, value), e);
        }
      }
      else {
        list = null;
      }
      return list;
    }
    // 属性
    else {
      Object value = AttrDataType.convert(propertyPath, spec.getType(), spec.getValue());
      if (value == null && useDefaultValue) {
        value = getDefaultValue(spec.getType());
      }
      return value;
    }
  }

  /**
   * 转换对象参数规格为 JSON 形式
   */
  @Nullable
  private static Map<String, Object> convertObjectSpecToJson(String propertyPath, ParameterSpec spec, boolean useDefaultValue) {
    // 有属性时，只能给属性赋值
    if (spec.hasChildren()) {
      Map<String, Object> obj = new LinkedHashMap<>();
      for (ParameterSpec child : spec.getChildren()) {
        obj.put(child.getName(), convertSpecToJson(propertyPath + "." + child.getName(), child, useDefaultValue));
      }
      return obj;
    }

    // 没有属性时，支持给对象赋值 JSON 字符串
    String value = StringUtils.trimToEmpty(spec.getValue());
    if (value.startsWith("{") && value.endsWith("}")) {
      Map<String, Object> map;
      try {
        map = JsonUtil.getObjectMapper().readValue(value, new TypeReference<Map<String, Object>>() {
        });
      }
      catch (Exception e) {
        throw new RuntimeException(String.format("对象转换失败：attr=%s，value=%s", propertyPath, value), e);
      }
      return map;
    }
    return null;
  }

  /**
   * 生成默认值
   */
  public static void generateDefaultValue(List<ParameterSpec> params) {
    for (ParameterSpec spec : params) {
      // 对象需要递归处理
      if (spec.getType() == AttrDataType.OBJECT) {
        if (spec.hasChildren()) {
          generateDefaultValue(spec.getChildren());
        }
      }
      else {
        spec.setValue(Objects.toString(getDefaultValue(spec.getType()), null));
      }
    }
  }

  /**
   * 根据参数类型获取默认值
   */
  @Nullable
  private static Object getDefaultValue(@Nullable AttrDataType dataType) {
    if (dataType == null) {
      return null;
    }
    switch (dataType) {
      case STRING:
      case DATE:
      case DATETIME:
        return "";
      case INTEGER:
        return 0L;
      case NUMBER:
        return BigDecimal.ZERO;
      case BOOLEAN:
        return false;
      default:
        break;
    }
    return null;
  }

  /**
   * AI 填充参数
   *
   * @param params 现有参数
   * @param specs 参数规格
   * @return 填充后的参数
   */
  public static Map<String, Object> aiFillParams(Long tenantId, @Nullable Map<String, Object> params, List<ParameterSpec> specs) {
    // 使用租户的默认模型
    Long modelId = tenantSettingInfoCache.getModelId(tenantId);
    LlmClient client = modelClientCache.getLlmClient(tenantId, modelId);

    // 使用 JSON Schema 结构以方便大模型理解
    ParameterSpec root = ParameterSpec.newRoot(specs);
    JsonSchemaNode schema = JsonSchemaUtil.convert(root);
    schema.setDescription(null);

    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage("你是参数补全助手，根据用户输入的参数结构(JSON Schema)、现有参数补充缺失的参数。参数值根据参数的类型、描述随机生成\n\n" +
        "限制:\n" +
        "1. 只输出补全后的完整参数(JSON), 不要输出多余的内容\n" +
        "2. 参数名称要和参数结构定义完全一致，不要做任何修改。即使参数名称中包含 \".\", 也不要转为嵌套的对象\n" +
        "3. 生成文本类型的参数值时（比如用户输入、问题、消息内容），一般应生成中文")
      .addUserMessage("参数结构:\n```\n" +
        JsonUtil.toJsonString(schema) +
        "\n```\n\n现有参数:\n```json\n" +
        JsonUtil.toJsonStringPretty(MapUtils.emptyIfNull(params)) +
        "\n```\n")
      .responseFormatType(ResponseFormatType.JSON_OBJECT)
      .build();
    ChatCompletionResponse response = client.chatCompletion(request);
    JsonNode node = MarkdownHelper.parseJsonAndAutoFix(response.getMessageContent(), client);
    Assert.notNull(node, () -> "参数补全失败，大模型未返回 JSON");
    Map<String, Object> result = JsonUtil.convert(node, new TypeReference<Map<String, Object>>() {
    });
    // 根据参数结构转换一次，确保参数的类型正确（解析 JSON 时，整数可能会被反序列化为 Integer, 而我们需要使用 Long, 以确保传输给前端时序列化为字符串）
    return ParamConverterUtil.convertRoot(root, result);
  }
}
