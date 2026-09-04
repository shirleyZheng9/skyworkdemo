package com.iwhalecloud.bote.service.skill.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowNodeCfgDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowNodeConfigGroupDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowNodeConfigResponseDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowNodeItemDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowParamDTO;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.base.OperLogQueryMapper;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.SkillFlowManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillFlowNodeCfgMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.orchestration.converter.SceneDslConverter;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import com.iwhalecloud.bote.service.skill.impl.helper.DiffOperLogHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 技能：流程 服务实现
 *
 * @author auto
 * @since 2024-09-18
 */
@Service
@RequiredArgsConstructor
public class SkillFlowManageServiceImpl implements ISkillFlowManageService {
  private static final Logger logger = LoggerFactory.getLogger(SkillFlowManageServiceImpl.class);

  private final SkillFlowManageMapper flowManageMapper;

  private final QuerySkillMapper querySkillMapper;

  private final ICatalogManageService catalogManageService;

  private final OperLogQueryMapper operLogQueryMapper;

  private final DiffOperLogHelper diffOperLogHelper;

  private final IResourceElementService resourceElementService;

  private final SkillFlowNodeCfgMapper skillFlowNodeCfgMapper;

  @Override
  public SkillFlowDTO findSkillFlow(Long tenantId, Long flowId) {
    SkillFlowDTO flow = flowManageMapper.getSkillFlow(tenantId, flowId);
    Assert.notNull(flow, "流程不存在");
    flow.setParam(flowManageMapper.getSkillFlowParam(tenantId, flowId));
    flow.setFlowSteps(null);
    return flow;
  }

  @Override
  @Transactional
  public ResultVO<SkillFlowDTO> saveSkillFlowBasicInfo(SkillFlowDTO flow) {
    Assert.isTrue(flow.getFlowId() == null || flow.getCopyFlowId() == null, "flowId 和 copyFlowId 不能同时不为空");
    // 复制流程
    if (flow.getCopyFlowId() != null) {
      return copyFlow(flow);
    }
    SkillFlowDTO oldFlow = null;
    if (flow.getFlowId() != null) {
      oldFlow = flowManageMapper.getSkillFlow(flow.getTenantId(), flow.getFlowId());
      Assert.notNull(oldFlow, "流程不存在");
    }
    // 校验
    ResultVO<SkillFlowDTO> validationResult = validateFlow(flow, oldFlow);
    if (validationResult != null) {
      return validationResult;
    }

    // 新增
    if (oldFlow == null) {
      flow.setFlowId(Sequences.SKILL_FLOW_ID.next());
      flow.setCreatorId(SessionUtil.getOptionalUserId());
      flow.setUpdatorId(flow.getCreatorId());
      flow.setStatusCd(BaseConsts.STATUS_CD_VALID);
      flowManageMapper.insertSkillFlow(flow);
    }
    // 修改
    else {
      flow.setFlowExplanation(ObjectUtils.getIfNull(flow.getFlowExplanation(), oldFlow.getFlowExplanation()));
      // 填充 fieldUpdateFlagMap
      flow.putFieldUpdateFlag("flowName", !Objects.equals(oldFlow.getFlowName(), flow.getFlowName()));
      flow.putFieldUpdateFlag("flowCode", !Objects.equals(oldFlow.getFlowCode(), flow.getFlowCode()));
      flow.putFieldUpdateFlag("flowType", !Objects.equals(oldFlow.getFlowType(), flow.getFlowType()));
      flow.putFieldUpdateFlag("flowDesc", !Objects.equals(oldFlow.getFlowDesc(), flow.getFlowDesc()));
      flow.putFieldUpdateFlag("catalogItemId", !Objects.equals(oldFlow.getCatalogItemId(), flow.getCatalogItemId()));
      flow.putFieldUpdateFlag("flowExplanation",
        !Objects.equals(oldFlow.getFlowExplanation(), flow.getFlowExplanation()));
      // 允许更新的字段列表
      List<String> updatableFields = Arrays.asList("flowName", "flowCode", "flowType", "flowDesc", "catalogItemId",
        "flowExplanation");
      if (updatableFields.stream().noneMatch(flow::checkFieldUpdateFlag)) {
        return BaseErrorConstant.NO_DIFFERENCE.toResult();
      }
      flow.setUpdatorId(SessionUtil.getOptionalUserId());
      flowManageMapper.updateSkillFlowBasicInfo(flow);
    }
    flow.setUpdatedTime(flowManageMapper.selectUpdatedTime(flow.getTenantId(), flow.getFlowId()));
    ResourceElementFactory.get(OperClassEnum.SKILL_FLOW.name()).submit(flow.getTenantId(), flow.getFlowId());
    return ResultVO.success(flow);
  }

