package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.dto.agent.SimpleAiDefinitionDTO;
import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.scene.SimpleClawEnvVariableDTO;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bote.service.model.helper.SkillToolConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 场景缓存
 *
 * @author chen.linfa
 * @since 2024-10-11
 */
@Component
public class SceneCache extends AbstractSkillCache<SimpleBotSceneDTO> {
  private final SceneQueryMapper sceneQueryMapper;
  private final FlowDslCache flowDslCache;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  public SceneCache(SceneQueryMapper sceneQueryMapper, FlowDslCache flowDslCache, TenantSettingInfoCache tenantSettingInfoCache) {
    super(CacheConsts.KEY_PREFIX_SCENE);
    this.sceneQueryMapper = sceneQueryMapper;
    this.flowDslCache = flowDslCache;
    this.tenantSettingInfoCache = tenantSettingInfoCache;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SCENE;
  }

  /**
   * 获取场景
   */
  public SimpleBotSceneDTO getScene(Long tenantId, Long sceneId) {
    SimpleBotSceneDTO scene = get(tenantId, sceneId);
    Assert.notNull(scene, () -> "智能体不存在，或未上架: sceneId=" + sceneId);
    return scene;
  }

  /**
   * 从数据库查询场景
   */
  public SimpleBotSceneDTO getSceneFromDb(Long tenantId, Long sceneId) {
    SimpleBotSceneDTO scene = sceneQueryMapper.selectSceneBySceneId(tenantId, sceneId, null, SceneConsts.SCENE_LABEL_JUMP,
      SceneConsts.SCENE_LABEL_CONFIRM_START, SceneConsts.SCENE_LABEL_AUTO_START);
    Assert.notNull(scene, () -> "智能体不存在，或未上架: sceneId=" + sceneId);
    return scene;
  }

  /**
   * 获取场景名称
   */
  @Nullable
  public String getSceneName(Long tenantId, Long sceneId) {
    SimpleBotSceneDTO scene = get(tenantId + CacheConsts.COLON + sceneId);
    return scene != null ? scene.getSceneName() : null;
  }

  @Nullable
  public List<SimpleFlowStepDTO> getFlowSteps(Long tenantId, Long sceneId) {
    SimpleBotSceneDTO scene = get(tenantId + CacheConsts.COLON + sceneId);
    if (scene != null && StringUtils.isNotEmpty(scene.getFlowStepJson())) {
      List<SimpleFlowStepDTO> steps = JsonUtil.parseJson(scene.getFlowStepJson(), new TypeReference<>() {
      });
      for (SimpleFlowStepDTO step : CollectionUtils.emptyIfNull(steps)) {
        if (StepType.WORKFLOW.equals(step.getNodeType())) {
          SceneDslDTO dsl = flowDslCache.get(tenantId, step.getFlowId());
          if (dsl != null && CollectionUtils.isNotEmpty(dsl.getFlowSteps())) {
            step.setChildren(dsl.getFlowSteps());
          }
        }
      }
      return steps;
    }
    return Collections.emptyList();
  }

  /**
   * 获取简单场景的技能列表
   */
  public List<SkillToolDTO> getSkillTools(SimpleBotSceneDTO scene, Long tenantId, Long modelId, boolean debug) {
    List<LlmSkillItem> skillItems = getSkillItems(scene, tenantId);
    return SkillToolConverter.convert(tenantId, modelId, skillItems, debug);
  }

  /**
   * 查询场景技能列表
   */
  public List<LlmSkillItem> getSkillItems(SimpleBotSceneDTO scene, Long tenantId) {
    // 惰性初始化
    List<LlmSkillItem> skillItems = scene.getSkillItems();
    if (skillItems == null) {
      skillItems = loadSkillItems(tenantId, scene.getSceneId());
      scene.setSkillItems(skillItems);
    }
    return skillItems;
  }

