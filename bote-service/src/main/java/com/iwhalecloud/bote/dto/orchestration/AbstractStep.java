package com.iwhalecloud.bote.dto.orchestration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.StepExceptionConfig.StepExceptionProcessingStrategy;
import com.iwhalecloud.bote.dto.orchestration.step.A2aStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSkillStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentSwitchStep;
import com.iwhalecloud.bote.dto.orchestration.step.AsyncWorkFlowStep;
import com.iwhalecloud.bote.dto.orchestration.step.BreakStep;
import com.iwhalecloud.bote.dto.orchestration.step.ContinueStep;
import com.iwhalecloud.bote.dto.orchestration.step.EndStep;
import com.iwhalecloud.bote.dto.orchestration.step.IfStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmSkillStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep;
import com.iwhalecloud.bote.dto.orchestration.step.LoopStep;
import com.iwhalecloud.bote.dto.orchestration.step.McpStep;
import com.iwhalecloud.bote.dto.orchestration.step.McpToolStep;
import com.iwhalecloud.bote.dto.orchestration.step.MessagePushStep;
import com.iwhalecloud.bote.dto.orchestration.step.PageFuncStep;
import com.iwhalecloud.bote.dto.orchestration.step.PageStep;
import com.iwhalecloud.bote.dto.orchestration.step.ParallelStep;
import com.iwhalecloud.bote.dto.orchestration.step.ParamExtractorStep;
import com.iwhalecloud.bote.dto.orchestration.step.PlaywrightStep;
import com.iwhalecloud.bote.dto.orchestration.step.PluginStep;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep;
import com.iwhalecloud.bote.dto.orchestration.step.RecommendationStep;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep;
import com.iwhalecloud.bote.dto.orchestration.step.SceneStep;
import com.iwhalecloud.bote.dto.orchestration.step.ScriptStep;
import com.iwhalecloud.bote.dto.orchestration.step.ServiceStep;
import com.iwhalecloud.bote.dto.orchestration.step.SetVariableStep;
import com.iwhalecloud.bote.dto.orchestration.step.SlmRetrievalStep;
import com.iwhalecloud.bote.dto.orchestration.step.SqlStep;
import com.iwhalecloud.bote.dto.orchestration.step.StartStep;
import com.iwhalecloud.bote.dto.orchestration.step.ToolboxStep;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.DeleteRecordsStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.InsertRecordStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.QueryRecordsStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.QuerySingleRecordStep;
import com.iwhalecloud.bote.dto.orchestration.step.database.UpdateRecordsStep;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 步骤抽象类
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
@JsonTypeInfo(use = Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
  @Type(name = StepType.START, value = StartStep.class),
  @Type(name = StepType.END, value = EndStep.class),
  @Type(name = StepType.REPLY, value = ReplyStep.class),
  @Type(name = StepType.MESSAGE_PUSH, value = MessagePushStep.class),
  @Type(name = StepType.SET_VARIABLE, value = SetVariableStep.class),
  @Type(name = StepType.IF, value = IfStep.class),
  @Type(name = StepType.LOOP, value = LoopStep.class),
  @Type(name = StepType.BREAK, value = BreakStep.class),
  @Type(name = StepType.CONTINUE, value = ContinueStep.class),
  @Type(name = StepType.PARALLEL, value = ParallelStep.class),
  @Type(name = StepType.PAGE, value = PageStep.class),
  @Type(name = StepType.PAGE_FUNC, value = PageFuncStep.class),
  @Type(name = StepType.TOOLBOX, value = ToolboxStep.class),
  @Type(name = StepType.SCRIPT, value = ScriptStep.class),
  @Type(name = StepType.SERVICE, value = ServiceStep.class),
  @Type(name = StepType.SQL, value = SqlStep.class),
  @Type(name = StepType.LLM_SKILL, value = LlmSkillStep.class),
  @Type(name = StepType.MCP, value = McpStep.class),
  @Type(name = StepType.MCP_TOOL, value = McpToolStep.class),
  @Type(name = StepType.PLAYWRIGHT, value = PlaywrightStep.class),
  @Type(name = StepType.WORKFLOW, value = WorkflowStep.class),
  @Type(name = StepType.ASYNC_WORKFLOW, value = AsyncWorkFlowStep.class),
  @Type(name = StepType.PARAM_EXTRACTOR, value = ParamExtractorStep.class),
  @Type(name = StepType.QUESTION_CLASSIFIER, value = QuestionClassifierStep.class),
  @Type(name = StepType.KNOWLEDGE_RETRIEVAL, value = KnowledgeRetrievalStep.class),
  @Type(name = StepType.KNOWLEDGE_CHAT, value = KnowledgeChatStep.class),
  @Type(name = StepType.WEKNORA_RETRIEVAL, value = WeKnoraKnowledgeRetrievalStep.class),
  @Type(name = StepType.WEKNORA_CHAT, value = WeKnoraKnowledgeChatStep.class),
  @Type(name = StepType.KNOWLEDGE_GRAPH_RETRIEVAL, value = KnowledgeGraphKnowledgeRetrievalStep.class),
  @Type(name = StepType.KNOWLEDGE_GRAPH_CHAT, value = KnowledgeGraphKnowledgeChatStep.class),
  @Type(name = StepType.LLM, value = LlmStep.class),
  @Type(name = StepType.AGENT, value = AgentStep.class),
  @Type(name = StepType.AGENT_SKILL, value = AgentSkillStep.class),
  @Type(name = StepType.A2A, value = A2aStep.class),
  @Type(name = StepType.SLM_RETRIEVAL, value = SlmRetrievalStep.class),
  @Type(name = StepType.PLUGIN, value = PluginStep.class),
  @Type(name = StepType.AGENT_SWITCH, value = AgentSwitchStep.class),
  @Type(name = StepType.SCENE, value = SceneStep.class),
  @Type(name = StepType.RECOMMENDATION, value = RecommendationStep.class),
  @Type(name = StepType.QUERY_SINGLE_RECORD, value = QuerySingleRecordStep.class),
  @Type(name = StepType.QUERY_RECORDS, value = QueryRecordsStep.class),
  @Type(name = StepType.INSERT_RECORD, value = InsertRecordStep.class),
  @Type(name = StepType.UPDATE_RECORDS, value = UpdateRecordsStep.class),
  @Type(name = StepType.DELETE_RECORDS, value = DeleteRecordsStep.class)
})
public abstract class AbstractStep {
  /** 步骤类型 */
  protected String type;
  /** 步骤名称（可能不唯一） */
  protected String name;
  /** 步骤编码（需唯一） */
  protected String code;
  /** 步骤描述（用于展示执行过程，非必填） */
  protected String desc;
  /** 下一步的步骤编码 */
  protected String next;
  /** 异常处理配置 */
  protected StepExceptionConfig exceptionConfig;

  public AbstractStep(String type) {
    this.type = type;
  }

  /**
   * 获取节点的异常处理策略
   */
  @JsonIgnore
  public StepExceptionProcessingStrategy getExceptionProcessingStrategy() {
    if (exceptionConfig != null) {
      return ObjectUtils.getIfNull(exceptionConfig.getStrategy(), StepExceptionProcessingStrategy.ABORT);
    }
    return StepExceptionProcessingStrategy.ABORT;
  }

  @Override
  public String toString() {
    try {
      return JsonUtil.toJsonString(this);
    }
    catch (RuntimeException e) {
      return super.toString();
    }
  }
}
