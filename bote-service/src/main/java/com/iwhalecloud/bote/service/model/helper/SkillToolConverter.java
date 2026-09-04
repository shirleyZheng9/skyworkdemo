package com.iwhalecloud.bote.service.model.helper;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.ApiSkillCache;
import com.iwhalecloud.bote.cache.DataTableCache;
import com.iwhalecloud.bote.cache.FlowDslCache;
import com.iwhalecloud.bote.cache.LlmPluginCache;
import com.iwhalecloud.bote.cache.McpClientCache;
import com.iwhalecloud.bote.cache.PageCache;
import com.iwhalecloud.bote.cache.PageFuncCache;
import com.iwhalecloud.bote.cache.PluginHubCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.SqlSkillCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.cache.ToolboxCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.doc.module.knowledge.cache.KnowledgeCache;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeExtItem;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.orchestration.step.InvokeSceneStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmSkillStep;
import com.iwhalecloud.bote.dto.orchestration.step.McpStep;
import com.iwhalecloud.bote.dto.orchestration.step.PageFuncStep;
import com.iwhalecloud.bote.dto.orchestration.step.PageStep;
import com.iwhalecloud.bote.dto.orchestration.step.PluginStep;
import com.iwhalecloud.bote.dto.orchestration.step.ServiceStep;
import com.iwhalecloud.bote.dto.orchestration.step.SqlStep;
import com.iwhalecloud.bote.dto.orchestration.step.ToolboxStep;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.CustomSqlStep;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.dto.skill.SimpleFunctionSkillDTO;
import com.iwhalecloud.bote.dto.skill.SimpleLlmPluginDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageDTO;
import com.iwhalecloud.bote.dto.skill.SimplePageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSqlSkillDTO;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.mapper.plugin.PluginManageMapper;
import com.iwhalecloud.bote.mcp.client.McpClient;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import com.iwhalecloud.bote.service.base.IDataSourceProviderService;
import com.iwhalecloud.bote.service.plugin.PluginFactory;
import com.iwhalecloud.bote.service.plugin.runner.AbstractPlugin;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 技能工具转换器
 *
 * <p>用于将平台的各种技能转为大模型工具</p>
 *
 * @author bianjp
 * @since 2025-04-17
 */
public final class SkillToolConverter {
  private SkillToolConverter() {
  }

