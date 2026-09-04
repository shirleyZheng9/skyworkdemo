package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 智能体相关常量
 *
 * @author bianjp
 * @since 2024-08-02
 */
public final class SceneConsts {
  private SceneConsts() {
  }

  /** 智能体类型: 智能体 */
  public static final String SCENE_TYPE_SCENE = "scene";
  /** 智能体类型: 对话流 */
  public static final String SCENE_TYPE_CHATFLOW = "chatflow";
  /** 智能体类型: 知识问答 */
  public static final String SCENE_TYPE_KNOWLEDGE = "knowledge";
  /** 智能体类型: A2A 服务 */
  public static final String SCENE_TYPE_A2A = "a2a";
  /** 智能体类型: claw */
  public static final String SCENE_TYPE_CLAW = "claw";
  /** 智能体类型列表 */
  public static final List<String> SCENE_TYPES = ImmutableList.of(SCENE_TYPE_SCENE, SCENE_TYPE_CHATFLOW, SCENE_TYPE_KNOWLEDGE, SCENE_TYPE_A2A, SCENE_TYPE_CLAW);

  /** 流程类型: 单步 */
  public static final String FLOW_TYPE_ONE_STEP = "oneStep";
  /** 流程类型: 多步 */
  public static final String FLOW_TYPE_MULTI_STEP = "multiStep";
  /** 流程类型列表 */
  public static final List<String> FLOW_TYPES = ImmutableList.of(FLOW_TYPE_ONE_STEP, FLOW_TYPE_MULTI_STEP);

  /** 测试用的对话 ID */
  public static final Long TEST_CONVERSATION_ID = 1L;

  /** 单节点调试的参数形式: 表格 */
  public static final String DEBUG_PARAMS_TABLE = "table";
  /** 单节点调试的参数形式: json */
  public static final String DEBUG_PARAMS_JSON = "json";

  /** 工具参数: 用户问句 */
  public static final String TOOL_PARAM_QUERY = "sys_query";

  /** 参数值表达式: 用户问句 */
  public static final String PARAM_VALUE_QUERY = "$.system.query";

  /** 智能体状态 上架：1 下架：0 */
  public static final String SCENE_STATUS_PUBLISH = "1";
  public static final String SCENE_STATUS_UNPUBLISH = "0";

  /** 循环类型: 列表 */
  public static final String LOOP_TYPE_LIST = "list";
  /** 循环类型: 对象 */
  public static final String LOOP_TYPE_OBJECT = "object";
  /** 循环类型: 范围 */
  public static final String LOOP_TYPE_RANGE = "range";

  /** 推荐类型: 智能体 */
  public static final String RECOMMENDATION_TYPE_SCENE = "scene";
  /** 推荐类型: 工作流 */
  public static final String RECOMMENDATION_TYPE_FLOW = "flow";
  /** 推荐类型: 页面 */
  public static final String RECOMMENDATION_TYPE_PAGE = "page";
  /** 推荐类型: 问题 */
  public static final String RECOMMENDATION_TYPE_QUESTION = "question";

  /** 智能体标签 - 副驾 */
  public static final String SCENE_LABEL_COPILOT = "副驾";
  /** 智能体标签 - 可跳变 */
  public static final String SCENE_LABEL_JUMP = "可跳变";
  /** 智能体标签 - 自动启动 */
  public static final String SCENE_LABEL_AUTO_START = "自动启动";
  /** 智能体标签 - 确认启动 */
  public static final String SCENE_LABEL_CONFIRM_START = "确认启动";
  /** 智能体标签 - 规划智能体 */
  public static final String SCENE_LABEL_PLAN_AGENT = "规划";
  /** 智能体标签 ID - 规划智能体 */
  public static final Long SCENE_LABEL_ID_PLAN_AGENT = 20250529L;
  /** 意图识别需例外的智能体 - 涉及的标签 */
  public static final List<String> EXCLUDE_LABELS = ImmutableList.of(SCENE_LABEL_COPILOT, SCENE_LABEL_PLAN_AGENT);

  /** 异常处理分支的连接桩编码*/
  public static final String EXCEPTION_EDGE_PORT = "exception";
  /** 节点异常信息 key */
  public static final String EXCEPTION_INFO_KEY = "exceptionInfo";
  /** 节点异常信息属性路径前缀 */
  public static final String EXCEPTION_INFO_PROPERTY_PATH_PREFIX = "exceptionInfo.";
  /** 节点异常信息中的错误编码 key */
  public static final String ERROR_CODE_KEY = "errorCode";
  /** 节点异常信息中的错误信息 key */
  public static final String ERROR_MESSAGE_KEY = "errorMessage";

  public static final String PROMPT_FILE_AGENT = "AGENTS.md";
  public static final String PROMPT_FILE_PROFILE = "PROFILE.md";
  public static final String PROMPT_FILE_SOUL = "SOUL.md";
  public static final List<String> PROMPT_FILES = List.of(PROMPT_FILE_AGENT, PROMPT_FILE_PROFILE, PROMPT_FILE_SOUL);
}
