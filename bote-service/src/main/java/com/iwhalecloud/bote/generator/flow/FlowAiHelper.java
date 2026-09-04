package com.iwhalecloud.bote.generator.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ApiParamUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.ExplainFlowParamsDTO;
import com.iwhalecloud.bote.dto.generator.flow.GenerateFlowParamsDTO;
import com.iwhalecloud.bote.dto.generator.flow.SaveFlowRequestDTO;
import com.iwhalecloud.bote.dto.generator.flow.SimplifiedFlowDTO;
import com.iwhalecloud.bote.dto.generator.flow.SimplifiedNodeDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.consts.ResponseFormatType;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bote.mapper.generator.FlowAiQueryMapper;
import com.iwhalecloud.bote.mapper.skill.SkillFlowManageMapper;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 流程 AI 辅助功能
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Service
@RequiredArgsConstructor
public class FlowAiHelper {
  private static final Logger logger = LoggerFactory.getLogger(FlowAiHelper.class);
  /** 提示词模板 */
  private static final ClassPathResource PROMPT_TEMPLATE = new ClassPathResource("prompt/generator/flow/template.md.ftl");
  /** 解释流程的提示词前缀 */
  private static final ClassPathResource PROMPT_PREFIX_EXPLAIN = new ClassPathResource("prompt/generator/flow/prefix-explain.md");
  /** 生成流程的提示词前缀 */
  private static final ClassPathResource PROMPT_PREFIX_GENERATE = new ClassPathResource("prompt/generator/flow/prefix-generate.md");

  private final SkillFlowManageMapper flowManageMapper;
  private final ISkillFlowManageService flowManageService;
  private final FlowConverter flowConverter;
  private final FlowAiQueryMapper flowAiQueryMapper;
  private final ModelClientCache modelClientCache;

  /** 技能提示词生成器映射, key 为技能类型 */
  private final Map<String, TriConsumer<StringBuilder, Long, List<Long>>> skillPromptGeneratorMap = ImmutableMap.<String, TriConsumer<StringBuilder, Long, List<Long>>>builder()
    .put(StepType.SERVICE, this::addServices)
    .put(StepType.SQL, this::addSqlServices)
    .put(StepType.LLM_SKILL, this::addLlmSkill)
    .put(StepType.TOOLBOX, this::addToolbox)
    .put(StepType.WORKFLOW, this::addWorkflow)
    .put(StepType.PAGE, this::addPage)
    .put(StepType.PAGE_FUNC, this::addPageFunc)
    .put(StepType.KNOWLEDGE_CHAT, this::addKnowledge)
    .build();
  /** 技能 ID 提取器映射，key 为技能类型 */
  private final Map<String, Function<Map<String, Object>, Long>> skillIdExtractorMap = ImmutableMap.<String, Function<Map<String, Object>, Long>>builder()
    .put(StepType.PAGE, nodeData -> MapUtils.getLong(nodeData, "pageId"))
    .put(StepType.PAGE_FUNC, nodeData -> MapUtils.getLong(nodeData, "pageFuncId"))
    .put(StepType.TOOLBOX, nodeData -> MapUtils.getLong(nodeData, "funcId"))
    .put(StepType.SERVICE, nodeData -> MapUtils.getLong(nodeData, "serviceId"))
    .put(StepType.SQL, nodeData -> MapUtils.getLong(nodeData, "sqlId"))
    .put(StepType.LLM_SKILL, nodeData -> MapUtils.getLong(nodeData, "apiId"))
    .put(StepType.WORKFLOW, nodeData -> MapUtils.getLong(nodeData, "flowId"))
    .put(StepType.KNOWLEDGE_CHAT, nodeData -> MapUtils.getLong(nodeData, "knowledgeId"))
    .put(StepType.KNOWLEDGE_RETRIEVAL, nodeData -> MapUtils.getLong(nodeData, "knowledgeId"))
    .build();

  /**
   * 生成流程
   *
   * <p>使用流式输出，一来在生成过程中给前端一些反馈，二来避免调用大模型超时（耗时较长，非流式输出容易触发请求超时）</p>
   *
   * @param params 生成参数
   * @param partialHandler 片段处理器
   * @return 生成的流程
   */
  public SkillFlowDTO generate(GenerateFlowParamsDTO params, Consumer<AssistantMessage> partialHandler) {
    ChatCompletionRequest chatCompletionRequest = buildGenerateRequest(params);
    // 收集大模型流式输出的完整内容，用来提取流程
    StringBuilder sb = new StringBuilder();

    // 调用大模型
    LlmClient client = getModelClient(params.getTenantId());
    Consumer<ChatCompletionResponse> eventHandler = response -> {
      AssistantMessage delta = response.getDelta();
      if (delta != null && !delta.isEmpty()) {
        // 收集输出内容
        if (StringUtils.isNotEmpty(delta.getContent())) {
          sb.append(delta.getContent());
        }
        partialHandler.accept(delta);
      }
    };
    client.chatCompletionStreamBlocking(chatCompletionRequest, eventHandler, SseUtil.requestListener);
    // 提取流程
    return extractGeneratedFlow(params, sb.toString());
  }