  /**
   * 复制流程
   */
  private ResultVO<SkillFlowDTO> copyFlow(SkillFlowDTO flow) {
    SkillFlowDTO sourceFlow = flowManageMapper.getSkillFlow(flow.getTenantId(), flow.getCopyFlowId());
    Assert.notNull(sourceFlow, "流程不存在");
    Assert.isTrue(!Objects.equals(flow.getFlowCode(), sourceFlow.getFlowCode()), "流程编码不能与复制的流程相同");
    // 校验
    ResultVO<SkillFlowDTO> validationResult = validateFlow(flow, sourceFlow);
    if (validationResult != null) {
      return validationResult;
    }

    // 拷贝源流程的编排配置
    flow.setFlowGraphJson(sourceFlow.getFlowGraphJson());
    flow.setFlowDsl(sourceFlow.getFlowDsl());
    flow.setCatalogItemId(flow.getCatalogItemId() == null ? sourceFlow.getCatalogItemId() : flow.getCatalogItemId());

    // 拷贝源流程的出入参配置
    SkillFlowParamDTO sourceParam = flowManageMapper.getSkillFlowParam(flow.getTenantId(), sourceFlow.getFlowId());
    if (sourceParam != null) {
      SkillFlowParamDTO param = new SkillFlowParamDTO();
      param.setTenantId(flow.getTenantId());
      param.setVariableJson(sourceParam.getVariableJson());
      param.setRequestJson(sourceParam.getRequestJson());
      param.setResponseJson(sourceParam.getResponseJson());
      param.setStatusCd(BaseConsts.STATUS_CD_VALID);
      flow.setParam(param);
    }
    DataDifference<SkillFlowDTO> difference = DataDifferenceStarter.computeSaveAndLog(null, flow, true,
      flow.getTenantId(), OperClassEnum.SKILL_FLOW);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<SkillFlowDTO> saveSkillFlow(SkillFlowDTO flow) {
    SkillFlowDTO oldFlow = flow.getFlowId() == null ? null : findSkillFlow(flow.getTenantId(), flow.getFlowId());
    ResultVO<SkillFlowDTO> validationResult = validateFlow(flow, oldFlow);
    if (validationResult != null) {
      return validationResult;
    }

    flow.setStatusCd(BaseConsts.STATUS_CD_VALID);
    flow.setFlowGraphJson(flow.getGraph() != null ? JsonUtil.toJsonStringCompact(flow.getGraph()) : null);
    flow.setFlowStepJson(
      CollectionUtils.isNotEmpty(flow.getFlowSteps()) ? JsonUtil.toJsonString(flow.getFlowSteps()) : null);
    fillFlowParam(flow, oldFlow);
    if (flow.getGraph() != null) {
      flow.setFlowDsl(SceneDslUtil.toJson(SceneDslConverter.convert(flow)));
      flow.setFlowGraphJson(JsonUtil.toJsonStringCompact(flow.getGraph()));
    }
    else {
      flow.setFlowDsl(null);
      flow.setFlowGraphJson(null);
    }
    DataDifference<SkillFlowDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldFlow, flow, true,
      flow.getTenantId(), OperClassEnum.SKILL_FLOW);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 校验流程
   */
  @Nullable
  private <T> ResultVO<T> validateFlow(SkillFlowDTO flow, @Nullable SkillFlowDTO oldFlow) {
    Assert.hasLength(flow.getFlowName(), "流程名称不能为空");
    Assert.hasLength(flow.getFlowCode(), "流程编码不能为空");
    Assert.hasLength(flow.getFlowType(), "流程类型不能为空");
    Assert.notNull(flow.getTenantId(), "租户 ID 不能为空");
    Assert.isTrue(SceneConsts.FLOW_TYPES.contains(flow.getFlowType()), () -> "未知的流程类型: " + flow.getFlowType());
    // 校验编码唯一性。未修改流程编码时不需要校验
    if (oldFlow == null || !Objects.equals(oldFlow.getFlowCode(), flow.getFlowCode())) {
      if (flowManageMapper.existsSkillFlowCode(flow.getTenantId(), flow.getFlowCode())) {
        return BaseErrorConstant.CHECK_CODE.toResult(flow.getFlowCode());
      }
    }
    // 类型由对话型改为任务型时，校验不能有对话型专属的节点
    if (!flow.isMultiStep() && oldFlow != null && oldFlow.isMultiStep()) {
      SceneGraphDTO graph = JsonUtil.parseJson(oldFlow.getFlowGraphJson(), SceneGraphDTO.class);
      if (graph != null && CollectionUtils.isNotEmpty(graph.getNodes())) {
        Assert.isTrue(graph.getNodes().stream().noneMatch(SceneGraphNodeDTO::isChatOnly),
          "工作流使用了对话型专属的节点（回复、页面、页面函数），不允许改为任务型");
      }
    }
    return null;
  }

  /**
   * 填充流程参数
   */
  private void fillFlowParam(SkillFlowDTO flow, @Nullable SkillFlowDTO old) {
    SkillFlowParamDTO param;
    if (old != null && old.getParam() != null) {
      param = BeanUtil.copy(old.getParam(), SkillFlowParamDTO.class);
    }
    else {
      param = new SkillFlowParamDTO();
      param.setStatusCd(BaseConsts.STATUS_CD_VALID);
      param.setTenantId(flow.getTenantId());
    }

    param.setVariableJson(
      CollectionUtils.isNotEmpty(flow.getVariables()) ? JsonUtil.toJsonStringCompact(flow.getVariables()) : null);
    param.setRequestJson(flow.getRequest() != null ? JsonUtil.toJsonStringCompact(flow.getRequest()) : null);
    param.setResponseJson(flow.getResponse() != null ? JsonUtil.toJsonStringCompact(flow.getResponse()) : null);
    flow.setParam(param);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillFlow(Long tenantId, Long flowId) {
    if (resourceElementService.existsRelatedResource(tenantId, flowId, DataSyncCodeEnum.SKILL_FLOW.getCode())) {
      return ResultVO.fail("工作流已存在关联配置数据，不允许删除");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    flowManageMapper.deleteSkillFlow(tenantId, flowId, userId);
    flowManageMapper.deleteSkillFlowParam(tenantId, flowId, userId);
    ResourceElementFactory.get(OperClassEnum.SKILL_FLOW.name()).clear(tenantId, flowId);
    return ResultVO.success();
  }

  @Override
  public List<SimpleSkillFlowDTO> querySkillFlowList(SkillQueryParams queryParams) {
    return querySkillMapper.selectSkillFlowList(queryParams);
  }

  @Override
  public PageInfo<SkillFlowDTO> querySkillFlowPage(SkillQueryParams queryParams) {
    if (!BaseConsts.FALSE.equals(queryParams.getConfigFlag())) {
      queryParams.setCatalogItemList(
        catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(),
          CatalogConsts.TYPE_SKILL));
    }
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return flowManageMapper.selectSkillFlowPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public PageInfo<SimpleSkillFlowDTO> querySimpleSkillFlowPage(SkillQueryParams queryParams) {
    queryParams.setCatalogItemList(
      catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(),
        CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillFlowPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public SkillFlowParamDTO findSkillFlowParam(Long tenantId, Long flowId) {
    return flowManageMapper.getSkillFlowParam(tenantId, flowId);
  }

  @Override
  public ResultVO<StandardServiceDiffViewDTO> findSkillFlowVersion(Long logId, Long tenantId) {
    SkillFlowDTO skillFlow = findSkillFlowByLogId(logId, tenantId);
    if (Objects.isNull(skillFlow)) {
      return ResultVO.fail("流程服务修改记录不存在, logId=" + logId);
    }
    if (Objects.isNull(skillFlow.getFlowGraphJson())) {
      return ResultVO.success();
    }
    SceneGraphDTO graph = JsonUtil.parseJson(skillFlow.getFlowGraphJson(), SceneGraphDTO.class);
    List<SceneGraphNodeDTO> treeNodes = new ArrayList<>();
    List<String> visitedNodes = new ArrayList<>();
    List<SceneGraphNodeDTO> nodes = graph == null ? null : graph.getNodes();
    List<SceneGraphEdgeDTO> edges = graph == null ? null : graph.getEdges();

    diffOperLogHelper.treeNode(nodes, edges, StepType.START, treeNodes, new HashSet<>(), visitedNodes);
    List<SceneGraphNodeDTO> useNodes = diffOperLogHelper.tileNode(treeNodes);
    StandardServiceDiffViewDTO view = new StandardServiceDiffViewDTO();
    view.setNodes(treeNodes);
    view.setGraph(graph);
    view.setUnUseNodes(CollectionUtils.emptyIfNull(nodes).stream().filter(node -> !useNodes.contains(node))
      .collect(Collectors.toList()));
    return ResultVO.success(view);
  }

  @Override
  public ResultVO<StandardServiceDiffViewDTO> diffSkillFlowOperLog(Long tenantId, Long oldLogId, Long logId) {
    SkillFlowDTO oldSkillFlow = findSkillFlowByLogId(oldLogId, tenantId);
    SkillFlowDTO skillFlow = findSkillFlowByLogId(logId, tenantId);
    if (Objects.isNull(skillFlow) || Objects.isNull(oldSkillFlow)) {
      return ResultVO.fail("流程服务修改记录不存在, logId=" + oldLogId);
    }
    if (oldSkillFlow.getUpdatedTime() != null && skillFlow.getUpdatedTime() != null && oldSkillFlow.getUpdatedTime()
      .after(skillFlow.getUpdatedTime())) {
      SkillFlowDTO temp = oldSkillFlow;
      oldSkillFlow = skillFlow;
      skillFlow = temp;
    }
    SceneGraphDTO graph = JsonUtil.parseJson(skillFlow.getFlowGraphJson(), SceneGraphDTO.class);
    SceneGraphDTO oldGraph = JsonUtil.parseJson(oldSkillFlow.getFlowGraphJson(), SceneGraphDTO.class);
    List<SceneGraphNodeDTO> nodes = graph == null ? null : graph.getNodes();
    List<SceneGraphEdgeDTO> edges = graph == null ? null : graph.getEdges();

    List<SceneGraphNodeDTO> oldNodes = oldGraph == null ? null : oldGraph.getNodes();
    List<SceneGraphEdgeDTO> oldEdges = oldGraph == null ? null : oldGraph.getEdges();

    return ResultVO.success(diffOperLogHelper.buildDiffView(nodes, edges, oldNodes, oldEdges));
  }

  @Override
  public ResultVO<List<SimpleFlowStepDTO>> generateFlowStep(SkillFlowDTO flow) {
    List<SimpleFlowStepDTO> flowSteps = new ArrayList<>();
    if (flow.getGraph() != null) {
      for (SceneGraphNodeDTO node : CollectionUtils.emptyIfNull(flow.getGraph().getNodes())) {
        if (!StepType.NOT_ALLOW_FLOW_STEP_TYPES.contains(node.getNodeType())) {
          SimpleFlowStepDTO step = new SimpleFlowStepDTO();
          step.setNodeName(node.getNodeName());
          step.setNodeCode(node.getNodeCode());
          step.setNodeType(node.getNodeType());
          flowSteps.add(step);
        }
      }
    }
    return ResultVO.success(flowSteps);
  }

  @Nullable
  private SkillFlowDTO findSkillFlowByLogId(Long logId, Long tenantId) {
    String skillFlowJson = operLogQueryMapper.getObjDescByLogId(logId, tenantId);
    if (StringUtils.isEmpty(skillFlowJson)) {
      return null;
    }
    return JsonUtil.parseJsonRequired(skillFlowJson, SkillFlowDTO.class);
  }

  @Override
  public List<SkillFlowNodeConfigResponseDTO> getFlowNodeConfigs() {
    List<SkillFlowNodeCfgDTO> allConfigs = skillFlowNodeCfgMapper.selectAllNodeConfigs();
    // 未开启 Agent Pool 对接时，屏蔽 PlayWright 节点
    if (!SystemParameter.AGENT_POOL_ENABLED.getBooleanValueFromDb()) {
      allConfigs = allConfigs.stream().filter(c -> !StepType.PLAYWRIGHT.equals(c.getCode())).toList();
    }

    SkillFlowNodeCfgDTO rootNode = null;
    List<SkillFlowNodeCfgDTO> nodeConfigs = new ArrayList<>();
    // 获取根节点和非根节点配置
    rootNode = getSkillFlowNodeCfg(allConfigs, rootNode, nodeConfigs);

    Map<String, String> groupNameMap = new HashMap<>();
    List<String> groupCodes = new ArrayList<>();

    List<SkillFlowNodeConfigResponseDTO> result = validate(rootNode);
    if (result != null) {
      return result;
    }

    // 从根节点的 tip 字段解析分组信息
    try {
      List<SkillFlowNodeConfigGroupDTO> groups = JsonUtil.parseJson(rootNode.getTip(), new TypeReference<>() {
      });
      if (CollectionUtils.isNotEmpty(groups)) {
        for (SkillFlowNodeConfigGroupDTO group : groups) {
          groupNameMap.put(group.getGroupCode(), group.getGroupName());
          groupCodes.add(group.getGroupCode());
        }
      }
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("解析根节点配置的 tip JSON 失败，返回空配置列表", e);
      }
      return new ArrayList<>();
    }

    // 如果分组编码列表为空，直接返回空 List
    if (groupCodes.isEmpty()) {
      if (logger.isDebugEnabled()) {
        logger.debug("分组编码列表为空，返回空配置列表");
      }
      return new ArrayList<>();
    }

    // 将非根节点按 groupCode 分组
    Map<String, List<SkillFlowNodeCfgDTO>> groupedConfigs = nodeConfigs.stream()
      .collect(Collectors.groupingBy(SkillFlowNodeCfgDTO::getGroupCode));

    // 转换为响应格式，按照根节点 tip 中定义的顺序（groupCodes 的顺序）
    return buildNodeConfigResponseList(groupCodes, groupedConfigs, groupNameMap);
  }

  /**
   * 获取技能流节点配置
   */
  @Nullable
  private SkillFlowNodeCfgDTO getSkillFlowNodeCfg(List<SkillFlowNodeCfgDTO> allConfigs,
    @Nullable SkillFlowNodeCfgDTO rootNode, List<SkillFlowNodeCfgDTO> nodeConfigs) {
    for (SkillFlowNodeCfgDTO config : allConfigs) {
      config.setKey(config.getCode());
      config.setCode(null);

      if ("T".equals(config.getIsRoot())) {
        rootNode = config;
      }
      else {
        nodeConfigs.add(config);
      }
    }
    return rootNode;
  }

  /**
   * 验证根节点配置
   */
  @Nullable
  private List<SkillFlowNodeConfigResponseDTO> validate(@Nullable SkillFlowNodeCfgDTO rootNode) {
    if (rootNode == null) {
      logger.warn("根节点配置不存在，返回空配置列表");
      return List.of();
    }

    if (StringUtils.isEmpty(rootNode.getTip())) {
      logger.warn("根节点配置的 tip 字段为空，返回空配置列表");
      return List.of();
    }
    return null;
  }

  /**
   * 构建节点配置响应 List
   *
   * @param groupCodes 分组编码列表
   * @param groupedConfigs 按分组编码分组的配置列表
   * @param groupNameMap 分组编码到分组名称的映射
   * @return 节点配置响应 List
   */
  private List<SkillFlowNodeConfigResponseDTO> buildNodeConfigResponseList(List<String> groupCodes,
    Map<String, List<SkillFlowNodeCfgDTO>> groupedConfigs, Map<String, String> groupNameMap) {
    List<SkillFlowNodeConfigResponseDTO> result = new ArrayList<>();
    for (String groupCode : groupCodes) {
      List<SkillFlowNodeCfgDTO> configs = groupedConfigs.get(groupCode);
      if (configs == null || configs.isEmpty()) {
        continue;
      }

      SkillFlowNodeConfigResponseDTO response = new SkillFlowNodeConfigResponseDTO();
      response.setGroupId(groupCode);
      response.setGroupName(groupNameMap.getOrDefault(groupCode, groupCode));

      List<SkillFlowNodeItemDTO> items = new ArrayList<>();
      for (SkillFlowNodeCfgDTO config : configs) {
        SkillFlowNodeItemDTO item = new SkillFlowNodeItemDTO();
        item.setKey(config.getKey());
        item.setName(config.getName());
        item.setDesc(config.getTip());

        // 设置 flowTypes：如果值为字符串 "null" 则返回空数组，有实际值则解析，否则不设置
        String flowTypes = config.getFlowTypes();
        if ("null".equals(flowTypes)) {
          item.setFlowTypes(new ArrayList<>());
        }
        else if (StringUtils.isNotEmpty(flowTypes)) {
          item.setFlowTypes(Arrays.asList(flowTypes.split(",")));
        }

        // 设置 sceneTypes：如果值为字符串 "null" 则返回空数组，有实际值则解析，否则不设置
        String sceneTypes = config.getSceneTypes();
        if ("null".equals(sceneTypes)) {
          item.setSceneTypes(new ArrayList<>());
        }
        else if (StringUtils.isNotEmpty(sceneTypes)) {
          item.setSceneTypes(Arrays.asList(sceneTypes.split(",")));
        }

        items.add(item);
      }
      response.setItems(items);
      result.add(response);
    }
    return result;
  }
}
