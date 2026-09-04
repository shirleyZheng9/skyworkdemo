package com.iwhalecloud.bote.service.bot.impl.helper;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.util.ApiParamUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneParamDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.LogicViewVariableDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.mapper.bot.BotSceneRelaManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillQueryMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 场景编排逻辑视图 - 辅助工具
 *
 * @author chen.linfa
 * @since 2024-09-06
 */
@Component
@RequiredArgsConstructor
public class LogicViewHepler {
  private final SkillQueryMapper skillQueryMapper;
  private final BotSceneRelaManageMapper botSceneRelaManageMapper;
  /** 技能入参提取器映射 */
  private final Map<String, BiConsumer<Long, List<BotSceneSkillDTO>>> requestExtractorMap = ImmutableMap.<String, BiConsumer<Long, List<BotSceneSkillDTO>>>builder()
    .put(StepType.SERVICE, this::processApiServices)
    .put(StepType.SQL, this::processSqlServices)
    .put(StepType.LLM_SKILL, this::processLlmSkills)
    .put(StepType.TOOLBOX, this::processServiceFunctions)
    .put(StepType.WORKFLOW, this::processWorkflows)
    .put(StepType.PAGE, this::processPages)
    .put(StepType.PAGE_FUNC, this::processPageFunctions)
    // 知识问答固定使用用户问句，不要构造入参
    .put(StepType.KNOWLEDGE_CHAT, (tenantId, skills) -> {
    })
    .put(StepType.MCP, this::processMcps)
    .put(StepType.PLUGIN, this::processPlugins)
    .put(StepType.DATA_TABLE, (tenantId, skills) -> {
    })
    .put(StepType.INVOKE_SCENE, this::processInvokeScenes)
    .build();

  /**
   * 为技能补充入参变量信息
   */
  public void fillVariableForSkill(BotSceneDTO scene) {
    Map<String, List<BotSceneSkillDTO>> skillsMap = scene.getSkills();
    if (MapUtils.isEmpty(skillsMap)) {
      return;
    }
    Long tenantId = scene.getTenantId();
    for (Entry<String, List<BotSceneSkillDTO>> entry : skillsMap.entrySet()) {
      List<BotSceneSkillDTO> skills = entry.getValue();
      if (CollectionUtils.isNotEmpty(skills)) {
        String skillType = entry.getKey();
        BiConsumer<Long, List<BotSceneSkillDTO>> extractor = requestExtractorMap.get(skillType);
        Assert.notNull(extractor, () -> "不支持的技能类型: " + skillType);
        extractor.accept(tenantId, skills);
      }
    }
  }

  /**
   * 构造逻辑视图变量
   */
  @Nullable
  @SuppressWarnings({"PMD.AvoidUnusedPrivateMethods", "PMD.UnusedPrivateMethod"})
  private List<LogicViewVariableDTO> buildVariable(String skillType, @Nullable String requestJson) {
    if (StringUtils.isEmpty(requestJson)) {
      return Collections.emptyList();
    }
    Assert.isTrue(requestJson.startsWith("{"), "入参结构必须是对象");
    ParameterSpec root = JsonUtil.parseJsonRequired(requestJson, ParameterSpec.class);
    return buildVariable(skillType, root);
  }

  /**
   * 构造逻辑视图变量
   */
  private List<LogicViewVariableDTO> buildVariable(String skillType, @Nullable ParameterSpec root) {
    if (root == null || !root.hasChildren()) {
      return Collections.emptyList();
    }
    List<LogicViewVariableDTO> variables = new ArrayList<>();
    for (ParameterSpec parameter : root.getChildren()) {
      flatParameters(parameter, variables, skillType);
    }
    return variables.stream().filter(p -> StringUtils.isEmpty(p.getDefaultValue())).collect(Collectors.toList());
  }

  /**
   * 扁平化参数列表
   */
  private void flatParameters(ParameterSpec parameter, List<LogicViewVariableDTO> variables, String type) {
    List<String> ignore = Arrays.asList("header", "path", "query", "body");
    // 服务类型节点，忽略虚拟的二级节点
    if (!StepType.SERVICE.equals(type) || !ignore.contains(parameter.getName())) {
      if (parameter.isProperty()) {
        variables.add(new LogicViewVariableDTO(parameter));
      }
      else if (parameter.isList()) {
        variables.add(new LogicViewVariableDTO(parameter));
      }
    }

    for (ParameterSpec child : CollectionUtils.emptyIfNull(parameter.getChildren())) {
      flatParameters(child, variables, type);
    }
  }

