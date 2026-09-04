/*
 * Copyright 2023-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iwhalecloud.bote.agent.tool.callback;

import com.iwhalecloud.bote.agent.annotation.ToolRequest;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;
import lombok.Builder;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 基于方法的工具回调
 *
 * @author bianjp
 * @since 2026-03-09
 */
public final class MethodToolCallback implements ToolCallback {
  private static final Logger logger = LoggerFactory.getLogger(MethodToolCallback.class);

  /** 工具 */
  private final Tool tool;
  /** 是否直接返回 */
  private final boolean returnDirect;
  /** 是否隐藏工具调用 */
  private final boolean hideToolCall;
  /** 工具方法 */
  private final Method toolMethod;
  /** 工具对象，为 null 时表示调用静态方法，否则表示调用实例方法 */
  @Nullable
  private final Object toolObject;

  @Builder
  public MethodToolCallback(Tool tool, boolean returnDirect, boolean hideToolCall, Method toolMethod, @Nullable Object toolObject) {
    Assert.notNull(tool, "tool cannot be null");
    Assert.notNull(toolMethod, "toolMethod cannot be null");
    Assert.isTrue(Modifier.isStatic(toolMethod.getModifiers()) || toolObject != null, "toolObject cannot be null for non-static methods");
    this.tool = tool;
    this.returnDirect = returnDirect;
    this.hideToolCall = hideToolCall;
    this.toolMethod = toolMethod;
    this.toolObject = toolObject;
  }

  @Override
  public Tool getTool() {
    return tool;
  }

  @Override
  public boolean returnDirect() {
    return returnDirect;
  }

  @Override
  public boolean hideToolCall() {
    return hideToolCall;
  }

  @Nullable
  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public Object call(@Nullable Map<String, Object> parameters, @Nullable ToolContext toolContext) {
    Object[] methodArguments = buildMethodArguments(MapUtils.emptyIfNull(parameters), toolContext);
    Object result;
    try {
      result = toolMethod.invoke(toolObject, methodArguments);
    }
    catch (IllegalAccessException e) {
      throw new IllegalStateException("无法调用工具方法: " + ExpUtil.getMsg(e), e);
    }
    catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof BssException bssException) {
        throw bssException;
      }
      if (cause instanceof ToolExecutionException toolExecutionException) {
        throw toolExecutionException;
      }
      // 参数校验异常
      if (cause instanceof IllegalArgumentException) {
        throw new ToolExecutionException(cause.getMessage(), cause);
      }
      throw new BssException("调用工具方法失败: " + ExpUtil.getMsg(cause), cause);
    }
    return result;
  }

  /**
   * 构造方法参数
   */
  private Object[] buildMethodArguments(Map<String, Object> toolInputArguments, @Nullable ToolContext toolContext) {
    Parameter[] parameters = toolMethod.getParameters();
    if (parameters.length == 0) {
      return new Object[0];
    }
    Object[] methodArguments = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      if (parameter.getType().isAssignableFrom(ToolContext.class)) {
        methodArguments[i] = toolContext;
      }
      else if (parameter.isAnnotationPresent(ToolRequest.class)) {
        methodArguments[i] = buildTypedArgument(null, toolInputArguments, parameter.getParameterizedType());
      }
      else {
        Object rawArgument = toolInputArguments.get(parameter.getName());
        methodArguments[i] = buildTypedArgument(parameter.getName(), rawArgument, parameter.getParameterizedType());
      }
    }
    return methodArguments;
  }

  /**
   * 构造方法参数，自动转换类型
   */
  @Nullable
  private Object buildTypedArgument(@Nullable String name, @Nullable Object value, Type type) {
    if (value == null) {
      return null;
    }
    try {
      if (type instanceof Class<?>) {
        return JsonUtil.convert(value, (Class<?>) type);
      }
      return JsonUtil.convert(value, JsonUtil.getObjectMapper().constructType(type));
    }
    catch (Exception e) {
      logger.error("Failed to convert method tool param: method={}, param={}, type={}, value={}", toolMethod, name, type, value, e);
      String error = name != null ? "Error: 参数 " + name + " 不合法: " : "Error: 解析参数失败: ";
      throw new ToolExecutionException(error + ExpUtil.getMsg(e), e);
    }
  }

}
