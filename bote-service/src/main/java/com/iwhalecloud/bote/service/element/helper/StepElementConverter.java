package com.iwhalecloud.bote.service.element.helper;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.step.A2aStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSkillStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSwitchStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSwitchStep.SceneInfo;
import com.iwhalecloud.bote.dto.orchestration.step.AsyncWorkFlowStep;
import com.iwhalecloud.bote.dto.orchestration.step.InvokeSceneStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmSkillStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep.LlmMessage;
import com.iwhalecloud.bote.dto.orchestration.step.McpToolStep;
import com.iwhalecloud.bote.dto.orchestration.step.PageFuncStep;
import com.iwhalecloud.bote.dto.orchestration.step.PageStep;
import com.iwhalecloud.bote.dto.orchestration.step.ParamExtractorStep;
import com.iwhalecloud.bote.dto.orchestration.step.PluginStep;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep;
import com.iwhalecloud.bote.dto.orchestration.step.ServiceStep;
import com.iwhalecloud.bote.dto.orchestration.step.SlmRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.SqlStep;
import com.iwhalecloud.bote.dto.orchestration.step.ToolboxStep;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.DeleteRecordsStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.InsertRecordStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.QueryRecordsStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.QuerySingleRecordStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.UpdateRecordsStep;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 编排步骤资源关联解析器
 *
 * @author chen.linfa
 * @since 2025-04-11
 */
public final class StepElementConverter {
  private StepElementConverter() {
  }

  private static final Map<String, TriConsumer<AbstractStep, List<ResourceElementDTO>, Long>> TYPES = new HashMap<>(16);
  /** 节点类型 -> 资源类型映射 */
  private static final Map<String, String> stepType2ElementTypeMap = ImmutableMap.<String, String>builder()
    .put(StepType.SERVICE, DataSyncCodeEnum.SKILL_SERVICE.getCode())
    .put(StepType.WORKFLOW, DataSyncCodeEnum.SKILL_FLOW.getCode())
    .put(StepType.PAGE, DataSyncCodeEnum.SKILL_PAGE.getCode())
    .put(StepType.TOOLBOX, DataSyncCodeEnum.SKILL_FUNCTION.getCode())
    .put(StepType.LLM_SKILL, DataSyncCodeEnum.SKILL_PLUGIN.getCode())
    .put(StepType.PLUGIN, StepType.PLUGIN)
    .put(StepType.MCP, DataSyncCodeEnum.SKILL_MCP.getCode())
    .put(StepType.PAGE_FUNC, DataSyncCodeEnum.SKILL_PAGE_FUNC.getCode())
    .put(StepType.SQL, DataSyncCodeEnum.SKILL_SQL.getCode())
    .put(StepType.KNOWLEDGE_CHAT, DataSyncCodeEnum.KNOWLEDGE.getCode())
    .put(StepType.DATA_TABLE, DataSyncCodeEnum.DATA_TABLE.getCode())
    .put(StepType.AGENT_SKILL, DataSyncCodeEnum.AGENT_SKILL.getCode())
    .put(StepType.INVOKE_SCENE, DataSyncCodeEnum.SCENE.getCode())
    .build();

