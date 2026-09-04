package com.iwhalecloud.bote.mcp.dto;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * stdio 模式的服务器参数
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
public class StdioServerParameters {
  /** 默认继承的环境变量名称 */
  private static final List<String> INHERITABLE_ENV_NAMES = SystemUtils.IS_OS_WINDOWS
    ? ImmutableList.of("APPDATA", "HOMEDRIVE", "HOMEPATH", "LOCALAPPDATA", "PATH", "PROCESSOR_ARCHITECTURE", "SYSTEMDRIVE", "SYSTEMROOT", "TEMP", "USERNAME", "USERPROFILE")
    : ImmutableList.of("HOME", "LOGNAME", "PATH", "SHELL", "TERM", "USER");

  /** 命令和参数列表 */
  private final List<String> command;
  /** 环境变量 */
  private final Map<String, String> env;

  public StdioServerParameters(List<String> command) {
    this(command, null);
  }

  public StdioServerParameters(List<String> command, @Nullable Map<String, String> env) {
    Assert.notEmpty(command, "命令不能为空");
    this.command = command;
    this.env = buildEnv(env);
  }

  /**
   * 构造环境变量
   */
  private static Map<String, String> buildEnv(@Nullable Map<String, String> env) {
    Map<String, String> map = new HashMap<>();
    // 添加默认环境变量
    for (Entry<String, String> entry : System.getenv().entrySet()) {
      if (INHERITABLE_ENV_NAMES.contains(entry.getKey()) && entry.getValue() != null && !entry.getValue().startsWith("()")) {
        map.put(entry.getKey(), entry.getValue());
      }
    }
    // 添加用户环境变量
    if (env != null && !env.isEmpty()) {
      map.putAll(env);
    }
    return map;
  }
}
