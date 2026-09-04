package com.iwhalecloud.bote.common.consts;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 会话消息类型
 *
 * <p>同时也用作发送给前端的 SSE 事件名称</p>
 *
 * @author bianjp
 * @since 2025-01-06
 */
@Getter
public enum ChatMessageType {
  /** 用户输入(包括正常输入、机器人指令、页面回调、页面函数回调) */
  INPUT("input"),
  /** 指令 */
  POINT("point"),
  /** 知识库的参考文档 */
  REFERENCES("references"),
  /** 知识库追问 */
  QUESTIONS("questions"),
  /** 知识库对话记录 */
  KNOWLEDGE_CHAT_LOG("knowledgeChatLog"),
  /** 网页 */
  WEB_PAGE_INFO("pageInfos"),
  /** 附件 */
  FILE_INFO("fileInfos"),
  /** 推理内容 */
  REASONING("reasoning"),
  /** 内容格式 */
  TEXT_CONTENT_TYPE("contentType"),
  /** 文本 */
  TEXT("text"),
  /** 文件下载配置（用于文本回复） */
  DOWNLOAD("download"),
  /** 页面 */
  PAGE("page"),
  /** 页面函数 */
  PAGE_FUNC("pageFunc"),
  /** A2UI 卡片 */
  A2UI("a2ui"),
  /** 选择场景 */
  SELECT_SCENE("selectScene"),
  /** 推荐 */
  RECOMMENDATION("recommendation"),
  /** 通用智能体消息 */
  GENERAL("general"),

  /** 工作流 Agent 节点的回复 */
  AGENT_REPLY("agentReply"),
  /** 工作流 Agent 节点的工具调用 */
  TOOL_CALL("toolCall", false),
  /** 工作流 Agent 节点的工具调用结果 */
  TOOL_CALL_RESULT("toolCallResult", false),

  /** 应用信息（用于向前端返回识别智能应用的结果） */
  BOT("bot", false),
  /** 上下文 ID */
  CONTEXT_ID("contextId", false),
  /** 场景信息(用于向前端返回意图识别的结果) */
  SCENE("scene", false),
  /** 结束 */
  DONE("done", false),
  /** 场景就绪 */
  READY("ready", false),
  /** 退出场景 */
  EXIT_SCENE("exitScene", false),
  /** 场景切换 */
  AGENT_SWITCH("agentSwitch"),
  /** 确认进入智能体 */
  CONFIRM_SCENE("confirmScene", false),
  /** PlayWright 自动化 */
  PLAYWRIGHT_AUTOMATION("playwrightAutomation", false),
  /** 响应对象（场景、工作流的调用结果，用于向前端返回调试日志） */
  RESPONSE("response", false),
  /** 节点日志（单节点调试使用） */
  STEP_LOG("stepLog", false),

  /** 当前步骤信息（工作流的调用结果对应步骤信息） */
  CURRENT_STEP_INFO("currentStepInfo", false),
  /** 流程步骤（用于向前端展示本轮对话的处理明细） */
  FLOW_STEP("flowStep", false),

  /** 确认计划 */
  CONFIRM_PLAN("confirmPlan"),
  /** 更新计划状态 */
  UPDATE_PLAN_STATE("updatePlanState", false),

  /** 错误信息 */
  ERROR("error"),
  /** 异常堆栈 */
  EXCEPTION("exception");

  /** 消息类型编码 */
  private final String code;
  /** 是否需要持久化（保存到会话消息表 bt_bot_session_msg） */
  private final boolean persist;

  ChatMessageType(String code) {
    this.code = code;
    this.persist = true;
  }

  ChatMessageType(String code, boolean persist) {
    this.code = code;
    this.persist = persist;
  }

  /**
   * 获取类型编码
   *
   * <p>添加 JsonValue 注解以使 Jackson 使用类型编码作为 JSON 序列化的结果（默认会使用枚举值名称，但名称是大写的不太方便）</p>
   */
  @JsonValue
  public String getCode() {
    return code;
  }

  /**
   * 根据编码获取消息类型
   */
  public static ChatMessageType ofCode(String code) {
    for (ChatMessageType type : values()) {
      if (type.getCode().equals(code)) {
        return type;
      }
    }
    throw new IllegalStateException("未知的消息类型: " + code);
  }

  /**
   * 根据编码获取消息类型
   */
  public static ChatMessageType ofCode(String code, ChatMessageType defaultType) {
    if (StringUtils.isEmpty(code)) {
      return defaultType;
    }
    return ofCode(code);
  }

  /**
   * 在会话列表展示的消息，允许的消息类型
   */
  public static List<String> allowLastMsgCode() {
    return Arrays.asList(INPUT.getCode(), GENERAL.getCode());
  }
}