  static {
    TYPES.put(KnowledgeChatStep.class.getSimpleName(), (step, elements, tenantId) -> {
      List<Long> ids = new ArrayList<>();
      String knowledgeId = ((KnowledgeChatStep) step).getKnowledgeId();
      if (StringUtils.isNumeric(knowledgeId)) {
        ids.add(Long.valueOf(knowledgeId));
      }
      else if (knowledgeId.contains(",")) {
        // 多个知识库 ID
        ids = Arrays.stream(knowledgeId.split(",")).map(Long::valueOf).collect(Collectors.toList());
      }
      for (Long id : ids) {
        ResourceElementDTO element = new ResourceElementDTO();
        element.setElementId(id);
        element.setElementType(DataSyncCodeEnum.KNOWLEDGE.getCode());
        elements.add(element);
      }
    });

    TYPES.put(KnowledgeRetrievalStep.class.getSimpleName(), (step, elements, tenantId) -> {
      String knowledgeId = ((KnowledgeRetrievalStep) step).getKnowledgeId();
      if (!StringUtils.isNumeric(knowledgeId)) {
        // 忽略引用变量
        return;
      }
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(Long.valueOf(knowledgeId));
      element.setElementType(DataSyncCodeEnum.KNOWLEDGE.getCode());
      elements.add(element);
    });

    TYPES.put(LlmSkillStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((LlmSkillStep) step).getApiId());
      element.setElementType(DataSyncCodeEnum.SKILL_PLUGIN.getCode());
      elements.add(element);
    });

    TYPES.put(LlmStep.class.getSimpleName(), (step, elements, tenantId) -> {
      EntityRelationParseUtil.handleModelElement(elements, ((LlmStep) step).getModelId(), tenantId);
      // 消息列表
      List<LlmMessage> messages = ((LlmStep) step).getMessages();
      if (CollectionUtils.isNotEmpty(messages)) {
        for (LlmMessage message : messages) {
          buildPromptElement(elements, message.getPromptId());
        }
      }
      // 用户消息
      buildPromptElement(elements, ((LlmStep) step).getPromptId());
    });

    TYPES.put(PageFuncStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((PageFuncStep) step).getPageFuncId());
      element.setElementType(DataSyncCodeEnum.SKILL_PAGE_FUNC.getCode());
      elements.add(element);
    });

    TYPES.put(PageStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((PageStep) step).getPageId());
      element.setElementType(DataSyncCodeEnum.SKILL_PAGE.getCode());
      elements.add(element);
    });

    TYPES.put(QuestionClassifierStep.class.getSimpleName(), (step, elements, tenantId) -> {
      EntityRelationParseUtil.handleModelElement(elements, ((QuestionClassifierStep) step).getModelId(), tenantId);
      buildPromptElement(elements, ((QuestionClassifierStep) step).getPromptId());
    });

    TYPES.put(ServiceStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((ServiceStep) step).getServiceId());
      element.setElementType(DataSyncCodeEnum.SKILL_SERVICE.getCode());
      elements.add(element);
    });

    TYPES.put(SqlStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((SqlStep) step).getSqlId());
      element.setElementType(DataSyncCodeEnum.SKILL_SQL.getCode());
      elements.add(element);
    });

    TYPES.put(ToolboxStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((ToolboxStep) step).getFuncId());
      element.setElementType(DataSyncCodeEnum.SKILL_FUNCTION.getCode());
      elements.add(element);
    });

    TYPES.put(WorkflowStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((WorkflowStep) step).getFlowId());
      element.setElementType(DataSyncCodeEnum.SKILL_FLOW.getCode());
      elements.add(element);
    });

    TYPES.put(McpToolStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((McpToolStep) step).getServerId());
      element.setElementType(DataSyncCodeEnum.SKILL_MCP.getCode());
      elements.add(element);
    });

    TYPES.put(AgentStep.class.getSimpleName(), (step, elements, tenantId) -> {
      EntityRelationParseUtil.handleModelElement(elements, ((AgentStep) step).getModelId(), tenantId);
      for (LlmSkillItem skill : CollectionUtils.emptyIfNull(((AgentStep) step).getSkills())) {
        ResourceElementDTO element = new ResourceElementDTO();
        element.setElementId(skill.getSkillId());
        element.setElementType(getElementType(skill.getSkillType()));
        elements.add(element);
      }
      buildPromptElement(elements, ((AgentStep) step).getPromptId());
    });
    TYPES.put(AgentSkillStep.class.getSimpleName(), (step, elements, tenantId) -> {
      EntityRelationParseUtil.handleModelElement(elements, ((AgentSkillStep) step).getModelId(), tenantId);
      for (LlmSkillItem skill : ListUtils.emptyIfNull(((AgentSkillStep) step).getSkills())) {
        ResourceElementDTO element = new ResourceElementDTO();
        element.setElementId(skill.getSkillId());
        element.setElementType(getElementType(skill.getSkillType()));
        elements.add(element);
      }
      buildPromptElement(elements, ((AgentSkillStep) step).getPromptId());
    });

    TYPES.put(ParamExtractorStep.class.getSimpleName(), (step, elements, tenantId) -> {
      EntityRelationParseUtil.handleModelElement(elements, ((ParamExtractorStep) step).getModelId(), tenantId);
      buildPromptElement(elements, ((ParamExtractorStep) step).getPromptId());
    });

    TYPES.put(QuerySingleRecordStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((QuerySingleRecordStep) step).getTableId());
      element.setElementType(DataSyncCodeEnum.DATA_TABLE.getCode());
      elements.add(element);
    });
    TYPES.put(QueryRecordsStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((QueryRecordsStep) step).getTableId());
      element.setElementType(DataSyncCodeEnum.DATA_TABLE.getCode());
      elements.add(element);
    });
    TYPES.put(InsertRecordStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((InsertRecordStep) step).getTableId());
      element.setElementType(DataSyncCodeEnum.DATA_TABLE.getCode());
      elements.add(element);
    });
    TYPES.put(UpdateRecordsStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((UpdateRecordsStep) step).getTableId());
      element.setElementType(DataSyncCodeEnum.DATA_TABLE.getCode());
      elements.add(element);
    });
    TYPES.put(DeleteRecordsStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((DeleteRecordsStep) step).getTableId());
      element.setElementType(DataSyncCodeEnum.DATA_TABLE.getCode());
      elements.add(element);
    });

    TYPES.put(PluginStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((PluginStep) step).getPluginId());
      element.setElementType(DataSyncCodeEnum.PLUGIN.getCode());
      elements.add(element);
    });

    TYPES.put(A2aStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((A2aStep) step).getAgentId());
      element.setElementType(DataSyncCodeEnum.A2A_AGENT.getCode());
      elements.add(element);
    });

    TYPES.put(AgentSwitchStep.class.getSimpleName(), (step, elements, tenantId) -> {
      List<Long> ids = new ArrayList<>();
      // 自动切换
      if (((AgentSwitchStep) step).getSceneId() != null) {
        ids.add(((AgentSwitchStep) step).getSceneId());
      }
      // 手动切换：自定义  引用、意图识别无法解析
      List<SceneInfo> scenes = ((AgentSwitchStep) step).getScenes();
      if (CollectionUtils.isNotEmpty(scenes)) {
        for (SceneInfo scene : scenes) {
          ids.add(Long.valueOf(scene.getSceneId()));
        }
      }
      for (Long id : ids) {
        ResourceElementDTO element = new ResourceElementDTO();
        element.setElementId(id);
        element.setElementType(DataSyncCodeEnum.SCENE.getCode());
        elements.add(element);
      }
    });

    TYPES.put(SlmRetrievalStep.class.getSimpleName(), (step, elements, tenantId) -> {
      EntityRelationParseUtil.handleModelElement(elements, ((SlmRetrievalStep) step).getModelId(), tenantId);
    });

    TYPES.put(AsyncWorkFlowStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((AsyncWorkFlowStep) step).getFlowId());
      element.setElementType(DataSyncCodeEnum.SKILL_FLOW.getCode());
      elements.add(element);
    });

    TYPES.put(InvokeSceneStep.class.getSimpleName(), (step, elements, tenantId) -> {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(((InvokeSceneStep) step).getSceneId());
      element.setElementType(DataSyncCodeEnum.SCENE.getCode());
      elements.add(element);
    });
  }

  /**
   * 构建提示词元素
   */
  private static void buildPromptElement(List<ResourceElementDTO> elements, Long promptId) {
    if (promptId != null) {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(promptId);
      element.setElementType(DataSyncCodeEnum.PROMPT.getCode());
      elements.add(element);
    }
  }

  public static void convert(AbstractStep step, List<ResourceElementDTO> elements, Long tenantId) {
    if (TYPES.containsKey(step.getClass().getSimpleName())) {
      TYPES.get(step.getClass().getSimpleName()).accept(step, elements, tenantId);
    }
  }

  /**
   * 简单智能体，根据技能类型，匹配对应的元素类型
   */
  public static String getElementType(String skillType) {
    String elementType = stepType2ElementTypeMap.get(skillType);
    Assert.notNull(elementType, () -> "未配置节点类型对应的资源类型: " + skillType);
    return elementType;
  }

  /**
   * 三参数消费者接口，用于处理带有租户ID的转换逻辑
   */
  @FunctionalInterface
  private interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
  }
}
