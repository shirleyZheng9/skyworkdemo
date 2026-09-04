package com.iwhalecloud.bote.agent.tool.util;

import com.iwhalecloud.bote.agent.tool.callback.SkillToolCallback;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.mcp.dto.response.CallToolResult;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 工具调用工具类
 *
 * @author bianjp
 * @since 2026-03-11
 */
public final class ToolCallUtil {
  private ToolCallUtil() {
  }

  /**
   * 检查工具调用是否成功
   *
   * @param skillTool 技能工具
   * @param output 工具调用输出
   * @return 是否成功
   */
  public static boolean checkToolCallSuccess(SkillToolDTO skillTool, @Nullable Object output) {
    // 检查 MCP 工具出参
    if (StepType.MCP.equals(skillTool.getSkillType())) {
      return checkMcpToolCallSuccess(output);
    }
    // 非 MCP 工具无法检查，固定返回 true
    return true;
  }

  /**
   * 检查工具调用是否成功
   *
   * @param toolCallback 工具回调
   * @param output 工具调用输出
   * @return 是否成功
   */
  public static boolean checkToolCallSuccess(ToolCallback toolCallback, @Nullable Object output) {
    // 检查 MCP 工具出参
    if (toolCallback instanceof SkillToolCallback skillToolCallback && StepType.MCP.equals(skillToolCallback.getSkillTool().getSkillType())) {
      return checkMcpToolCallSuccess(output);
    }
    // 非 MCP 工具无法检查，固定返回 true
    return true;
  }

  /**
   * 检查 MCP 工具调用是否成功
   */
  private static boolean checkMcpToolCallSuccess(@Nullable Object output) {
    // 识别 MCP 调用结果中的错误, null/false 都表示成功，true 表示失败
    Object isError = null;
    if (output instanceof CallToolResult) {
      isError = ((CallToolResult) output).getIsError();
    }
    // 兼容 MCP 调用结果是 Map 的情况
    else if (output instanceof Map) {
      isError = ((Map<?, ?>) output).get("isError");
    }
    return !Boolean.TRUE.equals(isError);
  }
}
