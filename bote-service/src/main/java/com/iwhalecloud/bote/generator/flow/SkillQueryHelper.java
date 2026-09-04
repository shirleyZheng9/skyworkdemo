package com.iwhalecloud.bote.generator.flow;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.generator.flow.SkillBasicInfoDTO;
import com.iwhalecloud.bote.mapper.generator.FlowAiQueryMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 技能查询辅助类
 *
 * @author bianjp
 * @since 2025-04-15
 */
@Service
public class SkillQueryHelper {
  /** 技能查询器映射，key 为技能类型 */
  private final Map<String, BiFunction<Long, List<Long>, List<SkillBasicInfoDTO>>> skillQueryMethodMap;

  public SkillQueryHelper(FlowAiQueryMapper flowAiQueryMapper) {
    this.skillQueryMethodMap = ImmutableMap.<String, BiFunction<Long, List<Long>, List<SkillBasicInfoDTO>>>builder()
      .put(StepType.SERVICE, flowAiQueryMapper::selectApiServiceInfoByIds)
      .put(StepType.SQL, flowAiQueryMapper::selectSqlServiceInfoByIds)
      .put(StepType.LLM_SKILL, flowAiQueryMapper::selectPluginInfoByIds)
      .put(StepType.TOOLBOX, flowAiQueryMapper::selectFunctionInfoByIds)
      .put(StepType.WORKFLOW, flowAiQueryMapper::selectFlowInfoByIds)
      .put(StepType.PAGE, flowAiQueryMapper::selectPageInfoByIds)
      .put(StepType.PAGE_FUNC, flowAiQueryMapper::selectPageFuncInfoByIds)
      .put(StepType.KNOWLEDGE_CHAT, (tenantId, ids) -> selectKnowledgeInfoByIds(tenantId, ids, flowAiQueryMapper))
      .put(StepType.KNOWLEDGE_RETRIEVAL, (tenantId, ids) -> selectKnowledgeInfoByIds(tenantId, ids, flowAiQueryMapper))
      .put(StepType.MCP, flowAiQueryMapper::selectMcpInfoByIds)
      .build();
  }

  /**
   * 批量查询技能的基本信息
   *
   * @param tenantId 租户 ID
   * @param skillIdsMap 技能 ID 列表映射, key 为技能类型, value 为技能 ID 列表
   * @return 技能基本信息映射，key 为技能类型
   */
  public Map<String, List<SkillBasicInfoDTO>> querySkills(Long tenantId, Map<String, List<Long>> skillIdsMap) {
    if (MapUtils.isEmpty(skillIdsMap)) {
      return Collections.emptyMap();
    }

    Map<String, List<SkillBasicInfoDTO>> skillMap = new HashMap<>();
    for (Entry<String, List<Long>> entry : skillIdsMap.entrySet()) {
      List<Long> ids = entry.getValue();
      if (CollectionUtils.isNotEmpty(ids)) {
        String skillType = entry.getKey();
        BiFunction<Long, List<Long>, List<SkillBasicInfoDTO>> queryMethod = skillQueryMethodMap.get(skillType);
        Assert.notNull(queryMethod, () -> "不支持的技能类型: " + skillType);
        skillMap.put(skillType, queryMethod.apply(tenantId, ids));
      }
    }
    return skillMap;
  }

  private List<SkillBasicInfoDTO> selectKnowledgeInfoByIds(Long tenantId, List<Long> ids, FlowAiQueryMapper flowAiQueryMapper) {
    List<SkillBasicInfoDTO> skills = flowAiQueryMapper.selectKnowledgeInfoByIds(tenantId, ids);
    // 知识库没有编码，使用技能 ID 构造技能编码
    for (SkillBasicInfoDTO skill : skills) {
      skill.setSkillCode("knowledge_" + skill.getSkillId());
    }
    return skills;
  }
}
