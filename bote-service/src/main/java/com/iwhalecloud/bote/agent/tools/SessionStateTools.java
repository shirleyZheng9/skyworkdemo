package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.memory.helper.ReminderGenerator;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.common.enums.SystemReminderType;
import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.dto.agent.MemoryMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.service.agent.ISessionStateService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 会话状态工具：按会话 + scope 持久化存储，避免上下文压缩丢失数据
 *
 * @author bianjp
 * @since 2026-04-13
 */
public final class SessionStateTools {
  private static final ISessionStateService agentSkillSessionDataService = SpringUtil.getBean(ISessionStateService.class);

  private SessionStateTools() {
  }

  @Tool(
    name = "session_state_update",
    description = """
      Persist data for scope in current session, only for data collected from user conversation, and intermediate results that are expected to be reused many turns later.
      The latest persisted session state is automatically provided to the model via <system-reminder> on every model call, so no extra query step is needed.
      Use this proactively in long-running or branching tasks to prevent loss from context compression.
      When merge is true (default), keys are merged and null values delete existing keys.
      When merge is false, existing data is replaced by the new object.
      """
  )
  public static String sessionStateUpdate(@ToolParam(description = "Session state namespace in current session (any stable scope identifier)") String scope,
                                          @ToolParam(description = "Whether to merge with existing data. Default true") @Nullable Boolean merge,
                                          @ToolParam(description = "Data object to persist") Map<String, Object> data,
                                          ToolContext toolContext) {
    Assert.hasLength(scope, "Error: scope is required");
    Assert.notEmpty(data, "Error: data is required");
    agentSkillSessionDataService.updateState(toolContext.sessionId(), scope, !Boolean.FALSE.equals(merge), data);
    return "Saved scope=%s".formatted(scope);
  }

  @Tool(
    name = "session_state_clear",
    description = """
      Delete all persisted data for scope in current session after related long-horizon data is no longer needed.
      Use this to avoid stale historical data affecting future turns.
      """
  )
  public static String sessionStateClear(@ToolParam(description = "Session state namespace in current session (any stable scope identifier)") String scope,
                                         ToolContext toolContext) {
    Assert.hasLength(scope, "Error: scope is required");
    agentSkillSessionDataService.clearState(toolContext.sessionId(), scope);
    return "Cleared scope=%s".formatted(scope);
  }

  /**
   * 获取会话状态提醒生成器
   */
  public static ReminderGenerator getSessionStateReminderGenerator(Long sessionId, @Nullable String contextId) {
    // contextId 过滤待与 SessionState 服务对齐；当前先按 session 维度提醒，保证编译与主链路可用
    return new SessionStateReminderGenerator(sessionId);
  }

  /**
   * 会话状态提醒生成器
   */
  @SuppressWarnings("ClassCanBeRecord")
  @RequiredArgsConstructor
  private static final class SessionStateReminderGenerator implements ReminderGenerator {
    private final Long sessionId;

    @Override
    @Nullable
    public SystemReminder generate(List<MemoryMessage> messages, Message message) {
      // 只对用户消息发送会话状态提醒，不对工具消息发送
      if (!(message instanceof UserMessage)) {
        return null;
      }
      Map<String, Map<String, Object>> allStateMap = agentSkillSessionDataService.listAllStates(sessionId);
      if (MapUtils.isEmpty(allStateMap)) {
        return null;
      }
      StringBuilder sb = new StringBuilder();
      sb.append("<system-reminder>\n");
      sb.append("Here is the latest persisted session state in current session.\n");
      sb.append("Proactively clean up stale data with `session_state_clear` once related data is no longer needed.\n\n");
      for (Entry<String, Map<String, Object>> entry : allStateMap.entrySet()) {
        sb.append("# scope=").append(entry.getKey()).append("\n").append(JsonUtil.toJsonString(entry.getValue())).append("\n");
      }
      sb.append("\n</system-reminder>");
      return new SystemReminder(SystemReminderType.SESSION_STATE_REMINDER, sb.toString());
    }
  }
}
