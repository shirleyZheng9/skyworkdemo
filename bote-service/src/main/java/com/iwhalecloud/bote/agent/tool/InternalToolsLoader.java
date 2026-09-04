package com.iwhalecloud.bote.agent.tool;

import com.iwhalecloud.bote.agent.tool.callback.MethodToolCallback;
import com.iwhalecloud.bote.agent.tool.callback.ToolCallback;
import com.iwhalecloud.bote.agent.tool.util.ToolUtils;
import com.iwhalecloud.bote.agent.tools.AgentTools;
import com.iwhalecloud.bote.agent.tools.BrowserTools;
import com.iwhalecloud.bote.agent.tools.CronTools;
import com.iwhalecloud.bote.agent.tools.DateTimeTools;
import com.iwhalecloud.bote.agent.tools.FileSystemTools;
import com.iwhalecloud.bote.agent.tools.MemoryTools;
import com.iwhalecloud.bote.agent.tools.OcrTools;
import com.iwhalecloud.bote.agent.tools.OntologyTools;
import com.iwhalecloud.bote.agent.tools.PromptFileEditTools;
import com.iwhalecloud.bote.agent.tools.SessionStateTools;
import com.iwhalecloud.bote.agent.tools.SkillSquareTools;
import com.iwhalecloud.bote.agent.tools.TaskTools;
import com.iwhalecloud.bote.agent.tools.WebFetchTools;
import com.iwhalecloud.bote.agent.tools.WebSearchTools;
import com.iwhalecloud.bote.agent.tools.shell.ShellTools;
import com.iwhalecloud.bote.common.enums.SandboxMode;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * 内置工具加载器
 *
 * @author bianjp
 * @since 2026-03-09
 */
public final class InternalToolsLoader {
  private InternalToolsLoader() {
  }

  /** 已加载的工具缓存，key 为工具的容器类 */
  private static final Map<Class<?>, List<ToolCallback>> loadedToolsMap = new ConcurrentHashMap<>();

  /**
   * 加载内置工具，用于工作流的 Agent Skill 节点
   *
   * @param sandboxMode 沙箱模式
   * @param clientOperatingSystem 客户端操作系统，仅用于 client 沙箱模式
   */
  public static List<ToolCallback> loadToolsForAgentSkill(SandboxMode sandboxMode, @Nullable String clientOperatingSystem) {
    List<ToolCallback> toolCallbacks = new ArrayList<>();
    //noinspection CollectionAddAllCanBeReplacedWithConstructor
    toolCallbacks.addAll(loadTools(FileSystemTools.class));
    toolCallbacks.add(ShellTools.buildExecuteShellCommandTool(sandboxMode, clientOperatingSystem));
    toolCallbacks.addAll(loadTools(DateTimeTools.class));
    return toolCallbacks;
  }

  /**
   * 加载内置工具，用于通用智能体
   *
   * @param sandboxMode 沙箱模式
   * @param clientOperatingSystem 客户端操作系统，仅用于 client 沙箱模式
   */
  public static List<ToolCallback> loadToolsForGeneralAgent(SandboxMode sandboxMode, @Nullable String clientOperatingSystem) {
    List<ToolCallback> toolCallbacks = new ArrayList<>();
    //noinspection CollectionAddAllCanBeReplacedWithConstructor
    toolCallbacks.addAll(loadTools(FileSystemTools.class));
    toolCallbacks.add(ShellTools.buildExecuteShellCommandTool(sandboxMode, clientOperatingSystem));
    toolCallbacks.addAll(loadTools(BrowserTools.class));
    toolCallbacks.addAll(loadTools(WebSearchTools.class));
    toolCallbacks.addAll(loadTools(WebFetchTools.class));
    toolCallbacks.addAll(loadTools(OcrTools.class));
    toolCallbacks.addAll(loadTools(DateTimeTools.class));
    toolCallbacks.addAll(loadTools(SpringUtil.getBean(PromptFileEditTools.class)));
    toolCallbacks.addAll(loadTools(CronTools.class));
    toolCallbacks.addAll(loadTools(SkillSquareTools.class));
    toolCallbacks.addAll(loadTools(TaskTools.class));
    toolCallbacks.addAll(loadTools(SessionStateTools.class));
    toolCallbacks.addAll(loadTools(AgentTools.class));
    toolCallbacks.addAll(loadTools(MemoryTools.class));
    toolCallbacks.addAll(loadTools(OntologyTools.class));
    return toolCallbacks;
  }

  /**
   * 从类中加载工具，只加载静态方法
   *
   * @param clazz 类
   * @return 工具列表
   */
  public static List<ToolCallback> loadTools(Class<?> clazz) {
    return loadedToolsMap.computeIfAbsent(clazz, k -> buildToolCallbacks(clazz, null));
  }

  /**
   * 从对象中加载工具，只加载非静态方法
   *
   * @param obj 对象
   * @return 工具列表
   */
  public static List<ToolCallback> loadTools(Object obj) {
    Class<?> clazz = AopUtils.isAopProxy(obj) ? AopUtils.getTargetClass(obj) : obj.getClass();
    return loadedToolsMap.computeIfAbsent(clazz, k -> buildToolCallbacks(clazz, obj));
  }

  /**
   * 从类或对象中加载工具
   */
  private static List<ToolCallback> buildToolCallbacks(Class<?> clazz, @Nullable Object obj) {
    return Arrays.stream(ReflectionUtils.getDeclaredMethods(clazz))
      // obj 为 null 时，加载静态方法；否则，加载非静态方法
      .filter(obj == null ? m -> Modifier.isStatic(m.getModifiers()) : m -> !Modifier.isStatic(m.getModifiers()))
      // 只加载公开方法
      .filter(m -> Modifier.isPublic(m.getModifiers()))
      .filter(ReflectionUtils.USER_DECLARED_METHODS::matches)
      .filter(m -> AnnotationUtils.findAnnotation(m, com.iwhalecloud.bote.agent.annotation.Tool.class) != null)
      .map(m -> {
        Pair<Tool, com.iwhalecloud.bote.agent.annotation.Tool> pair = ToolUtils.loadTool(m);
        return MethodToolCallback.builder()
          .tool(pair.getLeft())
          .returnDirect(pair.getRight().returnDirect())
          .hideToolCall(pair.getRight().hideToolCall())
          .toolMethod(m)
          .toolObject(obj)
          .build();
      })
      .map(c -> (ToolCallback) c)
      .toList();
  }

}
