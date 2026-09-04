package com.iwhalecloud.bote.service.orchestration.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bote.dto.orchestration.test.ExtractNodeParamsRequest;
import com.iwhalecloud.bote.dto.orchestration.test.TestNodeRequest;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 工作流节点调试辅助类
 *
 * @author bianjp
 * @since 2025-08-04
 */
public final class NodeDebugHelper {
  private NodeDebugHelper() {
  }

  /** 支持单节点调试的节点类型列表 */
  public static final List<String> SUPPORTED_STEP_TYPES = ImmutableList.of(
    StepType.IF,
    StepType.SERVICE,
    StepType.SQL,
    StepType.WORKFLOW,
    StepType.PLUGIN,
    StepType.PAGE,
    StepType.TOOLBOX,
    StepType.SCRIPT,
    StepType.LLM,
    StepType.AGENT,
    StepType.A2A,
    StepType.MCP_TOOL,
    StepType.LLM_SKILL,
    StepType.QUESTION_CLASSIFIER,
    StepType.PARAM_EXTRACTOR,
    StepType.KNOWLEDGE_RETRIEVAL,
    StepType.KNOWLEDGE_CHAT,
    StepType.SLM_RETRIEVAL
  );
  /** 需要使用流式输出的节点类型列表 */
  public static final List<String> STREAM_STEP_TYPES = ImmutableList.of(
    // 注意: 只有对话型工作流使用流式，任务型工作流使用非流式
    StepType.WORKFLOW,
    StepType.PAGE,
    StepType.LLM,
    StepType.AGENT,
    StepType.A2A,
    StepType.KNOWLEDGE_CHAT
  );
  /** 固定节点出参映射文件 */
  private static final Resource STATIC_STEP_OUTPUT_RESOURCE = new ClassPathResource("flow/static-step-output.json");
  /** 固定的节点出参映射，key 为节点类型 */
  private static final Map<String, ParameterSpec> staticStepOutputMap = loadStaticStepOutput();
  /** 参数引用匹配模式 */
  private static final List<Pattern> REFERENCE_PATTERNS = ImmutableList.of(
    // 整个属性值是引用: $.expression
    Pattern.compile("\"\\$\\.([\\w._\\[\\]]+)\""),
    // 模板字符串中的引用: ${expression}
    Pattern.compile("\\$\\{([\\w._\\[\\]$]+)}")
  );
  /** 特殊的引用表达式 */
  private static final List<String> SPECIAL_REFERENCES = ImmutableList.of("input", "variable", "context", "input.USER_INPUT", "system.query", "system.fileIds", "system.fileIds.[0]");
  /** 合法的引用表达式前缀 */
  private static final List<String> VALID_REFERENCE_PREFIXES = ImmutableList.of("input.", "step.", "loop.", "variable.", "context.");

  private static final ITenantSettingInfoManageService tenantSettingInfoManageService = SpringUtil.getBean(ITenantSettingInfoManageService.class);

