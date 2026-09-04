package com.iwhalecloud.bote.service.orchestration;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.A2aStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.AgentSkillStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.AgentStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.AgentSwitchStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.AsyncWorkFlowStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.BreakStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ContinueStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.EndStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.IfStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.KnowledgeChatStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.KnowledgeGraphKnowledgeChatStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.KnowledgeGraphKnowledgeRetrievalStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.KnowledgeRetrievalStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.WeKnoraKnowledgeChatStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.WeKnoraKnowledgeRetrievalStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.LlmSkillStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.LlmStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.LoopStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.McpToolStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.MessagePushStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.PageFuncStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.PageStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ParallelStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ParamExtractorStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.PlaceholderStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.PlaywrightStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.PluginStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.QuestionClassifierStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.RecommendationStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ReplyStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.SceneStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ScriptStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ServiceStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.SetVariableStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.SlmRetrievalStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.SqlStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.StartStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.ToolBoxStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.WorkflowStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.database.DeleteRecordsStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.database.InsertRecordStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.database.QueryRecordsStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.database.QuerySingleRecordStepConverter;
import com.iwhalecloud.bote.service.orchestration.converter.step.database.UpdateRecordsStepConverter;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.A2aStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.AgentSkillStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.AgentStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.AgentSwitchStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.AsyncWorkFlowStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.BreakStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ContinueStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.EndStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.IfStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.InvokeSceneStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeChatStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeGraphKnowledgeChatStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeGraphKnowledgeRetrievalStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeRetrievalStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.WeKnoraKnowledgeChatStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.WeKnoraKnowledgeRetrievalStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.LlmSkillStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.LlmStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.LoopStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.McpStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.McpToolStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.MessagePushStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.PageFuncStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.PageStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ParallelStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ParamExtractorStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.PlaywrightStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.PluginStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.QuestionClassifierStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.RecommendationStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ReplyStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.SceneStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ScriptStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ServiceStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.SetVariableStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.SlmRetrievalStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.SqlStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.StartStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.ToolboxStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.WorkflowStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.MemoryToolStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.database.CustomSqlStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.database.DeleteRecordStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.database.InsertRecordStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.database.QueryRecordsStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.database.QuerySingleRecordStepRunner;
import com.iwhalecloud.bote.service.orchestration.runner.step.database.UpdateRecordsStepRunner;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景步骤注册器
 *
 * @author bianjp
 * @since 2024-08-29
 */
@SuppressWarnings("rawtypes")
public final class SceneStepRegistry {
  /** 转换器映射 */
  private static final Map<String, AbstractStepConverter> converterMap = new HashMap<>();
  /** 执行器映射 */
  private static final Map<String, AbstractStepRunner> runnerMap = new HashMap<>();

  private SceneStepRegistry() {
  }