  /**
   * 处理 API 服务
   */
  private void processApiServices(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillServiceDTO> services = skillQueryMapper.selectApiServicesByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillServiceDTO service = IterableUtils.find(services, s -> skill.getSkillId().equals(s.getServiceId()));
      if (service != null) {
        skill.setInputVariables(buildVariable(StepType.SERVICE, ApiParamUtil.buildRequestSpec(service)));
      }
    }
  }

  /**
   * 处理 SQL 服务
   */
  private void processSqlServices(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillSqlDTO> services = skillQueryMapper.selectSqlServicesByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillSqlDTO service = IterableUtils.find(services, s -> skill.getSkillId().equals(s.getServiceId()));
      if (service != null) {
        skill.setInputVariables(buildVariable(StepType.SQL, service.getReqJson()));
      }
    }
  }

  /**
   * 处理插件
   */
  private void processLlmSkills(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillPluginDTO> plugins = skillQueryMapper.selectPluginsByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillPluginDTO plugin = IterableUtils.find(plugins, p -> skill.getSkillId().equals(p.getApiId()));
      if (plugin != null) {
        skill.setInputVariables(buildVariable(StepType.TOOLBOX, plugin.getReqJson()));
      }
    }
  }

  /**
   * 处理服务函数
   */
  private void processServiceFunctions(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillFunctionDTO> functions = skillQueryMapper.selectFunctionsByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillFunctionDTO function = IterableUtils.find(functions, f -> skill.getSkillId().equals(f.getFuncId()));
      if (function != null) {
        skill.setInputVariables(buildVariable(StepType.TOOLBOX, function.getReqJson()));
      }
    }
  }

  /**
   * 处理工作流
   */
  private void processWorkflows(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillFlowDTO> flows = skillQueryMapper.selectFlowsByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillFlowDTO flow = IterableUtils.find(flows, f -> skill.getSkillId().equals(f.getFlowId()));
      if (flow != null && flow.getParam() != null) {
        skill.setInputVariables(buildVariable(StepType.WORKFLOW, flow.getParam().getRequestJson()));
      }
    }
  }

  /**
   * 处理场景调用
   */
  private void processInvokeScenes(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<BotSceneParamDTO> scenes = botSceneRelaManageMapper.selectSceneParamsByIds(ids, tenantId);
    for (BotSceneSkillDTO skill : skills) {
      BotSceneParamDTO scene = IterableUtils.find(scenes, s -> skill.getSkillId().equals(s.getSceneId()));
      if (scene != null && StringUtils.isNotEmpty(scene.getRequestJson())) {
        skill.setInputVariables(buildVariable(StepType.INVOKE_SCENE, scene.getRequestJson()));
      }
    }
  }

  /**
   * 处理页面
   */
  private void processPages(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillPageDTO> pages = skillQueryMapper.selectPagesByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillPageDTO page = IterableUtils.find(pages, p -> skill.getSkillId().equals(p.getPageId()));
      if (page != null) {
        skill.setInputVariables(buildVariable(StepType.PAGE, page.getReqParamJson()));
      }
    }
  }

  /**
   * 处理页面函数
   */
  private void processPageFunctions(Long tenantId, List<BotSceneSkillDTO> skills) {
    Set<Long> ids = skills.stream().map(BotSceneSkillDTO::getSkillId).collect(Collectors.toSet());
    List<SkillPageFuncDTO> pageFunctions = skillQueryMapper.selectPageFuncListByIds(tenantId, ids);
    for (BotSceneSkillDTO skill : skills) {
      SkillPageFuncDTO pageFunc = IterableUtils.find(pageFunctions, p -> skill.getSkillId().equals(p.getPageFuncId()));
      if (pageFunc != null) {
        skill.setInputVariables(buildVariable(StepType.PAGE_FUNC, pageFunc.getReqJson()));
      }
    }
  }

  /**
   * 处理 MCP
   */
  @SuppressWarnings({"PMD.AvoidUnusedMethodParameters", "PMD.UnusedFormalParameter"})
  private void processMcps(Long tenantId, List<BotSceneSkillDTO> skills) {
    for (BotSceneSkillDTO skill : skills) {
      // mcp 没有编码，只有唯一的名称
      skill.setSkillCode(skill.getSkillName());
    }
  }

  @SuppressWarnings({"PMD.AvoidUnusedMethodParameters", "PMD.UnusedFormalParameter"})
  private void processPlugins(Long tenantId, List<BotSceneSkillDTO> skills) {
    for (BotSceneSkillDTO skill : skills) {
      skill.setSkillCode(skill.getSkillName());
    }
  }
}
