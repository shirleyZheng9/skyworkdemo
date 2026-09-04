/*
 * Copyright 2023-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iwhalecloud.bote.agent.tool.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.annotation.ToolRequest;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * 工具加载工具类
 *
 * @author bianjp
 * @since 2026-03-09
 */
public final class ToolUtils {
  /** 工具名称规则，以小写字母开头，只能包含字母、数字、下划线，长度为 1~50 */
  private static final Pattern TOOL_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9_]{0,49}$");
  /** JSON Schema 生成器 */
  private static final SchemaGenerator schemaGenerator = createSchemaGenerator();

  private ToolUtils() {
  }

  /**
   * 根据方法生成工具
   *
   * @param method 方法
   * @return 工具
   */
  public static Pair<com.iwhalecloud.bote.llm.client.dto.Tool, Tool> loadTool(Method method) {
    Assert.notNull(method, "method cannot be null");
    Tool tool = AnnotatedElementUtils.findMergedAnnotation(method, Tool.class);
    Assert.notNull(tool, () -> "方法 " + method.getDeclaringClass().getName() + "." + method.getName() + " 缺少 @Tool 注解");
    String name = tool.name();
    String description = tool.description();
    Assert.hasLength(name, () -> "方法 " + method.getDeclaringClass().getName() + "." + method.getName() + " 的 @Tool 注解未指定非空的 name");
    Assert.hasLength(description, () -> "方法 " + method.getDeclaringClass().getName() + "." + method.getName() + " 的 @Tool 注解未指定非空的 description");
    Assert.isTrue(TOOL_NAME_PATTERN.matcher(name).matches(), () -> "方法 " + method.getDeclaringClass().getName() + "." + method.getName() + " 的 @Tool 注解指定的 name 不合法");
    JsonSchemaNode parameters = generateForMethodInput(method);
    return Pair.of(new com.iwhalecloud.bote.llm.client.dto.Tool(name, description, parameters), tool);
  }

  /**
   * 根据方法参数生成 JSON Schema
   */
  @Nullable
  private static JsonSchemaNode generateForMethodInput(Method method) {
    // 优先处理 ToolRequest 注解
    Parameter toolRequestParameter = Arrays.stream(method.getParameters()).filter(p -> p.getAnnotation(ToolRequest.class) != null).findFirst().orElse(null);
    if (toolRequestParameter != null) {
      ObjectNode parameterNode = schemaGenerator.generateSchema(toolRequestParameter.getParameterizedType());
      return JsonUtil.convert(parameterNode, JsonSchemaNode.class);
    }

    Map<String, JsonSchemaNode> properties = new LinkedHashMap<>();
    List<String> required = new ArrayList<>();
    for (int i = 0; i < method.getParameterCount(); i++) {
      Parameter parameter = method.getParameters()[i];
      String parameterName = parameter.getName();
      Type parameterType = method.getGenericParameterTypes()[i];
      if (parameterType instanceof Class<?> parameterClass && ClassUtils.isAssignable(ToolContext.class, parameterClass)) {
        continue;
      }
      ToolParam toolParamAnnotation = parameter.getAnnotation(ToolParam.class);
      Assert.notNull(toolParamAnnotation, () -> "方法 " + method.getDeclaringClass().getName() + "." + method.getName() + " 的参数 " + parameterName + " 缺少 @ToolParam 注解");
      if (isMethodParameterRequired(parameter, toolParamAnnotation)) {
        required.add(parameterName);
      }
      ObjectNode parameterNode = schemaGenerator.generateSchema(parameterType);
      parameterNode.remove("format");
      String description = StringUtils.trimToNull(toolParamAnnotation.description());
      if (description != null) {
        parameterNode.put("description", description);
      }
      JsonSchemaNode node = JsonUtil.convert(parameterNode, JsonSchemaNode.class);
      if (toolParamAnnotation.enumValues().length > 0) {
        node.setEnumValues(toolParamAnnotation.enumValues());
      }
      properties.put(parameterName, node);
    }

    if (properties.isEmpty()) {
      return null;
    }
    return JsonSchemaNode.newObject(null, properties, required);
  }

  /**
   * 检查参数是否必填
   */
  private static boolean isMethodParameterRequired(Parameter parameter, ToolParam toolParamAnnotation) {
    // nullable 注解表示非必填
    if (parameter.getAnnotation(Nullable.class) != null) {
      return false;
    }
    return toolParamAnnotation.required();
  }

  /**
   * 构造 JSON Schema 生成器
   */
  private static SchemaGenerator createSchemaGenerator() {
    SchemaGeneratorConfigBuilder schemaGeneratorConfigBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
      .with(new JacksonModule())
      .with(new Swagger2Module())
      // 所有 schema 内联，避免生成 $ref (JsonSchemaNode 不支持)
      .with(Option.INLINE_ALL_SCHEMAS)
      .with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
      .with(Option.PLAIN_DEFINITION_KEYS);
    SchemaGeneratorConfig subtypeSchemaGeneratorConfig = schemaGeneratorConfigBuilder
      .without(Option.SCHEMA_VERSION_INDICATOR)
      .build();
    return new SchemaGenerator(subtypeSchemaGeneratorConfig);
  }

  /**
   * 从 yaml 文件加载工具配置
   */
  private static Map<String, com.iwhalecloud.bote.llm.client.dto.Tool> loadToolsFromYaml() {
    Map<String, com.iwhalecloud.bote.llm.client.dto.Tool> toolsMap = new LinkedHashMap<>();
    loadYaml(new ClassPathResource("agent/tools/file-system-tools.yaml"), toolsMap);
    return toolsMap;
  }

  /**
   * 加载 yaml 文件中的工具配置
   */
  private static void loadYaml(ClassPathResource resource, Map<String, com.iwhalecloud.bote.llm.client.dto.Tool> toolsMap) {
    try (InputStream inputStream = resource.getInputStream()) {
      List<Map<String, Object>> functions = new Yaml().load(inputStream);
      for (Map<String, Object> item : functions) {
        Function function = JsonUtil.convert(item, Function.class);
        toolsMap.put(function.getName(), new com.iwhalecloud.bote.llm.client.dto.Tool(function));
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
