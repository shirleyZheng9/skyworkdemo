package com.iwhalecloud.bote.service.bot.impl.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.doc.common.utils.KnowledgeClientUtil;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.database.SimpleDataTableDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.mcp.SimpleMcpServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleAgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillSqlDTO;
import com.iwhalecloud.bote.mapper.database.DataTableQueryMapper;
import com.iwhalecloud.bote.mapper.mcp.McpServerManageMapper;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 智能体技能查询辅助类
 *
 * @author chen.linfa
 * @since 2025-07-30
 */
@Service
public class SceneSkillQueryHelper {

  private final Map<String, Consumer<BotSceneSkillDTO>> skillQueryMethodMap;
  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);

  public SceneSkillQueryHelper(QuerySkillMapper skillQueryMapper, McpServerManageMapper mcpServerManageMapper,
    KnowledgeBaseManageMapper knowledgeBaseManageMapper, DataTableQueryMapper dataTableQueryMapper,
    SceneCache sceneCache) {
    this.skillQueryMethodMap = buildSkillQueryMethodMap(skillQueryMapper, mcpServerManageMapper, knowledgeBaseManageMapper,
      dataTableQueryMapper, sceneCache);
  }

  private Map<String, Consumer<BotSceneSkillDTO>> buildSkillQueryMethodMap(QuerySkillMapper skillQueryMapper,
    McpServerManageMapper mcpServerManageMapper, KnowledgeBaseManageMapper knowledgeBaseManageMapper,
    DataTableQueryMapper dataTableQueryMapper, SceneCache sceneCache) {
    return ImmutableMap.<String, Consumer<BotSceneSkillDTO>>builder()
      .put(StepType.SERVICE,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillService(skill.getTenantId(), skill.getSkillId()), SimpleSkillServiceDTO::getServiceName, SimpleSkillServiceDTO::toMap))
      .put(StepType.SQL,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillSql(skill.getTenantId(), skill.getSkillId()), SimpleSkillSqlDTO::getServiceName, SimpleSkillSqlDTO::toMap))
      .put(StepType.LLM_SKILL,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillPlugin(skill.getTenantId(), skill.getSkillId()), SimpleSkillPluginDTO::getApiName, SimpleSkillPluginDTO::toMap))
      .put(StepType.PLUGIN, this::fillPluginSkillInfo)
      .put(StepType.TOOLBOX,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillFunction(skill.getTenantId(), skill.getSkillId()), SimpleSkillFunctionDTO::getFuncName, SimpleSkillFunctionDTO::toMap))
      .put(StepType.WORKFLOW,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillFlow(skill.getTenantId(), skill.getSkillId()), SimpleSkillFlowDTO::getFlowName, SimpleSkillFlowDTO::toMap))
      .put(StepType.PAGE,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillPage(skill.getTenantId(), skill.getSkillId()), SimpleSkillPageDTO::getPageName, SimpleSkillPageDTO::toMap))
      .put(StepType.PAGE_FUNC,
        buildSkillConsumer(skill -> skillQueryMapper.getSkillPageFunc(skill.getTenantId(), skill.getSkillId()), SimpleSkillPageFuncDTO::getFuncName, SimpleSkillPageFuncDTO::toMap))
      .put(StepType.KNOWLEDGE_CHAT, skill -> setKnowledgeInfo(skill, knowledgeBaseManageMapper))
      .put(StepType.KNOWLEDGE_RETRIEVAL, skill -> setKnowledgeInfo(skill, knowledgeBaseManageMapper))
      .put(StepType.MCP,
        buildSkillConsumer(skill -> mcpServerManageMapper.selectSimpleMcpServerById(skill.getTenantId(), skill.getSkillId()), SimpleMcpServiceDTO::getServerName, SimpleMcpServiceDTO::toMap))
      .put(StepType.DATA_TABLE,
        buildSkillConsumer(skill -> dataTableQueryMapper.selectSimpleTable(skill.getTenantId(), skill.getSkillId()), SimpleDataTableDTO::getTableName, SimpleDataTableDTO::toMap))
      .put(StepType.INVOKE_SCENE, skill -> setSceneSkillInfo(sceneCache, skill))
      .put(StepType.AGENT_SKILL,
        buildSkillConsumer(skill -> skillQueryMapper.getAgentSkill(skill.getTenantId(), skill.getSkillId()), SimpleAgentSkillDTO::getSkillName, SimpleAgentSkillDTO::toMap))
      .build();
  }

  private <T> Consumer<BotSceneSkillDTO> buildSkillConsumer(Function<BotSceneSkillDTO, T> queryMethod,
    Function<T, String> nameGetter, Function<T, Map<String, Object>> infoGetter) {
    return skill -> {
      T dto = queryMethod.apply(skill);
      if (dto == null) {
        skill.setDeleted(true);
        return;
      }
      skill.setSkillName(nameGetter.apply(dto));
      skill.setSkillOriginInfo(infoGetter.apply(dto));
    };
  }

  private void fillPluginSkillInfo(BotSceneSkillDTO skill) {
    // 对接插件市场，暂不再次查询校验
    skill.setSkillOriginInfo(JsonUtil.parseJson(skill.getSkillJson(), new TypeReference<>() {
    }));
  }

  /**
   * 设置智能体技能信息
   */
  private void setSceneSkillInfo(SceneCache sceneCache, BotSceneSkillDTO skill) {
    SimpleBotSceneDTO scene = sceneCache.get(skill.getTenantId(), skill.getSkillId());
    if (scene == null) {
      skill.setDeleted(true);
      return;
    }
    skill.setSkillName(scene.getSceneName());
    Map<String, Object> originInfo = new HashMap<>();
    originInfo.put("sceneId", skill.getSkillId());
    originInfo.put("sceneName", scene.getSceneName());
    originInfo.put("sceneDesc", scene.getSceneDesc());
    originInfo.put("sceneType", scene.getSceneType());
    skill.setSkillOriginInfo(originInfo);
  }

  public void fillSkill(List<BotSceneSkillDTO> skills) {
    for (BotSceneSkillDTO skill : skills) {
      Consumer<BotSceneSkillDTO> queryMethod = skillQueryMethodMap.get(skill.getSkillType());
      Assert.notNull(queryMethod, () -> "不支持的技能类型: " + skill.getSkillType());
      queryMethod.accept(skill);
    }
  }

  /**
   * 查找已删除的技能
   *
   * @param skills 技能列表
   * @return 已删除的技能列表
   */
  public List<BotSceneSkillDTO> findDeletedSkills(List<BotSceneSkillDTO> skills, Long tenantId) {
    List<BotSceneSkillDTO> deletedSkills = new ArrayList<>();
    for (BotSceneSkillDTO skill : skills) {
      // PLUGIN 类型对接插件市场，不查库校验
      if (StepType.PLUGIN.equals(skill.getSkillType())) {
        continue;
      }
      skill.setTenantId(tenantId);
      Consumer<BotSceneSkillDTO> queryMethod = skillQueryMethodMap.get(skill.getSkillType());
      if (queryMethod == null) {
        continue;
      }
      queryMethod.accept(skill);
      if (skill.isDeleted()) {
        deletedSkills.add(skill);
      }
    }
    return deletedSkills;
  }

  private void setKnowledgeInfo(BotSceneSkillDTO skill, KnowledgeBaseManageMapper knowledgeBaseManageMapper) {
    SimpleKnowledgeBaseDTO dto = knowledgeBaseManageMapper.getSimpleKnowledge(skill.getTenantId(), skill.getSkillId());
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(skill.getTenantId());
    if (dto != null) {
      skill.setSkillName(dto.getKnowledgeName());
      skill.setSkillOriginInfo(dto.toMap());
    }
    else {
      // 第三方知识库
      if (StringUtils.isNotEmpty(skill.getSkillJson())) {
        Map<String, Object> stringObjectMap = JsonUtil.parseJsonRequired(skill.getSkillJson(), new TypeReference<Map<String, Object>>() {
        });
        skill.setSkillOriginInfo(stringObjectMap);
      }
      // 第三方知识库直接返回
      boolean isOtherKnowledge = KnowledgeClientUtil.getKnowledgeTypes().stream().anyMatch(KnowledgeConsts.OTHER_KNOWLEDGE_TYPES::contains);
      if (isOtherKnowledge) {
        return;
      }
      // 当知识库类型为空时，视为默认类型；当数据为空时，技能视为被删除
      if (StringUtils.isEmpty(knowledgeInfo.getKnowledgeType())) {
        skill.setDeleted(true);
      }
    }
  }
}
