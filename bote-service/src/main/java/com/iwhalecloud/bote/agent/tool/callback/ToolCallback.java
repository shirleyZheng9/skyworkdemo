package com.iwhalecloud.bote.agent.tool.callback;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.support.ToolExecutionResult;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 工具回调
 *
 * @author bianjp
 * @since 2026-02-03
 */
public interface ToolCallback {
  /**
   * 获取工具信息
   *
   * @return 工具信息
   */
  Tool getTool();

  /**
   * 获取工具名称
   *
   * @return 工具名称
   */
  default String getToolName() {
    return getTool().getFunction().getName();
  }

  /**
   * 是否直接返回（开启时工具出参作为最终结果，不再调用大模型）
   *
   * @return 是否直接返回
   */
  default boolean returnDirect() {
    return false;
  }

  /**
   * 是否隐藏工具调用
   *
   * @return 是否隐藏工具调用
   */
  default boolean hideToolCall() {
    return false;
  }

  /**
   * 调用工具
   *
   * @param arguments 工具参数(JSON 字符串)
   * @param toolContext 工具调用上下文
   * @return 工具调用结果
   */
  @Nullable
  default String call(@Nullable String arguments, @Nullable ToolContext toolContext) {
    Map<String, Object> parameters = parseArguments(arguments);
    // 调用工具
    Object result = call(parameters, toolContext);
    // 调用结果转为字符串
    return convertResultToString(result);
  }

  /**
   * 调用工具
   *
   * @param parameters 工具参数
   * @param toolContext 工具调用上下文
   * @return 工具调用结果
   */
  @Nullable
  Object call(@Nullable Map<String, Object> parameters, @Nullable ToolContext toolContext);

  /**
   * 解析工具参数
   *
   * @param arguments 工具参数(JSON 字符串)
   * @return 解析后的参数
   */
  @Nullable
  static Map<String, Object> parseArguments(@Nullable String arguments) {
    // 解析参数
    Map<String, Object> parameters;
    if (StringUtils.isEmpty(arguments)) {
      parameters = null;
    }
    else {
      parameters = JsonUtil.parseJsonRequired(arguments, new TypeReference<>() {
      });
    }
    return parameters;
  }

  /**
   * 将工具调用结果转换为字符串
   *
   * @param result 工具调用结果
   * @return 结果字符串
   */
  @Nullable
  static String convertResultToString(@Nullable Object result) {
    if (result == null || result instanceof String) {
      return (String) result;
    }
    if (result instanceof ToolExecutionResult toolExecutionResult) {
      return toolExecutionResult.getResult();
    }
    return JsonUtil.toJsonString(result);
  }
}
