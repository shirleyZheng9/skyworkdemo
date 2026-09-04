package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 场景步骤类型
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public final class StepType {
  private StepType() {
  }

  /** 开始 */
  public static final String START = "start";
  /** 结束 */
  public static final String END = "end";
  /** 回复 */
  public static final String REPLY = "reply";
  /** 指令 */
  public static final String MESSAGE_PUSH = "messagePush";
  /** 设置变量 */
  public static final String SET_VARIABLE = "setVariable";
  /** 如果 */
  public static final String IF = "if";
  /** 循环 */
  public static final String LOOP = "loop";
  /** 循环开始 */
  public static final String LOOP_START = "loopStart";
  /** 中断循环 */
  public static final String BREAK = "break";
  /** 继续循环 */
  public static final String CONTINUE = "continue";
  /** 并行 */
  public static final String PARALLEL = "parallel";
  /** 并行结束 */
  public static final String PARALLEL_END = "parallelEnd";
  /** 页面 */
  public static final String PAGE = "page";
  /** 页面函数 */
  public static final String PAGE_FUNC = "pageFunc";
  /** 工具箱 */
  public static final String TOOLBOX = "toolbox";
  /** 代码块 */
  public static final String SCRIPT = "script";
  /** 服务 */
  public static final String SERVICE = "service";
  /** SQL */
  public static final String SQL = "sql";
  /** 大模型能力 */
  public static final String LLM_SKILL = "llmSkill";
  /** 工作流 */
  public static final String WORKFLOW = "workflow";
  /** 异步工作流 */
  public static final String ASYNC_WORKFLOW = "asyncWorkflow";
  /** 参数提取 */
  public static final String PARAM_EXTRACTOR = "paramExtractor";
  /** 问题分类 */
  public static final String QUESTION_CLASSIFIER = "questionClassifier";
  /** 知识检索 */
  public static final String KNOWLEDGE_RETRIEVAL = "knowledgeRetrieval";
  /** 知识问答 */
  public static final String KNOWLEDGE_CHAT = "knowledgeChat";
  /** WeKnora 检索 */
  public static final String WEKNORA_RETRIEVAL = "weKnoraRetrieval";
  /** WeKnora 问答 */
  public static final String WEKNORA_CHAT = "weKnoraChat";
  /** knowledgeGraph 检索 */
  public static final String KNOWLEDGE_GRAPH_RETRIEVAL = "knowledgeGraphRetrieval";
  /** knowledgeGraph 问答 */
  public static final String KNOWLEDGE_GRAPH_CHAT = "knowledgeGraphChat";
  /** 大模型 */
  public static final String LLM = "llm";
  /** Agent */
  public static final String AGENT = "agent";
  /** Agent Skill */
  public static final String AGENT_SKILL = "agentSkill";
  /** A2A 服务 */
  public static final String A2A = "a2a";
  /** MCP 服务(虚拟步骤，用于调用 MCP 工具) */
  public static final String MCP = "mcp";
  /** MCP 工具 */
  public static final String MCP_TOOL = "mcpTool";
  /** PlayWright 自动化 */
  public static final String PLAYWRIGHT = "playwright";
  /** 占位符，前端布局使用，不进行任何处理 */
  public static final String PLACEHOLDER = "placeholder";
  /** 微调模型检索 */
  public static final String SLM_RETRIEVAL = "slmRetrieval";
  /** 插件 */
  public static final String PLUGIN = "plugin";
  /** 场景切换 */
  public static final String AGENT_SWITCH = "agentSwitch";
  /** 智能体 */
  public static final String SCENE = "scene";
  /** 调用智能体（将 ISceneChatService#run 封装为工具，供自主规划智能体调用） */
  public static final String INVOKE_SCENE = "invokeScene";
  /** 推荐 */
  public static final String RECOMMENDATION = "recommendation";

  /** 查询单条 */
  public static final String QUERY_SINGLE_RECORD = "querySingleRecord";
  /** 查询多条 */
  public static final String QUERY_RECORDS = "queryRecords";
  /** 新增记录 */
  public static final String INSERT_RECORD = "insertRecord";
  /** 修改记录 */
  public static final String UPDATE_RECORDS = "updateRecords";
  /** 删除记录 */
  public static final String DELETE_RECORDS = "deleteRecords";
  /** 数据库 SQL 操作（虚拟步骤，用于调用数据库工具） */
  public static final String DATA_TABLE = "dataTable";
  /** 长期记忆工具（运行时动态注入，不对应数据库存储的步骤类型） */
  public static final String MEMORY_TOOL = "memoryTool";

  /** 不允许输出流程步骤的类型 */
  public static final List<String> NOT_ALLOW_FLOW_STEP_TYPES = ImmutableList.of(START, END, IF, LOOP, LOOP_START, BREAK, CONTINUE, PARALLEL, PARALLEL_END);
}