  /**
   * 智能生成流程并保存（新增或修改）
   *
   * @param request 请求参数
   * @return 保存结果
   */
  public ResultVO<SkillFlowDTO> saveFlow(SaveFlowRequestDTO request) {
    Long tenantId = request.getTenantId();
    Long flowId = request.getFlowId();
    SkillFlowDTO oldFlow = flowId != null ? flowManageService.findSkillFlow(tenantId, flowId) : null;
    GenerateFlowParamsDTO generateParams = new GenerateFlowParamsDTO();
    generateParams.setTenantId(tenantId);
    if (StringUtils.isNotEmpty(request.getFlowType())) {
      generateParams.setFlowType(request.getFlowType());
    }
    else if (oldFlow != null) {
      generateParams.setFlowType(oldFlow.getFlowType());
    }
    else {
      generateParams.setFlowType(SceneConsts.FLOW_TYPE_ONE_STEP);
    }
    generateParams.setPrompt(request.getPrompt());
    ChatCompletionRequest chatCompletionRequest = buildGenerateRequest(generateParams);
    // 调用大模型
    LlmClient client = getModelClient(tenantId);
    ChatCompletionResponse response = client.chatCompletion(chatCompletionRequest);
    // 提取流程
    SkillFlowDTO flow = extractGeneratedFlow(generateParams, response.getMessageContent());
    if (oldFlow != null) {
      // 只修改流程图、变量、出入参
      oldFlow.setGraph(flow.getGraph());
      oldFlow.setRequest(flow.getRequest());
      oldFlow.setResponse(flow.getResponse());
      oldFlow.setVariables(flow.getVariables());
      flow = oldFlow;
    }
    else {
      // 如果流程编码已存在，则添加随机后缀
      if (flowManageMapper.existsSkillFlowCode(flow.getTenantId(), flow.getFlowCode())) {
        flow.setFlowCode(flow.getFlowCode() + RandomStringUtils.secure().nextNumeric(5));
      }
      flow.setCatalogItemId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
      flow.setTenantId(tenantId);
    }
    // 优先使用请求参数中的流程名称
    flow.setFlowName(StringUtils.defaultIfEmpty(request.getFlowName(), flow.getFlowName()));
    // 保存功能会用到线程本地变量
    TenantIdUtil.setTenantId(tenantId);
    ResultVO<SkillFlowDTO> result = flowManageService.saveSkillFlow(flow);
    // 没有变化时当作成功
    if (BaseErrorConstant.NO_DIFFERENCE.getErrorConstant().getCode().equals(result.getResultCode())) {
      return ResultVO.success(flow);
    }
    return result;
  }

  /**
   * 构造生成流程的大模型请求
   */
  private ChatCompletionRequest buildGenerateRequest(GenerateFlowParamsDTO params) {
    // 系统提示词
    String systemPrompt = buildSystemPrompt(true, params.getFlowType());
    // 用户提示词
    String userPrompt = buildUserPrompt(params.getTenantId(), params.getPrompt(), params.getSkillIdsMap());
    return ChatCompletionRequest.builder()
      .addSystemMessage(systemPrompt)
      .addUserMessage(userPrompt)
      .responseFormatType(ResponseFormatType.JSON_OBJECT)
      .build();
  }