  /**
   * 获取通用智能体配置
   */
  public SimpleAiDefinitionDTO getAiDefinition(SimpleBotSceneDTO scene, Long tenantId) {
    SimpleAiDefinitionDTO config = new SimpleAiDefinitionDTO();
    config.setTenantId(tenantId);
    if (scene.getModelId() == null || ModelConsts.DEFAULT_MODEL.equals(scene.getModelId())) {
      config.setModelId(tenantSettingInfoCache.getModelId(tenantId));
    }
    else {
      config.setModelId(scene.getModelId());
    }
    // TODO 识别平台模型
    config.setModelTenantId(tenantId);
    config.setEnvVariables(getClawEnvVariables(scene, tenantId, EnvUtil.getEnvCode()));

    List<LlmSkillItem> skillItems = getSkillItems(scene, tenantId);
    if (!skillItems.isEmpty()) {
      config.setPlatformMcpIds(skillItems.stream()
        .filter(item -> Boolean.TRUE.equals(item.getPlatform()))
        .filter(item -> StepType.MCP.equals(item.getSkillType()))
        .map(LlmSkillItem::getSkillId).toList());
      config.setMcpIds(skillItems.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getPlatform()))
        .filter(item -> StepType.MCP.equals(item.getSkillType()))
        .map(LlmSkillItem::getSkillId).toList());
      config.setPlatformAgentSkillIds(skillItems.stream()
        .filter(item -> Boolean.TRUE.equals(item.getPlatform()))
        .filter(item -> StepType.AGENT_SKILL.equals(item.getSkillType()))
        .map(LlmSkillItem::getSkillId).toList());
      config.setAgentSkillIds(skillItems.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getPlatform()))
        .filter(item -> StepType.AGENT_SKILL.equals(item.getSkillType()))
        .map(LlmSkillItem::getSkillId).toList());
      config.setSubagentSceneIds(skillItems.stream()
        .filter(item -> StepType.INVOKE_SCENE.equals(item.getSkillType()))
        .map(LlmSkillItem::getSkillId).toList());
    }
    return config;
  }

  /**
   * 查询 claw 工作区文件
   */
  public List<SimpleAiWorkspaceDTO> getClawWorkspaces(SimpleBotSceneDTO scene, Long tenantId) {
    List<SimpleAiWorkspaceDTO> list = scene.getClawWorkspaces();
    if (list == null) {
      list = sceneQueryMapper.selectWorkspaceBySceneId(tenantId, scene.getSceneId(), SceneConsts.PROMPT_FILES);
      scene.setClawWorkspaces(list);
    }
    return list;
  }

  /**
   * 查询 claw 环境变量
   */
  public Map<String, String> getClawEnvVariables(SimpleBotSceneDTO scene, Long tenantId, String envCode) {
    Map<String, String> envVariables = scene.getClawEnvVariables();
    if (envVariables == null) {
      List<SimpleClawEnvVariableDTO> envVariableList = sceneQueryMapper.selectEnvVariableBySceneId(tenantId, scene.getSceneId(), envCode);
      if (CollectionUtils.isEmpty(envVariableList)) {
        envVariables = Collections.emptyMap();
      }
      else {
        envVariables = new LinkedHashMap<>(envVariableList.size());
        for (SimpleClawEnvVariableDTO envVariable : envVariableList) {
          if (StringUtils.isNotEmpty(envVariable.getVariableCode())) {
            envVariables.put(envVariable.getVariableCode(), envVariable.getVariableVal());
          }
        }
      }
      scene.setClawEnvVariables(envVariables);
    }
    return envVariables;
  }

  /**
   * 从数据库查询简单场景的技能列表
   */
  private List<LlmSkillItem> loadSkillItems(Long tenantId, Long sceneId) {
    // TODO 优化检查是否是平台级技能的方式，最好给 bt_bot_scene_skill 增加 is_platform 字段
    List<BotSceneSkillDTO> skills = sceneQueryMapper.selectSkillsBySceneId(tenantId, sceneId);
    if (skills.isEmpty()) {
      return Collections.emptyList();
    }
    List<LlmSkillItem> skillItems = new ArrayList<>(skills.size());
    for (BotSceneSkillDTO skill : skills) {
      skill.setTenantId(tenantId);
      LlmSkillItem item = new LlmSkillItem();
      item.setSkillType(skill.getSkillType());
      item.setSkillId(skill.getSkillId());
      item.setExtSkillId(skill.getExtSkillId());
      item.setPlatform(skill.getPlatform());
      // 解析知识库技能json
      if (StringUtils.isNotEmpty(skill.getSkillJson()) && StepType.KNOWLEDGE_CHAT.equals(skill.getSkillType())) {
        Map<String, Object> skillJson = JsonUtil.parseJson(skill.getSkillJson(), new TypeReference<>() {
        });
        item.setKnowledgeType(MapUtils.getString(skillJson, "knowledgeType"));
      }
      setLlmSkillItem(skill, item);
      skillItems.add(item);
    }
    return skillItems;
  }

  @Override
  @Nullable
  protected SimpleBotSceneDTO loadById(Long tenantId, Long id) {
    return sceneQueryMapper.selectSceneBySceneId(tenantId, id, SceneConsts.SCENE_STATUS_PUBLISH, SceneConsts.SCENE_LABEL_JUMP,
      SceneConsts.SCENE_LABEL_CONFIRM_START, SceneConsts.SCENE_LABEL_AUTO_START);
  }

  @Override
  protected Map<Long, SimpleBotSceneDTO> loadByIds(Long tenantId, List<Long> ids) {
    List<SimpleBotSceneDTO> scenes = sceneQueryMapper.selectScenesBySceneIds(tenantId, ids, SceneConsts.SCENE_STATUS_PUBLISH, SceneConsts.SCENE_LABEL_JUMP,
      SceneConsts.SCENE_LABEL_CONFIRM_START, SceneConsts.SCENE_LABEL_AUTO_START);
    return scenes.stream().collect(Collectors.toMap(SimpleBotSceneDTO::getSceneId, Function.identity()));
  }

  @SuppressWarnings("unchecked")
  private void setLlmSkillItem(BotSceneSkillDTO skill, LlmSkillItem item) {
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(skill.getTenantId());
    // MCP 技能需要解析选中的工具名称列表
    if (StepType.MCP.equals(skill.getSkillType())) {
      skill.parseSkillConfig();
      Object toolNames = MapUtils.getObject(skill.getSkillConfig(), "mcpToolNames");
      if (toolNames instanceof List && !((List<?>) toolNames).isEmpty() && ((List<?>) toolNames).getFirst() instanceof String) {
        item.setToolNames((List<String>) toolNames);
      }
    }
    // 对接外系统知识库 名称需从技能原始信息提取
    setKnowledgeName(skill, item, knowledgeInfo);
    if (StepType.DATA_TABLE.equals(skill.getSkillType())) {
      skill.parseSkillConfig();
      Object allowed = MapUtils.getObject(skill.getSkillConfig(), "allowed");
      if (allowed instanceof List && !((List<?>) allowed).isEmpty()) {
        item.setDatabaseAlloweds((List<String>) allowed);
      }
    }
    setForPlugin(skill, item);
  }

  /**
   * 提取知识库技能的名称
   */
  private void setKnowledgeName(BotSceneSkillDTO skill, LlmSkillItem item, KnowledgeInfoDTO knowledgeInfo) {
    boolean isOtherKnowledge = KnowledgeConsts.OTHER_KNOWLEDGE_TYPES.contains(item.getKnowledgeType());
    if (StepType.KNOWLEDGE_CHAT.equals(skill.getSkillType()) && (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType()) || isOtherKnowledge)) {
      skill.parseSkillOriginInfo();
      String knowledgeName = MapUtils.getString(skill.getSkillOriginInfo(), "knowledgeName");
      item.setKnowledgeName(knowledgeName);
      String docName = MapUtils.getString(skill.getSkillOriginInfo(), "docName");
      item.setDocName(docName);
    }
  }

  @SuppressWarnings("unchecked")
  private void setForPlugin(BotSceneSkillDTO skill, LlmSkillItem item) {
    if (!StepType.PLUGIN.equals(skill.getSkillType())) {
      return;
    }
    if (skill.enabledPluginHub()) {
      item.setIsPluginHub(true);
      // 解析选中的工具名称列表
      skill.parseSkillConfig();
      Object toolNames = MapUtils.getObject(skill.getSkillConfig(), "toolNames");
      if (toolNames instanceof List && !((List<?>) toolNames).isEmpty() && ((List<?>) toolNames).getFirst() instanceof String) {
        item.setToolNames((List<String>) toolNames);
      }
    }
    else {
      item.setIsPluginHub(false);
    }
  }
}