  static {
    register(StepType.START, new StartStepConverter(), new StartStepRunner());
    register(StepType.END, new EndStepConverter(), new EndStepRunner());
    register(StepType.REPLY, new ReplyStepConverter(), new ReplyStepRunner());
    register(StepType.MESSAGE_PUSH, new MessagePushStepConverter(), new MessagePushStepRunner());
    register(StepType.SET_VARIABLE, new SetVariableStepConverter(), new SetVariableStepRunner());
    register(StepType.IF, new IfStepConverter(), new IfStepRunner());
    register(StepType.LOOP, new LoopStepConverter(), new LoopStepRunner());
    register(StepType.BREAK, new BreakStepConverter(), new BreakStepRunner());
    register(StepType.CONTINUE, new ContinueStepConverter(), new ContinueStepRunner());
    register(StepType.PARALLEL, new ParallelStepConverter(), new ParallelStepRunner());
    register(StepType.PAGE, new PageStepConverter(), new PageStepRunner());
    register(StepType.PAGE_FUNC, new PageFuncStepConverter(), new PageFuncStepRunner());
    register(StepType.TOOLBOX, new ToolBoxStepConverter(), new ToolboxStepRunner());
    register(StepType.SCRIPT, new ScriptStepConverter(), new ScriptStepRunner());
    register(StepType.SERVICE, new ServiceStepConverter(), new ServiceStepRunner());
    register(StepType.SQL, new SqlStepConverter(), new SqlStepRunner());
    register(StepType.LLM_SKILL, new LlmSkillStepConverter(), new LlmSkillStepRunner());
    register(StepType.PARAM_EXTRACTOR, new ParamExtractorStepConverter(), new ParamExtractorStepRunner());
    register(StepType.WORKFLOW, new WorkflowStepConverter(), new WorkflowStepRunner());
    register(StepType.ASYNC_WORKFLOW, new AsyncWorkFlowStepConverter(), new AsyncWorkFlowStepRunner());
    register(StepType.QUESTION_CLASSIFIER, new QuestionClassifierStepConverter(), new QuestionClassifierStepRunner());
    register(StepType.KNOWLEDGE_RETRIEVAL, new KnowledgeRetrievalStepConverter(), new KnowledgeRetrievalStepRunner());
    register(StepType.KNOWLEDGE_CHAT, new KnowledgeChatStepConverter(), new KnowledgeChatStepRunner());
    register(StepType.WEKNORA_RETRIEVAL, new WeKnoraKnowledgeRetrievalStepConverter(), new WeKnoraKnowledgeRetrievalStepRunner());
    register(StepType.WEKNORA_CHAT, new WeKnoraKnowledgeChatStepConverter(), new WeKnoraKnowledgeChatStepRunner());
    register(StepType.KNOWLEDGE_GRAPH_RETRIEVAL, new KnowledgeGraphKnowledgeRetrievalStepConverter(), new KnowledgeGraphKnowledgeRetrievalStepRunner());
    register(StepType.KNOWLEDGE_GRAPH_CHAT, new KnowledgeGraphKnowledgeChatStepConverter(), new KnowledgeGraphKnowledgeChatStepRunner());
    register(StepType.LLM, new LlmStepConverter(), new LlmStepRunner());
    register(StepType.AGENT, new AgentStepConverter(), new AgentStepRunner());
    register(StepType.A2A, new A2aStepConverter(), new A2aStepRunner());
    register(StepType.AGENT_SKILL, new AgentSkillStepConverter(), new AgentSkillStepRunner());
    register(StepType.MCP, null, new McpStepRunner());
    register(StepType.INVOKE_SCENE, null, new InvokeSceneStepRunner());
    register(StepType.MCP_TOOL, new McpToolStepConverter(), new McpToolStepRunner());
    register(StepType.PLAYWRIGHT, new PlaywrightStepConverter(), new PlaywrightStepRunner());
    register(StepType.SLM_RETRIEVAL, new SlmRetrievalStepConverter(), new SlmRetrievalStepRunner());
    register(StepType.PLACEHOLDER, new PlaceholderStepConverter(), null);
    register(StepType.PLUGIN, new PluginStepConverter(), new PluginStepRunner());
    register(StepType.AGENT_SWITCH, new AgentSwitchStepConverter(), new AgentSwitchStepRunner());
    register(StepType.SCENE, new SceneStepConverter(), new SceneStepRunner());
    register(StepType.RECOMMENDATION, new RecommendationStepConverter(), new RecommendationStepRunner());
    register(StepType.QUERY_SINGLE_RECORD, new QuerySingleRecordStepConverter(), new QuerySingleRecordStepRunner());
    register(StepType.QUERY_RECORDS, new QueryRecordsStepConverter(), new QueryRecordsStepRunner());
    register(StepType.INSERT_RECORD, new InsertRecordStepConverter(), new InsertRecordStepRunner());
    register(StepType.UPDATE_RECORDS, new UpdateRecordsStepConverter(), new UpdateRecordsStepRunner());
    register(StepType.DELETE_RECORDS, new DeleteRecordsStepConverter(), new DeleteRecordStepRunner());
    register(StepType.DATA_TABLE, null, new CustomSqlStepRunner());
    register(StepType.MEMORY_TOOL, null, new MemoryToolStepRunner());
  }

  /**
   * 注册步骤
   */
  private static <T extends AbstractStep> void register(String stepType, @Nullable AbstractStepConverter<T> converter, @Nullable AbstractStepRunner<T> runner) {
    if (converter != null) {
      converterMap.put(stepType, converter);
    }
    if (runner != null) {
      runnerMap.put(stepType, runner);
    }
  }

  /**
   * 获取步骤转换器
   */
  public static AbstractStepConverter getConverter(String stepType) {
    AbstractStepConverter converter = converterMap.get(stepType);
    Assert.notNull(converter, () -> "未知的节点类型: " + stepType);
    return converter;
  }

  /**
   * 获取步骤执行器
   */
  public static AbstractStepRunner getRunner(AbstractStep step) {
    return getRunner(step.getType());
  }

  /**
   * 获取步骤执行器
   */
  public static AbstractStepRunner getRunner(String stepType) {
    AbstractStepRunner runner = runnerMap.get(stepType);
    Assert.notNull(runner, () -> "未知的节点类型: " + stepType);
    return runner;
  }
}
