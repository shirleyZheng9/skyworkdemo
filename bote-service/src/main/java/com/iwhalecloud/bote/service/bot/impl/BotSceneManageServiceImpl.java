package com.iwhalecloud.bote.service.bot.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.cache.AiGenSceneCache;
import com.iwhalecloud.bote.cache.FlowDslCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.base.OperLogDTO;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import com.iwhalecloud.bote.dto.bot.BotSceneBatchOperDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneParamDTO;
import com.iwhalecloud.bote.dto.bot.BotScenePromptDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.ClawEnvVariableDTO;
import com.iwhalecloud.bote.dto.bot.ClawWorkspaceDTO;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotApplyParams;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphEdgeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.base.OperLogQueryMapper;
import com.iwhalecloud.bote.mapper.base.ResourceElementMapper;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneRelaManageMapper;
import com.iwhalecloud.bote.mapper.bot.ClawEnvVariableManageMapper;
import com.iwhalecloud.bote.mapper.bot.ClawWorkspaceManageMapper;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bote.service.base.IAiGeneratorService;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.base.ILabelManageService;
import com.iwhalecloud.bote.service.bot.IBotManageService;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.bot.IClawWorkspaceManageService;
import com.iwhalecloud.bote.service.bot.impl.helper.LogicViewHepler;
import com.iwhalecloud.bote.service.bot.impl.helper.SceneSkillQueryHelper;
import com.iwhalecloud.bote.service.element.IResourceElementCustomizer;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.orchestration.converter.SceneDslConverter;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import com.iwhalecloud.bote.service.skill.ISkillPluginManageService;
import com.iwhalecloud.bote.service.skill.ISkillServiceManageService;
import com.iwhalecloud.bote.service.skill.ISkillSqlManageService;
import com.iwhalecloud.bote.service.skill.impl.helper.DiffOperLogHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 智能体管理服务
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class BotSceneManageServiceImpl implements IBotSceneManageService {

  private static final Logger logger = LoggerFactory.getLogger(BotSceneManageServiceImpl.class);

  // @formatter:off
  private final BotSceneManageMapper sceneManageMapper;
  private final BotSceneRelaManageMapper sceneRelaManageMapper;
  private final BotRelaManageMapper botRelaManageMapper;
  private final ClawEnvVariableManageMapper envVariableManageMapper;
  private final ClawWorkspaceManageMapper workspaceManageMapper;
  private final SceneQueryMapper sceneQueryMapper;
  private final LogicViewHepler logicViewHepler;
  private final ILabelManageService labelManageService;
  private final OperLogQueryMapper operLogQueryMapper;
  private final DiffOperLogHelper diffOperLogHelper;
  private final ICatalogManageService catalogManageService;
  private final IBotManageService botManageService;
  private final IAiGeneratorService aiGeneratorService;
  private final AiGenSceneCache aiGenSceneCache;
  private final ISkillServiceManageService apiManageService;
  private final ISkillFlowManageService flowManageService;
  private final ISkillPluginManageService pluginManageService;
  private final ISkillSqlManageService sqlManageService;
  private final BotSceneRelaManageMapper botSceneRelaManageMapper;
  private final FlowDslCache flowDslCache;
  private final IResourceElementService resourceElementService;
  private final SceneSkillQueryHelper sceneSkillQueryHelper;
  private final ResourceElementMapper resourceElementMapper;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final LargeModelManageMapper largeModelManageMapper;
  private final IClawWorkspaceManageService clawWorkspaceManageService;
  // @formatter:on

  @Override
  @Transactional
  public ResultVO<BotSceneDTO> saveSceneInfo(BotSceneDTO scene) {
    BotSceneDTO oldScene = scene.getSceneId() == null ? null : getScene(scene.getTenantId(), scene.getSceneId(), false);
    // 校验
    ResultVO<BotSceneDTO> validationResult = validateScene(scene, oldScene);
    if (validationResult != null) {
      return validationResult;
    }
    if (scene.getCopySceneId() != null) {
      // 拷贝智能体
      return copyScene(scene);
    }
    scene.setStatusCd(BaseConsts.STATUS_CD_VALID);
    if (oldScene != null) {
      // 基本信息保存智能体，部分参数前端不会传递，需要回填，避免丢失
      scene.setSceneDsl(oldScene.getSceneDsl());
      scene.setSceneGraphJson(oldScene.getSceneGraphJson());
      scene.setSceneStatus(oldScene.getSceneStatus());
      scene.setFlowStepJson(oldScene.getFlowStepJson());
      scene.setModelId(oldScene.getModelId());
    }
    else {
      scene.setSceneStatus(SceneConsts.SCENE_STATUS_UNPUBLISH);
      scene.setSceneId(Sequences.BOT_SCENE_ID.next());
    }
    // 标签信息提前处理，方便后续的血缘计算
    int count = saveSceneLabel(scene);
    if (count > 0) {
      scene.setRemark(System.currentTimeMillis() + "");
    }
    DataDifference<BotSceneDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldScene, scene, false, scene.getTenantId(), OperClassEnum.SCENE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<BotSceneDTO> aiGenPromptScene(BotSceneDTO scene) {
    try {
      // 更新生成进度
      Long tenantId = scene.getTenantId();
      updateProcessStatus(scene.getProcessId(), 1);
      // 获取租户技能列表
      updateProcessStatus(scene.getProcessId(), 2);
      List<BotSceneSkillDTO> botSceneSkillDTOS = querySkillList(tenantId);
      // 生成智能体名称、功能描述、提示词
      String query = buildChatQuery(scene.getSceneDesc(), botSceneSkillDTOS);
      Map<String, Object> chatResult = aiGeneratorService.aiGenerateScene(tenantId, query);
      updateProcessStatus(scene.getProcessId(), 3);
      // 添加基本参数
      scene.setTenantId(tenantId);
      scene.setSceneType(SceneConsts.SCENE_TYPE_SCENE);
      scene.setStatusCd(BaseConsts.STATUS_CD_VALID);
      scene.setSceneStatus(SceneConsts.SCENE_STATUS_UNPUBLISH);
      scene.setSceneName(String.valueOf(chatResult.get("name")));
      scene.setSceneDesc(String.valueOf(chatResult.get("description")));
      scene.setScenePrompt(String.valueOf(chatResult.get("cueWords")));
      // 查询并设置预置目录
      CatalogQueryParams params = new CatalogQueryParams();
      params.setTenantId(tenantId);
      params.setCatalogType(SceneConsts.SCENE_TYPE_SCENE);
      List<CatalogDTO> catalogList = catalogManageService.queryCatalogTree(params);
      scene.setCatalogItemId(CollectionUtils.isNotEmpty(catalogList) ? catalogList.get(0).getCatalogId() : -1L);
      // 填充智能体提示词
      fillScenePrompt(scene);
      // 保存新智能体
      DataDifference<BotSceneDTO> difference = DataDifferenceStarter.computeSave(null, scene, true, tenantId);
      if (difference != null) {
        BotSceneDTO newScene = difference.getToSaveData();
        saveSceneLabel(newScene);
        // 获取并保存关联技能
        List<BotSceneSkillDTO> saveSceneSkills = getSaveSceneSkillList(newScene.getTenantId(), newScene.getSceneId(), botSceneSkillDTOS,
          chatResult.get("skillIdList"));
        if (CollectionUtils.isNotEmpty(saveSceneSkills)) {
          botSceneRelaManageMapper.batchInsertSceneSkill(saveSceneSkills);
        }
        updateProcessStatus(scene.getProcessId(), 4);
        return ResultVO.success(newScene);
      }
      updateProcessStatus(scene.getProcessId(), 4);
      return ResultVO.success(scene);
    }
    catch (Exception e) {
      logger.error("Failed to ai generate bote scene. err={}", e.getMessage(), e);
      updateProcessStatus(scene.getProcessId(), -1);
      throw e;
    }
  }

  /**
   * 获取用于保存的智能体技能列表
   */
  private List<BotSceneSkillDTO> getSaveSceneSkillList(Long tenantId, Long sceneId, List<BotSceneSkillDTO> botSceneSkillDTOS, Object matchSkillIds) {
    // 提取匹配的技能列表
    List<Long> skillIdList = JsonUtil.parseJson(JsonUtil.toJsonString(matchSkillIds), new TypeReference<List<Long>>() {
    });
    List<BotSceneSkillDTO> sceneSkillList = new ArrayList<>();
    if (skillIdList != null && !skillIdList.isEmpty()) {
      sceneSkillList = botSceneSkillDTOS.stream().filter(o -> skillIdList.contains(o.getSkillId())).collect(Collectors.toList());
      // 补充智能体技能信息
      for (BotSceneSkillDTO sceneSkillDTO : sceneSkillList) {
        sceneSkillDTO.setSceneSkillId(Sequences.BOT_SCENE_SKILL_ID.next());
        sceneSkillDTO.setSceneId(sceneId);
        sceneSkillDTO.setTenantId(tenantId);
        sceneSkillDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
      }
    }
    return sceneSkillList;
  }

  @Override
  public ResultVO<Map<String, Object>> getAiGenProcess(String processId) {
    // 获取生成进度信息
    String processInfo = aiGenSceneCache.get(processId);
    if (StringUtils.isEmpty(processInfo)) {
      return ResultVO.fail("查询不到智能体生成进度信息，请检查请求信息是否正确");
    }
    // 解析进度信息
    Map<String, Object> resultMap = JsonUtil.parseJson(processInfo, new TypeReference<Map<String, Object>>() {
    });
    if (resultMap == null) {
      return ResultVO.fail("智能体生成进度信息解析失败，请联系管理员");
    }
    else if (Objects.equals(resultMap.get("step"), -1)) {
      throw new BssException("智能体生成失败，请联系管理员");
    }
    return ResultVO.success(resultMap);
  }

  /**
   * 构建AI提示词
   */
  private String buildChatQuery(String sceneDesc, List<BotSceneSkillDTO> botSceneSkillDTOS) {
    List<Map<String, Object>> skillContentList = new ArrayList<>();
    for (BotSceneSkillDTO skillDTO : botSceneSkillDTOS) {
      Map<String, Object> map = new HashMap<>();
      map.put("技能ID", skillDTO.getSkillId());
      map.put("技能描述", skillDTO.getSkillMatchContent());
      skillContentList.add(map);
    }
    Map<String, Object> queryMap = new HashMap<>();
    queryMap.put("智能体描述", sceneDesc);
    queryMap.put("技能列表", skillContentList);
    return JsonUtil.toJsonString(queryMap);
  }

  /**
   * 查询技能列表
   */
  private List<BotSceneSkillDTO> querySkillList(Long tenantId) {
    List<BotSceneSkillDTO> sceneSkillDTOList = new ArrayList<>();
    // 构建技能查询参数：查询租户下的最新100条
    SkillQueryParams params = new SkillQueryParams();
    params.setPageNum(1);
    params.setPageSize(100);
    params.setSourceFrom("tenant");
    params.setTenantId(tenantId);
    // 查询API技能
    sceneSkillDTOList.addAll(queryApiSkillList(params));
    // 查询工作流技能
    sceneSkillDTOList.addAll(queryFlowSkillList(params));
    // 查询插件技能
    sceneSkillDTOList.addAll(queryPluginSkillList(params));
    // 查询SQL技能
    sceneSkillDTOList.addAll(querySqlSkillList(params));
    return sceneSkillDTOList;
  }

  /**
   * 查询API技能
   */
  private List<BotSceneSkillDTO> queryApiSkillList(SkillQueryParams params) {
    List<BotSceneSkillDTO> sceneSkillDTOList = new ArrayList<>();
    PageInfo<SkillServiceDTO> apiSkillPage = apiManageService.querySkillServicePage(params);
    for (SkillServiceDTO apiSkillDTO : apiSkillPage.getList()) {
      BotSceneSkillDTO sceneSkillDTO = new BotSceneSkillDTO();
      sceneSkillDTO.setSkillId(apiSkillDTO.getServiceId());
      sceneSkillDTO.setSkillCode(apiSkillDTO.getServiceCode());
      sceneSkillDTO.setSkillName(apiSkillDTO.getServiceName());
      sceneSkillDTO.setSkillJson(JsonUtil.toJsonString(apiSkillDTO));
      sceneSkillDTO.setSkillMatchContent(apiSkillDTO.getServiceName());
      sceneSkillDTO.setSkillType("service");
      sceneSkillDTOList.add(sceneSkillDTO);
    }
    return sceneSkillDTOList;
  }

  /**
   * 查询工作流技能
   */
  private List<BotSceneSkillDTO> queryFlowSkillList(SkillQueryParams params) {
    List<BotSceneSkillDTO> sceneSkillDTOList = new ArrayList<>();
    PageInfo<SkillFlowDTO> flowSkillPage = flowManageService.querySkillFlowPage(params);
    for (SkillFlowDTO flowSkillDTO : flowSkillPage.getList()) {
      BotSceneSkillDTO sceneSkillDTO = new BotSceneSkillDTO();
      sceneSkillDTO.setSkillId(flowSkillDTO.getFlowId());
      sceneSkillDTO.setSkillCode(flowSkillDTO.getFlowCode());
      sceneSkillDTO.setSkillName(flowSkillDTO.getFlowName());
      sceneSkillDTO.setSkillJson(JsonUtil.toJsonString(flowSkillDTO));
      sceneSkillDTO.setSkillMatchContent(flowSkillDTO.getFlowName() + "，" + flowSkillDTO.getFlowDesc());
      sceneSkillDTO.setSkillType("workflow");
      sceneSkillDTOList.add(sceneSkillDTO);
    }
    return sceneSkillDTOList;
  }

  /**
   * 查询插件技能
   */
  private List<BotSceneSkillDTO> queryPluginSkillList(SkillQueryParams params) {
    List<BotSceneSkillDTO> sceneSkillDTOList = new ArrayList<>();
    PageInfo<SkillPluginDTO> pluginSkillPage = pluginManageService.querySkillPluginPage(params);
    for (SkillPluginDTO pluginSkillDTO : pluginSkillPage.getList()) {
      BotSceneSkillDTO sceneSkillDTO = new BotSceneSkillDTO();
      sceneSkillDTO.setSkillId(pluginSkillDTO.getApiId());
      sceneSkillDTO.setSkillCode(pluginSkillDTO.getApiCode());
      sceneSkillDTO.setSkillName(pluginSkillDTO.getApiName());
      sceneSkillDTO.setSkillJson(JsonUtil.toJsonString(pluginSkillDTO));
      sceneSkillDTO.setSkillMatchContent(pluginSkillDTO.getApiName() + "，" + pluginSkillDTO.getRemark());
      sceneSkillDTO.setSkillType("llmSkill");
      sceneSkillDTOList.add(sceneSkillDTO);
    }
    return sceneSkillDTOList;
  }

  /**
   * 查询SQL技能
   */
  private List<BotSceneSkillDTO> querySqlSkillList(SkillQueryParams params) {
    List<BotSceneSkillDTO> sceneSkillDTOList = new ArrayList<>();
    PageInfo<SkillSqlDTO> sqlSkillPage = sqlManageService.querySkillSqlPage(params);
    for (SkillSqlDTO sqlSkillDTO : sqlSkillPage.getList()) {
      BotSceneSkillDTO sceneSkillDTO = new BotSceneSkillDTO();
      sceneSkillDTO.setSkillId(sqlSkillDTO.getServiceId());
      sceneSkillDTO.setSkillCode(sqlSkillDTO.getServiceCode());
      sceneSkillDTO.setSkillName(sqlSkillDTO.getServiceName());
      sceneSkillDTO.setSkillJson(JsonUtil.toJsonString(sqlSkillDTO));
      sceneSkillDTO.setSkillMatchContent(sqlSkillDTO.getServiceName() + "，" + sqlSkillDTO.getRemark());
      sceneSkillDTO.setSkillType("sql");
      sceneSkillDTOList.add(sceneSkillDTO);
    }
    return sceneSkillDTOList;
  }

  /**
   * 更新智能体生成进度状态
   */
  private void updateProcessStatus(String processId, Integer step) {
    String process = "";
    String progressInfo = "";
    switch (step) {
      case 1:
        process = "genBasicInfo";
        progressInfo = "正在生成智能体基础信息";
        break;
      case 2:
        process = "assembleSkill";
        progressInfo = "正在组装提示词和技能";
        break;
      case 3:
        process = "genScene";
        progressInfo = "正在生成完整智能体";
        break;
      case 4:
        process = "done";
        progressInfo = "智能体生成完成";
        break;
      case -1:
        process = "failed";
        progressInfo = "智能体生成失败";
        break;
      default:
        break;
    }
    Map<String, Object> map = new HashMap<>();
    map.put("process", process);
    map.put("progressInfo", progressInfo);
    map.put("step", step);
    aiGenSceneCache.save(processId, JsonUtil.toJsonString(map));
  }

  @Override
  @Transactional
  public ResultVO<Void> publishScene(String sceneStatus, Long sceneId, Long tenantId) {
    int affectedRows = sceneManageMapper.updateSceneStatus(tenantId, sceneStatus, sceneId, SessionUtil.getLoginInfo().getUserId());
    if (affectedRows == 0) {
      return ResultVO.fail("智能体不存在");
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public List<BotSceneDTO> publishSceneBatch(BotSceneBatchOperDTO sceneOperDTO) {
    Assert.notEmpty(sceneOperDTO.getSceneIds(), "智能体 ID 列表不能为空");
    Assert.hasText(sceneOperDTO.getSceneStatus(), "智能体状态不能为空");
    Assert.notNull(sceneOperDTO.getTenantId(), "租户ID不能为空");
    // 获取智能体信息列表
    List<BotSceneDTO> botSceneList = sceneManageMapper.selectListBySceneIds(sceneOperDTO.getSceneIds(), sceneOperDTO.getTenantId());
    if (CollectionUtils.isEmpty(botSceneList)) {
      return Collections.emptyList();
    }
    // 批量更新智能体发布状态
    sceneOperDTO.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    sceneManageMapper.batchUpdateSceneStatus(sceneOperDTO);
    return botSceneList;
  }

  @Override
  @Transactional
  public ResultVO<BotSceneDTO> saveScene(BotSceneDTO scene) {
    BotSceneDTO oldScene = scene.getSceneId() == null ? null : getScene(scene.getTenantId(), scene.getSceneId(), true);
    // 校验
    ResultVO<BotSceneDTO> validationResult = validateScene(scene, oldScene);
    if (validationResult != null) {
      return validationResult;
    }
    if (SceneConsts.SCENE_TYPE_CLAW.equals(scene.getSceneType())) {
      Assert.hasLength(scene.getAgentPrompt(), "角色定义不能为空");
      Assert.hasLength(scene.getProfilePrompt(), "资料配置不能为空");
      Assert.hasLength(scene.getSoulPrompt(), "行为准则不能为空");
    }
    fillSceneInfo(scene, oldScene);
    fillSceneByType(scene, oldScene);
    DataDifference<BotSceneDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldScene, scene, true, scene.getTenantId(), OperClassEnum.SCENE);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    Optional.ofNullable(difference.getToSaveData()).ifPresent(data -> {
      data.setUpdatedTime(new Date());
      sceneManageMapper.updateSceneUpdatedTime(data);
    });
    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 根据不同智能体，填充信息
   */
  private void fillSceneByType(BotSceneDTO scene, BotSceneDTO oldScene) {
    // 普通智能体需要构造工具
    if (SceneConsts.SCENE_TYPE_SCENE.equals(scene.getSceneType())) {
      validateDelete(scene);
      scene.setSceneDsl(null);
      scene.setSceneGraphJson(CollectionUtils.isEmpty(scene.getLogicViews()) ? null : JsonUtil.toJsonString(scene.getLogicViews()));
      fillSceneSkill(scene);
    }
    // claw 智能体
    else if (SceneConsts.SCENE_TYPE_CLAW.equals(scene.getSceneType())) {
      validateDelete(scene);
      scene.setSceneDsl(null);
      scene.setSceneGraphJson(null);
      fillSceneSkill(scene);
      clawWorkspaceManageService.fillWorkspace(scene);
      for (ClawEnvVariableDTO variable : CollectionUtils.emptyIfNull(scene.getEnvVariables())) {
        variable.setTenantId(scene.getTenantId());
        variable.setSceneId(scene.getSceneId());
        variable.setStatusCd(BaseConsts.STATUS_CD_VALID);
      }
    }
    // 知识问答智能体
    else if (SceneConsts.SCENE_TYPE_KNOWLEDGE.equals(scene.getSceneType())) {
      scene.setSceneDsl(null);
      scene.setSceneGraphJson(scene.getKnowledgeSetting() == null ? null : JsonUtil.toJsonString(scene.getKnowledgeSetting()));
      fillSceneSkill(scene);
      validateKnowledgeTypeConsistency(scene);
    }
    // 复杂智能体需要有流程图，但新增智能体时没有
    else if (scene.getGraph() != null) {
      scene.setSceneGraphJson(JsonUtil.toJsonStringCompact(scene.getGraph()));
      // 对话流、工作流需要构造 DSL
      scene.setSceneDsl(SceneDslUtil.toJson(SceneDslConverter.convert(scene)));
      // 编辑面板无法调整开场白
      scene.setPrologue(oldScene != null ? oldScene.getPrologue() : scene.getPrologue());
    }
  }

  /**
   * 校验知识问答智能体中所有知识库技能的knowledgeType字段一致性
   * 只支持一种类型的知识库
   */
  private void validateKnowledgeTypeConsistency(BotSceneDTO scene) {
    Set<String> knowledgeTypes = new HashSet<>();
    MapUtils.emptyIfNull(scene.getSkills()).values().forEach(skillList -> {
      ListUtils.emptyIfNull(skillList).forEach(skill -> {
        if (skill.getSkillOriginInfo() != null) {
          String knowledgeType = MapUtils.getString(skill.getSkillOriginInfo(), "knowledgeType");
          if (StringUtils.isNotEmpty(knowledgeType)) {
            knowledgeTypes.add(knowledgeType);
          }
        }
      });
    });

    if (knowledgeTypes.size() > 1) {
      throw new BssException("知识问答智能体只支持一种类型的知识库，当前存在多种类型: " + String.join("、", knowledgeTypes));
    }
  }


  /**
   * 校验删除的技能
   */
  private void validateDelete(BotSceneDTO scene) {
    List<BotSceneSkillDTO> allSkills = new ArrayList<>();
    for (List<BotSceneSkillDTO> list : MapUtils.emptyIfNull(scene.getSkills()).values()) {
      allSkills.addAll(ListUtils.emptyIfNull(list));
    }
    List<BotSceneSkillDTO> deletedSkills = sceneSkillQueryHelper.findDeletedSkills(allSkills, scene.getTenantId());
    if (CollectionUtils.isNotEmpty(deletedSkills)) {
      String deletedNames = deletedSkills.stream()
        .map(s -> StringUtils.isEmpty(s.getSkillName()) ? s.getSkillCode() : s.getSkillName())
        .collect(Collectors.joining("、"));
      throw new BssException("以下数据已被删除或未启用，请启用或请移除：" + deletedNames);
    }
    if (scene.getModelId() == null) {
      return;
    }
    Long modelId = null;
    if (Objects.equals(-1L, scene.getModelId())) {
      modelId = tenantSettingInfoCache.getModelIdOrNull(scene.getTenantId());
      if (modelId == null) {
        return;
      }
    }
    modelId = modelId == null ? scene.getModelId() : modelId;
    LargeModelDTO largeModel = largeModelManageMapper.getLargeModel(scene.getTenantId(), modelId);
    if (largeModel == null) {
      throw new BssException("大模型不存在，请切换大模型");
    }
  }

  /**
   * 校验智能体配置
   */
  @Nullable
  private <T> ResultVO<T> validateScene(BotSceneDTO scene, @Nullable BotSceneDTO oldScene) {
    if (!EnvUtil.isDevEnv() && oldScene != null && SceneConsts.SCENE_STATUS_PUBLISH.equals(oldScene.getSceneStatus())) {
      return ResultVO.fail("智能体状态为上架状态，不允许修改");
    }
    Assert.notNull(scene.getSceneType(), "智能体类型不能为空");
    Assert.isTrue(SceneConsts.SCENE_TYPES.contains(scene.getSceneType()), () -> "未知的智能体类型: " + scene.getSceneType());
    Assert.isTrue(!SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType()), "A2A 服务类型的智能体不允许直接编辑");
    Assert.hasLength(scene.getSceneName(), "智能体名称不能为空");
    Assert.notNull(scene.getCatalogItemId(), "目录 ID 不能为空");
    if (oldScene != null) {
      Assert.isTrue(scene.getSceneType().equals(oldScene.getSceneType()), "不允许修改智能体类型");
    }
    // 智能体名称需要唯一
    if (sceneManageMapper.existsSceneName(scene.getTenantId(), scene.getSceneId(), scene.getSceneName())) {
      return BaseErrorConstant.CHECK_NAME.toResult(scene.getSceneName());
    }
    return null;
  }

  @Override
  @Transactional
  public ResultVO<Void> removeScene(Long tenantId, Long sceneId) {
    if (resourceElementService.existsRelatedResource(tenantId, sceneId, DataSyncCodeEnum.SCENE.getCode())) {
      return ResultVO.fail("智能体已存在关联配置数据，不允许删除");
    }
    sceneManageMapper.deleteScene(tenantId, sceneId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SCENE.name()).clear(tenantId, sceneId);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public List<BotSceneDTO> removeSceneBatch(BotSceneBatchOperDTO sceneOperDTO) {
    Assert.notEmpty(sceneOperDTO.getSceneIds(), "智能体 ID 列表不能为空");
    Assert.notNull(sceneOperDTO.getTenantId(), "租户ID不能为空");
    // 过滤已经被使用的智能体，仅删除未被使用的智能体
    List<Long> validSceneIds = sceneOperDTO.getSceneIds().stream()
      .filter(sceneId -> !resourceElementService.existsRelatedResource(sceneOperDTO.getTenantId(), sceneId, DataSyncCodeEnum.SCENE.getCode()))
      .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(validSceneIds)) {
      return Collections.emptyList();
    }
    // 查询待删除的智能体列表
    List<BotSceneDTO> botSceneList = sceneManageMapper.selectListBySceneIds(validSceneIds, sceneOperDTO.getTenantId());
    if (CollectionUtils.isEmpty(botSceneList)) {
      return Collections.emptyList();
    }
    // 删除未使用的智能体
    sceneOperDTO.setSceneIds(validSceneIds);
    sceneOperDTO.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    sceneManageMapper.batchDeleteScene(sceneOperDTO);
    // 删除血缘关系
    IResourceElementCustomizer elementCustomizer = ResourceElementFactory.get(OperClassEnum.SCENE.name());
    validSceneIds.forEach(sceneId -> elementCustomizer.clear(sceneOperDTO.getTenantId(), sceneId));
    return botSceneList;
  }

  @Override
  @Transactional
  public List<BotSceneDTO> moveSceneBatch(BotSceneBatchOperDTO sceneOperDTO) {
    Assert.notEmpty(sceneOperDTO.getSceneIds(), "智能体 ID 列表不能为空");
    Assert.notNull(sceneOperDTO.getTenantId(), "租户ID不能为空");
    Assert.notNull(sceneOperDTO.getTargetCatalogItemId(), "移动的目标目录ID不能为空");
    // 获取智能体信息列表
    List<BotSceneDTO> botSceneList = sceneManageMapper.selectListBySceneIds(sceneOperDTO.getSceneIds(), sceneOperDTO.getTenantId());
    if (CollectionUtils.isEmpty(botSceneList)) {
      return Collections.emptyList();
    }
    // 批量更新智能体的目录ID
    sceneOperDTO.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    sceneManageMapper.batchUpdateSceneCatalogItemId(sceneOperDTO);
    // 纠正目录相关的血缘关系
    for (BotSceneDTO scene : botSceneList) {
      resourceElementMapper.updateElementByResourceId(sceneOperDTO.getTenantId(), scene.getSceneId(), DataSyncCodeEnum.CATALOG.getCode(),
        scene.getCatalogItemId(), sceneOperDTO.getTargetCatalogItemId());
    }
    return botSceneList;
  }

  private ResultVO<BotSceneDTO> copyScene(BotSceneDTO scene) {
    BotSceneDTO old = getScene(scene.getTenantId(), scene.getCopySceneId(), true);
    old.setSceneName(scene.getSceneName());
    old.setCatalogItemId(scene.getCatalogItemId());
    old.setRemark(scene.getRemark());
    old.setLabels(new ArrayList<>());
    old.setSceneStatus(SceneConsts.SCENE_STATUS_UNPUBLISH);
    old.setStatusCd(BaseConsts.STATUS_CD_VALID);
    old.setPrologue(scene.getPrologue());
    old.setSceneDesc(scene.getSceneDesc());
    old.setSceneIcon(scene.getSceneIcon());
    // 重置配置数据中的智能体 ID，并移除主键值
    Long newSceneId = Sequences.BOT_SCENE_ID.next();
    old.setSceneId(newSceneId);
    if (old.getParam() != null) {
      old.getParam().setParamId(null);
      old.getParam().setSceneId(newSceneId);
    }
    if (old.getPrompt() != null) {
      old.getPrompt().setScenePromptId(null);
      old.getPrompt().setSceneId(newSceneId);
    }
    for (Long labelId : CollectionUtils.emptyIfNull(scene.getLabelIds())) {
      LabelObjectRelDTO label = new LabelObjectRelDTO();
      label.setRelId(Sequences.LABEL_OBJECT_REL_ID.next());
      label.setLabelId(labelId);
      label.setObjectId(newSceneId);
      label.setObjectType(BaseConsts.LABEL_TYPE_SCENE);
      label.setStatusCd(BaseConsts.STATUS_CD_VALID);
      label.setTenantId(scene.getTenantId());
      old.getLabels().add(label);
    }
    for (BotSceneSkillDTO skill : CollectionUtils.emptyIfNull(old.getFlatSkills())) {
      skill.setSceneSkillId(null);
      skill.setSceneId(newSceneId);
    }
    for (ClawWorkspaceDTO workspace : ListUtils.emptyIfNull(old.getWorkspaces())) {
      workspace.setId(null);
      workspace.setSceneId(newSceneId);
    }
    for (ClawEnvVariableDTO envVariable : ListUtils.emptyIfNull(old.getEnvVariables())) {
      envVariable.setId(null);
      envVariable.setSceneId(newSceneId);
    }
    scene.setTenantId(old.getTenantId());
    DataDifferenceStarter.computeSaveAndLog(null, old, true, old.getTenantId(), OperClassEnum.SCENE);
    return ResultVO.success(old);
  }

  @Override
  @Nullable
  public BotSceneDTO getSceneInfo(Long tenantId, Long sceneId) {
    BotSceneDTO scene = sceneManageMapper.getScene(tenantId, sceneId);
    if (scene == null) {
      return null;
    }
    // 返回labelIds 方便前端处理
    List<LabelObjectRelDTO> labels = labelManageService.queryLabelObjectRelList(Collections.singletonList(sceneId), BaseConsts.LABEL_TYPE_SCENE,
      tenantId);
    scene.setLabelIds(CollectionUtils.emptyIfNull(labels).stream().map(LabelObjectRelDTO::getLabelId).collect(Collectors.toList()));
    return scene;
  }

  @Override
  public BotSceneDTO getSimpleScene(Long tenantId, Long sceneId) {
    return sceneManageMapper.selectSimpleScene(tenantId, sceneId);
  }

  @Override
  public BotSceneDTO getScene(Long tenantId, Long sceneId, boolean withChildren) {
    Assert.notNull(sceneId, "sceneId 不能为空");
    BotSceneDTO scene = sceneManageMapper.getScene(tenantId, sceneId);
    Assert.notNull(scene, () -> "智能体不存在: sceneId=" + sceneId);
    // 只有复杂智能体有入参、智能体变量
    if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
      scene.setParam(sceneRelaManageMapper.getSceneParam(scene.getTenantId(), sceneId));
    }
    // 基础信息扩展字段 labelIds，方便前端处理
    List<LabelObjectRelDTO> labels = labelManageService.queryLabelObjectRelList(Collections.singletonList(sceneId), BaseConsts.LABEL_TYPE_SCENE,
      tenantId);
    scene.setLabels(labels);
    scene.setLabelIds(CollectionUtils.emptyIfNull(labels).stream().map(LabelObjectRelDTO::getLabelId).collect(Collectors.toList()));

    // 复杂智能体没有提示词、技能
    if (withChildren && !SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
      BotScenePromptDTO prompt = sceneRelaManageMapper.getScenePrompt(scene.getTenantId(), sceneId);
      if (prompt != null) {
        scene.setPrompt(prompt);
        scene.setPromptId(prompt.getPromptId());
        scene.setScenePrompt(prompt.getScenePrompt());
      }
      // 技能按照类别分组，方便前端处理
      List<BotSceneSkillDTO> skills = sceneRelaManageMapper.selectSceneSkillList(scene.getTenantId(), sceneId);
      if (CollectionUtils.isNotEmpty(skills)) {
        sceneSkillQueryHelper.fillSkill(skills);
        scene.setFlatSkills(skills);
        scene.setSkills(skills.stream().collect(Collectors.groupingBy(BotSceneSkillDTO::getSkillType)));
      }

      if (SceneConsts.SCENE_TYPE_CLAW.equals(scene.getSceneType())) {
        // claw 类型，补充智能体定义、环境变量
        scene.setEnvVariables(envVariableManageMapper.selectClawEnvVariableList(tenantId, sceneId));
        clawWorkspaceManageService.flatWorkspace(scene);
      }
    }
    // 标记当前智能体是否有关联的机器人
    scene.setWithRelBot(botRelaManageMapper.existsBotSceneRelByScene(tenantId, null, sceneId));
    return scene;
  }

  @Override
  public PageInfo<BotSceneDTO> queryScenePage(BotSceneQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    queryParams.setCatalogItemList(
      catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_SCENE));
    //noinspection resource
    PageInfo<BotSceneDTO> pageInfo = sceneManageMapper.selectScenePage(queryParams, rowBounds).toPageInfo();
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      // 补充标签信息
      List<Long> sceneIds = pageInfo.getList().stream().map(BotSceneDTO::getSceneId).collect(Collectors.toList());
      Map<Long, List<LabelObjectRelDTO>> group = CollectionUtils.emptyIfNull(
          labelManageService.queryLabelObjectRelList(sceneIds, BaseConsts.LABEL_TYPE_SCENE, queryParams.getTenantId())).stream()
        .collect(Collectors.groupingBy(LabelObjectRelDTO::getObjectId));
      for (Entry<Long, List<LabelObjectRelDTO>> entry : group.entrySet()) {
        BotSceneDTO scene = IterableUtils.find(pageInfo.getList(), p -> Objects.equals(entry.getKey(), p.getSceneId()));
        scene.setLabels(entry.getValue());
        scene.setLabelIds(CollectionUtils.emptyIfNull(entry.getValue()).stream().map(LabelObjectRelDTO::getLabelId).collect(Collectors.toList()));
      }
    }
    return pageInfo;
  }

  @Override
  public List<BotSceneDTO> querySceneList(BotSceneQueryParams queryParams) {
    return sceneManageMapper.selectSceneList(queryParams);
  }

  @Override
  public PageInfo<BotSceneDTO> queryScenePageForBot(BotSceneQueryParams queryParams) {
    queryParams.setCatalogItemList(
      catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_SCENE));
    // noinspection resource
    return sceneManageMapper.queryScenePageForBot(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<BotSceneDTO> beyondQueryScenePage(BotSceneQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    return sceneManageMapper.beyondQueryScenePage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public String generatePrompt(BotSceneDTO scene) {
    fillSceneSkill(scene);
    // 补充技能入参变量
    logicViewHepler.fillVariableForSkill(scene);

    String template = SystemParameter.SCENE_TEMPLATE_PROMPT.getValueFromDb();

    // 包含上传附件标识
    boolean containsFileId = false;
    for (BotSceneSkillDTO skill : scene.getFlatSkills()) {
      containsFileId = IterableUtils.matchesAny(skill.getInputVariables(), p -> "fileId".equals(p.getName()));
      if (containsFileId) {
        break;
      }
    }

    Map<String, Object> prompt = new HashMap<>(4);
    prompt.put("sceneName", scene.getSceneName());
    prompt.put("logics", scene.getLogicViews());
    prompt.put("skills", scene.getFlatSkills());
    prompt.put("containsFileId", containsFileId);
    return FreemarkerUtil.process(template, prompt);
  }

  /**
   * 填充补充信息
   */
  private void fillSceneInfo(BotSceneDTO scene, @Nullable BotSceneDTO oldScene) {
    // 只有复杂智能体有入参、智能体变量
    if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
      BotSceneParamDTO param;
      if (oldScene != null && oldScene.getParam() != null) {
        param = BeanUtil.copy(oldScene.getParam(), BotSceneParamDTO.class);
      }
      else {
        param = new BotSceneParamDTO();
        param.setSceneId(scene.getSceneId());
        param.setTenantId(scene.getTenantId());
        param.setStatusCd(BaseConsts.STATUS_CD_VALID);
      }
      param.setVariableJson(CollectionUtils.isNotEmpty(scene.getVariables()) ? JsonUtil.toJsonStringCompact(scene.getVariables()) : null);
      param.setRequestJson(scene.getRequest() != null ? JsonUtil.toJsonStringCompact(scene.getRequest()) : null);
      scene.setParam(param);

      scene.setFlowStepJson(CollectionUtils.isNotEmpty(scene.getFlowSteps()) ? JsonUtil.toJsonString(scene.getFlowSteps()) : null);
    }
    fillScenePrompt(scene);
    // 保存智能体，不需要修改智能体基础信息
    if (oldScene != null) {
      scene.setLabels(oldScene.getLabels());
      scene.setRemark(oldScene.getRemark());
      scene.setSceneDesc(oldScene.getSceneDesc());
      scene.setSceneIcon(oldScene.getSceneIcon());
      scene.setSceneName(oldScene.getSceneName());
      scene.setCatalogItemId(oldScene.getCatalogItemId());
      scene.setSceneStatus(oldScene.getSceneStatus());
    }
    // 自定义模型信息
    if (scene.getCustomModelConfig() != null) {
      scene.setModelConfigJson(JsonUtil.toJsonString(scene.getCustomModelConfig()));
    }
    scene.setStatusCd(BaseConsts.STATUS_CD_VALID);
  }

  private void fillScenePrompt(BotSceneDTO scene) {
    if (scene.getPromptId() == null && StringUtils.isEmpty(scene.getScenePrompt())) {
      return;
    }
    BotScenePromptDTO prompt = sceneRelaManageMapper.getScenePrompt(scene.getTenantId(), scene.getSceneId());
    if (prompt == null) {
      prompt = new BotScenePromptDTO();
      prompt.setSceneId(scene.getSceneId());
      prompt.setTenantId(scene.getTenantId());
      prompt.setStatusCd(BaseConsts.STATUS_CD_VALID);
      prompt.setPromptType("scene");
    }
    prompt.setPromptId(scene.getPromptId());
    prompt.setScenePrompt(scene.getScenePrompt());
    scene.setPrompt(prompt);
  }

  private void fillSceneSkill(BotSceneDTO scene) {
    List<BotSceneSkillDTO> flatSkills = new ArrayList<>();
    for (List<BotSceneSkillDTO> list : MapUtils.emptyIfNull(scene.getSkills()).values()) {
      for (BotSceneSkillDTO skill : list) {
        skill.setSceneId(scene.getSceneId());
        skill.setTenantId(scene.getTenantId());
        if (skill.getSkillOriginInfo() != null) {
          skill.setSkillJson(JsonUtil.toJsonString(skill.getSkillOriginInfo()));
        }
        flatSkills.add(skill);
      }
    }
    scene.setFlatSkills(flatSkills);
  }

  private int saveSceneLabel(BotSceneDTO scene) {
    int count = 0;
    List<LabelObjectRelDTO> labels = labelManageService.queryLabelObjectRelList(Collections.singletonList(scene.getSceneId()),
      BaseConsts.LABEL_TYPE_SCENE, scene.getTenantId());
    List<LabelObjectRelDTO> insertLabels = new ArrayList<>();
    List<Long> deleteIds = new ArrayList<>();
    for (LabelObjectRelDTO dto : CollectionUtils.emptyIfNull(labels)) {
      if (!CollectionUtils.emptyIfNull(scene.getLabelIds()).contains(dto.getLabelId())) {
        deleteIds.add(dto.getRelId());
      }
    }
    for (Long labelId : CollectionUtils.emptyIfNull(scene.getLabelIds())) {
      boolean exists = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(labels), p -> Objects.equals(labelId, p.getLabelId()));
      if (!exists) {
        LabelObjectRelDTO label = new LabelObjectRelDTO();
        label.setRelId(Sequences.LABEL_OBJECT_REL_ID.next());
        label.setLabelId(labelId);
        label.setObjectId(scene.getSceneId());
        label.setObjectType(BaseConsts.LABEL_TYPE_SCENE);
        label.setTenantId(scene.getTenantId());
        label.setStatusCd(BaseConsts.STATUS_CD_VALID);
        label.setCreatorId(SessionUtil.getLoginInfo().getUserId());
        insertLabels.add(label);
      }
    }
    if (CollectionUtils.isNotEmpty(insertLabels)) {
      labelManageService.saveLabelObjectRel(insertLabels);
      count = count + insertLabels.size();
      if (IterableUtils.matchesAny(insertLabels, p -> Objects.equals(SceneConsts.SCENE_LABEL_ID_PLAN_AGENT, p.getLabelId()))) {
        // 带有规划标签，需要预置默认的智能体变量
        setVariableForPlanAgent(scene);
      }
    }
    if (CollectionUtils.isNotEmpty(deleteIds)) {
      labelManageService.deleteLabelObjectRel(deleteIds, scene.getTenantId());
      count = count + deleteIds.size();
    }
    return count;
  }

  @Override
  public List<RecommendedSceneDTO> queryRecommendedScenes(BotSceneQueryParams params) {
    return sceneQueryMapper.selectRecommendedScenes(params);
  }

  @Override
  @Nullable
  public ResultVO<StandardServiceDiffViewDTO> findSceneVersion(Long logId, Long tenantId) {
    BotSceneDTO scene = findScene(logId, tenantId);
    if (Objects.isNull(scene)) {
      return ResultVO.fail("智能体修改记录不存在, logId=" + logId);
    }
    if (Objects.isNull(scene.getGraph())) {
      return ResultVO.success();
    }
    SceneGraphDTO graph = JsonUtil.parseJson(JsonUtil.toJsonString(scene.getGraph()), SceneGraphDTO.class);
    List<SceneGraphNodeDTO> treeNodes = new ArrayList<>();
    List<String> visitedNodes = new ArrayList<>();
    diffOperLogHelper.treeNode(scene.getGraph().getNodes(), scene.getGraph().getEdges(), StepType.START, treeNodes, new HashSet<>(), visitedNodes);
    List<SceneGraphNodeDTO> useNodes = diffOperLogHelper.tileNode(treeNodes);
    StandardServiceDiffViewDTO view = new StandardServiceDiffViewDTO();
    view.setNodes(treeNodes);
    view.setGraph(graph);
    view.setUnUseNodes(scene.getGraph().getNodes().stream().filter(node -> !useNodes.contains(node)).collect(Collectors.toList()));
    return ResultVO.success(view);
  }

  @Override
  public ResultVO<StandardServiceDiffViewDTO> diffSceneOperLog(Long tenantId, Long oldLogId, Long logId) {
    BotSceneDTO oldScene = findScene(oldLogId, tenantId);
    BotSceneDTO scene = findScene(logId, tenantId);
    if (Objects.isNull(oldScene) || Objects.isNull(scene)) {
      return ResultVO.fail("智能体修改记录不存在, logId=" + logId);
    }
    List<SceneGraphNodeDTO> nodes = scene.getGraph() == null ? null : scene.getGraph().getNodes();
    List<SceneGraphEdgeDTO> edges = scene.getGraph() == null ? null : scene.getGraph().getEdges();

    List<SceneGraphNodeDTO> oldNodes = oldScene.getGraph() == null ? null : oldScene.getGraph().getNodes();
    List<SceneGraphEdgeDTO> oldEdges = oldScene.getGraph() == null ? null : oldScene.getGraph().getEdges();

    return ResultVO.success(diffOperLogHelper.buildDiffView(nodes, edges, oldNodes, oldEdges));
  }

  @Override
  @Transactional
  public ResultVO<Long> publish(BotApplyParams apply) {
    BotSceneDTO scene = getSceneInfo(apply.getTenantId(), apply.getSceneId());
    Assert.notNull(scene, "智能体信息获取失败");

    // 已确认逻辑：1.发布到存量应用时不处理智能体上下架逻辑：isPublish 为空表表示发布到存量应用
    // 2.发布为新的智能应用时，如果勾选isPublish才自动上架
    if (BaseConsts.TRUE.equals(apply.getIsPublish())) {
      publishScene(SceneConsts.SCENE_STATUS_PUBLISH, apply.getSceneId(), apply.getTenantId());
    }

    return botManageService.apply(apply, scene);
  }

  @Override
  public ResultVO<List<SimpleFlowStepDTO>> generateFlowStep(BotSceneDTO scene) {
    List<SimpleFlowStepDTO> flowSteps = new ArrayList<>();
    if (scene.getGraph() == null) {
      return ResultVO.success(flowSteps);
    }
    for (SceneGraphNodeDTO node : CollectionUtils.emptyIfNull(scene.getGraph().getNodes())) {
      if (StepType.NOT_ALLOW_FLOW_STEP_TYPES.contains(node.getNodeType())) {
        continue;
      }
      SimpleFlowStepDTO step = new SimpleFlowStepDTO();
      step.setNodeName(node.getNodeName());
      step.setNodeCode(node.getNodeCode());
      step.setNodeType(node.getNodeType());
      if (StepType.WORKFLOW.equals(node.getNodeType())) {
        Long flowId = MapUtils.getLong(node.getNodeData(), "flowId");
        if (flowId != null) {
          step.setFlowId(flowId);
          // 补充流程步骤信息
          SceneDslDTO dsl = flowDslCache.get(scene.getTenantId(), flowId);
          Assert.notNull(dsl, () -> "工作流不存在: id=" + flowId);
          step.setChildren(dsl.getFlowSteps());
        }
      }
      flowSteps.add(step);
    }
    return ResultVO.success(flowSteps);
  }

  private BotSceneDTO findScene(Long logId, Long tenantId) {
    String scene = operLogQueryMapper.getObjDescByLogId(logId, tenantId);
    if (StringUtils.isEmpty(scene)) {
      return null;
    }
    BotSceneDTO botScene = JsonUtil.parseJson(scene, BotSceneDTO.class);
    if (Objects.isNull(botScene)) {
      return null;
    }
    if (botScene.getGraph() == null) {
      botScene.setGraph(JsonUtil.parseJson(botScene.getSceneGraphJson(), SceneGraphDTO.class));
    }
    return botScene;
  }

  private void setVariableForPlanAgent(BotSceneDTO scene) {
    if (!SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType())) {
      return;
    }
    //@formatter:off
    ParameterSpec userMessage = ParameterSpec.builder()
      .key(UUIDUtils.randomFormatUuid())
      .type(AttrDataType.STRING)
      .name("userMessage")
      .description("用户消息")
      .build();
    ParameterSpec steps = ParameterSpec.builder()
      .key(UUIDUtils.randomFormatUuid())
      .type(AttrDataType.ANY)
      .name("steps")
      .description("步骤列表")
      .build();
    //@formatter:on
    BotSceneParamDTO param = sceneRelaManageMapper.getSceneParam(scene.getTenantId(), scene.getSceneId());
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (param == null) {
      BotSceneParamDTO dto = new BotSceneParamDTO();
      dto.setParamId(Sequences.BOT_SCENE_PARAM_ID.next());
      dto.setSceneId(scene.getSceneId());
      dto.setTenantId(scene.getTenantId());
      dto.setVariableJson(JsonUtil.toJsonString(ImmutableList.of(userMessage, steps)));
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
      dto.setCreatorId(userId);
      dto.setUpdatorId(userId);
      sceneRelaManageMapper.insertSceneParam(dto);
    }
    else {
      boolean exists = false;
      List<ParameterSpec> specs = null;
      if (StringUtils.isNotEmpty(param.getVariableJson())) {
        specs = JsonUtil.parseJson(param.getVariableJson(), new TypeReference<List<ParameterSpec>>() {
        });
        exists = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(specs),
          p -> Objects.equals(userMessage.getName(), p.getName()) || Objects.equals(steps.getName(), p.getName()));
      }
      if (!exists) {
        if (CollectionUtils.isEmpty(specs)) {
          specs = new ArrayList<>();
        }
        specs.add(userMessage);
        specs.add(steps);
        param.setVariableJson(JsonUtil.toJsonString(specs));
        param.setUpdatorId(userId);
        sceneRelaManageMapper.updateSceneParam(param);
      }
    }
  }

  @Override
  public BotSceneDTO getBotSceneveisonDetail(Long logId, Long tenantId) {
    OperLogDTO operLog = operLogQueryMapper.getOperLogDetail(logId, tenantId);
    if (operLog == null) {
      throw new BssException("智能体修改记录不存在, logId=" + logId);
    }
    String boteSceneJson = operLogQueryMapper.getObjDescByLogId(logId, tenantId);
    BotSceneDTO botScene = JsonUtil.parseJsonRequired(boteSceneJson, BotSceneDTO.class);
    botScene.setUpdatedTime(operLog.getUpdatedTime());
    return botScene;
  }
}
