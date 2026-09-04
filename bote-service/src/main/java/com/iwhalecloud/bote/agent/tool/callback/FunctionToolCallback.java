package com.iwhalecloud.bote.agent.tool.callback;

import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.Nullable;

/**
 * 基于函数的工具回调
 *
 * @author bianjp
 * @since 2026-03-09
 */
public class FunctionToolCallback<I> implements ToolCallback {
  /** 工具 */
  private final Tool tool;
  /** 工具入参类型 */
  private final Type toolInputType;
  /** 工具函数 */
  private final BiFunction<I, ToolContext, Object> toolFunction;

  public FunctionToolCallback(Tool tool, Type toolInputType, BiFunction<I, ToolContext, Object> toolFunction) {
    this.tool = tool;
    this.toolInputType = toolInputType;
    this.toolFunction = toolFunction;
  }

  public FunctionToolCallback(Tool tool, Type toolInputType, Function<I, Object> toolFunction) {
    this.tool = tool;
    this.toolInputType = toolInputType;
    this.toolFunction = (input, context) -> toolFunction.apply(input);
  }

  @Override
  public Tool getTool() {
    return tool;
  }

  @Nullable
  @Override
  public Object call(@Nullable Map<String, Object> parameters, @Nullable ToolContext toolContext) {
    I request = MapUtils.isEmpty(parameters) ? null : JsonUtil.convert(parameters, JsonUtil.getObjectMapper().constructType(toolInputType));
    return toolFunction.apply(request, toolContext);
  }
}