  private static final ApiSkillCache apiSkillCache = SpringUtil.getBean(ApiSkillCache.class);
  private static final SqlSkillCache sqlSkillCache = SpringUtil.getBean(SqlSkillCache.class);
  private static final LlmPluginCache llmPluginCache = SpringUtil.getBean(LlmPluginCache.class);
  private static final PluginManageMapper pluginManageMapper = SpringUtil.getBean(PluginManageMapper.class);
  private static final McpClientCache mcpClientCache = SpringUtil.getBean(McpClientCache.class);
  private static final ToolboxCache toolboxCache = SpringUtil.getBean(ToolboxCache.class);
  private static final FlowDslCache flowDslCache = SpringUtil.getBean(FlowDslCache.class);
  private static final PageCache pageCache = SpringUtil.getBean(PageCache.class);
  private static final PageFuncCache pageFuncCache = SpringUtil.getBean(PageFuncCache.class);
  private static final KnowledgeCache knowledgeCache = SpringUtil.getBean(KnowledgeCache.class);
  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);
  private static final DataTableCache dataTableCache = SpringUtil.getBean(DataTableCache.class);
  private static final IDataSourceProviderService dataSourceProviderService = SpringUtil.getBean(IDataSourceProviderService.class);
  private static final PluginHubCache pluginHubCache = SpringUtil.getBean(PluginHubCache.class);
  private static final SceneCache sceneCache = SpringUtil.getBean(SceneCache.class);
  /** 知识库技能的入参，固定只有一个问题参数 */
  private static final JsonSchemaNode KNOWLEDGE_PARAMS = JsonSchemaNode.newObject()
    .addProperty("question", "问题", JsonSchemaDataType.STRING, true);
  /** 调用智能体工具的入参：用户问题或需求 */
  private static final JsonSchemaNode INVOKE_SCENE_PARAMS = JsonSchemaNode.newObject()
    .addProperty("message", "用户问题或需求，将转发给该智能体处理", JsonSchemaDataType.STRING, true);
  /** 转换器映射，key 为技能类型 */
  private static final Map<String, TriFunction<Long, List<LlmSkillItem>, List<Long>, List<SkillToolDTO>>> converterMap = ImmutableMap.<String, TriFunction<Long, List<LlmSkillItem>, List<Long>, List<SkillToolDTO>>>builder()
    .put(StepType.SERVICE, SkillToolConverter::convertApiServices)
    .put(StepType.SQL, SkillToolConverter::convertSqlServices)
    .put(StepType.LLM_SKILL, SkillToolConverter::convertLlmSkills)
    .put(StepType.TOOLBOX, SkillToolConverter::convertServiceFunctions)
    .put(StepType.WORKFLOW, SkillToolConverter::convertWorkflows)
    .put(StepType.PAGE, SkillToolConverter::convertPages)
    .put(StepType.PAGE_FUNC, SkillToolConverter::convertPageFunctions)
    .put(StepType.KNOWLEDGE_CHAT, SkillToolConverter::convertKnowledgeBases)
    .put(StepType.KNOWLEDGE_RETRIEVAL, SkillToolConverter::convertKnowledgeRetrieval)
    .build();

  /**
   * 构造大模型工具列表
   *
   * @param tenantId 租户 ID
   * @param skills 平台的技能列表
   * @return 大模型工具列表
   */
  public static List<SkillToolDTO> convert(Long tenantId, Long modelId, List<LlmSkillItem> skills, boolean debug) {
    if (CollectionUtils.isEmpty(skills)) {
      return Collections.emptyList();
    }
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(tenantId);
    Map<String, List<LlmSkillItem>> skillIdsMap = skills.stream()
      .filter(skill -> StringUtils.isNotEmpty(skill.getSkillType()) && (skill.getSkillId() != null || StringUtils.isNotEmpty(skill.getExtSkillId())))
      .collect(Collectors.groupingBy(LlmSkillItem::getSkillType));

    return skillIdsMap.entrySet().parallelStream()
      .map(e -> convertBySkillType(tenantId, modelId, debug, knowledgeInfo, e.getKey(), e.getValue()))
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  /**
   * 根据技能类型转换技能
   */
  private static List<SkillToolDTO> convertBySkillType(Long tenantId, Long modelId, boolean debug,
    KnowledgeInfoDTO knowledgeInfo,
    String skillType, List<LlmSkillItem> skillItems) {
    if (skillType.equals(StepType.MCP)) {
      return convertMcpSkills(tenantId, skillItems);
    }
    if (skillType.equals(StepType.KNOWLEDGE_CHAT)) {
      List<SkillToolDTO> skillTools = convertKnowledgeChatIfNeeded(knowledgeInfo, skillItems);
      if (CollectionUtils.isNotEmpty(skillTools)) {
        return skillTools;
      }
    }
    if (skillType.equals(StepType.DATA_TABLE)) {
      return convertDatabaseSkills(tenantId, skillItems, debug);
    }
    if (skillType.equals(StepType.PLUGIN)) {
      return convertPlugins(tenantId, modelId, skillItems);
    }
    if (skillType.equals(StepType.INVOKE_SCENE)) {
      return convertInvokeScenes(tenantId, skillItems);
    }
    return convertByConverterMap(tenantId, skillType, skillItems);
  }

  /**
   * 转换知识库技能
   */
  private static List<SkillToolDTO> convertKnowledgeChatIfNeeded(KnowledgeInfoDTO knowledgeInfo, List<LlmSkillItem> skillItems) {
    // 如果知识库类型不为空，或者技能列表中包含其他知识类型，则转换知识库技能
    boolean isOtherKnowledge = skillItems.stream().filter(skill -> StringUtils.isNotEmpty(skill.getKnowledgeType()))
      .anyMatch(skill -> KnowledgeConsts.OTHER_KNOWLEDGE_TYPES.contains(skill.getKnowledgeType()));
    if (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType()) || isOtherKnowledge) {
      return convertKnowledgeAccess(skillItems);
    }
    return Collections.emptyList();
  }

  /**
   * 根据转换器映射转换技能
   */
  private static List<SkillToolDTO> convertByConverterMap(Long tenantId, String skillType, List<LlmSkillItem> skillItems) {
    TriFunction<Long, List<LlmSkillItem>, List<Long>, List<SkillToolDTO>> converter = converterMap.get(skillType);
    Assert.notNull(converter, () -> "不支持的技能类型: " + skillType);
    List<Long> ids = skillItems.stream().map(LlmSkillItem::getSkillId).collect(Collectors.toList());
    return converter.apply(tenantId, skillItems, ids);
  }

  /**
   * 转换 API 服务
   */
  private static List<SkillToolDTO> convertApiServices(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimpleServiceDTO> services = apiSkillCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(services.size());
    for (SimpleServiceDTO service : services) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(service.getServiceId()));
      ServiceStep step = new ServiceStep();
      step.setServiceId(service.getServiceId());
      skillTools.add(SkillToolDTO.builder(StepType.SERVICE, service.getServiceId(), step)
        .tool(service.getServiceCode(), service.getServiceName(), service.getRequest(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换 SQL 服务
   */
  private static List<SkillToolDTO> convertSqlServices(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimpleSqlSkillDTO> services = sqlSkillCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(services.size());
    for (SimpleSqlSkillDTO service : services) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(service.getServiceId()));
      SqlStep step = new SqlStep();
      step.setSqlId(service.getServiceId());
      skillTools.add(SkillToolDTO.builder(StepType.SQL, service.getServiceId(), step)
        .tool(service.getServiceCode(), service.getServiceName(), service.getRequest(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换大模型插件
   */
  private static List<SkillToolDTO> convertLlmSkills(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimpleLlmPluginDTO> plugins = llmPluginCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(plugins.size());
    for (SimpleLlmPluginDTO plugin : plugins) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(plugin.getPluginId()));
      LlmSkillStep step = new LlmSkillStep();
      step.setApiId(plugin.getPluginId());
      skillTools.add(SkillToolDTO.builder(StepType.LLM_SKILL, plugin.getPluginId(), step)
        .tool(plugin.getPluginCode(), plugin.getPluginName(), plugin.getRequest(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换插件
   */
  private static List<SkillToolDTO> convertPlugins(Long tenantId, Long modelId, List<LlmSkillItem> skills) {
    List<Long> ids = skills.stream().filter(p -> BooleanUtils.isNotTrue(p.getIsPluginHub())).map(LlmSkillItem::getSkillId).toList();
    List<SkillToolDTO> skillTools = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(ids)) {
      // 存量插件
      List<PluginDTO> plugins = pluginManageMapper.selectPluginsByIds(tenantId, ids);
      for (PluginDTO plugin : CollectionUtils.emptyIfNull(plugins)) {
        AbstractPlugin<?> runner = (AbstractPlugin<?>) PluginFactory.getPlugin(plugin.getPluginCode());
        LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(plugin.getPluginId()));
        PluginStep step = new PluginStep();
        step.setPluginId(plugin.getPluginId());
        step.setParameters(runner.getRequestParameter());
        step.setResponse(runner.getResponseParameter());
        skillTools.add(SkillToolDTO.builder(StepType.PLUGIN, plugin.getPluginId(), step)
          .tool(plugin.getPluginCode(), plugin.getPluginName(), runner.getRequestParameter(), skillItem.getParameters()).build());
      }
    }
    // 对接插件市场
    for (LlmSkillItem skill : skills) {
      if (ids.contains(skill.getSkillId())) {
        continue;
      }
      PluginDefinition plugin = pluginHubCache.get(tenantId, skill.getSkillId());
      Assert.notNull(plugin, () -> "查询不到有效插件，pluginId=" + skill.getSkillId());

      List<String> toolNames = CollectionUtils.emptyIfNull(plugin.getTools()).stream().map(PluginToolSpec::getName)
        .filter(p -> CollectionUtils.isEmpty(skill.getToolNames()) || skill.getToolNames().contains(p)).toList();
      for (String toolName : CollectionUtils.emptyIfNull(toolNames)) {
        PluginToolSpec toolSpec = IterableUtils.find(plugin.getTools(), p -> toolName.equals(p.getName()));
        Assert.notNull(toolSpec, () -> "查询不到有效插件工具，pluginId=" + skill.getSkillId() + "，toolName=" + toolName);
        skillTools.add(buildPluginSkillTool(skill.getSkillId(), modelId, toolSpec));
      }
    }
    return skillTools;
  }

  /**
   * 转换 MCP 服务
   */
  private static List<SkillToolDTO> convertMcpSkills(Long tenantId, List<LlmSkillItem> skills) {
    return skills.stream().parallel()
      .flatMap((skill) -> {
        Long serverId = skill.getSkillId();
        Long finalTenantId = Boolean.TRUE.equals(skill.getPlatform()) ? BaseConsts.PLATFORM_TENANT_ID : tenantId;
        McpClient client = mcpClientCache.getMcpClient(finalTenantId, serverId, true);
        return client.listAllToolsWithCache().stream()
          // 如果配置了工具名称列表，需要过滤
          .filter(CollectionUtils.isNotEmpty(skill.getToolNames()) ? t -> skill.getToolNames().contains(t.getName()) : t -> true)
          .map(t -> buildMcpSkillTool(t, serverId, skill, client));
      })
      .collect(Collectors.toList());
  }

  /**
   * 构造 MCP 技能工具
   */
  public static SkillToolDTO buildMcpSkillTool(McpTool mcpTool, @Nullable Long serverId, @Nullable LlmSkillItem skillItem, McpClient client) {
    McpStep step = new McpStep();
    step.setServerId(serverId);
    step.setToolName(mcpTool.getName());
    step.setName(client.getName() + ": " + mcpTool.getDescription());
    step.setClient(client);
    ParameterSpec customParameters = skillItem != null ? MapUtils.getObject(skillItem.getMcpToolParameters(), mcpTool.getName()) : null;
    return SkillToolDTO.builder(StepType.MCP, serverId, step)
      // 暂不考虑工具名称冲突
      .tool(mcpTool.getName(), mcpTool.getDescription(), mcpTool.getInputSchema(), customParameters)
      .build();
  }

  /**
   * 转换 数据库 服务
   */
  private static List<SkillToolDTO> convertDatabaseSkills(Long tenantId, List<LlmSkillItem> skills, boolean debug) {
    List<Long> ids = skills.stream().map(LlmSkillItem::getSkillId).toList();
    List<SimpleDataTableDTO> tables = dataTableCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(tables.size());
    Long dataSourceId = tables.getFirst().getDataSourceId();
    // 构造工具描述信息
    StringBuilder description = new StringBuilder();
    String databaseType = dataSourceProviderService.getDataSourceType(tenantId, dataSourceId).name().toLowerCase();
    description.append(databaseType).append(" query insert update delete tool.");
    for (SimpleDataTableDTO table : tables) {
      description.append(buildTableDescription(tenantId, table.getTableId()));
    }
    description.append("Use SQL to query insert update delete these table. You can write SQL statements directly to operate.");
    description.append("\n\n").append("For SELECT query results, you must format the output as follows:");
    description.append("\n").append("1. If the data is not empty, return it in an HTML table format with the following styling requirements:");
    description.append("\n").append("   - The table must be wrapped in a div with a light background color (e.g., #f5f5f5 or #fafafa) and rounded corners (border-radius: 8px)");
    description.append("\n").append("   - The header row must use the ColumnName (Chinese column name) from the table structure description above, NOT the ColumnCode");
    description.append("\n").append("     For example, if the table structure shows 'user_name(STRING): 用户名', use '用户名' as the header, not 'user_name'");
    description.append("\n").append("   - The header row must have a gray background color (e.g., #e8e8e8 or #f0f0f0)");
    description.append("\n").append("   - The table should have proper padding and spacing");
    description.append("\n").append("   - All columns should be left-aligned");
    description.append("\n").append("2. Use HTML table format with inline styles. The table must be wrapped in a div to achieve rounded corners. Example format:");
    description.append("\n").append("   <div style=\"background-color: #f5f5f5; border-radius: 8px; padding: 0; overflow: hidden;\">");
    description.append("\n").append("     <table style=\"width: 100%; border-collapse: collapse; background-color: #f5f5f5;\">");
    description.append("\n").append("       <thead>");
    description.append("\n").append("         <tr style=\"background-color: #e8e8e8;\">");
    description.append("\n").append("           <th style=\"padding: 12px; text-align: left; border-bottom: 1px solid #ddd;\">ColumnName1</th>");
    description.append("\n").append("           <th style=\"padding: 12px; text-align: left; border-bottom: 1px solid #ddd;\">ColumnName2</th>");
    description.append("\n").append("         </tr>");
    description.append("\n").append("       </thead>");
    description.append("\n").append("       <tbody>");
    description.append("\n").append("         <tr>");
    description.append("\n").append("           <td>value1</td>");
    description.append("\n").append("           <td>value2</td>");
    description.append("\n").append("         </tr>");
    description.append("\n").append("       </tbody>");
    description.append("\n").append("     </table>");
    description.append("\n").append("   </div>");
    description.append("\n").append("3. The column ``bote_data_tunnel`` is an internal field—both the column itself and its data must be completely hidden and excluded from the output table");
    description.append("\n").append("4. If the result is empty, it means the SQL query returned no information");

    // 构建数据渠道条件描述
    description.append(buildDataTunnelDescription(tables, debug));

    // 构造工具入参
    ParameterSpec root = new ParameterSpec();
    root.setType(AttrDataType.OBJECT);
    // SQL 入参
    ParameterSpec sqlSpec = new ParameterSpec();
    sqlSpec.setType(AttrDataType.STRING);
    sqlSpec.setName("sql");
    sqlSpec.setRequired(true);
    root.setChildren(Collections.singletonList(sqlSpec));

    CustomSqlStep step = new CustomSqlStep();
    step.setDataSourceId(dataSourceId);
    step.setTableIds(ids);
    step.setAllowed(skills.getFirst().getDatabaseAlloweds());
    skillTools.add(SkillToolDTO.builder(StepType.DATA_TABLE, dataSourceId, step)
      .tool("数据库工具", description.toString(), root, null)
      .build());
    return skillTools;
  }

  /**
   * 转换服务函数
   */
  private static List<SkillToolDTO> convertServiceFunctions(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimpleFunctionSkillDTO> functions = toolboxCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(functions.size());
    for (SimpleFunctionSkillDTO function : functions) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(function.getFuncId()));
      ToolboxStep step = new ToolboxStep();
      step.setFuncId(function.getFuncId());
      skillTools.add(SkillToolDTO.builder(StepType.TOOLBOX, function.getFuncId(), step)
        .tool(function.getFuncCode(), function.getFuncName(), function.getRequest(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换工作流
   */
  private static List<SkillToolDTO> convertWorkflows(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SceneDslDTO> flows = flowDslCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(flows.size());
    for (SceneDslDTO flow : flows) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(flow.getId()));
      WorkflowStep step = new WorkflowStep();
      step.setFlowId(flow.getId());
      skillTools.add(SkillToolDTO.builder(StepType.WORKFLOW, flow.getId(), step)
        .tool(flow.getCode(), flow.getName(), flow.getInput(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换页面
   */
  private static List<SkillToolDTO> convertPages(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimplePageDTO> pages = pageCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(pages.size());
    for (SimplePageDTO page : pages) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(page.getPageId()));
      PageStep step = new PageStep();
      step.setPageId(page.getPageId());
      skillTools.add(SkillToolDTO.builder(StepType.PAGE, page.getPageId(), step)
        .tool(page.getPageCode(), page.getPageName(), page.getRequest(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换页面函数
   */
  private static List<SkillToolDTO> convertPageFunctions(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimplePageFuncDTO> pageFunctions = pageFuncCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(pageFunctions.size());
    for (SimplePageFuncDTO pageFunc : pageFunctions) {
      LlmSkillItem skillItem = IterableUtils.find(skills, s -> s.getSkillId().equals(pageFunc.getPageFuncId()));
      PageFuncStep step = new PageFuncStep();
      step.setPageFuncId(pageFunc.getPageFuncId());
      skillTools.add(SkillToolDTO.builder(StepType.PAGE_FUNC, pageFunc.getPageFuncId(), step)
        .tool(pageFunc.getFuncCode(), pageFunc.getFuncName(), pageFunc.getRequest(), skillItem.getParameters())
        .build());
    }
    return skillTools;
  }

  /**
   * 转换「调用智能体」技能：将 ISceneChatService#run 封装为工具，供自主规划智能体调用
   */
  private static List<SkillToolDTO> convertInvokeScenes(Long tenantId, List<LlmSkillItem> skills) {
    List<SkillToolDTO> skillTools = new ArrayList<>(skills.size());
    for (LlmSkillItem item : skills) {
      Long sceneId = item.getSkillId();
      if (sceneId == null) {
        continue;
      }
      SimpleBotSceneDTO scene;
      try {
        scene = sceneCache.getScene(tenantId, sceneId);
      }
      catch (Exception ex) {
        continue;
      }
      InvokeSceneStep step = new InvokeSceneStep();
      step.setSceneId(sceneId);
      step.setSceneName(scene.getSceneName());
      String toolName = "invoke_scene_" + sceneId;
      String description = "调用智能体「" + scene.getSceneName() + ":" + scene.getSceneDesc() + "」。传入用户问题或需求，由该智能体处理并返回结果。";
      skillTools.add(SkillToolDTO.builder(StepType.INVOKE_SCENE, sceneId, step)
        .tool(toolName, description, INVOKE_SCENE_PARAMS, null)
        .build());
    }
    return skillTools;
  }

  /**
   * 转换知识库
   */
  @SuppressWarnings("PMD.UnusedFormalParameter")
  private static List<SkillToolDTO> convertKnowledgeBases(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimpleKnowledgeDTO> knowledgeBases = knowledgeCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(knowledgeBases.size());
    for (SimpleKnowledgeDTO knowledge : knowledgeBases) {
      KnowledgeChatStep step = new KnowledgeChatStep();
      step.setKnowledgeId(knowledge.getKnowledgeId().toString());
      skillTools.add(SkillToolDTO.builder(StepType.KNOWLEDGE_CHAT, knowledge.getKnowledgeId(), step)
        .tool("knowledge_" + knowledge.getKnowledgeId().toString(), knowledge.getKnowledgeName(), KNOWLEDGE_PARAMS, null)
        .build());
    }
    return skillTools;
  }

  /**
   * 转换知识库检索
   */
  @SuppressWarnings("PMD.UnusedFormalParameter")
  private static List<SkillToolDTO> convertKnowledgeRetrieval(Long tenantId, List<LlmSkillItem> skills, List<Long> ids) {
    List<SimpleKnowledgeDTO> knowledgeBases = knowledgeCache.batchGet(tenantId, ids);
    List<SkillToolDTO> skillTools = new ArrayList<>(knowledgeBases.size());
    for (SimpleKnowledgeDTO knowledge : knowledgeBases) {
      KnowledgeRetrievalStep step = new KnowledgeRetrievalStep();
      step.setKnowledgeId(knowledge.getKnowledgeId().toString());
      skillTools.add(SkillToolDTO.builder(StepType.KNOWLEDGE_RETRIEVAL, knowledge.getKnowledgeId(), step)
        .tool("knowledge_" + knowledge.getKnowledgeId().toString(), knowledge.getKnowledgeName(), KNOWLEDGE_PARAMS, null)
        .build());
    }
    return skillTools;
  }

  /**
   * 转换外部接入知识库
   */
  private static List<SkillToolDTO> convertKnowledgeAccess(List<LlmSkillItem> skills) {
    return skills.stream()
      .filter(item -> StepType.KNOWLEDGE_CHAT.equals(item.getSkillType()))
      .map(item -> {
        if (KnowledgeConsts.KNOWLEDGE_TYPE_WEKNORA.equals(item.getKnowledgeType())) {
          return buildWeknoraKnowledgeChatTool(item);
        }
        if (KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH.equals(item.getKnowledgeType())) {
          return buildKnowledgeGraphKnowledgeChatTool(item);
        }
        KnowledgeChatStep step = new KnowledgeChatStep();
        String toolName;
        if (StringUtils.isNotEmpty(item.getKnowledgeName())) {
          KnowledgeExtItem knowledgeExtItem = new KnowledgeExtItem();
          knowledgeExtItem.setKnowledgeName(item.getKnowledgeName());
          knowledgeExtItem.setKnowledgeId(item.getSkillId());
          step.setKnowledgeExt(JsonUtil.toJsonString(Collections.singletonList(knowledgeExtItem)));
          toolName = item.getKnowledgeName();
        }
        else {
          ResourceExtItem resourceExtItem = new ResourceExtItem();
          resourceExtItem.setResourceWid(item.getSkillId().toString());
          resourceExtItem.setResourceType(KnowledgeConsts.KNOW_BASE_RESOURCE);
          resourceExtItem.setDocName(item.getDocName());
          step.setResourceExt(JsonUtil.toJsonString(Collections.singletonList(resourceExtItem)));
          toolName = item.getDocName();
        }
        return SkillToolDTO.builder(StepType.KNOWLEDGE_CHAT, item.getSkillId(), step)
          .tool("knowledge_" + item.getSkillId(), toolName, KNOWLEDGE_PARAMS, null)
          .build();
      })
      .collect(Collectors.toList());
  }

  /**
   * 构造「weknora调用知识库」工具
   */
  private static SkillToolDTO buildWeknoraKnowledgeChatTool(LlmSkillItem item) {
    WeKnoraKnowledgeChatStep step = new WeKnoraKnowledgeChatStep();
    step.setWeKnoraKnowledgeBaseIds(item.getExtSkillId());
    return SkillToolDTO.builder(StepType.WEKNORA_CHAT, item.getSkillId(), step)
      .tool("knowledge_" + item.getExtSkillId(), item.getKnowledgeName(), KNOWLEDGE_PARAMS, null).build();
  }

  /**
   * 构造「knowledgeGraph 调用知识库」工具
   */
  private static SkillToolDTO buildKnowledgeGraphKnowledgeChatTool(LlmSkillItem item) {
    KnowledgeGraphKnowledgeChatStep step = new KnowledgeGraphKnowledgeChatStep();
    step.setKnowledgeGraphName(item.getExtSkillId());
    return SkillToolDTO.builder(StepType.KNOWLEDGE_GRAPH_CHAT, item.getSkillId(), step)
      .tool("knowledgeGraph_" + item.getExtSkillId(), item.getKnowledgeName(), KNOWLEDGE_PARAMS, null).build();
  }

  /**
   * 基于表结构定义，构造工具描述信息
   */
  private static String buildTableDescription(Long tenantId, Long tableId) {
    SimpleDataTableDTO table = dataTableCache.get(tenantId, tableId);
    Assert.notNull(table, () -> "查询不到表，tableId=" + tableId);
    String tableCode = table.getTableCode();
    String tableName = table.getTableName();
    StringBuilder columnDesc = new StringBuilder();
    for (SimpleDataTableColumnDTO column : table.getColumns()) {
      String tamplate = "- %s(%s): %s";
      columnDesc.append(String.format(tamplate, column.getColumnCode(), column.getDataType(), column.getColumnName()));
      if (!BaseConsts.TRUE.equals(column.getNullable())) {
        columnDesc.append("(required)");
      }
      columnDesc.append("\n");
    }
    String description = "Table name is '%s'. This table's desc is %s.%n" + "Table structure:%n%s%n";
    return String.format(description, tableCode, tableName, columnDesc.toString());
  }

  /**
   * 构造表数据渠道描述信息
   */
  private static String buildDataTunnelDescription(List<SimpleDataTableDTO> tables, boolean debug) {
    // 获取包含平台数据渠道字段的表名
    List<String> tableCodeList = tables.stream()
      .filter(t -> t.getColumns().stream().anyMatch(c -> c.getColumnCode().equals(BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL)))
      .map(SimpleDataTableDTO::getTableCode).toList();
    // 给包含平台数据渠道字段的表添加对应的描述
    if (CollectionUtils.isNotEmpty(tableCodeList)) {
      StringBuilder sb = new StringBuilder();
      String dataTunnelField = BaseConsts.PLATFORM_COLUMN_BOTE_DATA_TUNNEL;
      String dataTunnel = debug ? BaseConsts.DATA_TUNNEL_TEST : BaseConsts.DATA_TUNNEL_OFFICIAL;
      sb.append("\n\n").append("Please add the following conditions to the tables ").append(String.join(", ", tableCodeList)).append(":");
      sb.append("\n").append(String.format("- For the generated query update delete statement, please add the %s = '%s' filtering condition.", dataTunnelField, dataTunnel));
      sb.append("\n").append(String.format("- For the generated insert statement, please set the %s field value to '%s'.", dataTunnelField, dataTunnel));
      return sb.toString();
    }
    return "";
  }

  /**
   * 构造插件技能工具
   */
  private static SkillToolDTO buildPluginSkillTool(Long pluginId, @Nullable Long modelId, PluginToolSpec toolSpec) {
    PluginStep step = new PluginStep();
    step.setPluginId(pluginId);
    step.setModelId(modelId != null ? Long.toString(modelId) : null);
    step.setIsPluginHub(true);
    step.setToolName(toolSpec.getName());
    String description = StringUtils.isEmpty(toolSpec.getLlmDescription()) ? toolSpec.getDescription() : toolSpec.getLlmDescription();
    step.setName(toolSpec.getName() + ": " + description);
    return SkillToolDTO.builder(StepType.PLUGIN, pluginId, step)
      .tool(toolSpec.getName(), toolSpec.getDescription(), toolSpec.getInput().convert(), null)
      .build();
  }

}