  /**
   * 提取生成的流程
   */
  private SkillFlowDTO extractGeneratedFlow(GenerateFlowParamsDTO params, String content) {
    try {
      logger.trace("Generated flow: {}", content);
      SimplifiedFlowDTO simplifiedFlow = extractSimplifiedFlow(params.getTenantId(), content);
      SkillFlowDTO flow = flowConverter.revert(params.getTenantId(), simplifiedFlow, params.getFlowType());
      flow.setFlowType(params.getFlowType());
      return flow;
    }
    catch (BssException e) {
      logger.warn("Failed to generate flow: params={}, generated={}", params, content, e);
      throw e;
    }
    catch (RuntimeException e) {
      logger.error("Failed to generate flow: params={}, generated={}", params, content, e);
      throw new BssException("生成流程失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 提取简化版流程
   */
  private SimplifiedFlowDTO extractSimplifiedFlow(Long tenantId, String content) {
    JsonNode node = MarkdownHelper.parseJsonAndAutoFix(content, getModelClient(tenantId));
    if (node == null) {
      throw new BssException("大模型未生成 JSON");
    }
    return JsonUtil.convert(node, SimplifiedFlowDTO.class);
  }

  /**
   * 解释流程
   *
   * @param params 解释参数
   * @param partialHandler 片段处理器
   */
  public void explainStream(ExplainFlowParamsDTO params, Consumer<AssistantMessage> partialHandler) {
    // 构造流程的简化表示
    SimplifiedFlowDTO flow = flowConverter.convert(params.getTenantId(), params.getFlowId());
    // 系统提示词
    String systemPrompt = buildSystemPrompt(false, flow.getFlowType());
    // 用户提示词，将流程中用到的各种技能的说明添加到提示词中
    Map<String, List<Long>> skillIdsMap = extractSkillIdsMap(flow);
    String userPromptPrefix = "流程图: \n\n```json\n" + JsonUtil.toJsonString(flow) + "\n```";
    String userPrompt = buildUserPrompt(params.getTenantId(), userPromptPrefix, skillIdsMap);
    // 大模型请求对象
    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
      .addSystemMessage(systemPrompt)
      .addUserMessage(userPrompt)
      .build();

    // 调用大模型
    LlmClient client = getModelClient(params.getTenantId());
    Consumer<ChatCompletionResponse> eventHandler = response -> {
      AssistantMessage delta = response.getDelta();
      if (delta != null && !delta.isEmpty()) {
        partialHandler.accept(delta);
      }
    };
    client.chatCompletionStreamBlocking(chatCompletionRequest, eventHandler, SseUtil.requestListener);
  }

  /**
   * 提取流程中用到的各种技能
   */
  private Map<String, List<Long>> extractSkillIdsMap(SimplifiedFlowDTO flow) {
    Map<String, List<Long>> skillIdsMap = new HashMap<>();
    for (SimplifiedNodeDTO node : flow.getNodes()) {
      Function<Map<String, Object>, Long> extractor = skillIdExtractorMap.get(node.getType());
      if (extractor == null) {
        continue;
      }
      Long id = extractor.apply(node.getData());
      if (id != null) {
        // 知识检索当作知识问答
        String type = StepType.KNOWLEDGE_RETRIEVAL.equals(node.getType()) ? StepType.KNOWLEDGE_CHAT : node.getType();
        List<Long> ids = skillIdsMap.computeIfAbsent(type, k -> new ArrayList<>());
        if (!ids.contains(id)) {
          ids.add(id);
        }
      }
    }
    return skillIdsMap;
  }

  /**
   * 获取大模型客户端
   */
  private LlmClient getModelClient(Long tenantId) {
    String modelIdStr = SystemParameter.FLOW_AI_MODEL_ID.getValueFromDb();
    Assert.hasLength(modelIdStr, "未配置流程 AI 生成功能的大模型 ID");
    return modelClientCache.getLlmClient(tenantId, Long.parseLong(modelIdStr));
  }

  /**
   * 构造系统提示词
   */
  private String buildSystemPrompt(boolean isGenerate, String flowType) {
    String template;
    String prefix;
    try (InputStream templateStream = PROMPT_TEMPLATE.getInputStream();
         InputStream prefixStream = isGenerate ? PROMPT_PREFIX_GENERATE.getInputStream() : PROMPT_PREFIX_EXPLAIN.getInputStream()) {
      template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
      prefix = IOUtils.toString(prefixStream, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new BssException("读取提示词模板失败: " + e.getMessage(), e);
    }

    // 处理模板
    String content = FreemarkerUtil.process(template, ImmutableMap.of("isChatflow", SceneConsts.FLOW_TYPE_MULTI_STEP.equals(flowType)));
    return prefix.trim() + "\n\n" + content.trim();
  }

  /**
   * 构造用户提示词，添加技能说明
   */
  private String buildUserPrompt(Long tenantId, String prefix, Map<String, List<Long>> skillIdsMap) {
    // 没有技能时直接返回
    if (MapUtils.isEmpty(skillIdsMap)) {
      return prefix;
    }
    // 拼接用户提示词前缀和技能说明
    StringBuilder promptBuilder = new StringBuilder(prefix).append("\n\n");
    for (Entry<String, List<Long>> entry : skillIdsMap.entrySet()) {
      List<Long> skillIds = entry.getValue();
      if (CollectionUtils.isEmpty(skillIds)) {
        continue;
      }
      String skillType = entry.getKey();
      TriConsumer<StringBuilder, Long, List<Long>> generator = skillPromptGeneratorMap.get(skillType);
      Assert.notNull(generator, () -> "不支持的技能类型: " + skillType);
      generator.accept(promptBuilder, tenantId, skillIds);
    }
    return promptBuilder.toString();
  }

  /**
   * 添加 API 服务
   */
  private void addServices(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的 API 服务(用于 service 节点)\n\n");
    List<SkillServiceDTO> services = flowAiQueryMapper.selectApiServicesByIds(tenantId, skillIds);
    Assert.notEmpty(services, "API 服务不存在");
    for (SkillServiceDTO service : services) {
      prompt.append("### ").append(service.getServiceName()).append("\n\n");
      prompt.append("服务 ID: ").append(service.getServiceId()).append("\n\n");
      ParameterSpec request = ApiParamUtil.buildRequestSpec(service);
      ParameterSpec response = StringUtils.isEmpty(service.getResponseJson()) ? null : JsonUtil.parseJsonRequired(service.getResponseJson(), ParameterSpec.class);
      addInputAndOutput(prompt, request, response);
    }
    prompt.append("\n");
  }

  /**
   * 添加 SQL 服务
   */
  private void addSqlServices(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的 SQL 服务(用于 sql 节点)\n\n");
    List<SkillSqlDTO> services = flowAiQueryMapper.selectSqlServicesByIds(tenantId, skillIds);
    Assert.notEmpty(services, "SQL 服务不存在");
    for (SkillSqlDTO service : services) {
      prompt.append("### ").append(service.getServiceName()).append("\n\n");
      prompt.append("服务 ID: ").append(service.getServiceId()).append("\n\n");
      if (StringUtils.isNotEmpty(service.getRemark())) {
        prompt.append("服务描述: ").append(service.getRemark()).append("\n\n");
      }
      addInputAndOutput(prompt, service.getReqJson(), service.getRespJson());
    }
    prompt.append("\n");
  }

  /**
   * 添加工具箱
   */
  private void addToolbox(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的工具箱(用于 toolbox 节点)\n\n");
    List<SkillFunctionDTO> functions = flowAiQueryMapper.selectFunctionsByIds(tenantId, skillIds);
    Assert.notEmpty(functions, "服务函数不存在");
    for (SkillFunctionDTO function : functions) {
      prompt.append("### ").append(function.getFuncName()).append("\n\n");
      prompt.append("工具 ID: ").append(function.getFuncId()).append("\n\n");
      if (StringUtils.isNotEmpty(function.getRemark())) {
        prompt.append("工具描述: ").append(function.getRemark()).append("\n\n");
      }
      addInputAndOutput(prompt, function.getReqJson(), function.getRespJson());
    }
    prompt.append("\n");
  }

  /**
   * 添加工作流
   */
  private void addWorkflow(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的流程(用于 workflow 节点)\n\n");
    List<SkillFlowDTO> flows = flowAiQueryMapper.selectFlowsByIds(tenantId, skillIds);
    Assert.notEmpty(flows, "流程不存在");
    for (SkillFlowDTO flow : flows) {
      flow.parseParams();
      ParameterSpec response;
      if (flow.isMultiStep()) {
        if (CollectionUtils.isNotEmpty(flow.getVariables())) {
          response = AbstractNodeConverter.simplifyParameter(ParameterSpec.newRoot(flow.getVariables()));
        }
        else {
          response = null;
        }
      }
      else {
        response = AbstractNodeConverter.simplifyParameter(flow.getResponse());
      }
      prompt.append("### ").append(flow.getFlowName()).append("\n\n");
      prompt.append("流程 ID: ").append(flow.getFlowId()).append("\n\n");
      if (StringUtils.isNotEmpty(flow.getFlowDesc())) {
        prompt.append("流程描述: ").append(flow.getFlowDesc()).append("\n\n");
      }
      addInputAndOutput(prompt, AbstractNodeConverter.simplifyParameter(flow.getRequest()), response);
    }
    prompt.append("\n");
  }

  /**
   * 添加插件
   */
  private void addLlmSkill(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的插件(用于 llmSkill 节点)\n\n");
    List<SkillPluginDTO> plugins = flowAiQueryMapper.selectPluginsByIds(tenantId, skillIds);
    Assert.notEmpty(plugins, "插件不存在");
    for (SkillPluginDTO plugin : plugins) {
      prompt.append("### ").append(plugin.getApiName()).append("\n\n");
      prompt.append("插件 ID: ").append(plugin.getApiId()).append("\n\n");
      if (StringUtils.isNotEmpty(plugin.getRemark())) {
        prompt.append("插件描述: ").append(plugin.getRemark()).append("\n\n");
      }
      addInput(prompt, plugin.getReqJson());
    }
    prompt.append("\n");
  }

  /**
   * 添加页面
   */
  private void addPage(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的页面(用于 page 节点)\n\n");
    List<SkillPageDTO> pages = flowAiQueryMapper.selectPagesByIds(tenantId, skillIds);
    Assert.notEmpty(pages, "页面不存在");
    for (SkillPageDTO page : pages) {
      prompt.append("### ").append(page.getPageName()).append("\n\n");
      prompt.append("页面 ID: ").append(page.getPageId()).append("\n\n");
      addInput(prompt, page.getReqParamJson());
    }
    prompt.append("\n");
  }

  /**
   * 添加页面函数
   */
  private void addPageFunc(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的页面函数(用于 pageFunc 节点)\n\n");
    List<SkillPageFuncDTO> pageFuncList = flowAiQueryMapper.selectPageFuncListByIds(tenantId, skillIds);
    Assert.notEmpty(pageFuncList, "页面函数不存在");
    for (SkillPageFuncDTO pageFunc : pageFuncList) {
      prompt.append("### ").append(pageFunc.getFuncName()).append("\n\n");
      prompt.append("页面函数 ID: ").append(pageFunc.getPageFuncId()).append("\n\n");
      if (StringUtils.isNotEmpty(pageFunc.getRemark())) {
        prompt.append("页面函数描述: ").append(pageFunc.getRemark()).append("\n\n");
      }
      addInput(prompt, pageFunc.getReqJson());
    }
    prompt.append("\n");
  }

  /**
   * 添加知识库
   */
  private void addKnowledge(StringBuilder prompt, Long tenantId, List<Long> skillIds) {
    prompt.append("## 可用的知识库(用于 knowledgeChat, knowledgeRetrieval 节点)\n\n");
    List<KnowledgeBaseDTO> knowledgeList = flowAiQueryMapper.selectKnowledgeListByIds(tenantId, skillIds);
    Assert.notEmpty(knowledgeList, "知识库不存在");
    for (KnowledgeBaseDTO knowledge : knowledgeList) {
      prompt.append("### ").append(knowledge.getKnowledgeName()).append("\n\n");
      prompt.append("知识库 ID: ").append(knowledge.getKnowledgeId()).append("\n\n");
      if (StringUtils.isNotEmpty(knowledge.getKnowledgeDesc())) {
        prompt.append("服务描述: ").append(knowledge.getKnowledgeDesc()).append("\n\n");
      }
    }
    prompt.append("\n");
  }

  /**
   * 添加入参和出参结构
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void addInputAndOutput(StringBuilder prompt, @Nullable String requestJson, @Nullable String responseJson) {
    ParameterSpec request = null;
    if (StringUtils.isNotEmpty(requestJson)) {
      request = AbstractNodeConverter.simplifyParameter(JsonUtil.parseJson(requestJson, ParameterSpec.class));
    }
    ParameterSpec response = null;
    if (StringUtils.isNotEmpty(responseJson)) {
      response = AbstractNodeConverter.simplifyParameter(JsonUtil.parseJson(responseJson, ParameterSpec.class));
    }
    addInputAndOutput(prompt, request, response);
  }

  /**
   * 添加入参和出参结构
   */
  private void addInputAndOutput(StringBuilder prompt, @Nullable ParameterSpec request, @Nullable ParameterSpec response) {
    addInput(prompt, request);
    response = AbstractNodeConverter.simplifyParameter(response);
    if (response != null) {
      prompt.append("出参结构:\n\n```json\n");
      prompt.append(JsonUtil.toJsonStringPretty(response));
      prompt.append("\n```\n");
    }
    else {
      prompt.append("无出参\n");
    }
  }

  /**
   * 添加入参结构
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void addInput(StringBuilder prompt, @Nullable String requestJson) {
    ParameterSpec request = null;
    if (StringUtils.isNotEmpty(requestJson)) {
      request = AbstractNodeConverter.simplifyParameter(JsonUtil.parseJson(requestJson, ParameterSpec.class));
    }
    addInput(prompt, request);
  }

  /**
   * 添加入参结构
   */
  private void addInput(StringBuilder prompt, @Nullable ParameterSpec request) {
    request = AbstractNodeConverter.simplifyParameter(request);
    if (request != null) {
      prompt.append("入参结构:\n\n```json\n");
      prompt.append(JsonUtil.toJsonStringPretty(request));
      prompt.append("\n```\n\n");
    }
    else {
      prompt.append("无入参\n\n");
    }
  }

}
