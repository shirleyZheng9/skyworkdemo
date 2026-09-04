package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author qian.sisheng
 * @since 2024/10/15
 */
@SuppressWarnings("rawtypes")
public final class SwaggerApiUtil {
  private SwaggerApiUtil() {
  }
  private static final String RESPONSE_200 = "200";
  private static final String APPLICATION_JSON = "application/json";
  /** 数组对象名称 */
  private static final String DEFAULT_NEW_NODE = "listItem";

  @Nullable
  private static Schema getResponseSchema(Operation operation) {
    ApiResponses responses = operation.getResponses();
    if (responses == null || responses.get(RESPONSE_200) == null) {
      return null;
    }
    List<Schema> schemaSingleList = new ArrayList<>(1);
    Content content = responses.get(RESPONSE_200).getContent();
    if (content == null) {
      return null;
    }
    content.forEach((k, v) -> schemaSingleList.add(v.getSchema()));
    if (CollectionUtils.isNotEmpty(schemaSingleList)) {
      return schemaSingleList.get(0);
    }
    return null;
  }

  /**
   * 获取请求参数JSON
   *
   * @param operation swagger Operation
   * @param definitions 定义集合
   * @return 请求参数JSON
   */
  public static String getRequestJson(Operation operation, Map<String, Schema> definitions) {
    // 初始化根节点root
    ParameterSpec root = ParameterSpec.newRoot();
    root.setKey("-1");
    root.setRequired(false);
    // 参数子节点
    List<ParameterSpec> childes = new ArrayList<>();
    root.setChildren(childes);
    // 层级结构key初值
    // get post请求时的params
    List<Parameter> parameters = operation.getParameters();
    if (CollectionUtils.isNotEmpty(parameters)) {
      for (Parameter parameter : parameters) {
        // 每个参数子节点
        String uuid = UUID.randomUUID().toString();
        ParameterSpec child = ParameterSpec.builder().key(uuid).name(parameter.getName())
          .parentKey(root.getKey()).description(parameter.getDescription()).required(parameter.getRequired()).build();
        if (CollectionUtils.isNotEmpty(parameter.getSchema().getTypes()) && parameter.getSchema().getTypes().contains("array")) {
          root.setType(AttrDataType.ARRAY);
          buildParameterForList(parameter.getSchema(), definitions, root);
        }
        else {
          setType(parameter.getSchema(), child);
          recurSchemaToParam(parameter.getSchema(), definitions, child);
        }
        childes.add(child);
      }
    }
    // 补充 POST 请求时的 requestBody 解析
    RequestBody requestBody = operation.getRequestBody();
    if (requestBody != null) {
      // 根据key=application/json拿value
      Content content = requestBody.getContent();
      for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
        if (APPLICATION_JSON.equals(mediaTypeEntry.getKey())) {
          Schema schema = mediaTypeEntry.getValue().getSchema();
          if (CollectionUtils.isNotEmpty(schema.getTypes()) && schema.getTypes().contains("array")) {
            root.setType(AttrDataType.ARRAY);
            buildParameterForList(schema, definitions, root);
          }
          recurSchemaToParam(schema, definitions, root);
        }
      }
    }
    return JsonUtil.toJsonString(root);
  }

  /**
   * 获取响应参数JSON
   *
   * @param operation swagger Operation
   * @param definitions 定义集合
   * @return 响应参数JSON
   */
  public static String getResponseJson(Operation operation, Map<String, Schema> definitions) {
    // 初始化根节点root
    ParameterSpec root = ParameterSpec.newRoot();
    root.setKey("-1");
    // 获取出参
    Schema schema = getResponseSchema(operation);
    if (schema == null) {
      return JsonUtil.toJsonString(root);
    }
    if (CollectionUtils.isNotEmpty(schema.getTypes()) && schema.getTypes().contains("array")) {
      root.setType(AttrDataType.ARRAY);
      buildParameterForList(schema, definitions, root);
    }
    else {
      recurSchemaToParam(schema, definitions, root);
    }
    return JsonUtil.toJsonString(root);
  }

  /**
   * 获取指定类型的参数JSON（header、path、query）
   *
   * @param operation swagger Operation
   * @param definitions 定义集合
   * @param paramType 参数类型：header、path、query
   * @return 参数JSON
   */
  @Nullable
  public static String getParameterJson(Operation operation, Map<String, Schema> definitions, String paramType) {
    List<Parameter> parameters = operation.getParameters();
    if (CollectionUtils.isEmpty(parameters)) {
      return null;
    }
    // 过滤出指定类型的参数
    List<Parameter> filteredParams = parameters.stream()
      .filter(p -> paramType.equals(p.getIn()))
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(filteredParams)) {
      return null;
    }
    // 初始化根节点root
    ParameterSpec root = ParameterSpec.newRoot();
    root.setKey("-1");
    root.setRequired(false);
    // 参数子节点
    List<ParameterSpec> children = new ArrayList<>();
    root.setChildren(children);
    // 遍历参数
    for (Parameter parameter : filteredParams) {
      String uuid = UUID.randomUUID().toString();
      ParameterSpec child = ParameterSpec.builder()
        .key(uuid)
        .name(parameter.getName())
        .parentKey(root.getKey())
        .description(parameter.getDescription())
        .required(parameter.getRequired())
        .build();
      if (parameter.getSchema() != null) {
        if (CollectionUtils.isNotEmpty(parameter.getSchema().getTypes()) && parameter.getSchema().getTypes().contains("array")) {
          root.setType(AttrDataType.ARRAY);
          buildParameterForList(parameter.getSchema(), definitions, root);
        }
        else {
          setType(parameter.getSchema(), child);
          recurSchemaToParam(parameter.getSchema(), definitions, child);
        }
      }
      children.add(child);
    }
    return JsonUtil.toJsonString(root);
  }

  /**
   * 获取请求体参数JSON
   *
   * @param operation swagger Operation
   * @param definitions 定义集合
   * @return 请求体参数JSON
   */
  @Nullable
  public static String getBodyJson(Operation operation, Map<String, Schema> definitions) {
    RequestBody requestBody = operation.getRequestBody();
    if (requestBody == null) {
      return null;
    }
    // 初始化根节点root
    ParameterSpec root = ParameterSpec.newRoot();
    root.setKey("-1");
    root.setRequired(requestBody.getRequired());
    // 根据key=application/json拿value
    Content content = requestBody.getContent();
    if (content == null) {
      return JsonUtil.toJsonString(root);
    }
    for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
      if (APPLICATION_JSON.equals(mediaTypeEntry.getKey())) {
        Schema schema = mediaTypeEntry.getValue().getSchema();
        if (schema == null) {
          return JsonUtil.toJsonString(root);
        }
        if (CollectionUtils.isNotEmpty(schema.getTypes()) && schema.getTypes().contains("array")) {
          root.setType(AttrDataType.ARRAY);
          buildParameterForList(schema, definitions, root);
        }
        else {
          recurSchemaToParam(schema, definitions, root);
        }
        return JsonUtil.toJsonString(root);
      }
    }
    return JsonUtil.toJsonString(root);
  }

  /**
   * 依据Schema递归设置出入参内容
   *
   * @param schema Schema
   * @param definitions 定义集合
   * @param parent 参数父节点
   */
  private static void recurSchemaToParam(Schema schema, Map<String, Schema> definitions, ParameterSpec parent) {
    recurSchemaToParam(schema, definitions, parent, new ArrayList<>());
  }

  /**
   * 递归处理Schema的核心方法，带循环引用检测
   *
   * @param schema 当前处理的Schema
   * @param definitions 所有定义的Schema集合
   * @param parent 父级参数节点
   * @param visitedRefs 已访问的引用路径，用于检测循环引用
   */
  private static void recurSchemaToParam(Schema schema, Map<String, Schema> definitions, ParameterSpec parent, List<String> visitedRefs) {
    processProperties(schema, definitions, parent, visitedRefs);
    processRefSchema(schema, definitions, parent, visitedRefs);
  }

  /**
   * 处理属性
   */
  @SuppressWarnings("unchecked")
  private static void processProperties(Schema schema, Map<String, Schema> definitions, ParameterSpec parent, List<String> visitedRefs) {
    Map<String, Schema> properties = schema.getProperties();
    if (MapUtils.isEmpty(properties)) {
      return;
    }
    List<ParameterSpec> paramJsons = new ArrayList<>();
    // 遍历属性
    for (Map.Entry<String, Schema> schemaEntry : properties.entrySet()) {
      Schema targetSchema = schemaEntry.getValue();
      ParameterSpec child = ParameterSpec.builder().key(UUID.randomUUID().toString()).description(targetSchema.getDescription())
        .name(schemaEntry.getKey()).parentKey(parent.getKey()).build();
      // 处理数组类型
      if (CollectionUtils.isNotEmpty(targetSchema.getTypes()) && targetSchema.getTypes().contains("array")) {
        processArraySchema(definitions, visitedRefs, paramJsons, targetSchema, child);
      }
      else {
        // 设置节点类型
        setType(targetSchema, child);
        // 设置是否必填
        setMustFlagInProperties(schema, child);
        // 递归处理子节点
        recurSchemaToParam(targetSchema, definitions, child, new ArrayList<>(visitedRefs));
      }
      paramJsons.add(child);
    }
    // 将处理后的子节点添加到父节点中
    List<ParameterSpec> temp = new ArrayList<>(ListUtils.defaultIfNull(parent.getChildren(), Collections.emptyList()));
    temp.addAll(paramJsons);
    parent.setChildren(temp);
  }

  /**
   * 处理数组类型
   */
  private static void processArraySchema(Map<String, Schema> definitions, List<String> visitedRefs,
    List<ParameterSpec> paramJsons, Schema targetSchema,
    ParameterSpec child) {
    child.setType(AttrDataType.ARRAY);
    // 创建数组元素节点
    ParameterSpec sub = ParameterSpec.builder()
      .key(UUID.randomUUID().toString())
      .description(targetSchema.getDescription())
      .name(StringUtils.isEmpty(targetSchema.getName()) ? DEFAULT_NEW_NODE : targetSchema.getName())
      .parentKey(child.getKey())
      .build();

    // 如果数组元素是引用类型，需要检查循环引用
    if (targetSchema.getItems() != null && StringUtils.isNotEmpty(targetSchema.getItems().get$ref())) {
      String nameKey = StringUtils.substring(targetSchema.getItems().get$ref(), targetSchema.getItems().get$ref().lastIndexOf("/") + 1);
      // 检测循环引用，如果已访问过则跳过
      if (visitedRefs.contains(targetSchema.getItems().get$ref()) || (definitions.get(nameKey) != null && isSameDefinition(definitions.get(nameKey),
        targetSchema))) {
        paramJsons.add(child);
        return;
      }
    }
    // 设置子节点
    child.setChildren(Collections.singletonList(sub));
    // 设置数组元素的类型
    if (targetSchema.getItems() != null) {
      setType(targetSchema.getItems(), sub);
      // 递归处理数组元素
      recurSchemaToParam(targetSchema.getItems(), definitions, sub, new ArrayList<>(visitedRefs));
    }
  }

  /**
   * 处理引用类型
   */
  private static void processRefSchema(Schema schema, Map<String, Schema> definitions, ParameterSpec parent, List<String> visitedRefs) {
    if (StringUtils.isEmpty(schema.get$ref())) {
      return;
    }
    // 检测循环引用，如果已访问过则跳过
    if (visitedRefs.contains(schema.get$ref())) {
      return;
    }
    // 记录当前引用路径
    visitedRefs.add(schema.get$ref());
    String nameKey = schema.get$ref().substring(schema.get$ref().lastIndexOf("/") + 1);
    // 如果父节点没有名称，则使用定义名称
    if (StringUtils.isEmpty(parent.getName())) {
      parent.setName(nameKey);
    }
    Schema targetSchema = definitions.get(nameKey);
    if (targetSchema != null) {
      setType(targetSchema, parent);
      // 递归处理引用指向的Schema
      recurSchemaToParam(targetSchema, definitions, parent, visitedRefs);
    }
    // 回溯时移除当前引用路径
    visitedRefs.remove(visitedRefs.size() - 1);
  }

  /**
   * 判断两个Schema是否为相同定义
   */
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  private static boolean isSameDefinition(Schema schema1, Schema schema2) {
    if (schema1.get$ref() != null && schema2.get$ref() != null) {
      return schema1.get$ref().equals(schema2.get$ref());
    }
    return schema1 == schema2;
  }

  /**
   * 设置节点类型
   */
  private static void setType(Schema schema, ParameterSpec parameterSpec) {
    Set types = schema.getTypes();
    if (types != null && types.size() == 1) {
      parameterSpec.setType(AttrDataType.ofCode(types.iterator().next().toString()));
    }
    else  {
      parameterSpec.setType(AttrDataType.ANY);
    }
  }

  @SuppressWarnings("unchecked")
  private static void setMustFlagInProperties(Schema schema, ParameterSpec children) {
    children.setRequired(false);
    if (CollectionUtils.isNotEmpty(schema.getRequired())) {
      schema.getRequired().forEach(k -> {
        if (k.equals(children.getName())) {
          children.setRequired(true);
        }
      });
    }
  }

  private static void buildParameterForList(Schema parent, Map<String, Schema> definitions, ParameterSpec root) {
    Schema subSchema = parent.getItems();
    String uuId = UUID.randomUUID().toString();
    if (CollectionUtils.isNotEmpty(subSchema.getTypes()) && (subSchema.getTypes().contains("string") || subSchema.getTypes().contains("integer") || subSchema.getTypes()
      .contains("boolean"))) {
      //@formatter:off
      ParameterSpec sub = ParameterSpec.builder()
        .key(uuId)
        .name(DEFAULT_NEW_NODE)
        .parentKey(root.getKey())
        .description(subSchema.getDescription())
        .required(false)
        .build();
      //@formatter:on
      setType(subSchema, sub);
      root.setChildren(Collections.singletonList(sub));
    }
    else {
      //@formatter:off
      ParameterSpec sub = ParameterSpec.builder()
        .key(uuId)
        .type(AttrDataType.OBJECT)
        .parentKey(root.getKey())
        .build();
      //@formatter:on
      root.setChildren(Collections.singletonList(sub));
      recurSchemaToParam(subSchema, definitions, sub, new ArrayList<>());
    }
  }
}