  /**
   * 加载节点出参
   */
  private static Map<String, ParameterSpec> loadStaticStepOutput() {
    try (InputStream inputStream = STATIC_STEP_OUTPUT_RESOURCE.getInputStream()) {
      Map<String, List<ParameterSpec>> map = JsonUtil.parseJsonRequired(inputStream, new TypeReference<Map<String, List<ParameterSpec>>>() {
      });
      return map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> ParameterSpec.newRoot(e.getValue())));
    }
    catch (IOException e) {
      throw new BssException("加载节点出参失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 获取固定的节点出参结构
   *
   * @param stepType 节点类型
   * @return 节点出参结构，非固定节点返回 null
   */
  @Nullable
  public static ParameterSpec getStaticStepOutput(String stepType) {
    return staticStepOutputMap.get(stepType);
  }

  /**
   * 提取节点入参（调试节点时需要输入的参数）
   *
   * @return 参数列表
   */
  public static List<ParameterSpec> extractInputParams(ExtractNodeParamsRequest request) {
    AbstractStep step = convertStep(request.getTenantId(), request.getNodeCode(), request.getGraph());
    Assert.isTrue(SUPPORTED_STEP_TYPES.contains(step.getType()), () -> "节点【" + step.getName() + "】不支持单节点调试");
    // 提取引用表达式列表
    List<String> expressions = extractReferences(request, step);
    if (expressions.isEmpty()) {
      return Collections.emptyList();
    }

    // 根据引用表达式生成参数规格
    List<ParameterSpec> params = new ArrayList<>(expressions.size());
    for (String expression : expressions) {
      ParameterSpec param = parseReference(expression, request.getTenantId(), request.getGraph(), request.getRequest(), request.getVariables());
      // 清除参数规格中的无用属性
      AbstractNodeConverter.simplifyParameter(param);
      params.add(param);
    }
    return params;
  }

  /**
   * 转换节点
   */
  @SuppressWarnings("rawtypes")
  private static AbstractStep convertStep(Long tenantId, String nodeCode, SceneGraphDTO graph) {
    SceneGraphNodeDTO node = IterableUtils.find(graph.getNodes(), n -> nodeCode.equals(n.getNodeCode()));
    Assert.notNull(node, () -> "节点不存在: " + nodeCode);
    AbstractStepConverter converter = SceneStepRegistry.getConverter(node.getNodeType());
    return converter.convert(node, new ConverterContext(tenantId, graph, true));
  }

  /**
   * 提取节点配置中的引用表达式列表
   */
  private static List<String> extractReferences(ExtractNodeParamsRequest request, AbstractStep step) {
    // 转为 JSON 字符串，从字符串中提取引用表达式，以兼容各种节点
    String json = JsonUtil.toJsonStringCompact(step);
    // 应用表达式集合，自动去重
    Set<String> expressions = new LinkedHashSet<>();
    Matcher matcher;
    String expression;
    for (Pattern pattern : REFERENCE_PATTERNS) {
      matcher = pattern.matcher(json);
      while (matcher.find()) {
        expression = matcher.group(1);
        if (SPECIAL_REFERENCES.contains(expression)) {
          expressions.add(normalizeSpecialReference(expression));
        }
        else if (VALID_REFERENCE_PREFIXES.stream().anyMatch(expression::startsWith)) {
          expressions.add(expression);
        }
      }
    }

    // 对话型工作流节点内部可能引用了系统变量中的用户输入、文件 ID，从节点入参中无法识别到，暂时固定加上
    if (step instanceof WorkflowStep) {
      SceneGraphNodeDTO node = IterableUtils.find(request.getGraph().getNodes(), n -> step.getCode().equals(n.getNodeCode()));
      if (SceneConsts.FLOW_TYPE_MULTI_STEP.equals(MapUtils.getString(node.getNodeData(), "flowType"))) {
        expressions.add("input.USER_INPUT");
        expressions.add("system.fileIds");
      }
    }

    // 过滤引用，有相同前缀的只保留最短的一个，比如 input.customer.name 和 input.customer 只保留 input.customer
    List<String> filteredExpressions = new ArrayList<>();
    for (String item : expressions) {
      String prefix = item + ".";
      if (expressions.stream().noneMatch(r -> r.startsWith(prefix))) {
        filteredExpressions.add(item);
      }
    }
    return filteredExpressions;
  }

  /**
   * 规范特殊的引用表达式
   */
  private static String normalizeSpecialReference(String expression) {
    if ("system.query".equals(expression)) {
      return "input.USER_INPUT";
    }
    if (expression.startsWith("system.fileIds")) {
      return "system.fileIds";
    }
    return expression;
  }

  /**
   * 解析引用表达式，生成参数规格
   */
  private static ParameterSpec parseReference(String expression, Long tenantId, SceneGraphDTO graph, @Nullable ParameterSpec input, @Nullable List<ParameterSpec> variables) {
    // 特殊表达式
    if (SPECIAL_REFERENCES.contains(expression)) {
      return parseSpecialReference(expression, tenantId, input, variables);
    }

    String[] pieces = StringUtils.split(expression, '.');
    Assert.isTrue(pieces.length > 1, () -> "非法的引用表达式: " + expression);
    switch (pieces[0]) {
      case "input":
        return findNestedParam(input, expression, ArrayUtils.subarray(pieces, 1, pieces.length));
      case "step":
        return parseStepOutputReference(expression, pieces[1], ArrayUtils.subarray(pieces, 2, pieces.length), graph);
      case "loop":
        return parseLoopVariableReference(expression, tenantId, pieces[1], ArrayUtils.subarray(pieces, 2, pieces.length), graph, input, variables);
      case "variable":
        return findNestedParam(ParameterSpec.newRoot(variables), expression, ArrayUtils.subarray(pieces, 1, pieces.length));
      case "context":
        return findNestedParam(ParameterSpec.newRoot(getContextVariables(tenantId)), expression, ArrayUtils.subarray(pieces, 1, pieces.length));
      default:
        throw new BssException("不支持的引用表达式类型：" + expression);
    }
  }

  /**
   * 解析特殊的引用表达式
   */
  private static ParameterSpec parseSpecialReference(String expression, Long tenantId, @Nullable ParameterSpec input, @Nullable List<ParameterSpec> variables) {
    switch (expression) {
      case "input":
        return input != null ? input.toBuilder().name("input").description("入参").build() : ParameterSpec.newObject("input", "入参", null);
      case "variable":
        return ParameterSpec.newObject("variable", "变量", variables);
      case "context":
        return ParameterSpec.newObject("context", "上下文参数", getContextVariables(tenantId));
      case "input.USER_INPUT":
        return ParameterSpec.newProperty("input.USER_INPUT", "用户输入", AttrDataType.STRING);
      case "system.fileIds":
        return ParameterSpec.newList("system.fileIds", "用户上传的文件 ID 列表", ParameterSpec.newProperty("fileId", "文件 ID", AttrDataType.INTEGER));
      default:
        throw new IllegalArgumentException("未知的特殊引用表达式: " + expression);
    }
  }

  /**
   * 解析节点出参引用表达式
   */
  private static ParameterSpec parseStepOutputReference(String expression, String nodeCode, String[] pieces, SceneGraphDTO graph) {
    ParameterSpec stepOutput = getStepOutput(graph, nodeCode);
    if (pieces.length == 0) {
      return stepOutput;
    }
    return findNestedParam(stepOutput, expression, pieces);
  }

  /**
   * 获取节点的出参结构
   */
  private static ParameterSpec getStepOutput(SceneGraphDTO graph, String nodeCode) {
    SceneGraphNodeDTO node = IterableUtils.find(graph.getNodes(), n -> nodeCode.equals(n.getNodeCode()));
    Assert.notNull(node, () -> "节点不存在：" + nodeCode);
    // 部分节点的出参结构是固定的
    ParameterSpec spec = staticStepOutputMap.get(node.getNodeType());
    if (spec != null) {
      return spec;
    }
    Object outData = node.getNodeData().get("outData");
    return outData == null ? ParameterSpec.newRoot() : JsonUtil.convert(outData, ParameterSpec.class);
  }

  /**
   * 获取租户的上下文变量列表
   */
  private static List<ParameterSpec> getContextVariables(Long tenantId) {
    TenantSettingInfoDTO tenantSettingInfo = tenantSettingInfoManageService.findTenantSettingInfo(tenantId, BaseConsts.FUNC_TYPE_CHAT_CONTEXT);
    if (tenantSettingInfo == null || StringUtils.isEmpty(tenantSettingInfo.getSettingInfo())) {
      return Collections.emptyList();
    }
    List<Map<String, String>> contextVariables = JsonUtil.parseJsonRequired(tenantSettingInfo.getSettingInfo(), new TypeReference<List<Map<String, String>>>() {
    });
    return contextVariables.stream()
      .map(m -> ParameterSpec.newProperty(m.get("code"), m.get("name"), AttrDataType.ANY))
      .collect(Collectors.toList());
  }

  /**
   * 解析循环变量引用表达式
   */
  private static ParameterSpec parseLoopVariableReference(String expression, Long tenantId, String nodeCode, String[] pieces, SceneGraphDTO graph, @Nullable ParameterSpec input, @Nullable List<ParameterSpec> variables) {
    SceneGraphNodeDTO node = IterableUtils.find(graph.getNodes(), n -> nodeCode.equals(n.getNodeCode()));
    Assert.notNull(node, () -> "节点不存在：" + nodeCode);
    String loopType = MapUtils.getString(node.getNodeData(), "loopType");
    // 范围循环
    if (SceneConsts.LOOP_TYPE_RANGE.equals(loopType)) {
      return ParameterSpec.newProperty(expression, "循环变量", AttrDataType.INTEGER);
    }
    // 对象循环
    if (SceneConsts.LOOP_TYPE_OBJECT.equals(loopType)) {
      ParameterSpec loopVariableSpec;
      loopVariableSpec = ParameterSpec.newRoot(ImmutableList.of(ParameterSpec.newProperty("key", "键", AttrDataType.STRING),
        ParameterSpec.newProperty("value", "值", AttrDataType.ANY)));
      return findNestedParam(loopVariableSpec, expression, pieces);
    }
    // 列表循环
    String list = MapUtils.getString(node.getNodeData(), "list");
    Assert.isTrue(Strings.CS.startsWith(list, "$."), () -> "循环节点【" + nodeCode + "】的列表表达式必须以 $. 开头：" + list);
    // 获取循环列表的结构
    ParameterSpec listSpec = parseReference(list.substring(2), tenantId, graph, input, variables);
    if (!listSpec.isList() || !listSpec.hasChildren()) {
      return ParameterSpec.newProperty(expression, "循环变量", AttrDataType.ANY);
    }
    return findNestedParam(listSpec.getArrayElement(), expression, pieces);
  }


  /**
   * 获取嵌套参数的参数规格
   */
  private static ParameterSpec findNestedParam(@Nullable ParameterSpec root, String expression, String[] pieces) {
    if (root == null) {
      return ParameterSpec.newProperty(expression, null, AttrDataType.ANY);
    }
    if (pieces.length == 0) {
      return root.toBuilder().name(expression).description(null).build();
    }
    ParameterSpec param = root;
    for (String piece : pieces) {
      // [0] 表示引用列表的第一个元素
      if ("[0]".equals(piece)) {
        param = param.getArrayElement();
      }
      else {
        param = IterableUtils.find(param.getChildren(), p -> piece.equals(p.getName()));
      }
      if (param == null) {
        break;
      }
    }
    if (param == null) {
      return ParameterSpec.newProperty(expression, null, AttrDataType.ANY);
    }
    return param.toBuilder().name(expression).build();
  }

  /**
   * 根据节点参数模拟上下文
   *
   * @return 模拟上下文
   */
  public static SceneOrchestrationContext mockContext(TestNodeRequest request) {
    // 转换节点
    AbstractStep step = convertStep(request.getTenantId(), request.getNodeCode(), request.getGraph());
    SceneDslDTO dsl = new SceneDslDTO();
    dsl.setChatflow(request.getStream());
    dsl.setSteps(Collections.singletonList(step));

    // 请求对象
    OrchestrationEngineRequest engineRequest = new OrchestrationEngineRequest();
    engineRequest.setDebug(true);
    engineRequest.setDebugInnerService(true);
    engineRequest.setContextParams(new LinkedHashMap<>());
    engineRequest.setTenantId(request.getTenantId());
    // 单节点调试暂不支持多轮会话，不需要持久化流程变量
    engineRequest.setPersistContext(false);
    engineRequest.setContextId(SceneContextUtil.newContextId());

    // 上下文对象
    SceneOrchestrationContext context = new SceneOrchestrationContext();
    context.setRequest(engineRequest);
    context.setDsl(dsl);
    context.setParsedInputParameters(new LinkedHashMap<>());
    context.initRunLog();

    // 转换参数
    Map<String, Object> convertedParams;
    if (SceneConsts.DEBUG_PARAMS_JSON.equals(request.getFormat())) {
      convertedParams = ParamConverterUtil.convertRoot(ParameterSpec.newRoot(request.getParamSpecs()), request.getParams());
    }
    else {
      convertedParams = NodeParamConvertHelper.convertParamsToJson(request.getParamSpecs(), false);
    }
    // 填充参数
    for (Entry<String, Object> entry : convertedParams.entrySet()) {
      String path = entry.getKey();
      Object value = entry.getValue();
      if (value == null) {
        continue;
      }
      if ("input.USER_INPUT".equals(path)) {
        engineRequest.setMessageContent(Objects.toString(value, null));
      }
      else if ("system.fileIds".equals(path)) {
        engineRequest.setFileIds(JsonUtil.convert(value, new TypeReference<List<Long>>() {
        }));
      }
      else {
        fillParam(context, path, value);
      }
    }
    return context;
  }

  /**
   * 填充参数
   */
  private static void fillParam(SceneOrchestrationContext context, String path, Object value) {
    String[] pieces = StringUtils.split(path, '.');
    String[] remainingPieces = ArrayUtils.subarray(pieces, 1, pieces.length);
    switch (pieces[0]) {
      case "input":
        fillNestedProperty(context.getParsedInputParameters(), remainingPieces, value);
        break;
      case "step":
        fillNestedProperty(context.getStepOutput(), remainingPieces, value);
        break;
      case "loop":
        fillNestedProperty(context.getLoopVariables(), remainingPieces, value);
        break;
      case "variable":
        fillNestedProperty(context.getGlobalVariables(), remainingPieces, value);
        break;
      case "context":
        fillNestedProperty(context.getRequest().getContextParams(), remainingPieces, value);
        break;
      default:
        throw new BssException("不支持的引用表达式类型：" + path);
    }
  }

  /**
   * 填充嵌套属性
   *
   * @param map 要填充的 map
   * @param pieces 嵌套属性路径，中间层级的属性可能是对象、列表，指定列表元素的属性形式为 "[索引]"。比如 ["customer", "name"] 表示 map 中 customer 对象的 name 属性，["customers", "[0]", "name"] 表示 map 中 customers 列表的第一个元素的 name 属性
   * @param value 属性值
   */
  @SuppressWarnings("unchecked")
  private static void fillNestedProperty(Map<String, Object> map, String[] pieces, Object value) {
    if (pieces.length == 1) {
      map.put(pieces[0], value);
      return;
    }

    // 创建中间层级的属性
    Object lastContainer = createIntermediateProperties(map, pieces);

    // 设置最后一级属性的值
    String lastPiece = pieces[pieces.length - 1];
    if (isArrayIndex(lastPiece)) {
      int index = parseArrayIndex(lastPiece);
      List<Object> list = (List<Object>) lastContainer;
      // 确保列表有足够的元素
      while (list.size() <= index) {
        list.add(null);
      }
      list.set(index, value);
    }
    else {
      ((Map<String, Object>) lastContainer).put(lastPiece, value);
    }
  }

  /**
   * 创建中间层级的属性
   */
  @SuppressWarnings("unchecked")
  private static Object createIntermediateProperties(Map<String, Object> map, String[] pieces) {
    // 当前层级的对象/列表
    Object current = map;
    for (int i = 0; i < pieces.length - 1; i++) {
      String piece = pieces[i];
      String nextPiece = pieces[i + 1];
      // 当前是数组索引
      if (isArrayIndex(piece)) {
        int index = parseArrayIndex(piece);
        List<Object> list = (List<Object>) current;
        // 确保列表有足够的元素
        while (list.size() <= index) {
          list.add(null);
        }
        // 获取或创建下一层级的对象
        Object next = list.get(index);
        if (next == null) {
          next = createListOrObject(nextPiece);
          list.set(index, next);
        }
        current = next;
      }
      // 当前是对象属性
      else {
        Map<String, Object> currentMap = (Map<String, Object>) current;
        // 获取或创建下一层级的对象
        Object next = currentMap.get(piece);
        if (next == null) {
          next = createListOrObject(nextPiece);
          currentMap.put(piece, next);
        }
        current = next;
      }
    }
    return current;
  }

  /**
   * 创建列表或对象
   */
  private static Object createListOrObject(String nextPiece) {
    // 如果下一级属性是数组索引，则创建列表
    if (isArrayIndex(nextPiece)) {
      return new ArrayList<>();
    }
    return new LinkedHashMap<String, Object>();
  }

  /**
   * 判断是否为数组索引格式 "[数字]"
   */
  private static boolean isArrayIndex(String piece) {
    return piece.startsWith("[") && piece.endsWith("]");
  }

  /**
   * 解析数组索引，从 "[0]" 格式中提取数字
   */
  private static int parseArrayIndex(String piece) {
    return Integer.parseInt(piece.substring(1, piece.length() - 1));
  }

}
